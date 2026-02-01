package com.truewarg.claude.shared.di

import com.truewarg.claude.shared.api.ClaudeApiClient
import com.truewarg.claude.shared.data.repository.ChatRepository
import com.truewarg.claude.shared.data.repository.ConversationRepository
import com.truewarg.claude.shared.tools.*
import org.koin.dsl.module

/**
 * API module for Claude API client and repositories
 */
val apiModule = module {
    single { ClaudeApiClient(get(), get(), get()) }
    single { ConversationRepository(get(), get()) }

    // Tools
    single { CommandExecutor() }
    single { FileSystemTools(get()) }
    single { BashTools(get()) }
    single {
        ToolManager(
            executors = listOf(
                get<FileSystemTools>(),
                get<BashTools>()
            )
        )
    }

    single { ChatRepository(get(), get(), get(), get()) }
}
