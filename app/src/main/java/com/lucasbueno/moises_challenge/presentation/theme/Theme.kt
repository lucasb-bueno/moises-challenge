package com.lucasbueno.moises_challenge.presentation.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MusicAccent,
    onPrimary = MusicBlack,
    secondary = MusicTextSecondary,
    tertiary = MusicSurfaceElevated,
    background = MusicBlack,
    onBackground = MusicTextPrimary,
    surface = MusicBlack,
    onSurface = MusicTextPrimary,
    surfaceVariant = MusicSurface,
    onSurfaceVariant = MusicTextSecondary,
    outline = MusicStroke,
)

private val LightColorScheme = lightColorScheme(
    primary = MusicBlack,
    onPrimary = MusicAccent,
    secondary = MusicTextSecondary,
    tertiary = MusicSurfaceElevated,
    background = MusicBlack,
    onBackground = MusicTextPrimary,
    surface = MusicBlack,
    onSurface = MusicTextPrimary,
    surfaceVariant = MusicSurface,
    onSurfaceVariant = MusicTextSecondary,
    outline = MusicStroke,
)

/* Other default colors to override
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFF7F7F7),
    onSurface = Color(0xFFF7F7F7),
*/

@Composable
fun MoiseschallengeTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
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
