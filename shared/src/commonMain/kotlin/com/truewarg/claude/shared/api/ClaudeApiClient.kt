package com.truewarg.claude.shared.api

import com.truewarg.claude.shared.data.models.*
import com.truewarg.claude.shared.data.repository.SettingsRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * Claude API client for interacting with Anthropic's Claude API
 */
class ClaudeApiClient(
    private val httpClient: HttpClient,
    private val settingsRepository: SettingsRepository,
    private val json: Json
) {
    companion object {
        private const val BASE_URL = "https://api.anthropic.com"
        private const val API_VERSION = "2023-06-01"
    }

    /**
     * Get list of available models
     */
    suspend fun getModels(): Result<ModelsResponse> {
        return try {
            val apiKey = settingsRepository.getApiKey()
                ?: return Result.failure(ApiException("API key not configured"))

            val response = httpClient.get("$BASE_URL/v1/models") {
                headers {
                    append("x-api-key", apiKey)
                    append("anthropic-version", API_VERSION)
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
            }

            if (response.status.isSuccess()) {
                Result.success(response.body<ModelsResponse>())
            } else {
                val error = try {
                    response.body<ApiError>()
                } catch (e: Exception) {
                    ApiError("error", ErrorDetail("unknown", response.bodyAsText()))
                }
                Result.failure(ApiException(error.error.message))
            }
        } catch (e: Exception) {
            Result.failure(ApiException("Failed to fetch models: ${e.message}", e))
        }
    }

    /**
     * Send a message and get non-streaming response
     */
    suspend fun sendMessage(request: MessagesRequest): Result<MessagesResponse> {
        return try {
            val apiKey = settingsRepository.getApiKey()
                ?: return Result.failure(ApiException("API key not configured"))

            val response = httpClient.post("$BASE_URL/v1/messages") {
                headers {
                    append("x-api-key", apiKey)
                    append("anthropic-version", API_VERSION)
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
                contentType(ContentType.Application.Json)
                setBody(request.copy(stream = false))
            }

            if (response.status.isSuccess()) {
                Result.success(response.body<MessagesResponse>())
            } else {
                val error = try {
                    response.body<ApiError>()
                } catch (e: Exception) {
                    ApiError("error", ErrorDetail("unknown", response.bodyAsText()))
                }
                Result.failure(ApiException(error.error.message))
            }
        } catch (e: Exception) {
            Result.failure(ApiException("Failed to send message: ${e.message}", e))
        }
    }

    /**
     * Send a message and get streaming response
     * Returns a Flow of text chunks
     */
    suspend fun sendMessageStreaming(request: MessagesRequest): Result<Flow<String>> {
        return try {
            val apiKey = settingsRepository.getApiKey()
                ?: return Result.failure(ApiException("API key not configured"))

            val response = httpClient.post("$BASE_URL/v1/messages") {
                headers {
                    append("x-api-key", apiKey)
                    append("anthropic-version", API_VERSION)
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
                contentType(ContentType.Application.Json)
                setBody(request.copy(stream = true))
            }

            if (!response.status.isSuccess()) {
                val error = try {
                    response.body<ApiError>()
                } catch (e: Exception) {
                    ApiError("error", ErrorDetail("unknown", response.bodyAsText()))
                }
                return Result.failure(ApiException(error.error.message))
            }

            val channel: ByteReadChannel = response.bodyAsChannel()
            val streamFlow = flow {
                val buffer = StringBuilder()

                while (!channel.isClosedForRead) {
                    val chunk = channel.readUTF8Line() ?: break

                    // Skip empty lines and comments
                    if (chunk.isBlank() || chunk.startsWith(":")) continue

                    // Parse SSE format: "data: {...}"
                    if (chunk.startsWith("data: ")) {
                        val jsonData = chunk.removePrefix("data: ").trim()

                        // Check for end of stream
                        if (jsonData == "[DONE]") break

                        try {
                            val event = json.decodeFromString<StreamingEvent>(jsonData)

                            // Extract text from different event types
                            when (event.type) {
                                "content_block_delta" -> {
                                    event.delta?.text?.let { text ->
                                        emit(text)
                                        buffer.append(text)
                                    }
                                }
                                "message_stop" -> {
                                    break
                                }
                            }
                        } catch (e: Exception) {
                            // Skip malformed events
                            println("Failed to parse streaming event: $jsonData")
                        }
                    }
                }
            }

            Result.success(streamFlow)
        } catch (e: Exception) {
            Result.failure(ApiException("Failed to send streaming message: ${e.message}", e))
        }
    }

    /**
     * Validate API key by making a test request
     */
    suspend fun validateApiKey(apiKey: String): Result<Boolean> {
        return try {
            val response = httpClient.get("$BASE_URL/v1/models") {
                headers {
                    append("x-api-key", apiKey)
                    append("anthropic-version", API_VERSION)
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
            }

            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(ApiException("Failed to validate API key: ${e.message}", e))
        }
    }
}

/**
 * Custom exception for API errors
 */
class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)
