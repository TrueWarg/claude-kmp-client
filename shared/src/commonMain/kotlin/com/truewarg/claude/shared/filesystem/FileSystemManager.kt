package com.truewarg.claude.shared.filesystem

import kotlinx.coroutines.flow.Flow

/**
 * Cross-platform file system manager interface
 * Provides unified access to file system operations across Android and Desktop
 */
interface FileSystemManager {
    /**
     * Pick a folder/directory
     * @return The selected folder path or null if cancelled
     */
    suspend fun pickFolder(): String?

    /**
     * Pick a file
     * @return The selected file path or null if cancelled
     */
    suspend fun pickFile(): String?

    /**
     * Read file content as text
     * @param path The file path
     * @return The file content
     */
    suspend fun readFile(path: String): Result<String>

    /**
     * Write text content to file
     * @param path The file path
     * @param content The content to write
     */
    suspend fun writeFile(path: String, content: String): Result<Unit>

    /**
     * Create a new file
     * @param path The file path
     */
    suspend fun createFile(path: String): Result<Unit>

    /**
     * Delete a file
     * @param path The file path
     */
    suspend fun deleteFile(path: String): Result<Unit>

    /**
     * Check if file exists
     * @param path The file path
     * @return true if file exists, false otherwise
     */
    suspend fun fileExists(path: String): Boolean

    /**
     * List files in a directory
     * @param path The directory path
     * @return List of file entries in the directory
     */
    suspend fun listFiles(path: String): Result<List<FileEntry>>

    /**
     * Get file info
     * @param path The file path
     * @return File information
     */
    suspend fun getFileInfo(path: String): Result<FileInfo>

    /**
     * Watch directory for changes
     * @param path The directory path
     * @return Flow of file system events
     */
    fun watchDirectory(path: String): Flow<FileSystemEvent>

    /**
     * Check if app has necessary permissions
     */
    suspend fun hasPermissions(): Boolean

    /**
     * Request file system permissions
     */
    suspend fun requestPermissions(): Boolean
}

/**
 * File entry in a directory
 */
data class FileEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long
)

/**
 * Detailed file information
 */
data class FileInfo(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val isReadable: Boolean,
    val isWritable: Boolean,
    val extension: String?
)

/**
 * File system event types
 */
sealed class FileSystemEvent {
    data class FileCreated(val path: String) : FileSystemEvent()
    data class FileModified(val path: String) : FileSystemEvent()
    data class FileDeleted(val path: String) : FileSystemEvent()
}

/**
 * File system exception
 */
class FileSystemException(message: String, cause: Throwable? = null) : Exception(message, cause)
