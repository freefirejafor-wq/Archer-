package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ArcherDarkColorScheme = darkColorScheme(
    primary = NeonMatrixGreen,
    onPrimary = ObsidianDeep,
    primaryContainer = ObsidianElevated,
    onPrimaryContainer = NeonMatrixGreen,
    secondary = NeonQuantumCyan,
    onSecondary = ObsidianDeep,
    secondaryContainer = ObsidianCard,
    onSecondaryContainer = NeonQuantumCyan,
    tertiary = NeonSolarAmber,
    onTertiary = ObsidianDeep,
    tertiaryContainer = ObsidianCard,
    onTertiaryContainer = NeonSolarAmber,
    error = NeonCrimson,
    onError = TextPrimaryHud,
    background = ObsidianDeep,
    onBackground = TextPrimaryHud,
    surface = ObsidianSurface,
    onSurface = TextPrimaryHud,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondaryHud,
    outline = BorderHudGreen
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArcherDarkColorScheme,
        typography = Typography,
        content = content
    )
}
