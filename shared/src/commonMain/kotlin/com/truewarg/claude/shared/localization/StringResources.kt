package com.truewarg.claude.shared.localization

/**
 * Provides localized strings based on the selected language
 */
object StringResources {
    /**
     * Get strings for English language
     */
    val english = Strings(
        // Common
        appName = "Claude Client",
        ok = "OK",
        cancel = "Cancel",
        save = "Save",
        delete = "Delete",
        edit = "Edit",
        close = "Close",
        back = "Back",
        next = "Next",
        previous = "Previous",
        loading = "Loading...",
        error = "Error",
        retry = "Retry",
        success = "Success",

        // Navigation
        navHome = "Home",
        navChat = "Chat",
        navAgents = "Agents",
        navSkills = "Skills",
        navSettings = "Settings",
        navEditor = "Editor",

        // Settings
        settingsTitle = "Settings",
        settingsApiKey = "API Key",
        settingsApiKeyHint = "Enter your Claude API key",
        settingsModel = "Model",
        settingsLanguage = "Language",
        settingsTheme = "Theme",
        settingsThemeLight = "Light",
        settingsThemeDark = "Dark",
        settingsThemeSystem = "System",
        settingsMemorySize = "Memory Size",
        settingsSystemPrompt = "System Prompt",
        settingsSystemPromptHint = "Enter custom system prompt (optional)",

        // Chat
        chatTitle = "Chat",
        chatInputHint = "Type your message...",
        chatSend = "Send",
        chatNewConversation = "New Conversation",
        chatDeleteConversation = "Delete Conversation",
        chatDeleteConfirm = "Are you sure you want to delete this conversation?",
        chatEmptyState = "Start a new conversation with Claude",
        chatStreaming = "Claude is typing...",
        chatWelcomeMessage = """👋 Hello! I'm Claude, your AI coding assistant.

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

**What's your project directory, and how can I help you today?**""",
        chatErrorPrefix = "❌ **Error**",
        chatUnknownError = "Unknown error",
        chatThinking = "Thinking",
        chatThinkingStatus = "Thinking...",
        chatUsingTools = "Using tools...",
        chatWriting = "Writing...",
        chatProcessing = "Processing...",
        chatRunning = "Running",
        chatResult = "Result",
        chatLines = "lines",
        chatCollapse = "Collapse",
        chatExpand = "Expand",

        // Agents
        agentsTitle = "Custom Agents",
        agentsCreate = "Create Agent",
        agentsEdit = "Edit Agent",
        agentsDelete = "Delete Agent",
        agentsName = "Name",
        agentsNameHint = "Enter agent name",
        agentsDescription = "Description",
        agentsDescriptionHint = "Enter agent description",
        agentsInstructions = "Instructions",
        agentsInstructionsHint = "Enter agent instructions",
        agentsTools = "Available Tools",
        agentsToolsHint = "Select tools this agent can use",
        agentsEmptyState = "No agents created yet",

        // Skills
        skillsTitle = "Skills",
        skillsCreate = "Create Skill",
        skillsEdit = "Edit Skill",
        skillsDelete = "Delete Skill",
        skillsName = "Name",
        skillsDescription = "Description",
        skillsContent = "Content",
        skillsEmptyState = "No skills available",

        // Editor
        editorTitle = "Code Editor",
        editorSave = "Save",
        editorDiscard = "Discard",
        editorUnsavedChanges = "You have unsaved changes",
        editorLineNumbers = "Line Numbers",
        editorSyntaxHighlight = "Syntax Highlighting",

        // File System
        filesTitle = "Files",
        filesSelectFolder = "Select Folder",
        filesNoAccess = "No access to file system",
        filesPermissionRequired = "Storage permission required",
        filesSelectFile = "Select File",
        filesNoFiles = "No files found",
        filesFolder = "Folder",
        filesFile = "File",
        filesDirectory = "Directory",
        filesErrorLoading = "Error loading file",

        // Git
        gitTitle = "Git",
        gitCommit = "Commit",
        gitPush = "Push",
        gitPull = "Pull",
        gitStatus = "Status",
        gitBranch = "Branch",
        gitClone = "Clone",
        gitCommitMessage = "Commit Message",
        gitCommitMessageHint = "Enter commit message",
        gitNoChanges = "No changes to commit",

        // Errors
        errorNetwork = "Network error. Please check your connection.",
        errorApiKey = "Invalid API key. Please check your settings.",
        errorUnknown = "An unknown error occurred",
        errorFileNotFound = "File not found",
        errorPermission = "Permission denied",

        // Permissions
        permissionStorageTitle = "Storage Permission",
        permissionStorageMessage = "This app needs storage permission to access files",
        permissionGrant = "Grant Permission",
        permissionDenied = "Permission denied",
    )

