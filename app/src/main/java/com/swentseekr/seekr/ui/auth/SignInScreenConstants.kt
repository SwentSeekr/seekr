package com.swentseekr.seekr.ui.auth

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object SignInScreenDimensions {
  val TopSpacing: Dp = 120.dp
  val LogoSize: Dp = 180.dp
  val LogoCornerRadius: Dp = 64.dp
  val SpacerAfterLogo: Dp = 300.dp
  val BottomPadding: Dp = 64.dp
  val ButtonHorizontalPadding: Dp = 8.dp
  val ButtonHeight: Dp = 48.dp
  val ButtonCornerRadius: Dp = 12.dp
  val ButtonBorderWidth: Dp = 1.dp
  val ButtonIconSize: Dp = 32.dp
  val ButtonIconPaddingEnd: Dp = 8.dp
  val LoaderSize: Dp = 48.dp
  const val ButtonWidthFraction: Float = 0.7f
}

object SignInScreenTypography {
  val ButtonFontSize = 16.sp
}

object SignInScreenStrings {
  const val AppLogoDescription = "App Logo"
  const val GoogleLogoDescription = "Google Logo"
  const val SignInButtonLabel = "Sign in with Google"
  const val SuccessTemplate = "Login successful: %s"

  fun successMessage(email: String?): String {
    val safeEmail = email ?: "account"
    return SuccessTemplate.format(safeEmail)
  }
}
