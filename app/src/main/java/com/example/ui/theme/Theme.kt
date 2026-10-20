package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrismLightBlue,
    onPrimary = Color(0xFF00296B),
    primaryContainer = Color(0xFF003F9E),
    onPrimaryContainer = Color(0xFFD8E2FF),
    secondary = Color(0xFFBCC6E5),
    background = PrismBackgroundDark,
    surface = PrismSurfaceDark,
    surfaceVariant = PrismSurfaceVariantDark,
    onBackground = PrismOnSurfaceDark,
    onSurface = PrismOnSurfaceDark,
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val LightColorScheme = lightColorScheme(
    primary = PrismBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E2FF),
    onPrimaryContainer = Color(0xFF001945),
    secondary = Color(0xFF555F71),
    background = PrismBackgroundLight,
    surface = PrismSurfaceLight,
    surfaceVariant = PrismSurfaceVariantLight,
    onBackground = PrismOnSurfaceLight,
    onSurface = PrismOnSurfaceLight,
    onSurfaceVariant = Color(0xFF64748B)
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "SYSTEM",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemDark
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
