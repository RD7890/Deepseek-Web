package com.rohan.deepseek.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary             = DeepSeekBlue,
    onPrimary           = Color.White,
    primaryContainer    = DeepSeekBlueDark,
    onPrimaryContainer  = DeepSeekBlueLight,
    secondary           = DeepSeekBlueLight,
    onSecondary         = DarkBackground,
    tertiary            = Color(0xFF70C8FF),
    background          = DarkBackground,
    surface             = DarkSurface,
    surfaceVariant      = DarkSurfaceVariant,
    onBackground        = OnDarkSurface,
    onSurface           = OnDarkSurface,
    onSurfaceVariant    = OnDarkSurfaceVariant,
    error               = Color(0xFFFF6B6B),
    errorContainer      = Color(0xFF3A1818),
    onErrorContainer    = Color(0xFFFF9E9E)
)

private val LightColorScheme = lightColorScheme(
    primary             = DeepSeekBlue,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFFDDE3FF),
    onPrimaryContainer  = DeepSeekBlueDark,
    secondary           = DeepSeekBlueDark,
    onSecondary         = Color.White,
    tertiary            = Color(0xFF0099CC),
    background          = LightBackground,
    surface             = LightSurface,
    surfaceVariant      = LightSurfaceVariant,
    onBackground        = OnLightSurface,
    onSurface           = OnLightSurface,
    onSurfaceVariant    = OnLightSurfaceVariant
)

@Composable
fun DeepSeekTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = DeepSeekTypography,
        content     = content
    )
}
