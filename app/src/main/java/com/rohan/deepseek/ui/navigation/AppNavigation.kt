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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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

// ── Glass design tokens ──────────────────────────────────────────────────────
// Semi-transparent dark background — gradient makes it look like frosted glass
private val GlassBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xC81E2235), // top — slightly lighter, blue-tinted
        Color(0xD00A0D18)  // bottom — deeper dark
    )
)
// Subtle highlight at top edge, mimics light catching the glass rim
private val GlassHighlightBrush = Brush.verticalGradient(
    colors = listOf(Color(0x40FFFFFF), Color(0x00FFFFFF))
)
private val GlassBorder    = Color(0x33FFFFFF)
private val GlassSelected  = Color(0x28FFFFFF)
private val IconSelected   = Color(0xFFFFFFFF)
private val IconUnselected = Color(0x66FFFFFF)

@Composable
fun AppNavigation(vm: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentDest   = navBackStack?.destination

    // ── Root layout: Column so nav pill NEVER overlaps content ──────────────
    Column(modifier = Modifier.fillMaxSize()) {

        // ── Content area — pushes down below status bar / notch ─────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            NavHost(
                navController    = navController,
                startDestination = Screen.Home.route,
                modifier         = Modifier.fillMaxSize(),
                enterTransition  = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it / 8 } },
                exitTransition   = { fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 8 } },
                popEnterTransition  = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { -it / 8 } },
                popExitTransition   = { fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it / 8 } }
            ) {
                composable(Screen.Home.route)     { HomeScreen(vm) }
                composable(Screen.Settings.route) { SettingsScreen(vm) }
            }
        }

        // ── Glass pill nav bar — lives BELOW content in layout flow ──────────
        //    navigationBarsPadding() adds space for the system nav bar beneath the pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer shadow layer gives depth and the floating illusion
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation       = 24.dp,
                        shape           = RoundedCornerShape(50),
                        ambientColor    = Color(0x80000000),
                        spotColor       = Color(0x99000000)
                    )
                    .clip(RoundedCornerShape(50))
                    .background(GlassBrush)
                    .border(0.7.dp, GlassBorder, RoundedCornerShape(50))
                    .height(54.dp)
            ) {
                // Top-edge highlight stripe — light "reflection" on the glass rim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(GlassHighlightBrush)
                )

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
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
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
}

@Composable
private fun GlassNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = tween(160),
        label = "nav_scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
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
            .padding(horizontal = 24.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = if (selected) IconSelected else IconUnselected,
                modifier           = Modifier.size(19.dp)
            )
            Text(
                text       = label,
                color      = if (selected) IconSelected else IconUnselected,
                fontSize   = 9.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                lineHeight = 12.sp
            )
        }
    }
}
