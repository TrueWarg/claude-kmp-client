package com.truewarg.claude.shared.localization

/**
 * String resources for the application
 * Provides localized strings for all UI elements
 */
data class Strings(
    // Common
    val appName: String,
    val ok: String,
    val cancel: String,
    val save: String,
    val delete: String,
    val edit: String,
    val close: String,
    val back: String,
    val next: String,
    val previous: String,
    val loading: String,
    val error: String,
    val retry: String,
    val success: String,

    // Navigation
    val navHome: String,
    val navChat: String,
    val navAgents: String,
    val navSkills: String,
    val navSettings: String,
    val navEditor: String,

    // Settings
    val settingsTitle: String,
    val settingsApiKey: String,
    val settingsApiKeyHint: String,
    val settingsModel: String,
    val settingsLanguage: String,
    val settingsTheme: String,
    val settingsThemeLight: String,
    val settingsThemeDark: String,
    val settingsThemeSystem: String,
    val settingsMemorySize: String,
    val settingsSystemPrompt: String,
    val settingsSystemPromptHint: String,

    // Chat
    val chatTitle: String,
    val chatInputHint: String,
    val chatSend: String,
    val chatNewConversation: String,
    val chatDeleteConversation: String,
    val chatDeleteConfirm: String,
    val chatEmptyState: String,
    val chatStreaming: String,

    // Agents
    val agentsTitle: String,
    val agentsCreate: String,
    val agentsEdit: String,
    val agentsDelete: String,
    val agentsName: String,
    val agentsNameHint: String,
    val agentsDescription: String,
    val agentsDescriptionHint: String,
    val agentsInstructions: String,
    val agentsInstructionsHint: String,
    val agentsTools: String,
    val agentsToolsHint: String,
    val agentsEmptyState: String,

    // Skills
    val skillsTitle: String,
    val skillsCreate: String,
    val skillsEdit: String,
    val skillsDelete: String,
    val skillsName: String,
    val skillsDescription: String,
    val skillsContent: String,
    val skillsEmptyState: String,

    // Editor
    val editorTitle: String,
    val editorSave: String,
    val editorDiscard: String,
    val editorUnsavedChanges: String,
    val editorLineNumbers: String,
    val editorSyntaxHighlight: String,

    // File System
    val filesTitle: String,
    val filesSelectFolder: String,
    val filesNoAccess: String,
    val filesPermissionRequired: String,
    val filesSelectFile: String,

    // Git
    val gitTitle: String,
    val gitCommit: String,
    val gitPush: String,
    val gitPull: String,
    val gitStatus: String,
    val gitBranch: String,
    val gitClone: String,
    val gitCommitMessage: String,
    val gitCommitMessageHint: String,
    val gitNoChanges: String,

    // Errors
    val errorNetwork: String,
    val errorApiKey: String,
    val errorUnknown: String,
    val errorFileNotFound: String,
    val errorPermission: String,

    // Permissions
    val permissionStorageTitle: String,
    val permissionStorageMessage: String,
    val permissionGrant: String,
    val permissionDenied: String,
)
