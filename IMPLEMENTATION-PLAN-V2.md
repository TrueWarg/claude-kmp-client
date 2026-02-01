# Implementation Plan v2 - Chat-First Architecture

## Overview
Complete redesign to match Claude Code CLI philosophy - everything happens in one chat conversation.

## Phase 1: Minimal Working Chat (MVP)
**Goal:** Basic chat that can send/receive messages with streaming

### Commit 1: Data models for chat
- `ChatMessage` data class
- `AssistantContent` sealed class (Text, Thinking, ToolUse, etc.)
- `StreamingEvent` for SSE parsing
- `Conversation` model for storing chat history

### Commit 2: Update ClaudeApiClient for chat
- Simplify API client to focus on chat
- Implement streaming with proper SSE parsing
- Handle different content block types (text, thinking, tool_use)
- Parse streaming events correctly

### Commit 3: Chat repository and storage
- `ChatRepository` for API interactions
- `ConversationRepository` for local storage
- Handle message history
- Manage streaming state

### Commit 4: Basic ChatScreen UI
- Message list (LazyColumn)
- Text input at bottom
- Simple message bubbles (user vs assistant)
- Basic Material3 design

### Commit 5: Streaming implementation
- Real-time message updates
- Streaming cursor animation
- Handle partial messages
- Update UI as deltas arrive

**Milestone:** Can chat with Claude, see streaming responses, basic conversation flow works

---

## Phase 2: Tool Support
**Goal:** Claude can read/write files, run commands

### Commit 6: Tool execution framework
- `ToolExecutor` interface
- Tool result models
- File system tools (read, write, list)
- Basic bash execution

### Commit 7: Tool use in chat
- Send tool results back to API
- Tool use badge in messages
- Collapsible tool sections
- Show tool input/output

### Commit 8: File system integration
- Workspace folder selection
- File reading/writing
- Permission handling
- Scoped storage for Android

**Milestone:** Claude can read/write files, execute commands, show results in chat

---

## Phase 3: Enhanced Message Types
**Goal:** Better visualization of different content types

### Commit 9: Extended thinking support
- Parse thinking blocks
- Collapsible thinking sections
- Toggle to show/hide reasoning
- Styled differently from regular text

### Commit 10: Better tool visualization
- Expandable tool cards
- Syntax highlighting for tool input
- Tool execution status (running/done/error)
- Multiple tools in one message

### Commit 11: Progress indicators
- Show when Claude is "thinking"
- Tool execution progress
- Better loading states
- Typing indicators

**Milestone:** Rich message types, good UX for different content

---

## Phase 4: Interactive Elements
**Goal:** User can respond with selections, not just text

### Commit 12: AskUserQuestion tool
- Parse question blocks from API
- Render option buttons
- Send selection back to Claude
- Handle multi-select

### Commit 13: Option selection UI
- Tappable option cards
- Visual feedback
- Send structured response
- Continue conversation flow

**Milestone:** Bidirectional structured interaction works

---

## Phase 5: Conversation Management
**Goal:** Multiple conversations, persistence

### Commit 14: Conversation list
- List of past conversations
- Create new conversation
- Delete conversations
- Conversation titles

### Commit 15: Persistence
- Save messages to local DB
- Load conversation history
- Handle app restart
- Clear conversations

**Milestone:** Can manage multiple conversations

---

## Phase 6: Settings & Polish
**Goal:** Production-ready app

### Commit 16: Settings screen
- API key management (reuse from Phase 2)
- Model selection
- Language preference
- Clear data option

### Commit 17: UI polish
- Better message styling
- Code blocks with syntax highlighting
- Markdown rendering
- Dark mode

### Commit 18: Error handling
- Network errors
- API errors
- Tool execution errors
- Retry mechanisms

### Commit 19: Performance optimization
- Message virtualization
- Efficient streaming
- Memory management
- Background processing

**Milestone:** Production-ready app

---

## Technology Choices

### Keep from Current Implementation:
✅ Kotlin Multiplatform structure
✅ Compose Multiplatform UI
✅ Ktor for API
✅ Koin for DI
✅ Secure storage for API keys
✅ File system abstractions

### Remove from Current Implementation:
❌ Multiple screen navigation (Home, Agents, Skills, Editor)
❌ File tree view as separate screen
❌ Model selection as main feature
❌ Complex navigation system

### Add New:
✅ Streaming event parser
✅ Tool executor framework
✅ Message type renderer
✅ Conversation storage
✅ Interactive message elements

---

## Key Files (New Structure)

### Data Layer
```
shared/src/commonMain/kotlin/
├── data/models/
│   ├── ChatMessage.kt          (message with content blocks)
│   ├── AssistantContent.kt     (sealed class for content types)
│   ├── StreamingEvent.kt       (SSE event types)
│   ├── Conversation.kt         (conversation metadata)
│   └── ToolModels.kt          (tool use/result models)
├── data/repository/
│   ├── ChatRepository.kt       (main chat logic)
│   ├── ConversationRepository.kt (storage)
│   └── ToolExecutor.kt        (tool execution)
└── tools/
    ├── FileTools.kt           (read/write/list)
    └── BashTools.kt           (command execution)
```

### UI Layer
```
composeApp/src/commonMain/kotlin/ui/
├── chat/
│   ├── ChatScreen.kt          (main screen)
│   ├── MessageList.kt         (messages display)
│   ├── MessageItem.kt         (single message)
│   ├── TextContent.kt         (text rendering)
│   ├── ThinkingContent.kt     (thinking blocks)
│   ├── ToolUseContent.kt      (tool visualization)
│   ├── AskUserContent.kt      (option selection)
│   ├── ChatInput.kt           (text input)
│   └── StreamingIndicator.kt  (typing animation)
├── settings/
│   └── SettingsScreen.kt      (minimal settings)
└── conversations/
    └── ConversationsList.kt   (conversation history)
```

---

## Migration Strategy

### Option 1: Start Fresh (Recommended)
1. Create new branch `chat-first-v2`
2. Keep only foundational code:
   - DI setup (Koin modules)
   - API client (Claude API)
   - Secure storage (API keys)
   - Localization (strings)
   - File system manager
3. Delete everything else (navigation, multiple screens, etc.)
4. Build chat-first from scratch

### Option 2: Gradual Migration
1. Keep current branch
2. Delete unused screens (Agents, Skills, Editor, Home)
3. Simplify navigation to just Chat + Settings
4. Rebuild ChatScreen from scratch
5. Update models and repositories

**Recommendation:** Option 1 (Start Fresh) - cleaner, faster, avoids confusion

---

## Estimated Timeline

| Phase | Duration | Commits |
|-------|----------|---------|
| Phase 1: MVP Chat | 2 days | 5 commits |
| Phase 2: Tools | 2 days | 3 commits |
| Phase 3: Enhanced Messages | 1 day | 3 commits |
| Phase 4: Interactive | 1 day | 2 commits |
| Phase 5: Conversations | 1 day | 2 commits |
| Phase 6: Polish | 2 days | 4 commits |
| **Total** | **9 days** | **19 commits** |

---

## Next Steps

1. Review and approve this plan
2. Decide: Start fresh or migrate?
3. Create new branch
4. Begin Phase 1: Minimal Working Chat
5. Iterate quickly with working increments

---

## Success Criteria

The app is successful when:
- ✅ User can have a natural conversation with Claude
- ✅ Streaming responses work smoothly
- ✅ Claude can read/write files through conversation
- ✅ Tool use is visualized clearly
- ✅ Conversations are saved and restorable
- ✅ Works on both Android and Desktop
- ✅ Feels like "Claude Code CLI on mobile"
