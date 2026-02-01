package com.truewarg.claude.shared.data.repository

import com.truewarg.claude.shared.api.ClaudeApiClient
import com.truewarg.claude.shared.data.models.*
import com.truewarg.claude.shared.tools.ToolManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.random.Random

class ChatRepository(
    private val apiClient: ClaudeApiClient,
    private val conversationRepository: ConversationRepository,
    private val toolManager: ToolManager,
    private val json: Json
) {
    fun sendMessage(
        conversationId: String,
        userMessage: String,
        model: String = "claude-sonnet-4-20250514"
    ): Flow<ChatMessage> = flow {
        val conversation = conversationRepository.getConversation(conversationId)
            ?: throw IllegalArgumentException("Conversation not found: $conversationId")

        val userChatMessage = ChatMessage(
            id = generateId(),
            conversationId = conversationId,
            role = MessageRole.USER,
            content = listOf(ContentBlock.Text(userMessage)),
            timestamp = Clock.System.now().toEpochMilliseconds()
        )

        conversationRepository.addMessage(userChatMessage)
        conversationRepository.updateConversation(
            conversation.withUpdatedTime().withIncrementedMessages()
        )

        emit(userChatMessage)

        // Tool execution loop - continue until no more tools are requested
        var continueLoop = true
        while (continueLoop) {
            val history = conversationRepository.getMessages(conversationId)
            val apiMessages = history.map { it.toApiMessage() }

            // Convert tool definitions to JsonObject
            val toolDefinitions = toolManager.getAllToolDefinitions().map { tool ->
                json.decodeFromJsonElement<JsonObject>(
                    json.parseToJsonElement(json.encodeToString(tool))
                )
            }

            val request = MessagesRequest(
                model = model,
                messages = apiMessages,
                maxTokens = 4096,
                stream = true,
                tools = toolDefinitions
            )

            val result = apiClient.sendMessageStreaming(request)

            result.fold(
                onSuccess = { streamFlow ->
                    val assistantMessage = ChatMessage(
                        id = generateId(),
                        conversationId = conversationId,
                        role = MessageRole.ASSISTANT,
                        content = emptyList(),
                        timestamp = Clock.System.now().toEpochMilliseconds(),
                        isStreaming = true
                    )

                    conversationRepository.addMessage(assistantMessage)
                    emit(assistantMessage)

                    val contentBlocks = mutableListOf<ContentBlock>()
                    val toolUses = mutableListOf<ContentBlock.ToolUse>()

                    streamFlow.collect { event ->
                        when (event) {
                            is StreamingEvent.ContentBlockStart -> {
                                when (event.contentBlock.type) {
                                    "text" -> {
                                        contentBlocks.add(ContentBlock.Text(""))
                                    }
                                    "thinking" -> {
                                        contentBlocks.add(ContentBlock.Thinking(""))
                                    }
                                    "tool_use" -> {
                                        val toolUse = ContentBlock.ToolUse(
                                            id = event.contentBlock.id ?: "",
                                            name = event.contentBlock.name ?: "",
                                            input = ""
                                        )
                                        contentBlocks.add(toolUse)
                                    }
                                }
                            }

                            is StreamingEvent.ContentBlockDelta -> {
                                when (val delta = event.delta) {
                                    is Delta.TextDelta -> {
                                        val index = event.index
                                        if (index < contentBlocks.size && contentBlocks[index] is ContentBlock.Text) {
                                            val current = contentBlocks[index] as ContentBlock.Text
                                            contentBlocks[index] = ContentBlock.Text(
                                                current.text + delta.text
                                            )
                                        }
                                    }

                                    is Delta.ThinkingDelta -> {
                                        val index = event.index
                                        if (index < contentBlocks.size && contentBlocks[index] is ContentBlock.Thinking) {
                                            val current = contentBlocks[index] as ContentBlock.Thinking
                                            contentBlocks[index] = ContentBlock.Thinking(
                                                current.thinking + delta.thinking
                                            )
                                        }
                                    }

                                    is Delta.InputJsonDelta -> {
                                        val index = event.index
                                        if (index < contentBlocks.size && contentBlocks[index] is ContentBlock.ToolUse) {
                                            val current = contentBlocks[index] as ContentBlock.ToolUse
                                            contentBlocks[index] = current.copy(
                                                input = current.input + delta.partialJson
                                            )
                                        }
                                    }
                                }

                                val updated = assistantMessage.copy(
                                    content = contentBlocks.toList(),
                                    isStreaming = true
                                )
                                conversationRepository.updateMessage(updated)
                                emit(updated)
                            }

                            is StreamingEvent.MessageStop -> {
                                // Collect all tool uses from the message
                                toolUses.clear()
                                contentBlocks.forEach { block ->
                                    if (block is ContentBlock.ToolUse) {
                                        toolUses.add(block)
                                    }
                                }

                                val final = assistantMessage.copy(
                                    content = contentBlocks.toList(),
                                    isStreaming = false
                                )
                                conversationRepository.updateMessage(final)
                                conversationRepository.updateConversation(
                                    conversation.withUpdatedTime().withIncrementedMessages()
                                )
                                emit(final)

                                // If there are tool uses, execute them
                                if (toolUses.isNotEmpty()) {
                                    // Execute tools and add results as a new message
                                    val toolResults = mutableListOf<ContentBlock>()

                                    toolUses.forEach { toolUse ->
                                        val input = try {
                                            json.parseToJsonElement(toolUse.input).let { elem ->
                                                if (elem is JsonObject) {
                                                    elem.entries.associate { it.key to it.value }
                                                } else {
                                                    emptyMap()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            emptyMap()
                                        }

                                        val result = toolManager.executeTool(toolUse.name, input)

                                        toolResults.add(
                                            ContentBlock.ToolResult(
                                                toolUseId = toolUse.id,
                                                content = result.output,
                                                isError = result.isError
                                            )
                                        )
                                    }

                                    // Add tool results as a user message
                                    val toolResultMessage = ChatMessage(
                                        id = generateId(),
                                        conversationId = conversationId,
                                        role = MessageRole.USER,
                                        content = toolResults,
                                        timestamp = Clock.System.now().toEpochMilliseconds()
                                    )

                                    conversationRepository.addMessage(toolResultMessage)
                                    emit(toolResultMessage)

                                    // Continue the loop to get Claude's response to the tool results
                                    continueLoop = true
                                } else {
                                    // No more tools, end the loop
                                    continueLoop = false
                                }
                            }

                            is StreamingEvent.Error -> {
                                throw Exception("API Error: ${event.error.message}")
                            }

                            else -> {
                                // Ignore other events
                            }
                        }
                    }
                },
                onFailure = { error ->
                    continueLoop = false
                    throw error
                }
            )

            // Break if no tool uses were found (continueLoop was set to false in MessageStop)
            if (!continueLoop) break
        }
    }

    private fun ChatMessage.toApiMessage(): Message {
        val apiContent = content.map { block ->
            when (block) {
                is ContentBlock.Text -> ApiContentBlock(
                    type = "text",
                    text = block.text
                )

                is ContentBlock.Thinking -> ApiContentBlock(
                    type = "thinking",
                    text = block.thinking
                )

                is ContentBlock.ToolUse -> ApiContentBlock(
                    type = "tool_use",
                    id = block.id,
                    name = block.name,
                    input = try {
                        json.parseToJsonElement(block.input).let { elem ->
                            if (elem is JsonObject) {
                                elem.entries.associate { it.key to it.value.toString().trim('"') }
                            } else {
                                mapOf("data" to block.input)
                            }
                        }
                    } catch (e: Exception) {
                        mapOf("data" to block.input)
                    }
                )

                is ContentBlock.ToolResult -> ApiContentBlock(
                    type = "tool_result",
                    toolUseId = block.toolUseId,
                    text = block.content
                )
            }
        }

        return Message(
            role = when (role) {
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "assistant"
            },
            content = apiContent
        )
    }

    private fun generateId(): String {
        return "${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt()}"
    }
}
