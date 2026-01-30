package com.truewarg.claude.shared.di

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

/**
 * Desktop-specific Settings implementation using Java Preferences
 */
actual fun createSettings(): Settings {
    val preferences = Preferences.userRoot().node("com.truewarg.claude")
    return PreferencesSettings(preferences)
}
