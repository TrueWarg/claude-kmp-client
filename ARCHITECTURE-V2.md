# Claude KMP Client - Chat-First Architecture (v2)

## Core Concept
A mobile Claude client that works **exactly like Claude Code CLI** - everything happens in ONE chat conversation, no separate screens for agents/skills/editor.

## UI Structure

### Screens (Minimal)
1. **Chat Screen** (90% of user time)
   - Main conversation interface
   - All interactions happen here
   - Full-screen chat with input at bottom

2. **Settings Screen** (accessed via button)
   - API key input
   - Model selection (optional)
   - Language preference
   - That's it - nothing else needed

### No Separate Screens For:
- ❌ Agents - handled via chat commands
- ❌ Skills - handled via chat commands
- ❌ Editor - files shown inline in chat
- ❌ File browser - files accessed via conversation

## Message Types

### User Messages
```kotlin
sealed class UserMessage {
    data class Text(val content: String) : UserMessage()
    data class OptionSelected(val optionId: String, val value: String) : UserMessage()
}
```

### Assistant Messages
```kotlin
sealed class AssistantContent {
    // Regular text response
    data class Text(val content: String) : AssistantContent()

    // Thinking block (extended thinking)
    data class Thinking(val content: String) : AssistantContent()

    // Tool use
    data class ToolUse(
        val toolName: String,
        val input: Map<String, Any>,
        val result: String?
    ) : AssistantContent()

    // Progress indicator
    data class Progress(val message: String) : AssistantContent()

    // Ask user for input (buttons/options)
    data class AskUser(
        val question: String,
        val options: List<Option>
    ) : AssistantContent()
}

data class Option(
    val id: String,
    val label: String,
    val description: String
)
```

## Chat Message Model

```kotlin
data class ChatMessage(
    val id: String,
    val role: Role,
    val content: List<AssistantContent>, // Can have multiple blocks
    val timestamp: Long,
    val isStreaming: Boolean = false
)

enum class Role {
    USER,
    ASSISTANT
}
```

## API Integration

### Streaming Flow
```
User types message
    ↓
Send to Claude API with stream=true
    ↓
Receive SSE events:
    - message_start
    - content_block_start (text/thinking/tool_use)
    - content_block_delta (incremental content)
    - content_block_stop
    - message_stop
    ↓
Update UI in real-time
```

### Event Types from API
1. **text blocks** - Regular responses
2. **thinking blocks** - Reasoning (with extended thinking enabled)
3. **tool_use blocks** - When Claude needs to use tools
4. **tool_result blocks** - Results of tool execution

## Tool Implementation

### Built-in Tools (Implemented on Client)
```kotlin
sealed class Tool {
    // File operations
    object ReadFile : Tool()
    object WriteFile : Tool()
    object ListFiles : Tool()

    // Code execution
    object BashCommand : Tool()

    // User interaction
    object AskUserQuestion : Tool()
}
```

### Tool Execution Flow
```
1. Claude requests tool use (tool_use block)
2. Client executes tool locally
3. Client sends tool_result back to Claude
4. Claude continues with result
```

## UI Components

### Chat Screen Layout
```
┌─────────────────────────┐
│  ☰  Claude Chat    ⚙️   │ <- Top bar with menu & settings
├─────────────────────────┤
│                         │
│  [User message]         │
│                         │
│  [Assistant response]   │
│  ├─ Text content        │
│  ├─ Thinking (collapsed)│
│  └─ Tool use (badge)    │
│                         │
│  [Streaming response... │
│   ▌]                    │ <- Cursor shows streaming
│                         │
│  [Ask user question]    │
│  [Option 1] [Option 2]  │ <- Interactive buttons
│                         │
│         ⋮               │
├─────────────────────────┤
│ Type message...     [>] │ <- Input at bottom
└─────────────────────────┘
```

### Message Rendering

**Text Message:**
```
┌─────────────────────────┐
│ 👤 You                  │
│ Please read config.json │
└─────────────────────────┘
```

