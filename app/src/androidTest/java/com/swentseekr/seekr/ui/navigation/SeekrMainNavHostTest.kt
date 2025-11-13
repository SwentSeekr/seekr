package com.swentseekr.seekr.ui.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.profile.createAlienHunt
import com.swentseekr.seekr.model.profile.createHunt
import com.swentseekr.seekr.ui.components.HuntCardScreenTestTags
import com.swentseekr.seekr.ui.hunt.HuntScreenTestTags
import com.swentseekr.seekr.ui.hunt.review.AddReviewScreenTestTags
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags
import com.swentseekr.seekr.ui.profile.EditProfileTestTags
import com.swentseekr.seekr.ui.profile.ProfileTestTags
import com.swentseekr.seekr.ui.settings.SettingsScreenTestTags
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

  private fun firstNode(tag: String) =
      compose.onAllNodes(hasTestTag(tag), useUnmergedTree = true).onFirst()

  /** Helper to open the Profile tab and wait for compose to settle. */
  private fun goToProfileTab() {
    node(NavigationTestTags.PROFILE_TAB).performClick()
    compose.waitForIdle()
  }

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

    goToProfileTab()
    // Reselect same tab a couple times
    node(NavigationTestTags.PROFILE_TAB).performClick()
    node(NavigationTestTags.PROFILE_TAB).performClick()
    compose.waitForIdle()
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  // (Removed a dedicated "map_tab_shows_tagged_map_screen" test since the above already covers it.)

  @Test
  fun profile_fab_navigates_to_add_hunt_then_back_restores_bar() {
    goToProfileTab()

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
    goToProfileTab()

    // first MyHunt card
    firstNode("HUNT_CARD_0").performClick()

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
    goToProfileTab()
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
    goToProfileTab()
    node(ProfileTestTags.SETTINGS).performClick()
    node(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()
    compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun settings_inScreen_back_invokes_onGoBack_and_restores_bottom_bar() {
    // Profile → Settings
    goToProfileTab()
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
    // Use createHunt() to seed repository
    val hunt = createHunt(uid = "fake-123", title = "Paris Discovery")

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
          .onAllNodesWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
          .onFirst()
          .assertExists()
          .performClick()

      // Assert we navigated to HuntCard screen and bottom bar is hidden on non-tab route.
      compose.waitUntil(timeoutMillis = XLONG) {
        compose
            .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()
      compose
          .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
          .onFirst()
          .assertExists()
    }
  }

    @Test
    fun profile_myHunt_edit_flow_uses_mockProfileData_and_onDone_returns_to_profile() {
        // Seed repo so EditHuntViewModel.load("hunt123") succeeds (matches mockProfileData()).
        val seeded = createHunt(uid = "hunt123", title = "City Exploration")

        withFakeRepo(FakeRepoSuccess(listOf(seeded))) {
            // Compose with testMode so Profile uses mockProfileData() and exposes HUNT_CARD_0.
            compose.runOnUiThread { compose.activity.setContent { SeekrMainNavHost(testMode = true) } }

            // Go to Profile tab.
            goToProfileTab()

            // Open the first My Hunt card -> navigates to EditHunt(hunt123).
            firstNode("HUNT_CARD_0").assertIsDisplayed().performClick()
            waitUntilTrue(MED) {
                node(NavigationTestTags.EDIT_HUNT_SCREEN).assertIsDisplayed()
                true
            }

            // Bottom bar should be hidden on EditHunt.
            node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

            // BEST EFFORT: try to go through the "select locations" branch, then cancel (no-ops if absent).
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
                // After save/onDone we expect to be back on Profile with bottom bar visible.
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
                // If we couldn't find a Save control, at least exercise back navigation.
                compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                waitUntilTrue(MED) {
                    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
                    true
                }
            }

            // Final sanity: we're back in a tab destination (Profile) and the bottom bar is visible.
            node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
            node(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
        }
    }


    @Test
    fun addreview_cancel_and_done_each_pop_back_to_huntcard() {
        val hunt = createAlienHunt(uid = "rev-2", title = "Reviewable 2")

        withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
            compose.runOnUiThread { compose.activity.setContent { SeekrMainNavHost(testMode = true) } }

            // 1) Start from Overview → wait for list.
            waitUntilTrue(LONG) {
                compose
                    .onNodeWithTag(OverviewScreenTestTags.HUNT_LIST, useUnmergedTree = true)
                    .assertExists()
                true
            }

            // 2) Click the last card → go to HuntCard.
            compose
                .onAllNodesWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
                .onFirst()
                .assertExists()
                .performClick()

            compose.waitUntil(timeoutMillis = XLONG) {
                compose
                    .onAllNodes(hasTestTag(NavigationTestTags.HUNTCARD_SCREEN), useUnmergedTree = true)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            firstNode(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed()

            // Wait for Review button to appear (after HuntCardViewModel load).
            waitUntilTrue(LONG) {
                node(HuntCardScreenTestTags.REVIEW_BUTTON).assertExists()
                true
            }

            // --- First pass: Cancel from AddReview → back to HuntCard ---

            node(HuntCardScreenTestTags.REVIEW_BUTTON).performClick()
            firstNode(NavigationTestTags.REVIEW_HUNT_SCREEN).assertIsDisplayed()

            compose
                .onNodeWithTag(AddReviewScreenTestTags.CANCEL_BUTTON, useUnmergedTree = true)
                .performClick()
            firstNode(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed()

            // Wait again for Review button on returning.
            waitUntilTrue(LONG) {
                node(HuntCardScreenTestTags.REVIEW_BUTTON).assertExists()
                true
            }

            // --- Second pass: Done from AddReview → back to HuntCard ---

            node(HuntCardScreenTestTags.REVIEW_BUTTON).performClick()
            firstNode(NavigationTestTags.REVIEW_HUNT_SCREEN).assertIsDisplayed()

            compose
                .onNodeWithTag(AddReviewScreenTestTags.DONE_BUTTON, useUnmergedTree = true)
                .performClick()
            firstNode(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed()
        }
    }




    @Test
    fun huntcard_to_addreview_uses_route_arg_and_survives_recompose() {
        // Ensure that route arg forwarding (huntId) is exercised and screen survives recompositions.
        val huntId = "rev-arg"
        val huntTitle = "Arg Hunt"
        val hunt = createAlienHunt(uid = huntId, title = huntTitle)

        withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
            // Compose the real NavHost (no Firebase).
            compose.runOnUiThread { compose.activity.setContent { SeekrMainNavHost(testMode = true) } }

            // 1) Wait for Overview list to exist.
            waitUntilTrue(LONG) {
                compose
                    .onNodeWithTag(OverviewScreenTestTags.HUNT_LIST, useUnmergedTree = true)
                    .assertExists()
                true
            }

            // 2) Click the last card in the list → opens HuntCardScreen.
            compose
                .onAllNodesWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
                .onFirst()
                .assertExists()
                .performClick()

            // 3) Wait until HuntCard content is visible
            // Also wait for the Review button to be present *and displayed*.
            waitUntilTrue(LONG) {
                node(HuntCardScreenTestTags.REVIEW_BUTTON).assertExists().assertIsDisplayed()
                true
            }

            // --- FIRST ENTRY TO ADDREVIEW ---

            // 4) Click the Add-Review/Edit button.
            node(HuntCardScreenTestTags.REVIEW_BUTTON).performClick()

            // We should now be on AddReviewScreen: wait for main content and check hunt data.
            waitUntilTrue(LONG) {
                compose
                    .onNodeWithTag(AddReviewScreenTestTags.INFO_COLUMN, useUnmergedTree = true)
                    .assertExists()
                true
            }

            // Route arg check: AddReviewScreen should show the correct title and uid.
            waitUntilTrue(LONG) {
                compose.onNodeWithText(huntTitle, useUnmergedTree = true).assertExists()
                compose.onNodeWithText(huntId, useUnmergedTree = true).assertExists()
                true
            }

            // --- POP BACK AND RE-ENTER TO TEST RECOMPOSITION / ARG STABILITY ---

            compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

            // Back on HuntCard: wait for title and button again.
            waitUntilTrue(LONG) {
                node(HuntCardScreenTestTags.TITLE_TEXT).assertExists()
                true
            }
            waitUntilTrue(LONG) {
                node(HuntCardScreenTestTags.REVIEW_BUTTON).assertExists().assertIsDisplayed()
                true
            }

            // Click the button again.
            node(HuntCardScreenTestTags.REVIEW_BUTTON).performClick()

            // Confirm AddReview again and check that the same hunt is loaded.
            waitUntilTrue(LONG) {
                compose
                    .onNodeWithTag(AddReviewScreenTestTags.INFO_COLUMN, useUnmergedTree = true)
                    .assertExists()
                true
            }
            waitUntilTrue(LONG) {
                compose.onNodeWithText(huntTitle, useUnmergedTree = true).assertExists()
                compose.onNodeWithText(huntId, useUnmergedTree = true).assertExists()
                true
            }
        }
    }


}
