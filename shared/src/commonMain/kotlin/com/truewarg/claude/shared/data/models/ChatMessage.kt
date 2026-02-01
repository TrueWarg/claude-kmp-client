package com.truewarg.claude.shared.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String,
    val conversationId: String,
    val role: MessageRole,
    val content: List<ContentBlock>,
    val timestamp: Long,
    val isStreaming: Boolean = false
)

@Serializable
enum class MessageRole {
    USER,
    ASSISTANT
}

@Serializable
sealed class ContentBlock {
    @Serializable
    data class Text(val text: String) : ContentBlock()

    @Serializable
    data class Thinking(val thinking: String) : ContentBlock()

    @Serializable
    data class ToolUse(
        val id: String,
        val name: String,
        val input: String
    ) : ContentBlock()

    @Serializable
    data class ToolResult(
        val toolUseId: String,
        val content: String,
        val isError: Boolean = false
    ) : ContentBlock()
}