    /**
     * Get strings for Russian language
     */
    val russian = Strings(
        // Common
        appName = "Клиент Claude",
        ok = "ОК",
        cancel = "Отмена",
        save = "Сохранить",
        delete = "Удалить",
        edit = "Изменить",
        close = "Закрыть",
        back = "Назад",
        next = "Далее",
        previous = "Предыдущий",
        loading = "Загрузка...",
        error = "Ошибка",
        retry = "Повторить",
        success = "Успешно",

        // Navigation
        navHome = "Главная",
        navChat = "Чат",
        navAgents = "Агенты",
        navSkills = "Навыки",
        navSettings = "Настройки",
        navEditor = "Редактор",

        // Settings
        settingsTitle = "Настройки",
        settingsApiKey = "API Ключ",
        settingsApiKeyHint = "Введите ваш API ключ Claude",
        settingsModel = "Модель",
        settingsLanguage = "Язык",
        settingsTheme = "Тема",
        settingsThemeLight = "Светлая",
        settingsThemeDark = "Тёмная",
        settingsThemeSystem = "Системная",
        settingsMemorySize = "Размер памяти",
        settingsSystemPrompt = "Системный промпт",
        settingsSystemPromptHint = "Введите системный промпт (опционально)",

        // Chat
        chatTitle = "Чат",
        chatInputHint = "Введите сообщение...",
        chatSend = "Отправить",
        chatNewConversation = "Новый разговор",
        chatDeleteConversation = "Удалить разговор",
        chatDeleteConfirm = "Вы уверены, что хотите удалить этот разговор?",
        chatEmptyState = "Начните новый разговор с Claude",
        chatStreaming = "Claude печатает...",
        chatWelcomeMessage = """👋 Здравствуйте! Я Claude, ваш AI-помощник для программирования.

**Чтобы начать, мне нужно знать, где находится ваш проект.**

Пожалуйста, укажите путь к рабочей области в первом сообщении. Например:
• "Работай в /Users/yourname/my-project"
• "Используй ~/code/myapp как рабочую область"
• "Мой проект находится в C:\Users\name\projects\app"

После указания рабочей области я смогу:
✓ Читать и записывать файлы
✓ Выполнять команды
✓ Помогать с задачами программирования
✓ Исправлять ошибки и добавлять функции

**Какой у вас путь к проекту, и чем я могу помочь?**""",
        chatErrorPrefix = "❌ **Ошибка**",
        chatUnknownError = "Неизвестная ошибка",
        chatThinking = "Размышление",
        chatThinkingStatus = "Размышляю...",
        chatUsingTools = "Использую инструменты...",
        chatWriting = "Пишу...",
        chatProcessing = "Обработка...",
        chatRunning = "Выполняется",
        chatResult = "Результат",
        chatLines = "строк",
        chatCollapse = "Свернуть",
        chatExpand = "Развернуть",

        // Agents
        agentsTitle = "Пользовательские агенты",
        agentsCreate = "Создать агента",
        agentsEdit = "Редактировать агента",
        agentsDelete = "Удалить агента",
        agentsName = "Название",
        agentsNameHint = "Введите название агента",
        agentsDescription = "Описание",
        agentsDescriptionHint = "Введите описание агента",
        agentsInstructions = "Инструкции",
        agentsInstructionsHint = "Введите инструкции для агента",
        agentsTools = "Доступные инструменты",
        agentsToolsHint = "Выберите инструменты для агента",
        agentsEmptyState = "Агенты ещё не созданы",

        // Skills
        skillsTitle = "Навыки",
        skillsCreate = "Создать навык",
        skillsEdit = "Редактировать навык",
        skillsDelete = "Удалить навык",
        skillsName = "Название",
        skillsDescription = "Описание",
        skillsContent = "Содержание",
        skillsEmptyState = "Навыки недоступны",

        // Editor
        editorTitle = "Редактор кода",
        editorSave = "Сохранить",
        editorDiscard = "Отменить",
        editorUnsavedChanges = "У вас есть несохранённые изменения",
        editorLineNumbers = "Номера строк",
        editorSyntaxHighlight = "Подсветка синтаксиса",

        // File System
        filesTitle = "Файлы",
        filesSelectFolder = "Выбрать папку",
        filesNoAccess = "Нет доступа к файловой системе",
        filesPermissionRequired = "Требуется разрешение на хранилище",
        filesSelectFile = "Выбрать файл",
        filesNoFiles = "Файлы не найдены",
        filesFolder = "Папка",
        filesFile = "Файл",
        filesDirectory = "Директория",
        filesErrorLoading = "Ошибка загрузки файла",

        // Git
        gitTitle = "Git",
        gitCommit = "Коммит",
        gitPush = "Отправить",
        gitPull = "Получить",
        gitStatus = "Статус",
        gitBranch = "Ветка",
        gitClone = "Клонировать",
        gitCommitMessage = "Сообщение коммита",
        gitCommitMessageHint = "Введите сообщение коммита",
        gitNoChanges = "Нет изменений для коммита",

        // Errors
        errorNetwork = "Ошибка сети. Проверьте подключение.",
        errorApiKey = "Неверный API ключ. Проверьте настройки.",
        errorUnknown = "Произошла неизвестная ошибка",
        errorFileNotFound = "Файл не найден",
        errorPermission = "Доступ запрещён",

        // Permissions
        permissionStorageTitle = "Разрешение на хранилище",
        permissionStorageMessage = "Приложению требуется доступ к хранилищу для работы с файлами",
        permissionGrant = "Предоставить разрешение",
        permissionDenied = "Доступ запрещён",
    )

    /**
     * Get strings for the specified language
     */
    fun getStrings(language: Language): Strings {
        return when (language) {
            Language.ENGLISH -> english
            Language.RUSSIAN -> russian
        }
    }
}
