package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.AppThemeMode

private val NexaDarkColorScheme = darkColorScheme(
    primary = NexaCyanPrimary,
    onPrimary = Color(0xFF04131D),
    primaryContainer = Color(0xFF0E344D),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = NexaCyanSecondary,
    onSecondary = Color(0xFF042F2E),
    tertiary = NexaVioletTertiary,
    background = NexaDarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = NexaDarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = NexaDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155)
)

private val NexaLightColorScheme = lightColorScheme(
    primary = NexaLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0C4A6E),
    secondary = NexaLightSecondary,
    onSecondary = Color.White,
    tertiary = NexaLightTertiary,
    background = NexaLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = NexaLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = NexaLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun NexaIslandTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    languageCode: String = "ar",
    accentColorOverride: Long? = null,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val baseScheme = if (isDark) NexaDarkColorScheme else NexaLightColorScheme
    val colorScheme = if (accentColorOverride != null) {
        val customAccent = Color(accentColorOverride)
        baseScheme.copy(primary = customAccent)
    } else {
        baseScheme
    }

    val typography = buildNexaTypography(isArabic = languageCode != "en")

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    NexaIslandTheme(
        themeMode = if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT,
        content = content
    )
}
