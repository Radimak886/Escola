package com.radimak.tv.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val RadimakOrange = Color(0xFFF97316)
val RadimakGold = Color(0xFFFFB23D)
val RadimakBackground = Color(0xFF09090B)
val RadimakSurface = Color(0xFF151518)
val RadimakSurfaceHigh = Color(0xFF222226)
val RadimakTextMuted = Color(0xFFB4B4BA)

private val RadimakColors = darkColorScheme(
    primary = RadimakOrange,
    onPrimary = Color.White,
    secondary = RadimakGold,
    onSecondary = Color(0xFF1B1205),
    background = RadimakBackground,
    onBackground = Color(0xFFF7F7F8),
    surface = RadimakSurface,
    onSurface = Color(0xFFF7F7F8),
    surfaceVariant = RadimakSurfaceHigh,
    onSurfaceVariant = RadimakTextMuted,
    error = Color(0xFFFF6B6B),
)

@Composable
fun RadimakTvTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = RadimakBackground.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    MaterialTheme(
        colorScheme = RadimakColors,
        typography = androidx.compose.material3.Typography(),
        content = content,
    )
}
