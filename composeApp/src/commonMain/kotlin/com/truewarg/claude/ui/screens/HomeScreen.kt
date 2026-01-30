package com.truewarg.claude.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.truewarg.claude.shared.localization.LocalizationManager
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToAgents: () -> Unit,
    onNavigateToSkills: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val localizationManager: LocalizationManager = koinInject()
    val strings by localizationManager.strings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.navHome) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNavigateToChat,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(strings.navChat)
            }

            Button(
                onClick = onNavigateToAgents,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(strings.navAgents)
            }

            Button(
                onClick = onNavigateToSkills,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(strings.navSkills)
            }

            Button(
                onClick = onNavigateToSettings,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(strings.navSettings)
            }
        }
    }
}
