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
    primary = PrimaryOlive,
    onPrimary = PureWhite,
    primaryContainer = PaleSage,
    onPrimaryContainer = DeepBotanicalGreen,
    secondary = SageGreen,
    onSecondary = PureWhite,
    secondaryContainer = SoftCream,
    onSecondaryContainer = PrimaryText,
    tertiary = SoftMoss,
    onTertiary = DeepBotanicalGreen,
    background = WarmIvory,
    onBackground = PrimaryText,
    surface = PureCream,
    onSurface = PrimaryText,
    surfaceVariant = SoftCream,
    onSurfaceVariant = SecondaryText,
    outline = SubtleBorderStroke
)

private val DarkZenColorScheme = darkColorScheme(
    primary = SoftMoss,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = DarkOnSurfaceText,
    secondary = SageGreen,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkOnSurfaceText,
    tertiary = PaleSage,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkOnSurfaceText,
    surface = DarkSurface,
    onSurface = DarkOnSurfaceText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceText,
    outline = DarkSurfaceVariant
)

val ColorScheme.wateringAmber: Color
    get() = WateringAmber

val ColorScheme.attentionTerracotta: Color
    get() = AttentionTerracotta

val ColorScheme.healthyGreen: Color
    get() = HealthyGreen

val ColorScheme.deepBotanicalGreen: Color
    get() = DeepBotanicalGreen

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
