package com.truewarg.claude.ui.navigation

import androidx.compose.runtime.*
import com.truewarg.claude.ui.screens.*

/**
 * Simple navigation state management
 */
class NavigationState {
    var currentScreen by mutableStateOf<Screen>(Screen.Home)
        private set

    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }

    fun navigateBack() {
        currentScreen = Screen.Home
    }
}

@Composable
fun rememberNavigationState(): NavigationState {
    return remember { NavigationState() }
}

/**
 * Main navigation host
 */
@Composable
fun NavigationHost(navigationState: NavigationState) {
    when (navigationState.currentScreen) {
        Screen.Home -> HomeScreen(
            onNavigateToChat = { navigationState.navigateTo(Screen.Chat) },
            onNavigateToAgents = { navigationState.navigateTo(Screen.Agents) },
            onNavigateToSkills = { navigationState.navigateTo(Screen.Skills) },
            onNavigateToSettings = { navigationState.navigateTo(Screen.Settings) }
        )
        Screen.Chat -> ChatScreen(
            onNavigateBack = { navigationState.navigateBack() }
        )
        Screen.Agents -> AgentsScreen(
            onNavigateBack = { navigationState.navigateBack() }
        )
        Screen.Skills -> SkillsScreen(
            onNavigateBack = { navigationState.navigateBack() }
        )
        Screen.Settings -> SettingsScreen(
            onNavigateBack = { navigationState.navigateBack() }
        )
        Screen.Editor -> EditorScreen(
            onNavigateBack = { navigationState.navigateBack() }
        )
    }
}
