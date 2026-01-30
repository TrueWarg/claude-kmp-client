package com.truewarg.claude.ui.navigation

/**
 * Represents different screens in the application
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Chat : Screen("chat")
    data object Agents : Screen("agents")
    data object Skills : Screen("skills")
    data object Settings : Screen("settings")
    data object Editor : Screen("editor")
}
