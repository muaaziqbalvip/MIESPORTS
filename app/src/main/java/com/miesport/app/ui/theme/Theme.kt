package com.miesport.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MiEsportDarkColors = darkColorScheme(
    primary = NeonGreen,
    onPrimary = BackgroundBlack,
    secondary = GoldPrimary,
    onSecondary = BackgroundBlack,
    tertiary = AccentRed,
    background = BackgroundBlack,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error = DangerRed,
    onError = TextPrimary,
    outline = SurfaceGlassBorder
)

@Composable
fun MiEsportTheme(
    // App is always dark themed per spec, but keep the hook for future light mode
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? android.app.Activity)?.window
        window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
            it.statusBarColor = BackgroundBlack.toArgb()
            it.navigationBarColor = BackgroundBlack.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = MiEsportDarkColors,
        typography = AppTypography,
        content = content
    )
}
