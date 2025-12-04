package com.swentseekr.seekr.ui.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object SettingsScreenDefaults {
  val ALPHA_CHANGE: Float = 0.3f

  val SCREEN_PADDING: Dp = 24.dp
  val PADDING_MID: Dp = 20.dp
  val PADDING_TINY: Dp = 8.dp
  val PADDING_MINI: Dp = 4.dp

  val SPACER_HEIGHT: Dp = 24.dp
  val SPACER_HEIGHT_SMALL: Dp = 16.dp

  val ROUND_CORNER: Dp = 16.dp
  val ELEVATION: Dp = 4.dp

  val LOGOUT_TOP_PADDING: Dp = 32.dp

  val ITEMS_SPACING = 56.dp
  val COMPONENTS_PADDING = 12.dp

  val BUTTON_HEIGHT: Dp = 56.dp

  val COLUMN_WEIGHT = 1f
  const val NOTIFICATION_REQUEST_CODE = 1001
  const val TEST_VERIFICATION_TIMES_NULL = 0
  const val TEST_VERIFICATION_TIMES_NOT_NULL = 1
  const val TEST_CALL_ORDER_SIZE = 2
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
  const val NOTIFICATION_FIELD_2 = "Notifications enabled"

  const val DOCUMENT_TEST_TEXT = "Document should be created automatically"
  const val FIREBASE_TEST_ERROR = "FirebaseAuth currentUser should not be null"
  const val SIGN_IN_ERROR = "User not signed in"
  const val NOTIFICATION_ACCEPT_MESSAGE = "You will now receive app notifications"

  const val APP_VERSION_1 = "1.0.0"
  const val APP_VERSION_2 = "1.2.3"

  const val TEST_NOTIFICATION = "notification"
  const val TEST_REPOSITORY = "repository"
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
