package com.truewarg.claude.shared.tools

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

/**
 * Interface for executing bash/shell commands
 */
expect class CommandExecutor() {
    suspend fun execute(command: String, workingDirectory: String? = null): CommandResult
}

data class CommandResult(
    val output: String,
    val exitCode: Int,
    val isError: Boolean = exitCode != 0
)

class BashTools(
    private val commandExecutor: CommandExecutor
) : ToolExecutor {

    override suspend fun execute(toolName: String, input: Map<String, JsonElement>): ToolExecutionResult {
        return try {
            when (toolName) {
                "execute_command" -> executeCommand(input)
                else -> ToolExecutionResult(
                    output = "Unknown tool: $toolName",
                    isError = true
                )
            }
        } catch (e: Exception) {
            ToolExecutionResult(
                output = "Error executing $toolName: ${e.message}",
                isError = true
            )
        }
    }

    override fun getSupportedTools(): List<ToolDefinition> {
        return listOf(
            ToolDefinition(
                name = "execute_command",
                description = "Execute a shell command and return its output. Use with caution.",
                input_schema = InputSchema(
                    properties = mapOf(
                        "command" to PropertyDefinition(
                            type = "string",
                            description = "The shell command to execute"
                        ),
                        "working_directory" to PropertyDefinition(
                            type = "string",
                            description = "Optional working directory for the command"
                        )
                    ),
                    required = listOf("command")
                )
            )
        )
    }

    private suspend fun executeCommand(input: Map<String, JsonElement>): ToolExecutionResult {
        val command = input["command"]?.jsonPrimitive?.content
            ?: return ToolExecutionResult("Missing 'command' parameter", isError = true)
        val workingDirectory = input["working_directory"]?.jsonPrimitive?.content

        val result = commandExecutor.execute(command, workingDirectory)

        return ToolExecutionResult(
            output = buildString {
                appendLine("Command: $command")
                if (workingDirectory != null) {
                    appendLine("Working directory: $workingDirectory")
                }
                appendLine("Exit code: ${result.exitCode}")
                appendLine()
                appendLine("Output:")
                appendLine(result.output)
            },
            isError = result.isError
        )
    }
}
