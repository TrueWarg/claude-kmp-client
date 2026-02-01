package com.truewarg.claude.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.truewarg.claude.shared.data.models.ChatMessage
import com.truewarg.claude.shared.data.models.ContentBlock
import com.truewarg.claude.shared.data.models.MessageRole
import com.truewarg.claude.shared.data.repository.ChatRepository
import com.truewarg.claude.shared.data.repository.ConversationRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Format JSON string with proper indentation for better readability
 */
private fun formatJson(jsonString: String): String {
    return try {
        if (jsonString.isBlank()) return jsonString

        // Simple JSON formatter - adds newlines and indentation
        val result = StringBuilder()
        var indent = 0
        var inString = false
        var prevChar = ' '

        for (char in jsonString) {
            when {
                char == '"' && prevChar != '\\' -> {
                    inString = !inString
                    result.append(char)
                }
                !inString && (char == '{' || char == '[') -> {
                    result.append(char)
                    result.append('\n')
                    indent++
                    result.append("  ".repeat(indent))
                }
                !inString && (char == '}' || char == ']') -> {
                    result.append('\n')
                    indent--
                    result.append("  ".repeat(indent))
                    result.append(char)
                }
                !inString && char == ',' -> {
                    result.append(char)
                    result.append('\n')
                    result.append("  ".repeat(indent))
                }
                !inString && char == ':' -> {
                    result.append(char)
                    result.append(' ')
                }
                char.isWhitespace() && !inString -> {
                    // Skip extra whitespace outside strings
                }
                else -> result.append(char)
            }
            prevChar = char
        }

        result.toString()
    } catch (e: Exception) {
        // If formatting fails, return original string
        jsonString
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    chatRepository: ChatRepository = koinInject(),
    conversationRepository: ConversationRepository = koinInject()
) {
    var messages by remember { mutableStateOf(conversationRepository.getMessages(conversationId)) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(conversationId) {
        messages = conversationRepository.getMessages(conversationId)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = it,
                    duration = SnackbarDuration.Long
                )
                errorMessage = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val conversation = conversationRepository.getConversation(conversationId)
                    Text(conversation?.title ?: "Chat")
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            ChatInput(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank() && !isLoading) {
                        val message = inputText
                        inputText = ""
                        isLoading = true

                        scope.launch {
                            try {
                                chatRepository.sendMessage(
                                    conversationId = conversationId,
                                    userMessage = message
                                ).collect { updatedMessage ->
                                    messages = conversationRepository.getMessages(conversationId)
                                    scope.launch {
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error: ${e.message ?: "Unknown error"}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = !isLoading
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageItem(message = message)
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                message.content.forEachIndexed { index, block ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    when (block) {
                        is ContentBlock.Text -> {
                            Text(
                                text = block.text,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        is ContentBlock.Thinking -> {
                            ThinkingBlock(thinking = block.thinking)
                        }

                        is ContentBlock.ToolUse -> {
                            ToolUseBlock(toolUse = block, isRunning = message.isStreaming)
                        }

                        is ContentBlock.ToolResult -> {
                            ToolResultBlock(toolResult = block)
                        }
                    }
                }

                if (message.isStreaming) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ThinkingBlock(thinking: String) {
    var isExpanded by remember { mutableStateOf(false) }
    val preview = remember(thinking) {
        thinking.take(60).let { if (thinking.length > 60) "$it..." else it }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "💭",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Thinking",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!isExpanded && thinking.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preview,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded && thinking.isNotBlank()) {
                Divider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = thinking,
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.4
                        ),
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolUseBlock(toolUse: ContentBlock.ToolUse, isRunning: Boolean) {
    var isExpanded by remember { mutableStateOf(false) }
    val formattedInput = remember(toolUse.input) {
        formatJson(toolUse.input)
    }
    val inputPreview = remember(toolUse.input) {
        toolUse.input.take(50).let { if (toolUse.input.length > 50) "$it..." else it }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🔧",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = toolUse.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        if (isRunning) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(10.dp),
                                        strokeWidth = 1.5.dp,
                                        color = MaterialTheme.colorScheme.onTertiary
                                    )
                                    Text(
                                        text = "Running",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiary
                                    )
                                }
                            }
                        }
                    }
                    if (!isExpanded && toolUse.input.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = inputPreview,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded && toolUse.input.isNotBlank()) {
                Divider(
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = formattedInput,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.4
                        ),
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f),
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolResultBlock(toolResult: ContentBlock.ToolResult) {
    var isExpanded by remember { mutableStateOf(false) }
    val contentPreview = remember(toolResult.content) {
        toolResult.content.take(80).let { if (toolResult.content.length > 80) "$it..." else it }
    }
    val contentLines = remember(toolResult.content) {
        toolResult.content.lines().size
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (toolResult.isError) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (toolResult.isError) "❌" else "✅",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (toolResult.isError) "Error" else "Result",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (toolResult.isError) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        if (!toolResult.isError && contentLines > 1) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "$contentLines lines",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                    if (!isExpanded && toolResult.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = contentPreview,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = if (toolResult.isError) {
                                MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = if (toolResult.isError) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded && toolResult.content.isNotBlank()) {
                Divider(
                    color = if (toolResult.isError) {
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    },
                    thickness = 1.dp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (toolResult.isError) {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            }
                        )
                ) {
                    Text(
                        text = toolResult.content,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.4
                        ),
                        color = if (toolResult.isError) {
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                        },
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            when {
                                keyEvent.key == Key.Enter && !keyEvent.isShiftPressed -> {
                                    if (enabled && text.isNotBlank()) {
                                        onSend()
                                    }
                                    true
                                }
                                else -> false
                            }
                        } else {
                            false
                        }
                    },
                placeholder = { Text("Message... (Enter to send, Shift+Enter for new line)") },
                enabled = enabled,
                minLines = 1,
                maxLines = 6,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSend,
                enabled = enabled && text.isNotBlank(),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Send")
            }
        }
    }
}
