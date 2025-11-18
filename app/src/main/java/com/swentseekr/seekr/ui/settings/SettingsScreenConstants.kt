package com.swentseekr.seekr.ui.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object SettingsScreenDefaults {
  val SCREEN_PADDING: Dp = 24.dp
  val LOGOUT_TOP_PADDING: Dp = 32.dp

  val ITEMS_SPACING = 56.dp
  val COMPONENTS_PADDING = 12.dp

  val COLUMN_WEIGHT = 1f
}

object SettingsScreenStrings {
  const val TOP_BAR_TITLE = "Settings"
  const val BACK_CONTENT_DESCRIPTION = "Back"
  const val VERSION_LABEL = "App Version"
  const val UNKNOWN_VERSION = "Unknown"
  const val LOGOUT_LABEL = "Log out"
  const val NOTIFICATION_LABEL = "Notifications"
  const val PICTURES_LABEL = "My Pictures"
  const val LOCALISATION_LABEL = "My Localisation"
  const val EDIT_PROFILE_LABEL = "Edit Profile"
  const val APP_CONDITION_LABEL = "App Condition"

  const val USER_SETTINGS = "userSettings"
  const val NOTIFICATION_FIELD = "notificationsEnabled"
  const val PICTURES_FIELD = "picturesEnabled"
  const val LOCALISATION_FIELD = "localisationEnabled"
  const val DOCUMENT_TEST_TEXT = "Document should be created automatically"
  const val FIREBASE_TEST_ERROR = "FirebaseAuth currentUser should not be null"
  const val SIGN_IN_ERROR = "User not signed in"

  const val APP_VERSION_1 = "1.0.0"
  const val APP_VERSION_2 = "1.2.3"
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
