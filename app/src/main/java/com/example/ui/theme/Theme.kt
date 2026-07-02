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
    primary = GeoPrimaryDark,
    secondary = GeoSecondaryDark,
    tertiary = GeoTertiaryDark,
    background = GeoBackgroundDark,
    surface = GeoSurfaceDark,
    surfaceVariant = GeoSurfaceVariantDark,
    onPrimary = GeoBackgroundDark,
    onSecondary = GeoBackgroundLight,
    onTertiary = GeoBackgroundDark,
    onBackground = GeoBackgroundLight,
    onSurface = GeoBackgroundLight,
    error = GeoAccentRed
)

private val LightColorScheme = lightColorScheme(
    primary = GeoPrimary,
    secondary = GeoSecondary,
    tertiary = GeoTertiary,
    background = GeoBackgroundLight,
    surface = GeoSurfaceLight,
    surfaceVariant = GeoSurfaceVariantLight,
    onPrimary = GeoSurfaceLight,
    onSecondary = GeoSurfaceLight,
    onTertiary = GeoPrimary,
    onBackground = GeoPrimary,
    onSurface = Color(0xFF1A1C18),
    error = GeoAccentRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Enforce our custom nature-inspired theme by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
