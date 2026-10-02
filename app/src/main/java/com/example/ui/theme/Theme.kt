package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FintechGreen,
    onPrimary = Color.Black,
    primaryContainer = FintechGreenBg,
    onPrimaryContainer = FintechGreenLight,
    secondary = FintechGold,
    onSecondary = Color.Black,
    secondaryContainer = FintechGoldBg,
    onSecondaryContainer = FintechGoldLight,
    tertiary = FintechCyan,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCardLight,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceCardBorder,
    error = FintechRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
