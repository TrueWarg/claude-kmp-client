package com.truewarg.claude.shared.data.repository

import com.russhwolf.settings.Settings
import com.truewarg.claude.shared.api.ClaudeApiClient
import com.truewarg.claude.shared.data.models.ClaudeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository for managing Claude AI models
 * Handles fetching and caching of available models
 */
class ModelsRepository(
    private val apiClient: ClaudeApiClient,
    private val settings: Settings,
    private val json: Json
) {
    private val _models = MutableStateFlow<List<ClaudeModel>>(loadCachedModels())
    val models: StateFlow<List<ClaudeModel>> = _models.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Fetch available models from API
     * @param forceRefresh Force refresh even if cache is valid
     */
    suspend fun fetchModels(forceRefresh: Boolean = false) {
        // Check cache validity
        if (!forceRefresh && isCacheValid()) {
            return
        }

        _isLoading.value = true
        _error.value = null

        val result = apiClient.getModels()
        result.fold(
            onSuccess = { response ->
                _models.value = response.data
                cacheModels(response.data)
                _isLoading.value = false
            },
            onFailure = { exception ->
                _error.value = exception.message ?: "Unknown error"
                _isLoading.value = false
            }
        )
    }

    /**
     * Get model by ID
     */
    fun getModelById(id: String): ClaudeModel? {
        return _models.value.find { it.id == id }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clear cache
     */
    fun clearCache() {
        settings.remove(KEY_CACHED_MODELS)
        settings.remove(KEY_CACHE_TIMESTAMP)
        _models.value = emptyList()
    }

    private fun loadCachedModels(): List<ClaudeModel> {
        val cachedJson = settings.getStringOrNull(KEY_CACHED_MODELS) ?: return emptyList()
        return try {
            json.decodeFromString<List<ClaudeModel>>(cachedJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun cacheModels(models: List<ClaudeModel>) {
        val modelsJson = json.encodeToString(models)
        settings.putString(KEY_CACHED_MODELS, modelsJson)
        settings.putLong(KEY_CACHE_TIMESTAMP, Clock.System.now().toEpochMilliseconds())
    }

    private fun isCacheValid(): Boolean {
        val timestamp = settings.getLongOrNull(KEY_CACHE_TIMESTAMP) ?: return false
        val now = Clock.System.now().toEpochMilliseconds()
        val age = now - timestamp

        // Cache is valid for 24 hours
        return age < CACHE_VALIDITY_MS
    }

    companion object {
        private const val KEY_CACHED_MODELS = "cached_models"
        private const val KEY_CACHE_TIMESTAMP = "models_cache_timestamp"
        private const val CACHE_VALIDITY_MS = 24 * 60 * 60 * 1000L // 24 hours
    }
}
