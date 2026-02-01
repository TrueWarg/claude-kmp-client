package com.truewarg.claude.shared.di

import com.truewarg.claude.shared.filesystem.AndroidFileSystemManager
import com.truewarg.claude.shared.filesystem.FileSystemManager
import org.koin.dsl.module

/**
 * Android file system module
 */
actual val fileSystemModule = module {
    single<FileSystemManager> { AndroidFileSystemManager(get()) }
}
