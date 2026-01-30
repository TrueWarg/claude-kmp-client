package com.truewarg.claude.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.truewarg.claude.ui.navigation.NavigationHost
import com.truewarg.claude.ui.navigation.rememberNavigationState

@Composable
fun App() {
    val navigationState = rememberNavigationState()

    MaterialTheme {
        Surface(modifier = Modifier) {
            NavigationHost(navigationState = navigationState)
        }
    }
}
