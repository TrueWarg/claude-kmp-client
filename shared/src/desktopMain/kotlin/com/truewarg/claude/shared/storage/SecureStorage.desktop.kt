package com.truewarg.claude.shared.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Base64
import java.util.prefs.Preferences

/**
 * Desktop implementation of SecureStorage using Java Preferences
 * Uses Base64 encoding for basic obfuscation
 * Note: For production use, consider using a proper encryption library
 */
class DesktopSecureStorage : SecureStorage {

    private val preferences: Preferences by lazy {
        Preferences.userNodeForPackage(this::class.java).node(NODE_NAME)
    }

    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.IO) {
        val encoded = Base64.getEncoder().encodeToString(value.toByteArray())
        preferences.put(key, encoded)
        preferences.flush()
    }

    override suspend fun getString(key: String): String? = withContext(Dispatchers.IO) {
        val encoded = preferences.get(key, null) ?: return@withContext null
        try {
            String(Base64.getDecoder().decode(encoded))
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    override suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        preferences.remove(key)
        preferences.flush()
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        preferences.clear()
        preferences.flush()
    }

    override suspend fun contains(key: String): Boolean = withContext(Dispatchers.IO) {
        preferences.get(key, null) != null
    }

    companion object {
        private const val NODE_NAME = "claude_secure_storage"
    }
}
