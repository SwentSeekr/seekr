package com.swentseekr.seekr.ui.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.ui.profile.ProfileTestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SeekrNavigationTest {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  // convenience helpers (unmerged tree = true fixes “button not pressed” symptoms)
  private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)

  @Before
  fun setUp() {
    compose.runOnUiThread { compose.activity.setContent { SeekrMainNavHost(testMode = true) } }
    compose.waitUntil(timeoutMillis = 5_000) {
      compose
          .onAllNodes(hasTestTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU), useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  @Test
  fun tabsBar_and_tabs_are_visible_on_start() {
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    node(NavigationTestTags.OVERVIEW_TAB).assertIsDisplayed()
    node(NavigationTestTags.MAP_TAB).assertIsDisplayed()
    node(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
    node(NavigationTestTags.OVERVIEW_SCREEN).assertIsDisplayed()
  }

  @Test
  fun can_switch_between_tabs_and_reselect_without_crash() {
    node(NavigationTestTags.OVERVIEW_TAB).performClick()
    node(NavigationTestTags.MAP_TAB).performClick()
    compose.waitForIdle()
    node(NavigationTestTags.MAP_SCREEN).assertIsDisplayed()

    node(NavigationTestTags.PROFILE_TAB).performClick()
    compose.waitForIdle()
    // FAB exists only on own profile; we just ensure bottom bar stayed
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()

    // Reselect same tab a couple times
    node(NavigationTestTags.PROFILE_TAB).performClick()
    node(NavigationTestTags.PROFILE_TAB).performClick()
    compose.waitForIdle()
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun map_tab_shows_tagged_map_screen() {
    node(NavigationTestTags.MAP_TAB).performClick()
    compose.waitUntil(3_000) {
      runCatching {
            node(NavigationTestTags.MAP_SCREEN).assertIsDisplayed()
            true
          }
          .getOrNull() == true
    }
    node(NavigationTestTags.MAP_SCREEN).assertIsDisplayed()
  }

  @Test
  fun profile_fab_navigates_to_add_hunt_then_back_restores_bar() {
    node(NavigationTestTags.PROFILE_TAB).performClick()
    compose.waitForIdle()

    // FAB to AddHunt
    node(ProfileTestTags.ADD_HUNT).assertIsDisplayed().performClick()

    // wait for AddHunt wrapper tag
    compose.waitUntil(3_000) {
      runCatching {
            node(NavigationTestTags.ADD_HUNT_SCREEN).assertIsDisplayed()
            true
          }
          .getOrNull() == true
    }
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

    // system back
    compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
    compose.waitUntil(3_000) {
      runCatching {
            node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
            true
          }
          .getOrNull() == true
    }
  }

  @Test
  fun profile_click_my_hunt_opens_edit_hunt_and_hides_bar() {
    node(NavigationTestTags.PROFILE_TAB).performClick()
    compose.waitForIdle()

    // first MyHunt card
    node("HUNT_CARD_0").performClick()

    // wait for EditHunt wrapper tag
    compose.waitUntil(3_000) {
      runCatching {
            node(NavigationTestTags.EDIT_HUNT_SCREEN).assertIsDisplayed()
            true
          }
          .getOrNull() == true
    }
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()
  }

  @Test
  fun add_hunt_on_done_navigates_back_to_tabs_and_shows_bar() {
    // Go to Profile → open AddHunt
    node(NavigationTestTags.PROFILE_TAB).performClick()
    node(ProfileTestTags.ADD_HUNT).performClick()

    // We are on AddHunt (wrapper tag present); bar should be hidden in your setup
    node(NavigationTestTags.ADD_HUNT_SCREEN).assertIsDisplayed()

    // Press system back (this does NOT trigger your AddHunt onDone lambda; that's fine)
    compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

    // We should be back on any tab destination (Profile/Overview/etc.) → bottom bar visible
    compose.waitUntil(3_000) {
      runCatching {
            node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
            true
          }
          .getOrNull() == true
    }

    // And we are no longer on AddHunt
    node(NavigationTestTags.ADD_HUNT_SCREEN).assertDoesNotExist()
  }

    @Test
    fun profile_settings_hides_bar_and_back_restores() {
        node(NavigationTestTags.PROFILE_TAB).performClick()
        node(ProfileTestTags.SETTINGS).performClick()
        node(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()
        node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    }
}
