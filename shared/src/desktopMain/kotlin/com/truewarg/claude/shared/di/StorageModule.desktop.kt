package com.truewarg.claude.shared.di

import com.truewarg.claude.shared.storage.DesktopSecureStorage
import com.truewarg.claude.shared.storage.SecureStorage
import org.koin.dsl.module

/**
 * Desktop platform storage module
 */
actual val platformStorageModule = module {
    single<SecureStorage> { DesktopSecureStorage() }
}
