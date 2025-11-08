package com.swentseekr.seekr.ui.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags
import com.swentseekr.seekr.ui.profile.ProfileTestTags
import com.swentseekr.seekr.utils.FakeRepoSuccess
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SeekrNavigationTest {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  // convenience helpers (unmerged tree = true fixes “button not pressed” symptoms)
  private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)

  // --- tiny click helpers to robustly fire in-screen actions (tag, content-desc, or text) ---
  private fun tryClickByTag(vararg tags: String): Boolean =
      tags.any { tag ->
        runCatching {
              node(tag).performClick()
              true
            }
            .getOrNull() == true
      }

  private fun tryClickByDesc(vararg descs: String): Boolean =
      descs.any { d ->
        runCatching {
              compose.onNodeWithContentDescription(d, useUnmergedTree = true).performClick()
              true
            }
            .getOrNull() == true
      }

  private fun tryClickByText(vararg texts: String): Boolean =
      texts.any { t ->
        runCatching {
              compose.onNodeWithText(t, useUnmergedTree = true).performClick()
              true
            }
            .getOrNull() == true
      }

  private fun clickAny(vararg candidates: () -> Boolean) {
    val clicked = candidates.any { it() }
    check(clicked) { "No clickable candidate found for this action (tag/desc/text)." }
  }

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

    // Ensure we're on AddHunt
    node(NavigationTestTags.ADD_HUNT_SCREEN).assertIsDisplayed()

    pressBack()

    // Let Compose settle
    compose.waitForIdle()

    // Wait until: bottom bar is visible AND AddHunt wrapper no longer exists
    compose.waitUntil(timeoutMillis = 10_000) {
      val barVisible =
          runCatching {
                node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
                true
              }
              .getOrNull() == true

      val addHuntGone =
          compose
              .onAllNodes(hasTestTag(NavigationTestTags.ADD_HUNT_SCREEN), useUnmergedTree = true)
              .fetchSemanticsNodes()
              .isEmpty()

      barVisible && addHuntGone
    }

    // Final sanity asserts
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
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

  @Test
  fun settings_inScreen_back_invokes_onGoBack_and_restores_bottom_bar() {
    // Profile → Settings
    node(NavigationTestTags.PROFILE_TAB).performClick()
    node(ProfileTestTags.SETTINGS).performClick()
    node(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

    // Tap in-screen back (not system back)
    clickAny(
        { tryClickByTag("SETTINGS_BACK", "SettingsBack") },
        { tryClickByDesc("Back", "Navigate up") },
        { tryClickByText("Back") })

    // Back to a tab destination → bottom bar visible
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun overview_click_navigates_to_huntcard_with_passed_id_using_fake_repo() {
    // 1) Swap in a fake repo BEFORE composing, so VMs resolve it at construction time.
    val hunt =
        Hunt(
            uid = "fake-123",
            start = Location(48.8566, 2.3522, "Paris Center"),
            end = Location(48.8606, 2.3376, "Louvre"),
            middlePoints = emptyList(),
            status = HuntStatus.FUN,
            title = "Paris Discovery",
            description = "Walk to the Louvre.",
            time = 1.5,
            distance = 3.2,
            difficulty = Difficulty.EASY,
            authorId = "author-1",
            image = 0,
            reviewRate = 4.7)
    val previousRepo = HuntRepositoryProvider.repository
    HuntRepositoryProvider.repository = FakeRepoSuccess(listOf(hunt))

    try {
      // 2) Compose the real NavHost (no Firebase involved).
      compose.runOnUiThread { compose.activity.setContent { SeekrMainNavHost(testMode = true) } }

      // Wait for Overview to draw with list content.
      compose.waitUntil(timeoutMillis = 5_000) {
        runCatching {
              compose
                  .onNodeWithTag(OverviewScreenTestTags.HUNT_LIST, useUnmergedTree = true)
                  .assertExists()
              true
            }
            .getOrNull() == true
      }

      // 3) Click a card in the Overview list.
      // Overview tags each card; the last one is "LAST_HUNT_CARD" — safe to click.
      compose
          .onNodeWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
          .assertExists()
          .performClick()

      // 4) Assert we navigated to HuntCard screen.
      compose.waitUntil(timeoutMillis = 10_000) {
        runCatching {
              compose
                  .onAllNodes(
                      hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
                  .fetchSemanticsNodes()
                  .isNotEmpty()
            }
            .getOrNull() == true
      }

      compose
          .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
          .onFirst()
          .assertExists()
    } finally {
      // 5) Restore the real repo (important for isolation from other tests).
      HuntRepositoryProvider.repository = previousRepo
    }
  }

  // need test for edit hunt, overview, huntcard and review.
}
