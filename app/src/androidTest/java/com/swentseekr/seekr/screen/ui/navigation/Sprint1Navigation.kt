package com.swentseekr.seekr.screen.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
    composeTestRule.waitForIdle()
  }

  // Tag existence tests
  @Test
  fun allNavigationTagsAreDisplayed() {
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
  }

  // Bottom bar visibility and screen content
  @Test
  fun bottomNavigationIsAlwaysVisible() {
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertExists().performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertExists().performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun clickingMapTab_showsMapScreenContent() {
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertExists().performClick()
    composeTestRule.onNodeWithText("Map").assertIsDisplayed()
  }

  // Navigation actions
  @Test
  fun canNavigateBetweenTabs() {
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).assertExists().performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertExists().performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertExists().performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).assertExists().performClick()
  }

  // Additional coverage
  @Test
  fun reSelectingSameTabDoesNotCrash() {
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertExists().performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }
}
