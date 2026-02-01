package com.truewarg.claude.shared.data.repository

import com.russhwolf.settings.Settings
import com.truewarg.claude.shared.data.models.AppSettings
import com.truewarg.claude.shared.data.models.PermissionMode
import com.truewarg.claude.shared.storage.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository for managing application settings and API keys
 * Combines SecureStorage for sensitive data and Settings for general preferences
 */
class SettingsRepository(
    private val secureStorage: SecureStorage,
    private val settings: Settings,
    private val json: Json
) {
    private val _appSettings = MutableStateFlow(loadSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    /**
     * Save Claude API key securely
     */
    suspend fun saveApiKey(apiKey: String) {
        secureStorage.putString(SecureStorage.KEY_CLAUDE_API_KEY, apiKey)
    }

    /**
     * Get Claude API key
     */
    suspend fun getApiKey(): String? {
        return secureStorage.getString(SecureStorage.KEY_CLAUDE_API_KEY)
    }

    /**
     * Check if API key is configured
     */
    suspend fun hasApiKey(): Boolean {
        return secureStorage.contains(SecureStorage.KEY_CLAUDE_API_KEY)
    }

    /**
     * Validate API key format
     * Claude API keys start with "sk-ant-"
     */
    fun validateApiKey(apiKey: String): Boolean {
        return apiKey.isNotBlank() && apiKey.startsWith("sk-ant-")
    }

    /**
     * Save GitHub personal access token
     */
    suspend fun saveGithubToken(token: String) {
        secureStorage.putString(SecureStorage.KEY_GITHUB_TOKEN, token)
    }

    /**
     * Get GitHub personal access token
     */
    suspend fun getGithubToken(): String? {
        return secureStorage.getString(SecureStorage.KEY_GITHUB_TOKEN)
    }

    /**
     * Update app settings
     */
    fun updateSettings(newSettings: AppSettings) {
        _appSettings.value = newSettings
        saveSettings(newSettings)
    }

    /**
     * Update selected model
     */
    fun setSelectedModel(model: String) {
        updateSettings(_appSettings.value.copy(selectedModel = model))
    }

    /**
     * Update workspace path
     */
    fun setWorkspacePath(path: String) {
        updateSettings(_appSettings.value.copy(workspacePath = path))
    }

    /**
     * Update theme
     */
    fun setDarkTheme(isDark: Boolean) {
        updateSettings(_appSettings.value.copy(isDarkTheme = isDark))
    }

    /**
     * Update permission mode
     */
    fun setPermissionMode(mode: PermissionMode) {
        updateSettings(_appSettings.value.copy(permissionMode = mode))
    }

    /**
     * Update system prompt
     */
    fun setSystemPrompt(prompt: String) {
        updateSettings(_appSettings.value.copy(systemPrompt = prompt))
    }

    /**
     * Update GitHub username
     */
    fun setGithubUsername(username: String) {
        updateSettings(_appSettings.value.copy(githubUsername = username))
    }

    /**
     * Clear all settings and secure data
     */
    suspend fun clearAll() {
        secureStorage.clear()
        settings.clear()
        _appSettings.value = AppSettings()
    }

    private fun loadSettings(): AppSettings {
        val json = settings.getStringOrNull(KEY_APP_SETTINGS) ?: return AppSettings()
        return try {
            this.json.decodeFromString(json)
        } catch (e: Exception) {
            AppSettings()
        }
    }

    private fun saveSettings(appSettings: AppSettings) {
        val json = this.json.encodeToString(appSettings)
        settings.putString(KEY_APP_SETTINGS, json)
    }

    companion object {
        private const val KEY_APP_SETTINGS = "app_settings"
    }
}
