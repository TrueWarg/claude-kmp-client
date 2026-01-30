package com.truewarg.claude.android

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.truewarg.claude.shared.di.AndroidContextProvider
import com.truewarg.claude.shared.di.appModule
import com.truewarg.claude.ui.App
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ClaudeApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Android context provider
        AndroidContextProvider.init(this)

        // Initialize Koin
        startKoin {
            androidContext(this@ClaudeApplication)
            modules(appModule)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
