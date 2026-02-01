package com.truewarg.claude.shared.filesystem

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.*
import javax.swing.JFileChooser

/**
 * Desktop implementation of FileSystemManager using java.io.File and java.nio.file
 */
class DesktopFileSystemManager : FileSystemManager {

    override suspend fun pickFolder(): String? = withContext(Dispatchers.IO) {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Select Folder"
        }

        val result = chooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile.absolutePath
        } else {
            null
        }
    }

    override suspend fun pickFile(): String? = withContext(Dispatchers.IO) {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.FILES_ONLY
            dialogTitle = "Select File"
        }

        val result = chooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile.absolutePath
        } else {
            null
        }
    }

    override suspend fun readFile(path: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists()) {
                return@withContext Result.failure(FileSystemException("File not found: $path"))
            }
            if (!file.canRead()) {
                return@withContext Result.failure(FileSystemException("File not readable: $path"))
            }
            Result.success(file.readText())
        } catch (e: Exception) {
            Result.failure(FileSystemException("Failed to read file: ${e.message}", e))
        }
    }

    override suspend fun writeFile(path: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeText(content)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(FileSystemException("Failed to write file: ${e.message}", e))
        }
    }

    override suspend fun createFile(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.createNewFile()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(FileSystemException("Failed to create file: ${e.message}", e))
        }
    }

    override suspend fun deleteFile(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(FileSystemException("Failed to delete file: ${e.message}", e))
        }
    }

    override suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.IO) {
        File(path).exists()
    }

    override suspend fun listFiles(path: String): Result<List<FileEntry>> = withContext(Dispatchers.IO) {
        try {
            val directory = File(path)
            if (!directory.exists()) {
                return@withContext Result.failure(FileSystemException("Directory not found: $path"))
            }
            if (!directory.isDirectory) {
                return@withContext Result.failure(FileSystemException("Not a directory: $path"))
            }

            val files = directory.listFiles()?.map { file ->
                FileEntry(
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = file.isDirectory,
                    size = if (file.isFile) file.length() else 0L,
                    lastModified = file.lastModified()
                )
            } ?: emptyList()

            Result.success(files.sortedWith(compareBy({ !it.isDirectory }, { it.name })))
        } catch (e: Exception) {
            Result.failure(FileSystemException("Failed to list files: ${e.message}", e))
        }
    }

    override suspend fun getFileInfo(path: String): Result<FileInfo> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists()) {
                return@withContext Result.failure(FileSystemException("File not found: $path"))
            }

            val info = FileInfo(
                path = file.absolutePath,
                name = file.name,
                isDirectory = file.isDirectory,
                size = if (file.isFile) file.length() else 0L,
                lastModified = file.lastModified(),
                isReadable = file.canRead(),
                isWritable = file.canWrite(),
                extension = file.extension.takeIf { it.isNotEmpty() }
            )

            Result.success(info)
        } catch (e: Exception) {
            Result.failure(FileSystemException("Failed to get file info: ${e.message}", e))
        }
    }

    override fun watchDirectory(path: String): Flow<FileSystemEvent> = flow {
        try {
            val watchService = FileSystems.getDefault().newWatchService()
            val pathToWatch = Paths.get(path)

            pathToWatch.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
            )

            while (true) {
                val key = watchService.take()

                for (event in key.pollEvents()) {
                    val kind = event.kind()
                    val fileName = event.context() as Path
                    val fullPath = pathToWatch.resolve(fileName).toString()

                    when (kind) {
                        StandardWatchEventKinds.ENTRY_CREATE -> emit(FileSystemEvent.FileCreated(fullPath))
                        StandardWatchEventKinds.ENTRY_MODIFY -> emit(FileSystemEvent.FileModified(fullPath))
                        StandardWatchEventKinds.ENTRY_DELETE -> emit(FileSystemEvent.FileDeleted(fullPath))
                    }
                }

                if (!key.reset()) {
                    break
                }
            }
        } catch (e: Exception) {
            // Flow completed or cancelled
        }
    }

    override suspend fun hasPermissions(): Boolean {
        // Desktop doesn't need special permissions
        return true
    }

    override suspend fun requestPermissions(): Boolean {
        // Desktop doesn't need special permissions
        return true
    }
}
