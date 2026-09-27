package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val YawarLightColorScheme = lightColorScheme(
    primary = YawarBlue,
    onPrimary = Color.White,
    primaryContainer = PaleBlue,
    onPrimaryContainer = YawarNavy,
    secondary = ClinicalGreen,
    onSecondary = Color.White,
    secondaryContainer = PaleGreen,
    onSecondaryContainer = DeepGreen,
    tertiary = YawarNavy,
    onTertiary = Color.White,
    tertiaryContainer = PaleBlue,
    onTertiaryContainer = YawarBlue,
    background = SurfaceBackground,
    onBackground = Ink,
    surface = CardBackground,
    onSurface = Ink,
    surfaceVariant = PaleBlue,
    onSurfaceVariant = Slate,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = SurfaceBackground,
    surfaceContainerHigh = ChipBg,
    surfaceContainerHighest = PaleBlue,
    outline = BorderColor,
    outlineVariant = BorderColor,
    error = DangerText,
    onError = Color.White,
    errorContainer = DangerBg,
    onErrorContainer = EmergencyRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false, // Keep consistent brand identity
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = YawarLightColorScheme,
        typography = Typography,
        shapes = YawarShapes,
        content = content
    )
}

@Composable
fun YawarTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) = MyApplicationTheme(darkTheme = darkTheme, dynamicColor = false, content = content)
