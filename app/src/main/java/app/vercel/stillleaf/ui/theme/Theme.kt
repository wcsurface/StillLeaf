package app.vercel.stillleaf.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightZenColorScheme = lightColorScheme(
    primary = PrimarySage,
    onPrimary = PureWhiteSurface,
    primaryContainer = SurfaceVariantCream,
    onPrimaryContainer = DarkForestSage,
    secondary = MutedOlive,
    onSecondary = PureWhiteSurface,
    secondaryContainer = SurfaceVariantCream,
    onSecondaryContainer = OnSurfaceTextDark,
    tertiary = EarthWarmAccent,
    onTertiary = DarkForestSage,
    background = OffWhiteBackground,
    onBackground = OnSurfaceTextDark,
    surface = PureWhiteSurface,
    onSurface = OnSurfaceTextDark,
    surfaceVariant = SurfaceVariantCream,
    onSurfaceVariant = OnSurfaceMutedText,
    outline = SubtleBorderStroke
)

private val DarkZenColorScheme = darkColorScheme(
    primary = DarkPrimarySage,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = DarkOnSurfaceText,
    secondary = MutedOlive,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkOnSurfaceText,
    tertiary = EarthWarmAccent,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkOnSurfaceText,
    surface = DarkSurface,
    onSurface = DarkOnSurfaceText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceText,
    outline = DarkSurfaceVariant
)

val ColorScheme.terracotta: Color
    get() = TerracottaSoft

@Composable
fun StillLeafTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkZenColorScheme else LightZenColorScheme

    val view = LocalView.current
    if (!view.isInEditMode && view.context is Activity) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ZenTypography,
        shapes = ZenShapes,
        content = content
    )
}
