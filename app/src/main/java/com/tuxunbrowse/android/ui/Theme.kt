package com.tuxunbrowse.android.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Material 3 theme for TuxunBrowse: a quiet blue-and-paper palette.
 * A browser should stay out of the way of the page it shows.
 */
val BrowseBlue = Color(0xFF0B57D0)
val BrowseBlueDeep = Color(0xFF0842A0)
val PaperBg = Color(0xFFF7F8FA)
val InkText = Color(0xFF1A1C1E)

private val LightScheme = lightColorScheme(
    primary = BrowseBlue,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3E3FD),
    onPrimaryContainer = BrowseBlueDeep,
    secondary = Color(0xFF5A6B84),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE1E8F2),
    onSecondaryContainer = Color(0xFF2E3B4E),
    tertiary = Color(0xFF3F7A5E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCDEBDA),
    onTertiaryContainer = Color(0xFF1E4D38),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF7A1A12),
    background = PaperBg,
    onBackground = InkText,
    surface = PaperBg,
    onSurface = InkText,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0F3F7),
    surfaceContainer = Color(0xFFE7ECF2),
    surfaceContainerHigh = Color(0xFFDDE4EC),
    surfaceContainerHighest = Color(0xFFD2DBE6),
    surfaceVariant = Color(0xFFE1E6ED),
    onSurfaceVariant = Color(0xFF4A5568),
    surfaceTint = BrowseBlue,
    inverseSurface = Color(0xFF23272C),
    inverseOnSurface = Color(0xFFF0F3F7),
    inversePrimary = Color(0xFFA8C7FA),
    outline = Color(0xFFB6C0CE),
    outlineVariant = Color(0xFFD9E0E9),
    scrim = Color(0xFF000000)
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFA8C7FA),
    onPrimary = Color(0xFF0A305F),
    primaryContainer = Color(0xFF0842A0),
    onPrimaryContainer = Color(0xFFD3E3FD),
    secondary = Color(0xFFB9C7DC),
    onSecondary = Color(0xFF26303F),
    secondaryContainer = Color(0xFF3C4757),
    onSecondaryContainer = Color(0xFFD5E0F0),
    tertiary = Color(0xFF8FD0AB),
    onTertiary = Color(0xFF0E3A26),
    tertiaryContainer = Color(0xFF245C40),
    onTertiaryContainer = Color(0xFFCDEBDA),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF111417),
    onBackground = Color(0xFFE3E7EC),
    surface = Color(0xFF111417),
    onSurface = Color(0xFFE3E7EC),
    surfaceContainerLowest = Color(0xFF0B0D0F),
    surfaceContainerLow = Color(0xFF191C1F),
    surfaceContainer = Color(0xFF1E2226),
    surfaceContainerHigh = Color(0xFF282C31),
    surfaceContainerHighest = Color(0xFF33373D),
    surfaceVariant = Color(0xFF3A4046),
    onSurfaceVariant = Color(0xFFB9C1CB),
    surfaceTint = Color(0xFFA8C7FA),
    inverseSurface = Color(0xFFE3E7EC),
    inverseOnSurface = Color(0xFF23272C),
    inversePrimary = BrowseBlue,
    outline = Color(0xFF6B7684),
    outlineVariant = Color(0xFF3A4046),
    scrim = Color(0xFF000000)
)

@Composable
fun TuxunBrowseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content
    )
}
