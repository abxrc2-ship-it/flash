package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TorchAmber,
    onPrimary = Color(0xFF241500),
    primaryContainer = Color(0xFF3F2B00),
    onPrimaryContainer = TorchAmberBright,
    secondary = TorchAmberBright,
    onSecondary = Color(0xFF201600),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

private val OledColorScheme = darkColorScheme(
    primary = TorchAmber,
    onPrimary = Color(0xFF241500),
    primaryContainer = Color(0xFF2B1D00),
    onPrimaryContainer = TorchAmberBright,
    secondary = TorchAmberBright,
    onSecondary = Color(0xFF201600),
    background = OledBackground,
    onBackground = DarkTextPrimary,
    surface = OledSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = OledSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = OledBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB45309),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun FlashLightTheme(
    isOledMode: Boolean = false,
    forceDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isOledMode -> OledColorScheme
        forceDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
