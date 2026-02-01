package com.truewarg.claude.shared.di

import com.truewarg.claude.shared.data.repository.SettingsRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Storage and repository module
 */
val storageModule = module {
    // SecureStorage is provided by platform-specific modules
    single { SettingsRepository(get(), get(), get()) }
}

/**
 * Platform-specific storage module
 * Must be provided by each platform (Android, Desktop)
 */
expect val platformStorageModule: Module
