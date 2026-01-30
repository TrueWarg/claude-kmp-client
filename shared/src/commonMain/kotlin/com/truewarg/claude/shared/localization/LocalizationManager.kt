package com.truewarg.claude.shared.localization

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages application localization and language switching
 */
class LocalizationManager(private val settings: Settings) {
    private val _currentLanguage = MutableStateFlow(loadLanguage())
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    private val _strings = MutableStateFlow(StringResources.getStrings(_currentLanguage.value))
    val strings: StateFlow<Strings> = _strings.asStateFlow()

    /**
     * Load saved language from settings
     */
    private fun loadLanguage(): Language {
        val languageCode = settings.get(KEY_LANGUAGE, Language.ENGLISH.code)
        return Language.fromCode(languageCode)
    }

    /**
     * Set the current language and save to settings
     */
    fun setLanguage(language: Language) {
        _currentLanguage.value = language
        _strings.value = StringResources.getStrings(language)
        settings[KEY_LANGUAGE] = language.code
    }

    /**
     * Get current strings synchronously
     */
    fun getCurrentStrings(): Strings = _strings.value

    companion object {
        private const val KEY_LANGUAGE = "app_language"
    }
}
