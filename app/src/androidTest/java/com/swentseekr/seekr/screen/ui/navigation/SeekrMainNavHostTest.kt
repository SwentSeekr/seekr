package com.swentseekr.seekr.screen.ui.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.ui.navigation.NavigationTestTags
import com.swentseekr.seekr.ui.navigation.SeekrMainNavHost
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SeekrNavigationTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setUp() {
    composeTestRule.runOnUiThread {
      // Always start with a clean Compose hierarchy
      composeTestRule.activity.setContent { SeekrMainNavHost() }
    }

    // Wait until the UI is actually rendered before proceeding
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodes(hasTestTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  @Test
  fun allNavigationTagsAreDisplayed() {
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
  }

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
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).performClick()
  }

  @Test
  fun reSelectingSameTabDoesNotCrash() {
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  // NEW TESTS BELOW -------------------------------------------------------------
  /**
   * @Test fun navigateToHuntCardScreen_displaysHuntCard() { // Simulate navigation to HuntCard
   *   manually composeTestRule.runOnUiThread { composeTestRule.activity.setContent { val
   *   navController = rememberNavController() SeekrMainNavHost(navController = navController)
   *   navController.navigate(SeekrDestination.HuntCard.createRoute("huntTest123")) } }
   *
   * composeTestRule.waitUntil(timeoutMillis = 5_000) { composeTestRule
   * .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN)) .fetchSemanticsNodes()
   * .isNotEmpty() }
   *
   * composeTestRule.onNodeWithTag(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed() }
   *
   * @Test fun huntCardScreen_goBack_returnsToPreviousTab() { composeTestRule.runOnUiThread {
   *   composeTestRule.activity.setContent { val navController = rememberNavController()
   *   SeekrMainNavHost(navController = navController)
   *   navController.navigate(SeekrDestination.HuntCard.createRoute("huntTest123")) } }
   *
   * composeTestRule.waitUntil(timeoutMillis = 5_000) { composeTestRule
   * .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN)) .fetchSemanticsNodes()
   * .isNotEmpty() }
   *
   * // Go back (simulate back press) composeTestRule.activityRule.scenario.onActivity {
   * it.onBackPressedDispatcher.onBackPressed() }
   *
   * // After going back, bottom nav should be visible again
   * composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed() }*
   */
}
