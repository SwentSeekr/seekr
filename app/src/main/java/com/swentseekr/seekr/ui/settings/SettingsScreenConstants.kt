package com.swentseekr.seekr.ui.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * UI constants for the Settings screen.
 *
 * Centralizes layout dimensions, spacing, alpha values, and sizing used across all Settings
 * composables.
 *
 * Prevents hardcoded values and ensures visual consistency.
 */
object SettingsScreenDefaults {

  // -----------
  // Alpha
  // -----------
  val ALPHA_CHANGE: Float = 0.3f

  // -----------
  // Padding
  // -----------
  val PADDING_MID: Dp = 20.dp
  val PADDING_TINY: Dp = 8.dp
  val PADDING_MINI: Dp = 4.dp

  // -----------
  // Spacers
  // -----------
  val SPACER_HEIGHT: Dp = 24.dp
  val SPACER_HEIGHT_SMALL: Dp = 16.dp

  // -----------
  // Shape & elevation
  // -----------

  val ROUND_CORNER: Dp = 16.dp
  val ELEVATION: Dp = 4.dp

  // -----------
  // Layout
  // -----------
  val ITEMS_SPACING = 56.dp
  val COMPONENTS_PADDING = 12.dp

  val BUTTON_HEIGHT: Dp = 56.dp

  val COLUMN_WEIGHT = 1f
}

/**
 * Static text values used by the Settings screen.
 *
 * Extracted for reuse, consistency, and test stability.
 */
object SettingsScreenStrings {
  // -----------
  // Top bar
  // -----------

  const val TOP_BAR_TITLE = "Settings"
  const val BACK_CONTENT_DESCRIPTION = "Back"

  // -----------
  // Labels
  // -----------

  const val VERSION_LABEL = "App Version"
  const val UNKNOWN_VERSION = "Unknown"
  const val LOGOUT_LABEL = "Log out"

  const val NOTIFICATION_LABEL = "Notifications"
  const val PICTURES_LABEL = "My Pictures"
  const val LOCALISATION_LABEL = "My Localisation"
  const val EDIT_PROFILE_LABEL = "Edit Profile"
  const val APP_CONDITION_LABEL = "App Condition"

  // -----------
  // Notification messages
  // -----------

  const val NOTIFICATION_FIELD_2 = "Notifications enabled"
  const val NOTIFICATION_ACCEPT_MESSAGE = "You will now receive app notifications"

  // -----------
  // Version strings (test / fallback)
  // -----------

  const val APP_VERSION_1 = "1.0.0"
  const val APP_VERSION_2 = "1.2.3"

  // -----------
  // Section headers
  // -----------

  const val ACCOUNT = "Account"
  const val LEGAL = "Legal"

  // -----------
  // Misc
  // -----------

  const val PACKAGE = "package"
}

/**
 * Semantic test tags for the Settings screen.
 *
 * Used by Compose UI tests for deterministic node lookup.
 */
object SettingsScreenTestTags {

  // -----------
  // Navigation
  // -----------

  const val SETTINGS_SCREEN = "settingsScreen"
  const val LOGOUT_BUTTON = "logoutButton"
  const val APP_VERSION_TEXT = "appVersionText"
  const val BACK_BUTTON = "backButton"

  // -----------
  // Account actions
  // -----------

  const val LOGOUT_BUTTON = "logoutButton"
  const val EDIT_PROFILE_BUTTON = "editProfileButton"

  // -----------
  // App info
  // -----------

  const val APP_VERSION_TEXT = "appVersionText"

  // -----------
  // Toggles
  // -----------

  const val NOTIFICATIONS_TOGGLE = "notificationsToggle"
  const val PICTURES_TOGGLE = "picturesToggle"
  const val LOCALISATION_TOGGLE = "localisationToggle"

  // -----------
  // Legal
  // -----------

  const val APP_CONDITION_BUTTON = "appConditionButton"
}
