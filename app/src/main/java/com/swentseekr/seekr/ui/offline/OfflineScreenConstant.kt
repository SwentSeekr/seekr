package com.swentseekr.seekr.ui.offline

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.theme.EasyGreen
import com.swentseekr.seekr.ui.theme.Green

object OfflineConstants {

  // --- Existing strings & UI constants ---
  const val OFFLINE_TITLE = "You're offline"
  const val OFFLINE_MESSAGE = "Connect to the internet at least one time to continue."
  const val OPEN_SETTINGS_BUTTON = "Open settings"

  const val OFFLINE_MAP_MESSAGE = "Map is unavailable while you're offline."
  const val OFFLINE_OVERVIEW_MESSAGE =
      "No internet? No problem. You can still access your saved hunts."
  const val SHOW_DOWNLOADED_HUNTS_BUTTON = "Continue without connection"
  const val MAP_ICON = "Offline Map Icon"
  const val OVERVIEW_ICON = "Offline Overview Icon"

  val ICON_SPACING = 16.dp
  val MESSAGE_SPACING = 8.dp
  val BUTTON_SPACING = 24.dp
  val SCREEN_PADDING = 24.dp

  // --- Numeric defaults ---
  const val DEFAULT_INT = 0
  const val DEFAULT_DOUBLE = 0.0

  // --- Empty list default (typed for Hunt) ---
  val DEFAULT_HUNT_LIST: List<Hunt> = emptyList()

  // --- Card styling ---
  // Use themed "easy" green for offline card background
  val LIGHT_GREEN_BACKGROUND = EasyGreen
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
  val BUTTON_CONTAINER_COLOR = Green
}
