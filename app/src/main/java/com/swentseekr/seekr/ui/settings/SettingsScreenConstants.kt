package com.swentseekr.seekr.ui.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object SettingsScreenDefaults {
  val ScreenPadding: Dp = 24.dp
  val LogoutTopPadding: Dp = 32.dp

  val ItemSpacing = 56.dp
  val ComponentsPadding = 12.dp

  val ColumnWeight = 1f
}

object SettingsScreenStrings {
  const val TopBarTitle = "Settings"
  const val BackContentDescription = "Back"
  const val VersionLabel = "App Version"
  const val UnknownVersion = "Unknown"
  const val LogoutLabel = "Log out"
  const val NotificationsLabel = "Notifications"
  const val PicturesLabel = "My Pictures"
  const val LocalisationLabel = "My Localisation"
  const val EditProfileLabel = "Edit Profile"
  const val AppConditionLabel = "App Condition"
}

object SettingsScreenTestTags {
  const val LOGOUT_BUTTON = "logoutButton"
  const val APP_VERSION_TEXT = "appVersionText"
  const val BACK_BUTTON = "backButton"
  const val EDIT_PROFILE_BUTTON = "editProfileButton"

  const val NOTIFICATIONS_TOGGLE = "notificationsToggle"
  const val PICTURES_TOGGLE = "picturesToggle"
  const val LOCALISATION_TOGGLE = "localisationToggle"
  const val APP_CONDITION_BUTTON = "appConditionButton"
}
