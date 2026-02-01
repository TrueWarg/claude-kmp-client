package com.truewarg.claude.shared.di

import com.truewarg.claude.shared.storage.AndroidSecureStorage
import com.truewarg.claude.shared.storage.SecureStorage
import org.koin.dsl.module

/**
 * Android platform storage module
 */
actual val platformStorageModule = module {
    single<SecureStorage> { AndroidSecureStorage(get()) }
}
