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

                            // Add welcome message explaining what to do
                            val welcomeMessage = com.truewarg.claude.shared.data.models.ChatMessage(
                                id = "${timestamp}-welcome",
                                conversationId = newConversation.id,
                                role = com.truewarg.claude.shared.data.models.MessageRole.ASSISTANT,
                                content = listOf(
                                    com.truewarg.claude.shared.data.models.ContentBlock.Text(
                                        """👋 Hello! I'm Claude, your AI coding assistant.

**To get started, I need to know where your project is located.**

Please tell me your workspace path in your first message. For example:
• "Work in /Users/yourname/my-project"
• "Use ~/code/myapp as workspace"
• "My project is at C:\Users\name\projects\app"

Once you specify the workspace, I can:
✓ Read and write files
✓ Execute commands
✓ Help with coding tasks
✓ Fix bugs and add features

**What's your project directory, and how can I help you today?**"""
                                    )
                                ),
                                timestamp = timestamp
                            )
                            conversationRepository.addMessage(welcomeMessage)

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
