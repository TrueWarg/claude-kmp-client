package com.truewarg.claude.shared.di

import org.koin.core.module.Module

/**
 * Platform-specific file system module
 * Must be provided by each platform (Android, Desktop)
 */
expect val fileSystemModule: Module
