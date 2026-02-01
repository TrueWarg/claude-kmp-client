package com.truewarg.claude.shared.di

import com.truewarg.claude.shared.localization.LocalizationManager
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Localization module
 */
val localizationModule = module {
    single { LocalizationManager(get()) }
}

/**
 * Main application module that aggregates all DI modules
 */
val appModule: List<Module> = listOf(
    coreModule,
    localizationModule,
    platformStorageModule,
    storageModule,
    apiModule,
    // Additional modules will be added here as we implement features
    // gitModule,
    // agentModule,
    // toolsModule,
)
