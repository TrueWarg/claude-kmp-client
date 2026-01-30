package com.truewarg.claude.shared.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android-specific Settings implementation using SharedPreferences
 */
actual fun createSettings(): Settings {
    val context = AndroidContextProvider.context
    val sharedPreferences = context.getSharedPreferences("claude_prefs", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(sharedPreferences)
}

/**
 * Provider for Android Context
 */
object AndroidContextProvider {
    lateinit var context: Context
        private set

    fun init(context: Context) {
        this.context = context.applicationContext
    }
}
