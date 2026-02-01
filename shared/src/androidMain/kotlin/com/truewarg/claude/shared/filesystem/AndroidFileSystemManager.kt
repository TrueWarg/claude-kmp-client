package com.truewarg.claude.shared.filesystem

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android implementation of FileSystemManager
 * Uses DocumentFile API for Scoped Storage compatibility (API 29+)
 * Falls back to java.io.File for internal storage
 */
class AndroidFileSystemManager(
    private val context: Context
) : FileSystemManager {

    private var currentWorkspaceUri: Uri? = null

    /**
     * Set workspace URI from document tree picker
     * This should be called from Activity with Intent.ACTION_OPEN_DOCUMENT_TREE
     */
    fun setWorkspaceUri(uri: Uri) {
        currentWorkspaceUri = uri
        // Persist permission for this URI
        context.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    override suspend fun pickFolder(): String? {
        // This needs to be implemented in the Activity layer
        // Return null for now - will be handled by UI
        return null
    }

    override suspend fun pickFile(): String? {
        // This needs to be implemented in the Activity layer
        // Return null for now - will be handled by UI
        return null
    }

    override suspend fun readFile(path: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Try as regular file path first (for internal storage)
            val file = File(path)
            if (file.exists() && file.canRead()) {
                return@withContext Result.success(file.readText())
            }

            // Try as document file
            currentWorkspaceUri?.let { workspaceUri ->
                val documentFile = findDocumentFile(path, workspaceUri)
                if (documentFile?.canRead() == true) {
                    val content = context.contentResolver.openInputStream(documentFile.uri)?.use { stream ->
                        stream.bufferedReader().readText()
                    }
                    return@withContext Result.success(content ?: "")
                }
            }

            Result.failure(FileSystemException("File not found or not readable: $path"))
        } catch (e: Exception) {
            Result.failure(FileSystemException("Failed to read file: ${e.message}", e))
        }
    }

    override suspend fun writeFile(path: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Try as regular file path first (for internal storage)
            val file = File(path)
            if (file.parentFile?.exists() == true || file.absolutePath.startsWith(context.filesDir.absolutePath)) {
                file.parentFile?.mkdirs()
                file.writeText(content)
                return@withContext Result.success(Unit)
            }

            // Try as document file
            currentWorkspaceUri?.let { workspaceUri ->
                val documentFile = findDocumentFile(path, workspaceUri)
                if (documentFile != null) {
                    context.contentResolver.openOutputStream(documentFile.uri)?.use { stream ->
                        stream.bufferedWriter().write(content)
                    }
                    return@withContext Result.success(Unit)
                }
            }

            Result.failure(FileSystemException("Cannot write to file: $path"))
        } catch (e: Exception) {
            Result.failure(FileSystemException("Failed to write file: ${e.message}", e))
        }
    }

    override suspend fun createFile(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.absolutePath.startsWith(context.filesDir.absolutePath)) {
                file.parentFile?.mkdirs()
                file.createNewFile()
                return@withContext Result.success(Unit)
            }

            currentWorkspaceUri?.let { workspaceUri ->
                val documentFile = DocumentFile.fromTreeUri(context, workspaceUri)
                val fileName = File(path).name
                documentFile?.createFile("*/*", fileName)
                return@withContext Result.success(Unit)
            }

            Result.failure(FileSystemException("Cannot create file: $path"))
        } catch (e: Exception) {
            Result.failure(FileSystemException("Failed to create file: ${e.message}", e))
        }
    }

    override suspend fun deleteFile(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
                return@withContext Result.success(Unit)
            }

            currentWorkspaceUri?.let { workspaceUri ->
                val documentFile = findDocumentFile(path, workspaceUri)
                if (documentFile != null) {
                    documentFile.delete()
                    return@withContext Result.success(Unit)
                }
            }

            Result.failure(FileSystemException("File not found: $path"))
        } catch (e: Exception) {
            Result.failure(FileSystemException("Failed to delete file: ${e.message}", e))
        }
    }

    override suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(path)
        if (file.exists()) {
            return@withContext true
        }

        currentWorkspaceUri?.let { workspaceUri ->
            val documentFile = findDocumentFile(path, workspaceUri)
            return@withContext documentFile?.exists() ?: false
        }

        false
    }

    override suspend fun listFiles(path: String): Result<List<FileEntry>> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists() && file.isDirectory) {
                val files = file.listFiles()?.map { f ->
                    FileEntry(
                        name = f.name,
                        path = f.absolutePath,
                        isDirectory = f.isDirectory,
                        size = if (f.isFile) f.length() else 0L,
                        lastModified = f.lastModified()
                    )
                } ?: emptyList()
                return@withContext Result.success(files.sortedWith(compareBy({ !it.isDirectory }, { it.name })))
            }

            currentWorkspaceUri?.let { workspaceUri ->
                val documentFile = findDocumentFile(path, workspaceUri)
                    ?: DocumentFile.fromTreeUri(context, workspaceUri)

                if (documentFile?.isDirectory == true) {
                    val files = documentFile.listFiles().map { doc ->
                        FileEntry(
                            name = doc.name ?: "",
                            path = doc.uri.toString(),
                            isDirectory = doc.isDirectory,
                            size = if (doc.isFile) doc.length() else 0L,
                            lastModified = doc.lastModified()
                        )
                    }
                    return@withContext Result.success(files.sortedWith(compareBy({ !it.isDirectory }, { it.name })))
                }
            }

            Result.failure(FileSystemException("Directory not found: $path"))
        } catch (e: Exception) {
            Result.failure(FileSystemException("Failed to list files: ${e.message}", e))
        }
    }

    override suspend fun getFileInfo(path: String): Result<FileInfo> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists()) {
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
                return@withContext Result.success(info)
            }

            currentWorkspaceUri?.let { workspaceUri ->
                val documentFile = findDocumentFile(path, workspaceUri)
                if (documentFile?.exists() == true) {
                    val info = FileInfo(
                        path = documentFile.uri.toString(),
                        name = documentFile.name ?: "",
                        isDirectory = documentFile.isDirectory,
                        size = if (documentFile.isFile) documentFile.length() else 0L,
                        lastModified = documentFile.lastModified(),
                        isReadable = documentFile.canRead(),
                        isWritable = documentFile.canWrite(),
                        extension = documentFile.name?.substringAfterLast('.', "")?.takeIf { it.isNotEmpty() }
                    )
                    return@withContext Result.success(info)
                }
            }

            Result.failure(FileSystemException("File not found: $path"))
        } catch (e: Exception) {
            Result.failure(FileSystemException("Failed to get file info: ${e.message}", e))
        }
    }

    override fun watchDirectory(path: String): Flow<FileSystemEvent> = flow {
        // Android FileObserver could be used here, but it's complex with Scoped Storage
        // For now, return empty flow
        // TODO: Implement FileObserver for internal storage paths
    }

    override suspend fun hasPermissions(): Boolean {
        // Check if we have persistent permission for workspace
        if (currentWorkspaceUri != null) {
            return true
        }

        // Check if we need storage permissions (for API < 29)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        return false
    }

    override suspend fun requestPermissions(): Boolean {
        // This needs to be handled in Activity layer
        // Return false for now - will be handled by UI
        return false
    }

    private fun findDocumentFile(path: String, workspaceUri: Uri): DocumentFile? {
        val workspaceRoot = DocumentFile.fromTreeUri(context, workspaceUri) ?: return null

        // If path is already a URI
        if (path.startsWith("content://")) {
            return DocumentFile.fromSingleUri(context, Uri.parse(path))
        }

        // Try to find by relative path
        val fileName = File(path).name
        return findFileByName(workspaceRoot, fileName)
    }

    private fun findFileByName(directory: DocumentFile, fileName: String): DocumentFile? {
        directory.listFiles().forEach { file ->
            if (file.name == fileName) {
                return file
            }
            if (file.isDirectory) {
                findFileByName(file, fileName)?.let { return it }
            }
        }
        return null
    }
}
