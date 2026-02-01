package com.truewarg.claude.shared.data.models

import kotlinx.serialization.Serializable

/**
 * Application settings model
 */
@Serializable
data class AppSettings(
    val selectedModel: String = "claude-3-5-sonnet-20241022",
    val workspacePath: String = "",
    val maxTokens: Int = 4096,
    val temperature: Double = 1.0,
    val systemPrompt: String = "",
    val githubUsername: String = "",
    val permissionMode: PermissionMode = PermissionMode.ASK,
    val isDarkTheme: Boolean = false
)

/**
 * Permission mode for file operations
 */
@Serializable
enum class PermissionMode {
    ASK,      // Ask before each operation
    AUTO,     // Automatically allow
    MANUAL    // Manual approval required
}
