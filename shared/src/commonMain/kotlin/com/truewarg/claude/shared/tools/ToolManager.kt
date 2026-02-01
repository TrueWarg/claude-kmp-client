package com.truewarg.claude.shared.tools

import kotlinx.serialization.json.JsonElement

/**
 * Manages all available tools and routes execution to appropriate executors
 */
class ToolManager(
    private val executors: List<ToolExecutor>
) {
    private val toolMap: Map<String, ToolExecutor> = buildMap {
        executors.forEach { executor ->
            executor.getSupportedTools().forEach { tool ->
                put(tool.name, executor)
            }
        }
    }

    /**
     * Get all supported tool definitions for Claude API
     */
    fun getAllToolDefinitions(): List<ToolDefinition> {
        return executors.flatMap { it.getSupportedTools() }
    }

    /**
     * Execute a tool by name
     */
    suspend fun executeTool(toolName: String, input: Map<String, JsonElement>): ToolExecutionResult {
        val executor = toolMap[toolName]
            ?: return ToolExecutionResult(
                output = "Unknown tool: $toolName. Available tools: ${toolMap.keys.joinToString()}",
                isError = true
            )

        return executor.execute(toolName, input)
    }

    /**
     * Check if a tool is supported
     */
    fun isToolSupported(toolName: String): Boolean {
        return toolName in toolMap
    }
}
