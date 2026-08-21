package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Pure White and Vibrant Sky Blue Color Scheme (ثيم أبيض وسمائي فاتح فقط بدون أي ألوان داكنة)
private val BrightSkyWhiteColorScheme = lightColorScheme(
    primary = SkyBluePrimary,
    onPrimary = Color.White,
    primaryContainer = SkyBlueContainer,
    onPrimaryContainer = SkyBlueOnContainer,
    secondary = SkySecondary,
    onSecondary = Color.White,
    secondaryContainer = SkySecondaryContainer,
    onSecondaryContainer = SkyOnSecondaryContainer,
    tertiary = CyanAccent,
    onTertiary = Color.White,
    tertiaryContainer = CyanAccentContainer,
    onTertiaryContainer = SkyBlueOnContainer,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderColorSky,
    outlineVariant = BorderColorLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Forced to false - no dark colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BrightSkyWhiteColorScheme,
        typography = Typography,
        content = content
    )
}

