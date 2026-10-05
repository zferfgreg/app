package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val isDark: Boolean,
    val bg: Color,
    val bgElevated: Color,
    val surface: Color,
    val surfaceLight: Color,
    val surfaceBorder: Color,
    val surfaceHighlight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val green: Color = FintechGreen,
    val greenLight: Color = FintechGreenLight,
    val greenBg: Color = FintechGreenBg,
    val red: Color = FintechRed,
    val redLight: Color = FintechRedLight,
    val redBg: Color = FintechRedBg,
    val gold: Color = FintechGold,
    val goldLight: Color = FintechGoldLight,
    val goldBg: Color = FintechGoldBg,
    val cyan: Color = FintechCyan,
    val cyanLight: Color = FintechCyanLight,
    val cyanBg: Color = FintechCyanBg,
    val purple: Color = FintechPurple
)

val DarkAppColors = AppColors(
    isDark = true,
    bg = DarkBg,
    bgElevated = DarkBgElevated,
    surface = SurfaceCard,
    surfaceLight = SurfaceCardLight,
    surfaceBorder = SurfaceCardBorder,
    surfaceHighlight = SurfaceCardHighlight,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textMuted = TextMuted
)

val LightAppColors = AppColors(
    isDark = false,
    bg = LightBg,
    bgElevated = LightBgElevated,
    surface = LightSurfaceCard,
    surfaceLight = LightSurfaceCardLight,
    surfaceBorder = LightSurfaceCardBorder,
    surfaceHighlight = LightSurfaceCardHighlight,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

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

private val LightColorScheme = lightColorScheme(
    primary = FintechGreen,
    onPrimary = Color.White,
    primaryContainer = FintechGreenBg,
    onPrimaryContainer = FintechGreen,
    secondary = FintechGold,
    onSecondary = Color.Black,
    secondaryContainer = FintechGoldBg,
    onSecondaryContainer = FintechGold,
    tertiary = FintechCyan,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurfaceCard,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceCardLight,
    onSurfaceVariant = LightTextSecondary,
    outline = LightSurfaceCardBorder,
    error = FintechRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
