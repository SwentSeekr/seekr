package com.swentseekr.seekr.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
    darkColorScheme(
        primary = Green,
        onPrimary = Black,
        secondary = Orange,
        onSecondary = GrayDislike,
        tertiary = GrayBackgound,
        onTertiary = Black,
        background = Black,
        onBackground = White,
        surface = GrayBackgound,
        onSurface = Black,
        error = DarkError,
        onError = DarkOnError)

private val LightColorScheme =
    lightColorScheme(
        primary = Green,
        onPrimary = White,
        secondary = Orange,
        onSecondary = GrayDislike,
        tertiary = GrayBackgound,
        onTertiary = Black,
        background = White,
        onBackground = Black,
        surface = GrayBackgound,
        onSurface = Black,
        error = LightError,
        onError = LightOnError,
    )

@Composable
fun SampleAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
  val colorScheme = LightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = colorScheme.primary.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
