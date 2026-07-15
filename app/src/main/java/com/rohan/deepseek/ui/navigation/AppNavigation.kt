package com.rohan.deepseek.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rohan.deepseek.ui.screens.HomeScreen
import com.rohan.deepseek.ui.screens.SettingsScreen
import com.rohan.deepseek.viewmodel.AppViewModel

sealed class Screen(val route: String, val label: String) {
    object Home     : Screen("home",     "Chat")
    object Settings : Screen("settings", "Settings")
}

@Composable
fun AppNavigation(vm: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentDest   = navBackStack?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(Screen.Home, Screen.Settings).forEach { screen ->
                    val selected = currentDest?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick  = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        icon  = {
                            Icon(
                                imageVector = when (screen) {
                                    Screen.Home     -> Icons.Filled.Chat
                                    Screen.Settings -> Icons.Filled.Settings
                                },
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            modifier         = Modifier.padding(innerPadding),
            enterTransition  = {
                fadeIn(tween(180)) + slideInHorizontally(tween(180)) { it / 6 }
            },
            exitTransition   = {
                fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { -it / 6 }
            },
            popEnterTransition  = {
                fadeIn(tween(180)) + slideInHorizontally(tween(180)) { -it / 6 }
            },
            popExitTransition   = {
                fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { it / 6 }
            }
        ) {
            composable(Screen.Home.route)     { HomeScreen(vm) }
            composable(Screen.Settings.route) { SettingsScreen(vm) }
        }
    }
}
