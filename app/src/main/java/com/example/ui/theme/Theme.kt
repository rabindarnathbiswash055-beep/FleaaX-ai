package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ZiaDarkColorScheme = darkColorScheme(
    primary = ZiaSapphireBlue,
    onPrimary = Color(0xFF040B1A),
    primaryContainer = ZiaSapphireContainer,
    onPrimaryContainer = ZiaTextPrimary,
    secondary = ZiaNeuralViolet,
    onSecondary = Color.White,
    secondaryContainer = ZiaVioletContainer,
    onSecondaryContainer = ZiaTextPrimary,
    tertiary = ZiaAcousticCyan,
    onTertiary = Color(0xFF031922),
    tertiaryContainer = ZiaCyanContainer,
    onTertiaryContainer = ZiaTextPrimary,
    background = ZiaObsidianBg,
    onBackground = ZiaTextPrimary,
    surface = ZiaMidnightSurface,
    onSurface = ZiaTextPrimary,
    surfaceVariant = ZiaCardSurface,
    onSurfaceVariant = ZiaTextSecondary,
    outline = ZiaElevatedBorder,
    error = ZiaCrimsonError,
    onError = Color.White
)

val ZiaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ZiaAITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ZiaDarkColorScheme,
        typography = Typography,
        shapes = ZiaShapes,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    ZiaAITheme(content = content)
}
