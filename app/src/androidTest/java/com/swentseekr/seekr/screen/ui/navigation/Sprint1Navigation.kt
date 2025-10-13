package com.swentseekr.seekr.screen.ui.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.ui.navigation.NavigationTestTags
import com.swentseekr.seekr.ui.navigation.SeekrApp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SeekrNavigationTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setUp() {
    composeTestRule.activityRule.scenario.onActivity { activity ->
      activity.setContent { SeekrApp() }
    }

    // Wait for Compose to settle before starting assertions
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

  @Test
  fun canNavigateBetweenTabs() {
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).assertExists().performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertExists().performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertExists().performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).assertExists().performClick()
  }

  @Test
  fun reSelectingSameTabDoesNotCrash() {
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertExists().performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }
}
