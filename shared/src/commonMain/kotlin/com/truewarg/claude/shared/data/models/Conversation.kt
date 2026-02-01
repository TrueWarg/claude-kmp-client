package com.truewarg.claude.shared.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val messageCount: Int = 0
)

fun Conversation.withUpdatedTime(timestamp: Long = System.currentTimeMillis()): Conversation {
    return copy(updatedAt = timestamp)
}

fun Conversation.withIncrementedMessages(): Conversation {
    return copy(messageCount = messageCount + 1)
}
