package com.truewarg.claude.shared.di

import com.truewarg.claude.shared.api.ClaudeApiClient
import org.koin.dsl.module

/**
 * API module for Claude API client
 */
val apiModule = module {
    single { ClaudeApiClient(get(), get(), get()) }
}
