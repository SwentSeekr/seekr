package com.swentseekr.seekr.ui.navigation

import androidx.compose.ui.graphics.Color
import com.swentseekr.seekr.ui.theme.Black
import com.swentseekr.seekr.ui.theme.GrassGreen
import com.swentseekr.seekr.ui.theme.White

object SeekrNavigationDefaults {
  val BottomBarContainerColor: Color = GrassGreen
  val BottomBarIconColor: Color = Black
  val ScaffoldContainerColor: Color = White
  const val IGNORED_TEST_TAG = "IGNORED"

  // --- Offline / shared extra routes ---
  // Route + arg name for the review images screen (used online & offline)
  const val REVIEW_IMAGES_ROUTE = "reviewImages/{reviewId}"
  const val REVIEW_IMAGES_REVIEW_ID_ARG = "reviewId"
  const val HUNT_ID = "huntId"

  const val OVERVIEW_ROUTE = "overview"
  const val OVERVIEW_LABEL = "Overview"
  const val MAP_ROUTE = "map"
  const val MAP_LABEL = "Map"
  const val PROFILE_ROUTE = "profile"
  const val PROFILE_LABEL = "Profile"
  const val PROFILE_PATH = "profile/"
  const val REVIEWS_PATH = "/reviews"
  const val PROFILE_REVIEWS_LABEL = "Profile Reviews"
  const val HUNT_CARD_ROUTE = "hunt/{huntId}"
  const val HUNT_CARD_LABEL = "Hunt"
  const val HUNT_PATH = "hunt/"
  const val EDIT_HUNT_ROUTE = "edit_hunt/{huntId}"
  const val EDIT_HUNT_LABEL = "Edit Hunt"
  const val EDIT_HUNT_PATH = "edit_hunt/"
  const val ADD_REVIEW_ROUTE = "add_review/{huntId}"
  const val ADD_REVIEW_LABEL = "Add Review"
  const val ADD_REVIEW_PATH = "add_review/"
  const val EDIT_REVIEW_ROUTE = "edit_review/{huntId}/{reviewId}"
  const val EDIT_REVIEW_LABEL = "Edit Review"
  const val EDIT_REVIEW_PATH = "edit_review/"
  const val ADD_HUNT_ROUTE = "add_hunt"
  const val ADD_HUNT_LABEL = "Add Hunt"
  const val SETTINGS_ROUTE = "settings"
  const val SETTINGS_LABEL = "Settings"
  const val TERMS_AND_CONDITION_ROUTE = "terms_conditions"
  const val TERMS_AND_CONDITION_LABEL = "Terms & Conditions"
  const val EDIT_PROFILE_ROUTE = "edit_profile"
  const val EDIT_PROFILE_LABEL = "Edit Profile"
  const val PROFILE_REVIEWS_ROUTE = "profile/{userId}/reviews"

  const val USER_ID = "userId"
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
  const val EDIT_REVIEW_HUNT_SCREEN = "EDIT_REVIEW_HUNT_SCREEN"
  const val SETTINGS_SCREEN = "SETTINGS_SCREEN"
  const val TERMS_CONDITIONS_SCREEN = "TERMS_CONDITIONS_SCREEN"
  const val EDIT_PROFILE_SCREEN = "EDIT_PROFILE_SCREEN"

  // --- review images screen test tag (used by offline nav) ---
  const val IMAGE_REVIEW_SCREEN = "IMAGE_REVIEW_SCREEN"
}
