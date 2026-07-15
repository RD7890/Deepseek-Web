package com.rohan.deepseek.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

// Glass constants — neutral, not blue
private val GlassBg     = Color(0xCC0A0D18)
private val GlassBorder = Color(0x28FFFFFF)
private val GlassSelected = Color(0x33FFFFFF)

@Composable
fun AppNavigation(vm: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentDest   = navBackStack?.destination

    Box(modifier = Modifier.fillMaxSize()) {

        // Full-screen NavHost — content sits behind the floating bar
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            modifier         = Modifier.fillMaxSize(),
            enterTransition  = { fadeIn(tween(180)) + slideInHorizontally(tween(180)) { it / 6 } },
            exitTransition   = { fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { -it / 6 } },
            popEnterTransition  = { fadeIn(tween(180)) + slideInHorizontally(tween(180)) { -it / 6 } },
            popExitTransition   = { fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { it / 6 } }
        ) {
            composable(Screen.Home.route)     { HomeScreen(vm) }
            composable(Screen.Settings.route) { SettingsScreen(vm) }
        }

        // Floating glass pill nav bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 36.dp, vertical = 14.dp)
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(50))
                .background(GlassBg)
                .border(0.8.dp, GlassBorder, RoundedCornerShape(50))
        ) {
            Row(
                modifier              = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                listOf(Screen.Home, Screen.Settings).forEach { screen ->
                    val selected = currentDest?.hierarchy?.any { it.route == screen.route } == true
                    GlassNavItem(
                        icon     = if (screen == Screen.Home) Icons.Filled.Chat else Icons.Filled.Settings,
                        label    = screen.label,
                        selected = selected,
                        onClick  = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassNavItem(
    icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (selected) 1.08f else 1f, tween(160), label = "scale")
    val alpha  = if (selected) 1f else 0.45f

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .then(
                if (selected) Modifier.background(GlassSelected)
                else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 18.dp, vertical = 6.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = Color.White.copy(alpha = alpha),
                modifier           = Modifier.size(18.dp)
            )
            Text(
                text       = label,
                color      = Color.White.copy(alpha = alpha),
                fontSize   = 9.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                lineHeight = 12.sp
            )
        }
    }
}
