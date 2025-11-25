package com.swentseekr.seekr.ui.offline

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object OfflineConstants {

  // --- Existing strings & UI constants ---
  const val OFFLINE_TITLE = "You're offline"
  const val OFFLINE_MESSAGE = "Connect to the internet at least one time to continue."
  const val OPEN_SETTINGS_BUTTON = "Open settings"

  const val OFFLINE_MAP_MESSAGE = "Map is unavailable while you're offline."
  const val OFFLINE_OVERVIEW_MESSAGE =
      "Overview is unavailable while you're offline.\nYou can still see your downloaded hunts."
  const val SHOW_DOWNLOADED_HUNTS_BUTTON = "Show downloaded hunts"

  val ICON_SPACING = 16.dp
  val MESSAGE_SPACING = 8.dp
  val BUTTON_SPACING = 24.dp
  val SCREEN_PADDING = 24.dp

  // --- Numeric defaults ---
  const val DEFAULT_INT = 0
  const val DEFAULT_DOUBLE = 0.0

  // --- Empty list default (typed for Hunt) ---
  val DEFAULT_HUNT_LIST = emptyList<com.swentseekr.seekr.model.hunt.Hunt>()

  // --- Card styling ---
  val LIGHT_GREEN_BACKGROUND = Color(0xFFDFF5E3)
  val OFFLINE_CARD_HEIGHT = 200.dp
  val OFFLINE_CARD_WIDTH_RATIO = 0.8f
  val CARD_SHAPE = RoundedCornerShape(16.dp)
  val OFFLINE_ICON_SIZE = 48.dp
}
