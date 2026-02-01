package com.truewarg.claude.shared.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of SecureStorage using EncryptedSharedPreferences
 * Provides hardware-backed encryption for API keys and sensitive data
 */
class AndroidSecureStorage(private val context: Context) : SecureStorage {

    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val sharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override suspend fun getString(key: String): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(key, null)
    }

    override suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().remove(key).apply()
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        sharedPreferences.edit().clear().apply()
    }

    override suspend fun contains(key: String): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.contains(key)
    }

    companion object {
        private const val PREFS_NAME = "claude_secure_storage"
    }
}