**Assistant with Tool Use:**
```
┌─────────────────────────┐
│ 🤖 Claude               │
│ I'll read that file.    │
│                         │
│ 🔧 read_file            │ <- Tool badge, tap to expand
│    config.json          │
│                         │
│ The config contains...  │
└─────────────────────────┘
```

**Streaming Response:**
```
┌─────────────────────────┐
│ 🤖 Claude               │
│ Let me analyze that▌    │ <- Cursor animates
└─────────────────────────┘
```

**Ask User Question:**
```
┌─────────────────────────┐
│ 🤖 Claude               │
│ Which approach?         │
│                         │
│ ┌─────────────────────┐ │
│ │ Option A            │ │ <- Tappable buttons
│ │ Simple but limited  │ │
│ └─────────────────────┘ │
│ ┌─────────────────────┐ │
│ │ Option B            │ │
│ │ Complex but powerful│ │
│ └─────────────────────┘ │
└─────────────────────────┘
```

## Data Layer

### Repositories
```kotlin
// Main API interaction
class ChatRepository(
    private val apiClient: ClaudeApiClient,
    private val toolExecutor: ToolExecutor
) {
    suspend fun sendMessage(
        conversationId: String,
        message: String
    ): Flow<StreamingEvent>

    suspend fun executeToolAndContinue(
        conversationId: String,
        toolUse: ToolUse
    ): Flow<StreamingEvent>
}

// Local tool execution
class ToolExecutor(
    private val fileSystemManager: FileSystemManager
) {
    suspend fun execute(
        toolName: String,
        input: Map<String, Any>
    ): ToolResult
}

// Conversation storage
class ConversationRepository {
    suspend fun saveMessage(message: ChatMessage)
    suspend fun getConversation(id: String): List<ChatMessage>
    suspend fun listConversations(): List<Conversation>
}
```

## Key Differences from Current Implementation

### BEFORE (Wrong ❌):
- Multiple screens (Home, Chat, Agents, Skills, Editor, Settings)
- Separate UI for file browsing
- Separate UI for model selection
- Complex navigation
- Traditional mobile app structure

### AFTER (Correct ✅):
- ONE chat screen (everything happens here)
- Settings screen (just API key + model)
- Files accessed via conversation
- Tool use shown inline
- CLI-like experience on mobile

## Implementation Strategy

### Phase 1: Minimal Working Chat (1-2 days)
- Basic chat UI (messages list + input)
- API integration with streaming
- Default model (no selection yet)
- Text-only messages
- No tools yet

### Phase 2: Tool Support (2-3 days)
- Implement basic tools (read_file, write_file, list_files)
- Tool execution on client
- Tool result visualization
- File system integration

### Phase 3: Enhanced Messages (1-2 days)
- Extended thinking blocks
- Collapsible tool use sections
- Progress indicators
- Better streaming UX

### Phase 4: User Interaction (1 day)
- AskUserQuestion tool
- Option selection UI
- Interactive buttons

### Phase 5: Polish (1-2 days)
- Conversation persistence
- New conversation button
- Settings screen
- Language selection

## File Structure (Simplified)

```
shared/
  ├── api/
  │   └── ClaudeApiClient.kt
  ├── data/
  │   ├── models/
  │   │   ├── ChatMessage.kt
  │   │   ├── AssistantContent.kt
  │   │   └── StreamingEvent.kt
  │   └── repository/
  │       ├── ChatRepository.kt
  │       ├── ConversationRepository.kt
  │       └── ToolExecutor.kt
  └── tools/
      ├── FileTools.kt
      └── BashTools.kt

composeApp/
  └── ui/
      ├── chat/
      │   ├── ChatScreen.kt
      │   ├── MessageItem.kt
      │   ├── StreamingIndicator.kt
      │   └── ToolUseCard.kt
      └── settings/
          └── SettingsScreen.kt
```

## Summary

**Core Principle:** The app is a **conversation interface**, not a traditional mobile app with tabs and screens. Everything Claude can do (read files, run commands, spawn agents) happens through natural language in the chat, with results shown inline.

This matches how Claude Code CLI works, but on mobile with touch-friendly UI.
