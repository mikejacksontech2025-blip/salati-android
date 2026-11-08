package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = GoldBright,
    onPrimary = NavyDarkBackground,
    primaryContainer = GoldContainerDark,
    onPrimaryContainer = GoldLight,
    secondary = GoldLight,
    onSecondary = NavyDarkBackground,
    tertiary = EmeraldAccent,
    background = NavyDarkBackground,
    onBackground = TextWhite,
    surface = NavyDarkSurface,
    onSurface = TextWhite,
    surfaceVariant = NavyDarkSurfaceVariant,
    onSurfaceVariant = TextLightGray,
    outline = NavyDarkBorder,
    error = CrimsonAlert
)

private val LightColorScheme = lightColorScheme(
    primary = GoldDark,
    onPrimary = TextWhite,
    primaryContainer = GoldContainerLight,
    onPrimaryContainer = GoldDark,
    secondary = GoldPrimary,
    onSecondary = TextWhite,
    tertiary = EmeraldAccent,
    background = LightBackground,
    onBackground = TextDark,
    surface = LightSurface,
    onSurface = TextDark,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextDark,
    outline = LightBorder,
    error = CrimsonAlert
)

@Composable
fun SalatiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Enforce Right-to-Left (RTL) layout direction across the entire app for authentic Arabic typography
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Retain alias for compatibility with template
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SalatiTheme(darkTheme = darkTheme, content = content)
}
