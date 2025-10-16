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
import com.swentseekr.seekr.ui.profile.ProfileTestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SeekrNavigationTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setUp() {
    composeTestRule.runOnUiThread { composeTestRule.activity.setContent { SeekrMainNavHost() } }

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

  // ---------------- NEW TESTS FOR ADD HUNT FLOW ----------------

  @Test
  fun profileFab_navigatesToAddHuntPlaceholder() {
    // Go to Profile
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()

    // Tap FAB with tag ADD_HUNT
    composeTestRule.onNodeWithTag(ProfileTestTags.ADD_HUNT).assertIsDisplayed().performClick()

    // Expect placeholder screen visible
    composeTestRule.onNodeWithTag(NavigationTestTags.ADD_HUNT_SCREEN).assertIsDisplayed()
  }

  @Test
  fun addHuntPlaceholder_backNavigatesToProfile() {
    // Navigate to Add Hunt as above
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.onNodeWithTag(ProfileTestTags.ADD_HUNT).performClick()

    // Press system back
    composeTestRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

    // We should be back in Profile; bottom bar visible and Profile tab exists
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
  }
}
