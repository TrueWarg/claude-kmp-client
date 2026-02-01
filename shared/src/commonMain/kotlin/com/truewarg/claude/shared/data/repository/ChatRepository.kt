package com.truewarg.claude.shared.data.repository

import com.truewarg.claude.shared.api.ClaudeApiClient
import com.truewarg.claude.shared.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlin.random.Random

class ChatRepository(
    private val apiClient: ClaudeApiClient,
    private val conversationRepository: ConversationRepository
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

        val history = conversationRepository.getMessages(conversationId)
        val apiMessages = history.map { it.toApiMessage() }

        val request = MessagesRequest(
            model = model,
            messages = apiMessages,
            maxTokens = 4096,
            stream = true
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
                var currentTextIndex = -1
                var currentThinkingIndex = -1
                var currentToolUseIndex = -1

                streamFlow.collect { event ->
                    when (event) {
                        is StreamingEvent.ContentBlockStart -> {
                            when (event.contentBlock.type) {
                                "text" -> {
                                    currentTextIndex = event.index
                                    contentBlocks.add(ContentBlock.Text(""))
                                }
                                "thinking" -> {
                                    currentThinkingIndex = event.index
                                    contentBlocks.add(ContentBlock.Thinking(""))
                                }
                                "tool_use" -> {
                                    currentToolUseIndex = event.index
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
                                    if (currentTextIndex >= 0 && currentTextIndex < contentBlocks.size) {
                                        val current = contentBlocks[currentTextIndex] as? ContentBlock.Text
                                        contentBlocks[currentTextIndex] = ContentBlock.Text(
                                            (current?.text ?: "") + delta.text
                                        )
                                    }
                                }

                                is Delta.ThinkingDelta -> {
                                    if (currentThinkingIndex >= 0 && currentThinkingIndex < contentBlocks.size) {
                                        val current = contentBlocks[currentThinkingIndex] as? ContentBlock.Thinking
                                        contentBlocks[currentThinkingIndex] = ContentBlock.Thinking(
                                            (current?.thinking ?: "") + delta.thinking
                                        )
                                    }
                                }

                                is Delta.InputJsonDelta -> {
                                    if (currentToolUseIndex >= 0 && currentToolUseIndex < contentBlocks.size) {
                                        val current = contentBlocks[currentToolUseIndex] as? ContentBlock.ToolUse
                                        contentBlocks[currentToolUseIndex] = current!!.copy(
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
                            val final = assistantMessage.copy(
                                content = contentBlocks.toList(),
                                isStreaming = false
                            )
                            conversationRepository.updateMessage(final)
                            conversationRepository.updateConversation(
                                conversation.withUpdatedTime().withIncrementedMessages()
                            )
                            emit(final)
                        }

                        is StreamingEvent.Error -> {
                            throw Exception("API Error: ${event.error.message}")
                        }

                        else -> {
                        }
                    }
                }
            },
            onFailure = { error ->
                throw error
            }
        )
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
                    input = mapOf("data" to block.input)
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
