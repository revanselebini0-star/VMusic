package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VanzMusicColorScheme = darkColorScheme(
    primary = AppleRed,
    onPrimary = AppleTextPrimary,
    primaryContainer = AppleDarkCard,
    onPrimaryContainer = AppleTextPrimary,
    secondary = ApplePink,
    onSecondary = AppleTextPrimary,
    background = AppleBlack,
    onBackground = AppleTextPrimary,
    surface = AppleBlack,
    onSurface = AppleTextPrimary,
    surfaceVariant = AppleDarkElevated,
    onSurfaceVariant = AppleTextSecondary,
    outline = AppleBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Apple Music dark aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VanzMusicColorScheme,
        typography = Typography,
        content = content
    )
}
