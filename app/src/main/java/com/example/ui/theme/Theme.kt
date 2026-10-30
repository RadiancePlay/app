package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RadianceDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF041E19),
    primaryContainer = Color(0xFF0F3B35),
    onPrimaryContainer = NeonCyan,
    secondary = NeonMagenta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A0A26),
    onSecondaryContainer = Color(0xFFFFD8E6),
    tertiary = CyberBlue,
    onTertiary = Color(0xFF032830),
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevatedDark,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorderDark
)

@Composable
fun RadiancePlayTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RadianceDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    RadiancePlayTheme(content = content)
}
