package com.swentseekr.seekr.ui.navigation

object NavigationTestConstants {
  // --- Hunt IDs used in tests ---
  const val HUNT_123 = "hunt_123"
  const val NOTIFICATION_HUNT_456 = "notification_hunt_456"
  const val REMOVE_AFTER_NAV_HUNT = "remove_after_nav"
  const val SINGLE_TOP_HUNT = "single_top_hunt"
  const val BACK_TO_OVERVIEW_HUNT = "back_to_overview_hunt"
  const val SPECIAL_CHARS_HUNT = "hunt-with_special.chars123"
  const val KILLED_APP_HUNT = "killed_app_hunt"
  const val TEST_ROUTE_HUNT = "test_route_hunt"

  // --- Titles used in tests ---
  const val TITLE_DEEPLINK_HUNT = "DeepLink Hunt"
  const val TITLE_NOTIFICATION_HUNT = "Notification Hunt"
  const val TITLE_REMOVE_TEST_HUNT = "Remove Test Hunt"
  const val TITLE_SINGLE_TOP_HUNT = "Single Top Hunt"
  const val TITLE_BACK_TEST_HUNT = "Back Test Hunt"
  const val TITLE_SPECIAL_CHARS_HUNT = "Special Chars Hunt"
  const val TITLE_KILLED_APP_HUNT = "Killed App Hunt"

  // --- Intent extra keys ---
  const val EXTRA_HUNT_ID = "huntId"
  const val HUNT_REMOVAL_SENTENCE = "huntId should be removed from intent after navigation"
}

object SeekrMainNavHostConstants {

  const val NAVIGATION_MESSAGE = "HUNT_ID should be removed after navigation"
  const val DEEP_LINK_ID = "deep-123"
}
