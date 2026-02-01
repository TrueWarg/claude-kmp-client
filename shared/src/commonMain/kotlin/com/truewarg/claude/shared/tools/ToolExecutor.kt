package com.truewarg.claude.shared.tools

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Result of tool execution
 */
@Serializable
data class ToolExecutionResult(
    val output: String,
    val isError: Boolean = false
)

/**
 * Interface for executing tools requested by Claude
 */
interface ToolExecutor {
    suspend fun execute(toolName: String, input: Map<String, JsonElement>): ToolExecutionResult
    fun getSupportedTools(): List<ToolDefinition>
}

/**
 * Tool definition for Claude API
 */
@Serializable
data class ToolDefinition(
    val name: String,
    val description: String,
    val input_schema: InputSchema
)

@Serializable
data class InputSchema(
    val type: String = "object",
    val properties: Map<String, PropertyDefinition>,
    val required: List<String> = emptyList()
)

@Serializable
data class PropertyDefinition(
    val type: String,
    val description: String
)
