package com.truewarg.claude.shared.di

import org.koin.core.module.Module

/**
 * Main application module that aggregates all DI modules
 */
val appModule: List<Module> = listOf(
    coreModule,
    // Additional modules will be added here as we implement features
    // apiModule,
    // repositoryModule,
    // storageModule,
    // gitModule,
    // agentModule,
    // toolsModule,
)
