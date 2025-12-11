package com.swentseekr.seekr.ui.navigation

import androidx.compose.ui.graphics.Color
import com.swentseekr.seekr.ui.theme.Black
import com.swentseekr.seekr.ui.theme.GrassGreen
import com.swentseekr.seekr.ui.theme.White

object SeekrNavigationDefaults {
  val BottomBarContainerColor: Color = GrassGreen
  val BottomBarIconColor: Color = Black
  val ScaffoldContainerColor: Color = White
  const val IgnoredTestTag = "IGNORED"

  // --- Offline / shared extra routes ---
  // Route + arg name for the review images screen (used online & offline)
  const val REVIEW_IMAGES_ROUTE = "reviewImages/{reviewId}"
  const val REVIEW_IMAGES_REVIEW_ID_ARG = "reviewId"
}

/**
 * Centralized navigation-related test tags.
 *
 * Used in instrumented and UI tests to find elements using `composeTestRule.onNodeWithTag(...)`.
 */
object NavigationTestTags {
  const val BOTTOM_NAVIGATION_MENU = "BOTTOM_NAVIGATION_MENU"
  const val OVERVIEW_TAB = "OVERVIEW_TAB"
  const val MAP_TAB = "MAP_TAB"
  const val PROFILE_TAB = "PROFILE_TAB"
  const val HUNTCARD_SCREEN = "HUNTCARD_SCREEN"
  const val ADD_HUNT_SCREEN = "ADD_HUNT_SCREEN"
  const val MAP_SCREEN = "MAP_SCREEN"
  const val OVERVIEW_SCREEN = "OVERVIEW_SCREEN"
  const val EDIT_HUNT_SCREEN = "EDIT_HUNT_SCREEN"
  const val REVIEW_HUNT_SCREEN = "REVIEW_HUNT_SCREEN"
  const val EDIT_REVIEW_HUNT_SCREEN= "EDIT_REVIEW_HUNT_SCREEN"
  const val SETTINGS_SCREEN = "SETTINGS_SCREEN"
  const val EDIT_PROFILE_SCREEN = "EDIT_PROFILE_SCREEN"

  // --- review images screen test tag (used by offline nav) ---
  const val IMAGE_REVIEW_SCREEN = "IMAGE_REVIEW_SCREEN"
}
