package com.truewarg.claude.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.truewarg.claude.shared.data.repository.SettingsRepository
import com.truewarg.claude.shared.filesystem.FileEntry
import com.truewarg.claude.shared.filesystem.FileSystemManager
import com.truewarg.claude.shared.localization.LocalizationManager
import com.truewarg.claude.ui.components.FileTreeView
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(onNavigateBack: () -> Unit) {
    val localizationManager: LocalizationManager = koinInject()
    val settingsRepository: SettingsRepository = koinInject()
    val fileSystemManager: FileSystemManager = koinInject()

    val strings by localizationManager.strings.collectAsState()
    val appSettings by settingsRepository.appSettings.collectAsState()

    var selectedFile by remember { mutableStateOf<FileEntry?>(null) }
    var fileContent by remember { mutableStateOf("") }
    var isLoadingFile by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Load file content when file is selected
    LaunchedEffect(selectedFile) {
        selectedFile?.let { file ->
            if (!file.isDirectory) {
                isLoadingFile = true
                fileSystemManager.readFile(file.path).fold(
                    onSuccess = { content ->
                        fileContent = content
                        isLoadingFile = false
                    },
                    onFailure = { exception ->
                        fileContent = "${strings.filesErrorLoading}: ${exception.message}"
                        isLoadingFile = false
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.editorTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                fileSystemManager.pickFolder()?.let { folderPath ->
                                    settingsRepository.setWorkspacePath(folderPath)
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = strings.filesSelectFolder)
                    }
                }
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // File tree view (left panel)
            Surface(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight(),
                tonalElevation = 1.dp
            ) {
                if (appSettings.workspacePath.isNotBlank()) {
                    FileTreeView(
                        fileSystemManager = fileSystemManager,
                        rootPath = appSettings.workspacePath,
                        onFileSelected = { file ->
                            selectedFile = file
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = strings.filesSelectFolder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
            )

            // File content view (right panel)
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                when {
                    selectedFile == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.filesSelectFile,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    selectedFile?.isDirectory == true -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${strings.filesDirectory}: ${selectedFile?.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    isLoadingFile -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // File header
                            Text(
                                text = selectedFile?.name ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Divider()

                            // File content (read-only for now)
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                tonalElevation = 1.dp,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = fileContent,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp)
                                        .verticalScroll(scrollState)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
