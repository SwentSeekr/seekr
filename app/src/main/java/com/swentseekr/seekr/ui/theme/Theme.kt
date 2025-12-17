package com.swentseekr.seekr.ui.theme

import android.app.Activity
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

/**
 * Light color scheme definition for the application.
 *
 * Maps semantic Material colors to the app’s brand palette. This scheme is used by the global
 * [MaterialTheme].
 */
private val LightColorScheme =
    lightColorScheme(
        primary = Green,
        onPrimary = White,
        secondary = Salmon,
        onSecondary = GrayDislike,
        tertiary = LoadingGray,
        onTertiary = Black,
        background = LightGrayBackground,
        onBackground = Black,
        surface = LightGrayBackground,
        onSurface = Black,
        surfaceVariant = White,
        onSurfaceVariant = LoadingGray,
        outline = TabInactiveGray,
        error = LightError,
        onError = LightOnError,
        primaryContainer = LightGrayBackground,
        onPrimaryContainer = Black,
        surfaceContainer = EasyGreen,
        tertiaryContainer = StatBackground,
        onTertiaryContainer = StatTextDark)

/**
 * Custom color palette extending Material colors.
 *
 * Holds domain-specific colors that do not fit into the Material color scheme (statuses, difficulty
 * levels, map overlays).
 *
 * Marked as [Stable] to avoid unnecessary recompositions.
 */
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
    val disliked: Color,
    val orangeButton: Color
)

/**
 * CompositionLocal providing access to [AppColors].
 *
 * Allows any composable in the hierarchy to access domain-specific colors via
 * `LocalAppColors.current`.
 */
val LocalAppColors = staticCompositionLocalOf {
  AppColors(
      statusFun = StatusFun,
      statusSport = StatusSport,
      difficultyEasy = DifficultyEasy,
      difficultyHard = DifficultyHard,
      difficultyIntermediate = Orange,
      statusDiscover = StatusDiscover,
      mapRoute = Blue,
      liked = RedLike,
      disliked = GrayDislike,
      orangeButton = Orange)
}

/**
 * Root theme composable for the application.
 *
 * Responsibilities:
 * - Apply the Material color scheme and typography
 * - Provide [AppColors] through CompositionLocal
 * - Configure system UI (status bar color and appearance)
 *
 * @param content Root composable content of the app.
 */
@Composable
fun SampleAppTheme(content: @Composable () -> Unit) {
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
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
