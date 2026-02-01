package com.truewarg.claude.shared.tools

import com.truewarg.claude.shared.filesystem.FileSystemManager
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

class FileSystemTools(
    private val fileSystemManager: FileSystemManager
) : ToolExecutor {

    override suspend fun execute(toolName: String, input: Map<String, JsonElement>): ToolExecutionResult {
        return try {
            when (toolName) {
                "read_file" -> executeReadFile(input)
                "write_file" -> executeWriteFile(input)
                "list_files" -> executeListFiles(input)
                "create_file" -> executeCreateFile(input)
                "delete_file" -> executeDeleteFile(input)
                "file_exists" -> executeFileExists(input)
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
                name = "read_file",
                description = "Read the contents of a file at the specified path",
                input_schema = InputSchema(
                    properties = mapOf(
                        "path" to PropertyDefinition(
                            type = "string",
                            description = "The absolute or relative path to the file to read"
                        )
                    ),
                    required = listOf("path")
                )
            ),
            ToolDefinition(
                name = "write_file",
                description = "Write content to a file at the specified path. Creates the file if it doesn't exist.",
                input_schema = InputSchema(
                    properties = mapOf(
                        "path" to PropertyDefinition(
                            type = "string",
                            description = "The absolute or relative path to the file to write"
                        ),
                        "content" to PropertyDefinition(
                            type = "string",
                            description = "The content to write to the file"
                        )
                    ),
                    required = listOf("path", "content")
                )
            ),
            ToolDefinition(
                name = "list_files",
                description = "List all files and directories in the specified directory",
                input_schema = InputSchema(
                    properties = mapOf(
                        "path" to PropertyDefinition(
                            type = "string",
                            description = "The absolute or relative path to the directory to list"
                        )
                    ),
                    required = listOf("path")
                )
            ),
            ToolDefinition(
                name = "create_file",
                description = "Create a new empty file at the specified path",
                input_schema = InputSchema(
                    properties = mapOf(
                        "path" to PropertyDefinition(
                            type = "string",
                            description = "The absolute or relative path for the new file"
                        )
                    ),
                    required = listOf("path")
                )
            ),
            ToolDefinition(
                name = "delete_file",
                description = "Delete a file at the specified path",
                input_schema = InputSchema(
                    properties = mapOf(
                        "path" to PropertyDefinition(
                            type = "string",
                            description = "The absolute or relative path to the file to delete"
                        )
                    ),
                    required = listOf("path")
                )
            ),
            ToolDefinition(
                name = "file_exists",
                description = "Check if a file or directory exists at the specified path",
                input_schema = InputSchema(
                    properties = mapOf(
                        "path" to PropertyDefinition(
                            type = "string",
                            description = "The absolute or relative path to check"
                        )
                    ),
                    required = listOf("path")
                )
            )
        )
    }

    private suspend fun executeReadFile(input: Map<String, JsonElement>): ToolExecutionResult {
        val path = input["path"]?.jsonPrimitive?.content
            ?: return ToolExecutionResult("Missing 'path' parameter", isError = true)

        return fileSystemManager.readFile(path).fold(
            onSuccess = { content ->
                ToolExecutionResult(content)
            },
            onFailure = { error ->
                ToolExecutionResult(
                    output = "Failed to read file: ${error.message}",
                    isError = true
                )
            }
        )
    }

    private suspend fun executeWriteFile(input: Map<String, JsonElement>): ToolExecutionResult {
        val path = input["path"]?.jsonPrimitive?.content
            ?: return ToolExecutionResult("Missing 'path' parameter", isError = true)
        val content = input["content"]?.jsonPrimitive?.content
            ?: return ToolExecutionResult("Missing 'content' parameter", isError = true)

        return fileSystemManager.writeFile(path, content).fold(
            onSuccess = {
                ToolExecutionResult("File written successfully to: $path")
            },
            onFailure = { error ->
                ToolExecutionResult(
                    output = "Failed to write file: ${error.message}",
                    isError = true
                )
            }
        )
    }

    private suspend fun executeListFiles(input: Map<String, JsonElement>): ToolExecutionResult {
        val path = input["path"]?.jsonPrimitive?.content
            ?: return ToolExecutionResult("Missing 'path' parameter", isError = true)

        return fileSystemManager.listFiles(path).fold(
            onSuccess = { files ->
                val output = buildString {
                    appendLine("Files in $path:")
                    files.forEach { file ->
                        val type = if (file.isDirectory) "[DIR]" else "[FILE]"
                        val size = if (file.isDirectory) "" else " (${file.size} bytes)"
                        appendLine("$type ${file.name}$size")
                    }
                    if (files.isEmpty()) {
                        appendLine("(empty directory)")
                    }
                }
                ToolExecutionResult(output)
            },
            onFailure = { error ->
                ToolExecutionResult(
                    output = "Failed to list files: ${error.message}",
                    isError = true
                )
            }
        )
    }

    private suspend fun executeCreateFile(input: Map<String, JsonElement>): ToolExecutionResult {
        val path = input["path"]?.jsonPrimitive?.content
            ?: return ToolExecutionResult("Missing 'path' parameter", isError = true)

        return fileSystemManager.createFile(path).fold(
            onSuccess = {
                ToolExecutionResult("File created successfully: $path")
            },
            onFailure = { error ->
                ToolExecutionResult(
                    output = "Failed to create file: ${error.message}",
                    isError = true
                )
            }
        )
    }

    private suspend fun executeDeleteFile(input: Map<String, JsonElement>): ToolExecutionResult {
        val path = input["path"]?.jsonPrimitive?.content
            ?: return ToolExecutionResult("Missing 'path' parameter", isError = true)

        return fileSystemManager.deleteFile(path).fold(
            onSuccess = {
                ToolExecutionResult("File deleted successfully: $path")
            },
            onFailure = { error ->
                ToolExecutionResult(
                    output = "Failed to delete file: ${error.message}",
                    isError = true
                )
            }
        )
    }

    private suspend fun executeFileExists(input: Map<String, JsonElement>): ToolExecutionResult {
        val path = input["path"]?.jsonPrimitive?.content
            ?: return ToolExecutionResult("Missing 'path' parameter", isError = true)

        val exists = fileSystemManager.fileExists(path)
        return ToolExecutionResult("File exists: $exists")
    }
}
