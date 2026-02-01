package com.truewarg.claude.shared.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Claude AI Model information
 */
@Serializable
data class ClaudeModel(
    val id: String,
    @SerialName("display_name")
    val displayName: String,
    @SerialName("created_at")
    val createdAt: String,
    val type: String = "model"
)

/**
 * Response from /v1/models endpoint
 */
@Serializable
data class ModelsResponse(
    val data: List<ClaudeModel>,
    @SerialName("has_more")
    val hasMore: Boolean = false,
    @SerialName("first_id")
    val firstId: String? = null,
    @SerialName("last_id")
    val lastId: String? = null
)

/**
 * Message content block (API format)
 */
@Serializable
data class ApiContentBlock(
    val type: String,
    val text: String? = null,
    @SerialName("tool_use_id")
    val toolUseId: String? = null,
    val id: String? = null,
    val name: String? = null,
    val input: Map<String, String>? = null
)

/**
 * Message in a conversation
 */
@Serializable
data class Message(
    val role: String, // "user" or "assistant"
    val content: List<ApiContentBlock>
)

/**
 * Simple message constructor
 */
fun Message(role: String, text: String): Message {
    return Message(role, listOf(ApiContentBlock("text", text = text)))
}

/**
 * Usage statistics
 */
@Serializable
data class Usage(
    @SerialName("input_tokens")
    val inputTokens: Int,
    @SerialName("output_tokens")
    val outputTokens: Int
)

/**
 * Request to /v1/messages endpoint
 */
@Serializable
data class MessagesRequest(
    val model: String,
    val messages: List<Message>,
    @SerialName("max_tokens")
    val maxTokens: Int = 4096,
    val temperature: Double = 1.0,
    val system: String? = null,
    val stream: Boolean = false,
    @SerialName("top_p")
    val topP: Double? = null,
    @SerialName("top_k")
    val topK: Int? = null,
    @SerialName("stop_sequences")
    val stopSequences: List<String>? = null,
    val tools: List<kotlinx.serialization.json.JsonObject>? = null
)

/**
 * Response from /v1/messages endpoint
 */
@Serializable
data class MessagesResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<ApiContentBlock>,
    val model: String,
    @SerialName("stop_reason")
    val stopReason: String?,
    @SerialName("stop_sequence")
    val stopSequence: String? = null,
    val usage: Usage
)

/**
 * API Error response
 */
@Serializable
data class ApiError(
    val type: String,
    val error: ErrorDetail
)

@Serializable
data class ErrorDetail(
    val type: String,
    val message: String
)
