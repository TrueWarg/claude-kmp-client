package com.truewarg.claude.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.truewarg.claude.shared.data.repository.ModelsRepository
import com.truewarg.claude.shared.data.repository.SettingsRepository
import com.truewarg.claude.shared.localization.LocalizationManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val localizationManager: LocalizationManager = koinInject()
    val settingsRepository: SettingsRepository = koinInject()
    val modelsRepository: ModelsRepository = koinInject()

    val strings by localizationManager.strings.collectAsState()
    val appSettings by settingsRepository.appSettings.collectAsState()
    val models by modelsRepository.models.collectAsState()
    val modelsLoading by modelsRepository.isLoading.collectAsState()
    val modelsError by modelsRepository.error.collectAsState()

    var apiKey by remember { mutableStateOf("") }
    var apiKeyLoaded by remember { mutableStateOf(false) }
    var showApiKeyError by remember { mutableStateOf(false) }
    var apiKeySaved by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Load API key on first composition
    LaunchedEffect(Unit) {
        val key = settingsRepository.getApiKey()
        apiKey = key ?: ""
        apiKeyLoaded = true

        // Fetch models if API key exists
        if (!key.isNullOrBlank()) {
            modelsRepository.fetchModels()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = strings.back)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // API Key Section
            Text(
                text = strings.settingsApiKey,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = {
                    apiKey = it
                    showApiKeyError = false
                    apiKeySaved = false
                },
                label = { Text(strings.settingsApiKeyHint) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = showApiKeyError,
                supportingText = if (showApiKeyError) {
                    { Text(strings.errorApiKey) }
                } else null,
                enabled = apiKeyLoaded
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            if (settingsRepository.validateApiKey(apiKey)) {
                                settingsRepository.saveApiKey(apiKey)
                                showApiKeyError = false
                                apiKeySaved = true
                                // Fetch models after saving API key
                                modelsRepository.fetchModels(forceRefresh = true)
                            } else {
                                showApiKeyError = true
                            }
                        }
                    },
                    enabled = apiKey.isNotBlank()
                ) {
                    Text(strings.save)
                }

                if (apiKeySaved) {
                    Text(
                        text = strings.success,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            Divider()

            // Model Selection Section
            Text(
                text = strings.settingsModel,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (modelsLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (modelsError != null) {
                Text(
                    text = modelsError ?: strings.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = {
                        scope.launch {
                            modelsRepository.clearError()
                            modelsRepository.fetchModels(forceRefresh = true)
                        }
                    }
                ) {
                    Text(strings.retry)
                }
            } else if (models.isEmpty()) {
                Text(
                    text = strings.settingsApiKey + " " + strings.loading,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                var expanded by remember { mutableStateOf(false) }
                val selectedModel = models.find { it.id == appSettings.selectedModel }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedModel?.displayName ?: appSettings.selectedModel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.settingsModel) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        models.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model.displayName) },
                                onClick = {
                                    settingsRepository.setSelectedModel(model.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Divider()

            // System Prompt Section
            Text(
                text = strings.settingsSystemPrompt,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = appSettings.systemPrompt,
                onValueChange = { settingsRepository.setSystemPrompt(it) },
                label = { Text(strings.settingsSystemPromptHint) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )
        }
    }
}
