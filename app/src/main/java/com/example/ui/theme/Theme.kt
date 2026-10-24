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

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = MintContainer,
    onPrimaryContainer = ForestGreenDark,
    secondary = MintSecondary,
    onSecondary = Color.White,
    secondaryContainer = MintLight,
    onSecondaryContainer = ForestGreenDark,
    tertiary = WarningOrange,
    onTertiary = Color.White,
    tertiaryContainer = WarningOrangeLight,
    onTertiaryContainer = WarningOrangeDark,
    error = DangerRed,
    onError = Color.White,
    errorContainer = DangerRedLight,
    onErrorContainer = DangerRed,
    background = NeutralBackground,
    onBackground = NeutralTextPrimary,
    surface = NeutralSurface,
    onSurface = NeutralTextPrimary,
    surfaceVariant = NeutralSurfaceVariant,
    onSurfaceVariant = NeutralTextSecondary,
    outline = NeutralBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenPrimary,
    onPrimary = ForestGreenDark,
    primaryContainer = ForestGreenPrimary,
    onPrimaryContainer = MintContainer,
    secondary = MintSecondary,
    onSecondary = Color.White,
    secondaryContainer = ForestGreenDark,
    onSecondaryContainer = MintContainer,
    tertiary = WarningOrange,
    onTertiary = Color.Black,
    tertiaryContainer = WarningOrangeDark,
    onTertiaryContainer = WarningOrangeLight,
    error = DangerRed,
    onError = Color.White,
    background = DarkGreenBackground,
    onBackground = Color(0xFFE2E8E4),
    surface = DarkGreenSurface,
    onSurface = Color(0xFFE2E8E4),
    surfaceVariant = Color(0xFF1E2823),
    onSurfaceVariant = Color(0xFFB0BEB6),
    outline = Color(0xFF33443C)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our brand colors
    content: @Composable () -> Unit,
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
