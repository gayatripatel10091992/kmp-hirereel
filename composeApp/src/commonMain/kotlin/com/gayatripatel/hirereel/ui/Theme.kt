package com.gayatripatel.hirereel.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2DFFF),
    onPrimaryContainer = Color(0xFF14006B),
    secondary = Color(0xFFE5484D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD7),
    onSecondaryContainer = Color(0xFF410004),
    tertiary = Color(0xFF00897B),
    tertiaryContainer = Color(0xFFB2DFDB),
    onTertiaryContainer = Color(0xFF00201C),
    background = Color(0xFFFBF8FF),
    surface = Color(0xFFFBF8FF),
    surfaceContainerLow = Color(0xFFF5F2FD),
    surfaceContainer = Color(0xFFEFECF7),
    surfaceContainerHigh = Color(0xFFE9E6F2),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFC3C0FF),
    onPrimary = Color(0xFF2600A3),
    primaryContainer = Color(0xFF3B2FC9),
    onPrimaryContainer = Color(0xFFE2DFFF),
    secondary = Color(0xFFFFB3AE),
    onSecondary = Color(0xFF68000D),
    secondaryContainer = Color(0xFF930019),
    onSecondaryContainer = Color(0xFFFFDAD7),
    tertiary = Color(0xFF80CBC4),
    tertiaryContainer = Color(0xFF00504A),
    onTertiaryContainer = Color(0xFFB2DFDB),
    background = Color(0xFF131218),
    surface = Color(0xFF131218),
    surfaceContainerLow = Color(0xFF1B1A21),
    surfaceContainer = Color(0xFF201F26),
    surfaceContainerHigh = Color(0xFF2A2930),
)

val RecordRed = Color(0xFFE5484D)

@Composable
fun HireReelTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) Dark else Light, content = content)
}
