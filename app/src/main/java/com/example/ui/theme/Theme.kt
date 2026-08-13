package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SecurityColorScheme = darkColorScheme(
    primary = ShieldCyanPrimary,
    onPrimary = Color.Black,
    secondary = ShieldCyanSecondary,
    onSecondary = Color.Black,
    tertiary = ShieldSafeGreen,
    onTertiary = Color.Black,
    background = ShieldDarkBg,
    onBackground = ShieldTextPrimary,
    surface = ShieldSurface,
    onSurface = ShieldTextPrimary,
    surfaceVariant = ShieldSurfaceVariant,
    onSurfaceVariant = ShieldTextSecondary,
    error = ShieldCriticalRed,
    onError = Color.White
)

@Composable
fun ShieldAITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SecurityColorScheme,
        typography = Typography,
        content = content
    )
}

