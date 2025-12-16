package com.swentseekr.seekr.ui.auth

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dimension constants used in the Sign-In screen.
 *
 * Includes spacing, logo size, button dimensions, padding, and loader size.
 */
object SignInScreenDimensions {

  // ----------------------
  // Logo
  // ----------------------
  val TopSpacing: Dp = 120.dp
  val LogoSize: Dp = 180.dp
  val LogoCornerRadius: Dp = 64.dp
  val SpacerAfterLogo: Dp = 300.dp

  // ----------------------
  // Buttons
  // ----------------------
  val BottomPadding: Dp = 64.dp
  val ButtonHorizontalPadding: Dp = 8.dp
  val ButtonHeight: Dp = 48.dp
  val ButtonCornerRadius: Dp = 12.dp
  val ButtonBorderWidth: Dp = 1.dp
  val ButtonIconSize: Dp = 32.dp
  val ButtonIconPaddingEnd: Dp = 8.dp

  // ----------------------
  // Other / Loader / Bottom Padding
  // ----------------------
  val LoaderSize: Dp = 48.dp
  const val ButtonWidthFraction: Float = 0.7f
}

object SignInScreenTypography {
  val ButtonFontSize = 16.sp
}

/** String constants and helper functions for the Sign-In screen. */
object SignInScreenStrings {
  const val AppLogoDescription = "App Logo"
  const val GoogleLogoDescription = "Google Logo"
  const val SignInButtonLabel = "Sign in with Google"
  const val SuccessTemplate = "Login successful: %s"

  /**
   * Returns a formatted success message after sign-in.
   *
   * @param email The user’s email; if null, defaults to "account".
   * @return A formatted success message string.
   */
  fun successMessage(email: String?): String {
    val safeEmail = email ?: "account"
    return SuccessTemplate.format(safeEmail)
  }
}
