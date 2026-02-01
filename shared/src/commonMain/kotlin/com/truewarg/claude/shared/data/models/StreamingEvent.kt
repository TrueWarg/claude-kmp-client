package com.truewarg.claude.shared.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class StreamingEvent {
    @Serializable
    @SerialName("message_start")
    data class MessageStart(val message: MessageMetadata) : StreamingEvent()

    @Serializable
    @SerialName("content_block_start")
    data class ContentBlockStart(
        val index: Int,
        @SerialName("content_block")
        val contentBlock: ContentBlockInfo
    ) : StreamingEvent()

    @Serializable
    @SerialName("content_block_delta")
    data class ContentBlockDelta(
        val index: Int,
        val delta: Delta
    ) : StreamingEvent()

    @Serializable
    @SerialName("content_block_stop")
    data class ContentBlockStop(val index: Int) : StreamingEvent()

    @Serializable
    @SerialName("message_delta")
    data class MessageDelta(val delta: MessageDeltaInfo) : StreamingEvent()

    @Serializable
    @SerialName("message_stop")
    object MessageStop : StreamingEvent()

    @Serializable
    @SerialName("ping")
    object Ping : StreamingEvent()

    @Serializable
    @SerialName("error")
    data class Error(val error: ErrorInfo) : StreamingEvent()
}

@Serializable
data class MessageMetadata(
    val id: String,
    val type: String,
    val role: String,
    val model: String
)

@Serializable
data class ContentBlockInfo(
    val type: String,
    val id: String? = null,
    val name: String? = null
)

@Serializable
sealed class Delta {
    @Serializable
    @SerialName("text_delta")
    data class TextDelta(val text: String) : Delta()

    @Serializable
    @SerialName("thinking_delta")
    data class ThinkingDelta(val thinking: String) : Delta()

    @Serializable
    @SerialName("input_json_delta")
    data class InputJsonDelta(@SerialName("partial_json") val partialJson: String) : Delta()
}

@Serializable
data class MessageDeltaInfo(
    @SerialName("stop_reason")
    val stopReason: String? = null,
    @SerialName("stop_sequence")
    val stopSequence: String? = null
)

@Serializable
data class ErrorInfo(
    val type: String,
    val message: String
)
