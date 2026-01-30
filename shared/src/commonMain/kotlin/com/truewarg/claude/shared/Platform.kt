package com.truewarg.claude.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
