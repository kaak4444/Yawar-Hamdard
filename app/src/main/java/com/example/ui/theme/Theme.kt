package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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
    onTertiaryContainer = YawarNavyDark,
    background = SurfaceBackground,
    onBackground = Ink,
    surface = CardBackground,
    onSurface = Ink,
    surfaceVariant = PaleBlue,
    onSurfaceVariant = Slate,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFCFDFF),
    surfaceContainer = SurfaceBackground,
    surfaceContainerHigh = ChipBg,
    surfaceContainerHighest = Color(0xFFE9EFF7),
    outline = BorderColor,
    outlineVariant = Color(0xFFEEF2F7),
    error = DangerText,
    onError = Color.White,
    errorContainer = DangerBg,
    onErrorContainer = EmergencyRed
)

private val YawarDarkColorScheme = darkColorScheme(
    primary = Color(0xFF90B5FF),
    onPrimary = YawarNavyDark,
    primaryContainer = YawarNavy,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF5CE3A6),
    onSecondary = YawarNavyDark,
    secondaryContainer = DeepGreen,
    onSecondaryContainer = Color(0xFFC3F3DE),
    tertiary = Color(0xFF82B8FF),
    onTertiary = Color(0xFF002E68),
    tertiaryContainer = YawarNavyDark,
    onTertiaryContainer = Color(0xFFD7E5FF),
    background = Color(0xFF0C1322),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF131D33),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1A2644),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainerLowest = Color(0xFF0A1120),
    surfaceContainerLow = Color(0xFF101A2C),
    surfaceContainer = Color(0xFF131D33),
    surfaceContainerHigh = Color(0xFF1A2640),
    surfaceContainerHighest = Color(0xFF223050),
    outline = Color(0xFF2E3E66),
    outlineVariant = Color(0xFF22304E),
    error = Color(0xFFFF847C),
    onError = Color(0xFF5E0004),
    errorContainer = Color(0xFF8C0009),
    onErrorContainer = Color(0xFFFFD4D1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent brand identity
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) YawarDarkColorScheme else YawarLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = YawarShapes,
        content = content
    )
}

@Composable
fun YawarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = MyApplicationTheme(darkTheme = darkTheme, dynamicColor = false, content = content)
