package com.truewarg.claude.shared.storage

/**
 * Secure storage interface for sensitive data like API keys
 * Platform-specific implementations handle encryption and secure storage
 */
interface SecureStorage {
    /**
     * Store a value securely
     * @param key The key to store the value under
     * @param value The value to store
     */
    suspend fun putString(key: String, value: String)

    /**
     * Retrieve a securely stored value
     * @param key The key to retrieve
     * @return The stored value or null if not found
     */
    suspend fun getString(key: String): String?

    /**
     * Remove a stored value
     * @param key The key to remove
     */
    suspend fun remove(key: String)

    /**
     * Clear all stored values
     */
    suspend fun clear()

    /**
     * Check if a key exists
     * @param key The key to check
     * @return true if the key exists, false otherwise
     */
    suspend fun contains(key: String): Boolean

    companion object {
        const val KEY_CLAUDE_API_KEY = "claude_api_key"
        const val KEY_GITHUB_TOKEN = "github_token"
    }
}
