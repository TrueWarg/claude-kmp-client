package com.truewarg.claude.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.truewarg.claude.shared.data.models.Conversation
import com.truewarg.claude.shared.data.repository.ConversationRepository
import com.truewarg.claude.shared.data.repository.SettingsRepository
import com.truewarg.claude.ui.screens.ChatScreen
import com.truewarg.claude.ui.screens.SettingsScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun App(
    settingsRepository: SettingsRepository = koinInject(),
    conversationRepository: ConversationRepository = koinInject()
) {
    var hasApiKey by remember { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            hasApiKey = settingsRepository.getApiKey() != null
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier) {
            when (hasApiKey) {
                null -> {
                    // Loading state
                }
                false -> {
                    SettingsScreen(
                        onApiKeyConfigured = {
                            hasApiKey = true
                        }
                    )
                }
                true -> {
                    val conversationId = remember {
                        val conversations = conversationRepository.getConversations()
                        if (conversations.isEmpty()) {
                            val timestamp = System.currentTimeMillis()
                            val newConversation = Conversation(
                                id = "$timestamp-${(0..999999).random()}",
                                title = "New Chat",
                                createdAt = timestamp,
                                updatedAt = timestamp
                            )
                            conversationRepository.createConversation(newConversation)
                            newConversation.id
                        } else {
                            conversations.first().id
                        }
                    }
                    ChatScreen(conversationId = conversationId)
                }
            }
        }
    }
}
