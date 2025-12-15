package com.swentseekr.seekr.ui.offline

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.theme.EasyGreen
import com.swentseekr.seekr.ui.theme.Green

/**
 * Centralized constants used across the offline UI layer.
 *
 * This object groups all string literals, spacing values, text sizes, and reusable styling
 * primitives that are specific to offline-related screens, such as:
 * - [OfflineRequiredScreen]
 * - [OfflineOverviewScreen] / [OfflineOverviewHuntsScreen]
 * - [OfflineMapScreen]
 * - Offline profile components (e.g. [OfflineCachedProfileScreen])
 *
 * Goals:
 * - Keep offline UI consistent (spacing, shapes, colors, copy).
 * - Avoid duplication of magic numbers and repeated hard-coded strings.
 * - Provide a single place to adjust offline branding / messaging.
 *
 * The values defined here are intentionally UI-focused and are not meant to be used as generic
 * domain constants.
 */
object OfflineConstants {

  // --- Generic offline / required screen strings ---
  const val OFFLINE_TITLE = "You're offline"
  const val OFFLINE_MESSAGE = "Connect to the internet at least one time to continue."
  const val OPEN_SETTINGS_BUTTON = "Open settings"

  // ContentDescription for the warning icon on the required screen
  const val OFFLINE_REQUIRED_ICON_DESCRIPTION = "Offline warning icon"

  // --- Map / overview strings ---
  const val OFFLINE_MAP_MESSAGE = "Map is unavailable while you're offline."
  const val OFFLINE_OVERVIEW_MESSAGE =
      "No internet? No problem. You can still access your saved hunts."
  const val SHOW_DOWNLOADED_HUNTS_BUTTON = "Continue without connection"
  const val MAP_ICON = "Offline Map Icon"
  const val OVERVIEW_ICON = "Offline Overview Icon"

  // --- Shared layout spacing ---
  val ICON_SPACING = 16.dp
  val MESSAGE_SPACING = 8.dp
  val BUTTON_SPACING = 24.dp
  val SCREEN_PADDING = 24.dp

  // --- Profile screen paddings & layout (from OfflineProfileConstants) ---
  val PROFILE_SCREEN_HORIZONTAL_PADDING = 16.dp
  val PROFILE_SCREEN_VERTICAL_PADDING = 8.dp
  val PROFILE_SECTION_TOP_PADDING = 24.dp
  val PROFILE_SMALL_PADDING = 4.dp
  val PROFILE_MEDIUM_PADDING = 16.dp
  val PROFILE_ICON_VERTICAL_PADDING = 10.dp
  val PROFILE_ICON_HORIZONTAL_PADDING = 40.dp

  // --- Profile text sizes ---
  val PROFILE_TEXT_SIZE_PSEUDONYM = 20.sp
  val PROFILE_TEXT_SIZE_BODY = 16.sp
  val PROFILE_TEXT_SIZE_SECONDARY = 14.sp

  const val PROFILE_COLUMN_WEIGHT = 1f

  // --- Profile text strings ---
  const val OFFLINE_NO_PROFILE = "Offline – no cached profile available"
  const val NO_HUNTS_YET = "No hunts yet"
  const val HUNTS_DONE_SUFFIX = " hunts done"
  const val REVIEWS_SUFFIX = " reviews"

  const val TAB_MY_HUNTS = "My hunts"
  const val TAB_DONE_HUNTS = "Done hunts"
  const val TAB_LIKED_HUNTS = "Liked hunts"

  // --- Numeric defaults ---
  const val DEFAULT_INT = 0
  const val DEFAULT_DOUBLE = 0.0

  // --- Empty list default (typed for Hunt) ---
  val DEFAULT_HUNT_LIST: List<Hunt> = emptyList()

  // --- Card styling ---
  val OFFLINE_CARD_HEIGHT = 450.dp
  const val OFFLINE_CARD_WIDTH_RATIO = 0.8f
  val CARD_SHAPE = RoundedCornerShape(16.dp)
  val OFFLINE_ICON_SIZE = 48.dp

  // --- Offline overview layout spacings ---
  val OVERVIEW_TOP_SPACER_HEIGHT = 64.dp
  val OVERVIEW_BUTTON_TOP_SPACER_HEIGHT = 100.dp
  val OVERVIEW_BUTTON_BOTTOM_SPACER_HEIGHT = 50.dp

  // --- Button styling ---
  val BUTTON_SHAPE = RoundedCornerShape(50)
  const val BUTTON_WIDTH_RATIO = 0.7f
  // Use primary green from theme for the offline CTA button
}
