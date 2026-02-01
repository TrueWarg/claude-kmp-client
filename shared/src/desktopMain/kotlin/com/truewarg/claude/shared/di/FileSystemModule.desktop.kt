package com.truewarg.claude.shared.di

import com.truewarg.claude.shared.filesystem.DesktopFileSystemManager
import com.truewarg.claude.shared.filesystem.FileSystemManager
import org.koin.dsl.module

/**
 * Desktop file system module
 */
actual val fileSystemModule = module {
    single<FileSystemManager> { DesktopFileSystemManager() }
}
