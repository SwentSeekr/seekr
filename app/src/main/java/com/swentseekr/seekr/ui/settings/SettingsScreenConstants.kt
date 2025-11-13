package com.swentseekr.seekr.ui.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object SettingsScreenDefaults {
  val ScreenPadding: Dp = 24.dp
  val ItemSpacing: Dp = 16.dp
  val LogoutTopPadding: Dp = 32.dp
}

object SettingsScreenStrings {
  const val TopBarTitle = "Settings"
  const val BackContentDescription = "Back"
  const val VersionLabel = "App Version"
  const val UnknownVersion = "Unknown"
  const val LogoutLabel = "Log out"
  const val EDIT_PROFILE_TEXT = "editProfileButton"
}

object SettingsScreenTestTags {
  const val LOGOUT_BUTTON = "logoutButton"
  const val APP_VERSION_TEXT = "appVersionText"
  const val BACK_BUTTON = "backButton"
    const val EDIT_PROFILE_BUTTON = "editProfileButton"
}
