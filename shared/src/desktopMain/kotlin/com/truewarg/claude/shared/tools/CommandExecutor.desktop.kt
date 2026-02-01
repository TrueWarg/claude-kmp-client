package com.truewarg.claude.shared.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class CommandExecutor {
    actual suspend fun execute(command: String, workingDirectory: String?): CommandResult {
        return withContext(Dispatchers.IO) {
            try {
                val processBuilder = ProcessBuilder()

                // Use sh -c to execute the command in a shell
                processBuilder.command("sh", "-c", command)

                if (workingDirectory != null) {
                    processBuilder.directory(File(workingDirectory))
                }

                processBuilder.redirectErrorStream(true)

                val process = processBuilder.start()
                val output = process.inputStream.bufferedReader().use { it.readText() }
                val exitCode = process.waitFor()

                CommandResult(
                    output = output,
                    exitCode = exitCode
                )
            } catch (e: Exception) {
                CommandResult(
                    output = "Failed to execute command: ${e.message}",
                    exitCode = -1,
                    isError = true
                )
            }
        }
    }
}
