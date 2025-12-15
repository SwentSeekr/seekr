package com.swentseekr.seekr.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme =
    lightColorScheme(
        primary = Green,
        onPrimary = White,
        secondary = Salmon,
        onSecondary = GrayDislike,
        tertiary = LoadingGray,
        onTertiary = Black,
        background = LightGrayBackgound,
        onBackground = Black,
        surface = LightGrayBackgound,
        onSurface = Black,
        surfaceVariant = White,
        onSurfaceVariant = LoadingGray,
        outline = TabInactiveGray,
        error = LightError,
        onError = LightOnError,
        primaryContainer = LightGrayBackgound,
        onPrimaryContainer = Black,
        surfaceContainer = EasyGreen,
        tertiaryContainer = StatBackground ,
        onTertiaryContainer = StatTextDark
    )

@Stable
class AppColors(
    val statusFun: Color,
    val statusSport: Color,
    val statusDiscover: Color,
    val difficultyEasy: Color,
    val difficultyHard: Color,
    val difficultyIntermediate: Color,
    val mapRoute: Color,
    val liked: Color,
    val disliked: Color
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        statusFun = StatusFun,
        statusSport = StatusSport,
        difficultyEasy = DifficultyEasy,
        difficultyHard = DifficultyHard,
        difficultyIntermediate = DifficultyIntermediate,
        statusDiscover = StatusDiscover,
        mapRoute = Blue,
        liked = RedLike,
        disliked = GrayDislike
    )
}

@Composable
fun SampleAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val appColors = LocalAppColors.current
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
