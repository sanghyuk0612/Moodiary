package com.sanghyuk.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = MoodPrimary,
    secondary = MoodSecondary,
    tertiary = MoodAccent,
    surface = MoodSurface,
    background = MoodBackground,
    onBackground = MoodText,
    onSurface = MoodText,
)

private val DarkColorScheme = darkColorScheme(
    primary = MoodPrimary,
    secondary = MoodSecondary,
    tertiary = MoodAccent,
)

@Composable
fun MoodiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
