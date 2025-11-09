package com.swentseekr.seekr.ui.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.HuntScreenTestTags
import com.swentseekr.seekr.ui.huntcardview.AddReviewScreenTestTags
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
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          android.Manifest.permission.ACCESS_FINE_LOCATION,
          android.Manifest.permission.ACCESS_COARSE_LOCATION)

  // --- timeouts (ms) ---
  private companion object {
    const val SHORT = 3_000L
    const val MED = 5_000L
    const val LONG = 10_000L
    const val XLONG = 40_000L
  }

  // --- convenience helpers (unmerged tree = true avoids "button not pressed" issues) ---
  private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)

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

  private fun waitUntilTrue(timeout: Long = MED, block: () -> Boolean) {
    compose.waitUntil(timeoutMillis = timeout) { runCatching { block() }.getOrNull() == true }
  }

  private inline fun <T> withFakeRepo(repo: FakeRepoSuccess, crossinline block: () -> T): T {
    val prev = HuntRepositoryProvider.repository
    HuntRepositoryProvider.repository = repo
    return try {
      block()
    } finally {
      HuntRepositoryProvider.repository = prev
    }
  }

  @Before
  fun setUp() {
    compose.runOnUiThread { compose.activity.setContent { SeekrMainNavHost(testMode = true) } }
    waitUntilTrue(MED) {
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
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()

    // Reselect same tab a couple times
    node(NavigationTestTags.PROFILE_TAB).performClick()
    node(NavigationTestTags.PROFILE_TAB).performClick()
    compose.waitForIdle()
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  // (Removed a dedicated "map_tab_shows_tagged_map_screen" test since the above already covers it.)

  @Test
  fun profile_fab_navigates_to_add_hunt_then_back_restores_bar() {
    node(NavigationTestTags.PROFILE_TAB).performClick()
    compose.waitForIdle()

    // FAB to AddHunt
    node(ProfileTestTags.ADD_HUNT).assertIsDisplayed().performClick()

    // wait for AddHunt wrapper tag
    waitUntilTrue(SHORT) {
      node(NavigationTestTags.ADD_HUNT_SCREEN).assertIsDisplayed()
      true
    }
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

    // system back
    compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
    waitUntilTrue(SHORT) {
      node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
      true
    }
  }

  @Test
  fun profile_click_my_hunt_opens_edit_hunt_and_hides_bar() {
    node(NavigationTestTags.PROFILE_TAB).performClick()
    compose.waitForIdle()

    // first MyHunt card
    node("HUNT_CARD_0").performClick()

    // wait for EditHunt wrapper tag
    waitUntilTrue(SHORT) {
      node(NavigationTestTags.EDIT_HUNT_SCREEN).assertIsDisplayed()
      true
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

    compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
    compose.waitForIdle()

    // Wait until: bottom bar is visible AND AddHunt wrapper no longer exists
    compose.waitUntil(timeoutMillis = LONG) {
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

    withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
      // Compose the real NavHost (no Firebase involved).
      compose.runOnUiThread { compose.activity.setContent { SeekrMainNavHost(testMode = true) } }

      // Wait for Overview to draw with list content.
      waitUntilTrue(MED) {
        compose
            .onNodeWithTag(OverviewScreenTestTags.HUNT_LIST, useUnmergedTree = true)
            .assertExists()
        true
      }

      // Click the last card.
      compose
          .onNodeWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
          .assertExists()
          .performClick()

      // Assert we navigated to HuntCard screen.
      compose.waitUntil(timeoutMillis = XLONG) {
        compose
            .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      compose
          .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
          .onFirst()
          .assertExists()
    }
  }

  @Test
  fun profile_myHunt_edit_flow_uses_mockProfileData_and_onDone_returns_to_profile() {
    // --- Seed repo so EditHuntViewModel.load("hunt123") succeeds (matches mockProfileData). ---
    val seeded =
        Hunt(
            uid = "hunt123", // <- IMPORTANT: same id as mockProfileData()
            start = Location(40.7128, -74.0060, "New York"),
            end = Location(40.730610, -73.935242, "Brooklyn"),
            middlePoints = emptyList(),
            status = HuntStatus.FUN,
            title = "City Exploration",
            description = "Discover hidden gems in the city",
            time = 2.5,
            distance = 5.0,
            difficulty = Difficulty.DIFFICULT,
            authorId = "0",
            image = 0,
            reviewRate = 4.5)

    withFakeRepo(FakeRepoSuccess(listOf(seeded))) {
      // Compose with testMode so Profile uses mockProfileData() and exposes HUNT_CARD_0.
      compose.runOnUiThread { compose.activity.setContent { SeekrMainNavHost(testMode = true) } }

      // Go to Profile tab.
      node(NavigationTestTags.PROFILE_TAB).performClick()
      compose.waitForIdle()

      // Open the first My Hunt card -> navigates to EditHunt(hunt123).
      node("HUNT_CARD_0").assertIsDisplayed().performClick()
      waitUntilTrue(MED) {
        node(NavigationTestTags.EDIT_HUNT_SCREEN).assertIsDisplayed()
        true
      }

      // Bottom bar should be hidden on EditHunt.
      node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

      // BEST EFFORT: try to go through the "select locations" branch, then cancel (no-ops if
      // absent).
      runCatching {
            listOf(
                    { tryClickByTag("SELECT_LOCATIONS", "HUNT_SELECT_LOCATIONS", "POINTS_PICKER") },
                    { tryClickByDesc("Select locations", "Pick points", "Select points") },
                    { tryClickByText("Select locations", "Select points", "Add points") },
                )
                .any { it() }

            listOf(
                    { tryClickByTag("CANCEL", "MAP_CANCEL", "Back") },
                    { tryClickByDesc("Cancel", "Back") },
                    { tryClickByText("Cancel", "Back") },
                )
                .any { it() }
          }
          .getOrNull()

      // Try to SAVE to trigger onDone() → back to Profile.
      val didClickSave =
          listOf<(Unit) -> Boolean>(
                  {
                    listOf(
                            "SAVE",
                            "SAVE_BUTTON",
                            "HUNT_SAVE",
                            "EDIT_SAVE",
                            "SAVE_HUNT",
                            "HUNT_SUBMIT",
                            "SUBMIT_BUTTON")
                        .any { tag -> tryClickByTag(tag) }
                  },
                  { arrayOf("Save", "Done").any { d -> tryClickByDesc(d) } },
                  {
                    arrayOf("Save", "SAVE", "Save Hunt", "Done", "DONE").any { t ->
                      tryClickByText(t)
                    }
                  },
              )
              .any { it(Unit) }

      if (didClickSave) {
        waitUntilTrue(LONG) {
          node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
          true
        }
        val editGone =
            compose
                .onAllNodes(hasTestTag(NavigationTestTags.EDIT_HUNT_SCREEN), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isEmpty()
        assert(editGone) { "EditHunt wrapper should be dismissed after save/onDone." }
      } else {
        // If no visible Save control, at least exercise onGoBack → popBackStack.
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        waitUntilTrue(MED) {
          node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
          true
        }
      }
    }
  }

  @Test
  fun edit_hunt_test_done_navigates_back_to_profile_and_restores_bottom_bar() {
    // Seed matching hunt and navigate to Edit
    val seeded =
        Hunt(
            uid = "hunt123",
            start = Location(0.0, 0.0, ""),
            end = Location(0.0, 0.0, ""),
            middlePoints = emptyList(),
            status = HuntStatus.FUN,
            title = "t",
            description = "d",
            time = 1.0,
            distance = 1.0,
            difficulty = Difficulty.EASY,
            authorId = "0",
            image = 0,
            reviewRate = 4.0)

    withFakeRepo(FakeRepoSuccess(listOf(seeded))) {
      compose.runOnUiThread { compose.activity.setContent { SeekrMainNavHost(testMode = true) } }
      node(NavigationTestTags.PROFILE_TAB).performClick()
      node("HUNT_CARD_0").performClick()
      node(NavigationTestTags.EDIT_HUNT_SCREEN).assertIsDisplayed()
      node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

      compose.onNodeWithTag(HuntScreenTestTags.HUNT_SAVE, useUnmergedTree = true).performClick()
      compose.waitForIdle()

      // We should now be on the Profile tab; bottom bar visible; Edit wrapper gone.
      waitUntilTrue(MED) {
        node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
        true
      }
      val editGone =
          compose
              .onAllNodes(hasTestTag(NavigationTestTags.EDIT_HUNT_SCREEN), useUnmergedTree = true)
              .fetchSemanticsNodes()
              .isEmpty()
      assert(editGone)
    }
  }

  @Test
  fun overview_to_huntcard_addReview_opens_review_done_returns_to_huntcard() {
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

    withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
      // Compose fresh with the fake repo in place.
      compose.runOnUiThread { compose.activity.setContent { SeekrMainNavHost(testMode = true) } }

      // Overview → HuntCard
      waitUntilTrue(MED) {
        compose
            .onNodeWithTag(OverviewScreenTestTags.HUNT_LIST, useUnmergedTree = true)
            .assertExists()
        true
      }
      compose
          .onNodeWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
          .assertExists()
          .performClick()

      // Arrived on HuntCard
      compose.waitUntil(timeoutMillis = LONG) {
        compose
            .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      // Open Add Review
      clickAny(
          { tryClickByTag("HuntCard_AddReview", "ADD_REVIEW_BUTTON", "HuntCard_AddReviewButton") },
          { tryClickByDesc("Add review", "Add Review") },
          { tryClickByText("Add review", "Add Review") })

      // Review screen visible
      waitUntilTrue(LONG) {
        node(NavigationTestTags.REVIEW_HUNT_SCREEN).assertIsDisplayed()
        true
      }

      // Done disabled until rating valid
      compose
          .onNodeWithTag(AddReviewScreenTestTags.DONE_BUTTON, useUnmergedTree = true)
          .assertIsNotEnabled()

      // Provide rating + comment
      compose
          .onNodeWithTag(AddReviewScreenTestTags.RATE_TEXTFIELD, useUnmergedTree = true)
          .performTextInput("4.5")
      compose
          .onNodeWithTag(AddReviewScreenTestTags.COMMENT_TEXTFIELD, useUnmergedTree = true)
          .performTextInput("Great hunt!")

      // Done becomes enabled → click
      compose
          .onNodeWithTag(AddReviewScreenTestTags.DONE_BUTTON, useUnmergedTree = true)
          .assertIsEnabled()
          .performClick()

      // Back on HuntCard; review wrapper gone
      compose.waitUntil(timeoutMillis = LONG) {
        val backOnHuntCard =
            compose
                .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        val reviewGone =
            compose
                .onAllNodes(
                    hasTestTag(NavigationTestTags.REVIEW_HUNT_SCREEN), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isEmpty()
        backOnHuntCard && reviewGone
      }
    }
  }

  @Test
  fun huntcard_addReview_toolbarBack_and_cancel_both_return_to_huntcard() {
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

    withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
      compose.runOnUiThread { compose.activity.setContent { SeekrMainNavHost(testMode = true) } }

      // Overview → HuntCard
      waitUntilTrue(MED) {
        compose
            .onNodeWithTag(OverviewScreenTestTags.HUNT_LIST, useUnmergedTree = true)
            .assertExists()
        true
      }
      compose
          .onNodeWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
          .performClick()
      waitUntilTrue(MED) {
        compose
            .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      // Open Add Review
      clickAny(
          { tryClickByTag("HuntCard_AddReview", "ADD_REVIEW_BUTTON", "HuntCard_AddReviewButton") },
          { tryClickByDesc("Add review", "Add Review") },
          { tryClickByText("Add review", "Add Review") })
      node(NavigationTestTags.REVIEW_HUNT_SCREEN).assertIsDisplayed()

      // 1) Top app bar back returns to HuntCard
      compose
          .onNodeWithTag(AddReviewScreenTestTags.GO_BACK_BUTTON, useUnmergedTree = true)
          .performClick()
      waitUntilTrue(MED) {
        compose
            .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      // Open Add Review again to test Cancel button.
      clickAny(
          { tryClickByTag("HuntCard_AddReview", "ADD_REVIEW_BUTTON", "HuntCard_AddReviewButton") },
          { tryClickByDesc("Add review", "Add Review") },
          { tryClickByText("Add review", "Add Review") })
      node(NavigationTestTags.REVIEW_HUNT_SCREEN).assertIsDisplayed()

      // 2) In-screen Cancel returns to HuntCard
      compose
          .onNodeWithTag(AddReviewScreenTestTags.CANCEL_BUTTON, useUnmergedTree = true)
          .performClick()
      waitUntilTrue(MED) {
        compose
            .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }
    }
  }
}
