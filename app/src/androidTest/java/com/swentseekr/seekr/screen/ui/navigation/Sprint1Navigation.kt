package com.swentseekr.seekr.screen.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.swentseekr.seekr.ui.navigation.NavigationTestTags
import com.swentseekr.seekr.ui.navigation.SeekrApp
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SeekrNavigationTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setUp() {
    composeTestRule.setContent { SeekrApp() }
  }

  // -------------------------------------------------
  // Tag existence tests
  // -------------------------------------------------
  @Test
  fun allNavigationTagsAreDisplayed() {
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
  }

  // -------------------------------------------------
  // Bottom bar visibility
  // -------------------------------------------------
  @Test
  fun bottomNavigationIsAlwaysVisible() {
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()

    // Navigate to Map
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()

    // Navigate to Profile
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  // -------------------------------------------------
  // Navigation actions
  // -------------------------------------------------
  @Test
  fun canNavigateBetweenTabs() {
    // Start on Overview
    composeTestRule
        .onNodeWithTag(NavigationTestTags.OVERVIEW_TAB)
        .assertIsDisplayed()
        .performClick()

    // Navigate to Map
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed().performClick()

    // Navigate to Profile
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed().performClick()

    // Back to Overview
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
  }

  // -------------------------------------------------
  // Specific tab tests
  // -------------------------------------------------
  @Test
  fun overviewTabIsClickable() {
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun mapTabIsClickable() {
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun profileTabIsClickable() {
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }
}
