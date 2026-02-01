package com.truewarg.claude.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.truewarg.claude.shared.filesystem.FileEntry
import com.truewarg.claude.shared.filesystem.FileSystemManager
import com.truewarg.claude.shared.localization.LocalizationManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun FileTreeView(
    fileSystemManager: FileSystemManager,
    rootPath: String,
    onFileSelected: (FileEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizationManager: LocalizationManager = koinInject()
    val strings by localizationManager.strings.collectAsState()

    var fileEntries by remember { mutableStateOf<List<FileEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(rootPath) {
        if (rootPath.isNotBlank()) {
            isLoading = true
            error = null

            fileSystemManager.listFiles(rootPath).fold(
                onSuccess = { files ->
                    fileEntries = files
                    isLoading = false
                },
                onFailure = { exception ->
                    error = exception.message
                    isLoading = false
                }
            )
        }
    }

    Column(modifier = modifier) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${strings.error}: $error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            fileEntries.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(strings.filesNoFiles)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(fileEntries) { entry ->
                        FileTreeItem(
                            fileEntry = entry,
                            fileSystemManager = fileSystemManager,
                            onFileSelected = onFileSelected,
                            depth = 0
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FileTreeItem(
    fileEntry: FileEntry,
    fileSystemManager: FileSystemManager,
    onFileSelected: (FileEntry) -> Unit,
    depth: Int,
    modifier: Modifier = Modifier
) {
    val localizationManager: LocalizationManager = koinInject()
    val strings by localizationManager.strings.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }
    var children by remember { mutableStateOf<List<FileEntry>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (fileEntry.isDirectory) {
                        isExpanded = !isExpanded
                        if (isExpanded && children.isEmpty()) {
                            scope.launch {
                                fileSystemManager.listFiles(fileEntry.path).fold(
                                    onSuccess = { files -> children = files },
                                    onFailure = {}
                                )
                            }
                        }
                    } else {
                        onFileSelected(fileEntry)
                    }
                }
                .padding(start = (depth * 16).dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = when {
                    fileEntry.isDirectory -> if (isExpanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowRight
                    else -> Icons.Filled.Done
                },
                contentDescription = if (fileEntry.isDirectory) strings.filesFolder else strings.filesFile,
                tint = if (fileEntry.isDirectory)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = fileEntry.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.7f)
            )

            if (!fileEntry.isDirectory) {
                Text(
                    text = formatFileSize(fileEntry.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (fileEntry.isDirectory && isExpanded && children.isNotEmpty()) {
            children.forEach { child ->
                FileTreeItem(
                    fileEntry = child,
                    fileSystemManager = fileSystemManager,
                    onFileSelected = onFileSelected,
                    depth = depth + 1
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
