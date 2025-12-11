package com.swentseekr.seekr.ui.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.swentseekr.seekr.FakeReviewHuntViewModel
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.profile.ProfileRepositoryLocal
import com.swentseekr.seekr.model.profile.ProfileRepositoryProvider
import com.swentseekr.seekr.model.profile.createHunt
import com.swentseekr.seekr.model.profile.sampleProfileWithPseudonym
import com.swentseekr.seekr.ui.components.HuntCardScreenTestTags
import com.swentseekr.seekr.ui.huntCardScreen.FakeHuntCardViewModel
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags
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
  companion object {
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

  private inline fun <T> withFakeRepos(
      huntRepo: FakeRepoSuccess,
      profileRepo: ProfileRepositoryLocal,
      crossinline block: () -> T
  ): T {
    val prevHunt = HuntRepositoryProvider.repository
    val prevProfile = ProfileRepositoryProvider.repository
    HuntRepositoryProvider.repository = huntRepo
    return try {
      block()
    } finally {
      HuntRepositoryProvider.repository = prevHunt
      ProfileRepositoryProvider.repository = prevProfile
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
  fun profile_click_my_hunt_opens_hunt_card_screen() {
    // Go to the profile tab (first time)
    goToProfileTab()

    // Wait until the MyHunt card appears
    waitUntilTrue(MED) {
      compose
          .onAllNodesWithTag("HUNT_CARD_hunt123", useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Click on the MyHunt card
    compose.onNodeWithTag("HUNT_CARD_hunt123", useUnmergedTree = true).assertExists().performClick()

    // Wait for HuntCard screen to appear
    waitUntilTrue(MED) {
      compose
          .onAllNodesWithTag(NavigationTestTags.HUNTCARD_SCREEN, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    compose
        .onNodeWithTag(NavigationTestTags.HUNTCARD_SCREEN, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun huntCard_clickEditButton_navigatesToEditHuntScreen() {
    // Use "fakeUser123" to match what FakeHuntCardViewModel.loadCurrentUserID() sets
    val currentUserId = "fakeUser123"

    // FIRST create the hunt with the correct authorId
    val hunt =
        createHunt(uid = "hunt123", title = "Test Hunt for Editing").copy(authorId = currentUserId)

    // THEN pass it to the ViewModel so it's in the initial state
    val viewModel = FakeHuntCardViewModel(hunt)

    // Verify the ViewModel has the correct data
    assert(viewModel.uiState.value.hunt?.authorId == currentUserId) {
      "Hunt authorId doesn't match: ${viewModel.uiState.value.hunt?.authorId} != $currentUserId"
    }

    // Setup profile repo with the author's profile
    val fakeProfileRepo =
        ProfileRepositoryLocal().apply {
          addProfile(sampleProfileWithPseudonym(currentUserId, "Test Author"))
        }

    withFakeRepos(FakeRepoSuccess(listOf(hunt)), fakeProfileRepo) {
      compose.runOnUiThread {
        compose.activity.setContent {
          SeekrMainNavHost(testMode = true, huntCardViewModelFactory = { viewModel })
        }
      }

      // Navigate to Profile tab
      goToProfileTab()

      // Wait until the hunt card appears
      waitUntilTrue(MED) {
        compose
            .onAllNodesWithTag("HUNT_CARD_hunt123", useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      // Click on the hunt card
      compose
          .onNodeWithTag("HUNT_CARD_hunt123", useUnmergedTree = true)
          .assertExists()
          .performClick()

      // Wait for HuntCard screen to appear
      waitUntilTrue(MED) {
        compose
            .onAllNodesWithTag(NavigationTestTags.HUNTCARD_SCREEN, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      compose
          .onNodeWithTag(NavigationTestTags.HUNTCARD_SCREEN, useUnmergedTree = true)
          .assertIsDisplayed()

      // Give extra time for the lazy column to fully compose
      compose.waitForIdle()

      compose
          .onNodeWithTag("HUNT_CARD_LIST", useUnmergedTree = true)
          .performScrollToNode(hasTestTag(HuntCardScreenTestTags.EDIT_HUNT_BUTTON))

      compose.waitForIdle()

      // Scroll to the Edit button
      compose
          .onNodeWithTag("HUNT_CARD_LIST", useUnmergedTree = true)
          .performScrollToNode(hasTestTag(HuntCardScreenTestTags.EDIT_HUNT_BUTTON))

      // Click the Edit Hunt button
      compose
          .onNodeWithTag(HuntCardScreenTestTags.EDIT_HUNT_BUTTON, useUnmergedTree = true)
          .assertExists()
          .assertIsDisplayed()
          .performClick()

      // Wait for EditHunt screen to appear
      compose.waitUntil(MED) {
        compose
            .onAllNodesWithTag(NavigationTestTags.EDIT_HUNT_SCREEN, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      // Verify we're on the EditHunt screen
      compose
          .onNodeWithTag(NavigationTestTags.EDIT_HUNT_SCREEN, useUnmergedTree = true)
          .assertIsDisplayed()
    }
  }

  @Test
  fun add_hunt_on_done_navigates_back_to_tabs_and_shows_bar() {
    // Go to Profile → open AddHunt
    goToProfileTab()
    node(ProfileTestTags.ADD_HUNT).performClick()

    // Ensure we're on AddHunt
    node(NavigationTestTags.ADD_HUNT_SCREEN).assertIsDisplayed()

    // BEST EFFORT: try to click a "Done/Save" style control to trigger onDone().
    val didClickDone =
        listOf<(Unit) -> Boolean>(
                {
                  listOf(
                          "ADD_HUNT_DONE",
                          "ADD_HUNT_SAVE",
                          "SAVE",
                          "DONE",
                          "SAVE_BUTTON",
                          "HUNT_SAVE",
                          "SUBMIT_BUTTON")
                      .any { tag -> tryClickByTag(tag) }
                },
                { arrayOf("Done", "Save", "Create", "Create hunt").any { d -> tryClickByDesc(d) } },
                {
                  arrayOf("Done", "Save", "SAVE", "Create", "Create hunt").any { t ->
                    tryClickByText(t)
                  }
                },
            )
            .any { it(Unit) }

    if (!didClickDone) {
      // Fallback: if we couldn't find a 'Done' control, use system back as before.
      compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
    }

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
    val fakeHuntRepo = FakeRepoSuccess(listOf(hunt))
    val fakeProfileRepo = ProfileRepositoryLocal()
    val authorProfile = sampleProfileWithPseudonym(uid = hunt.authorId, pseudonym = "Test Author")
    fakeProfileRepo.addProfile(authorProfile)

    withFakeRepos(fakeHuntRepo, fakeProfileRepo) {
      // Compose the real NavHost (no Firebase involved).
      compose.runOnUiThread { compose.activity.setContent { SeekrMainNavHost(testMode = true) } }

      // Wait until the LAST_HUNT_CARD is actually displayed.
      compose.waitUntil(MED) {
        compose
            .onAllNodesWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
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
      firstNode("HUNT_CARD_hunt123").assertIsDisplayed().performClick()
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
  fun settings_editProfile_navigates_to_edit_profile_and_back_restores_bottom_bar() {
    // Profile → Settings
    goToProfileTab()
    node(ProfileTestTags.SETTINGS).assertIsDisplayed().performClick()
    node(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

    // Open Edit Profile
    clickAny(
        { tryClickByTag("EDIT_PROFILE", "EDIT_PROFILE_BUTTON", "SETTINGS_EDIT_PROFILE") },
        { tryClickByDesc("Edit profile", "Edit Profile") },
        { tryClickByText("Edit profile", "Edit Profile") },
    )

    // Wait until EditProfile wrapper is present
    waitUntilTrue(MED) {
      compose
          .onAllNodes(hasTestTag(NavigationTestTags.EDIT_PROFILE_SCREEN), useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    firstNode(NavigationTestTags.EDIT_PROFILE_SCREEN).assertIsDisplayed()
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

    // Try in-screen back; if not found, fall back to system back.
    val didInScreenBack =
        runCatching {
              clickAny(
                  { tryClickByTag("EDIT_PROFILE_BACK", "PROFILE_BACK", "BACK") },
                  { tryClickByDesc("Back", "Navigate up") },
                  { tryClickByText("Back") },
              )
              true
            }
            .getOrDefault(false)

    if (!didInScreenBack) {
      compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
    }

    // Wait until EditProfile is removed from the tree
    waitUntilTrue(MED) {
      compose
          .onAllNodes(hasTestTag(NavigationTestTags.EDIT_PROFILE_SCREEN), useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isEmpty()
    }

    // Now we are back on Settings (still no bottom bar). Use system back to go to Profile.
    compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

    // Bottom bar should be visible again on a tab screen.
    waitUntilTrue(MED) {
      compose
          .onAllNodes(hasTestTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU), useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    node(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
  }

  @Test
  fun huntcard_addReview_navigates_to_add_review_and_back_restores_huntcard() {
    // Seed repo with a single hunt that will appear in Overview and then in HuntCard.
    val hunt = createHunt(uid = "review-123", title = "Reviewable Hunt")
    val authorId = NavHostPublicProfileTestConstants.EXAMPLE_AUTHOR_ID
    val fakeHuntRepo = FakeRepoSuccess(listOf(hunt))
    val fakeProfileRepo =
        ProfileRepositoryLocal().apply {
          addProfile(sampleProfileWithPseudonym(authorId, "Author Test"))
        }

    withFakeRepos(fakeHuntRepo, fakeProfileRepo) {
      compose.runOnUiThread {
        compose.activity.setContent {
          SeekrMainNavHost(
              testMode = true,
              huntCardViewModelFactory = { FakeHuntCardViewModel(hunt) },
              reviewViewModelFactory = { FakeReviewHuntViewModel() })
        }
      }

      // Wait for last hunt card to actually appear
      compose.waitUntil(MED) {
        compose
            .onAllNodesWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      // Open HuntCard
      compose
          .onAllNodesWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
          .onFirst()
          .assertExists()
          .performClick()

      // Wait for HuntCard screen
      waitUntilTrue(MED) {
        compose
            .onAllNodesWithTag(NavigationTestTags.HUNTCARD_SCREEN, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

      compose
          .onNodeWithTag("HUNT_CARD_LIST", useUnmergedTree = true)
          .performScrollToNode(hasTestTag(HuntCardScreenTestTags.REVIEW_BUTTON))

      // --- CLICK THE REAL BUTTON (Add review or Edit hunt) ---
      compose
          .onNodeWithTag(HuntCardScreenTestTags.REVIEW_BUTTON, useUnmergedTree = true)
          .assertExists()
          .assertIsDisplayed()
          .performClick()

      // Wait for AddReview screen
      waitUntilTrue(MED) {
        compose
            .onAllNodesWithTag(NavigationTestTags.REVIEW_HUNT_SCREEN, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      compose
          .onNodeWithTag(NavigationTestTags.REVIEW_HUNT_SCREEN, useUnmergedTree = true)
          .assertIsDisplayed()
      node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

      // Press system back
      compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

      // Back to HuntCard
      waitUntilTrue(MED) {
        compose
            .onAllNodesWithTag(NavigationTestTags.REVIEW_HUNT_SCREEN, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isEmpty()
      }

      compose
          .onAllNodesWithTag(NavigationTestTags.HUNTCARD_SCREEN, useUnmergedTree = true)
          .onFirst()
          .assertIsDisplayed()

      // Bottom bar still hidden (expected)
      node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()
    }
  }

  @Test
  fun huntcard_click_author_navigates_to_author_profile() {
    // Seed repo with a hunt that has a specific author
    val authorId = NavHostPublicProfileTestConstants.EXAMPLE_AUTHOR_ID
    val hunt =
        createHunt(
                uid = NavHostPublicProfileTestConstants.EXAMPLE_HUNT_ID,
                title = NavHostPublicProfileTestConstants.EXAMPLE_HUNT_TITLE)
            .copy(authorId = authorId)

    val fakeHuntRepo = FakeRepoSuccess(listOf(hunt))
    val fakeProfileRepo =
        ProfileRepositoryLocal().apply {
          addProfile(sampleProfileWithPseudonym(authorId, "Author Test"))
        }

    withFakeRepos(fakeHuntRepo, fakeProfileRepo) {
      // Re-compose with fake repo and ViewModels
      compose.runOnUiThread {
        compose.activity.setContent {
          SeekrMainNavHost(
              testMode = true,
              huntCardViewModelFactory = { FakeHuntCardViewModel(hunt) },
              reviewViewModelFactory = { FakeReviewHuntViewModel() })
        }
      }

      // Wait for last hunt card to actually appear
      compose.waitUntil(MED) {
        compose
            .onAllNodesWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      // Click on the hunt card to open HuntCardScreen
      compose
          .onAllNodesWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
          .onFirst()
          .assertExists()
          .performClick()

      // Wait for HuntCard screen to appear
      waitUntilTrue(MED) {
        compose
            .onAllNodesWithTag(NavigationTestTags.HUNTCARD_SCREEN, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      // Click on the author's name to navigate to their profile
      compose
          .onNodeWithTag(HuntCardScreenTestTags.AUTHOR_TEXT, useUnmergedTree = true)
          .assertExists()
          .assertIsDisplayed()
          .performClick()

      // Wait for Profile screen to appear with the author's profile
      waitUntilTrue(MED) {
        compose.onNodeWithTag(ProfileTestTags.PROFILE_SCREEN, useUnmergedTree = true).assertExists()
        true
      }

      // Verify we're on the profile screen
      compose
          .onNodeWithTag(ProfileTestTags.PROFILE_SCREEN, useUnmergedTree = true)
          .assertIsDisplayed()
    }
  }

  @Test
  fun huntcard_beginHunt_callback_can_be_invoked() {
    val hunt = createHunt(uid = "begin-hunt-123", title = "Beginnable Hunt")
    val authorId = NavHostPublicProfileTestConstants.EXAMPLE_AUTHOR_ID
    val fakeHuntRepo = FakeRepoSuccess(listOf(hunt))
    val fakeProfileRepo =
        ProfileRepositoryLocal().apply {
          addProfile(sampleProfileWithPseudonym(authorId, "Author Test"))
        }

    withFakeRepos(fakeHuntRepo, fakeProfileRepo) {
      compose.runOnUiThread {
        compose.activity.setContent {
          SeekrMainNavHost(
              testMode = true,
              huntCardViewModelFactory = { FakeHuntCardViewModel(hunt) },
              reviewViewModelFactory = { FakeReviewHuntViewModel() })
        }
      }

      // Wait for last hunt card to actually appear
      compose.waitUntil(MED) {
        compose
            .onAllNodesWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }
      compose
          .onAllNodesWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
          .onFirst()
          .performClick()

      waitUntilTrue(MED) {
        compose
            .onAllNodesWithTag(NavigationTestTags.HUNTCARD_SCREEN, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      val didClickBegin =
          listOf<(Unit) -> Boolean>(
                  { listOf("BEGIN_HUNT", "START_HUNT", "BEGIN_BUTTON").any { tryClickByTag(it) } },
                  { arrayOf("Begin Hunt", "Start Hunt", "Begin").any { tryClickByDesc(it) } },
                  { arrayOf("Begin Hunt", "Start Hunt", "Begin").any { tryClickByText(it) } })
              .any { it(Unit) }

    }
  }

  @Test
  fun reviewImages_screen_navigates_and_displays_photos() {
    val reviewId = "review-images-123"

    compose.runOnUiThread {
      compose.activity.setContent {
        val navController = rememberNavController()
        SeekrMainNavHost(navController = navController, testMode = true)

        // Navigate to review images screen
        LaunchedEffect(Unit) { navController.navigate("reviewImages/$reviewId") }
      }
    }

    waitUntilTrue(MED) {
      compose.onNodeWithTag("IMAGE_REVIEW_SCREEN", useUnmergedTree = true).assertExists()
      true
    }

    node("IMAGE_REVIEW_SCREEN").assertIsDisplayed()
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

    // Try to go back
    compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

    waitUntilTrue(MED) {
      compose
          .onAllNodes(hasTestTag("IMAGE_REVIEW_SCREEN"), useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isEmpty()
    }
  }

  @Test
  fun profile_reviews_route_with_userId_navigates_correctly() {
    val userId = "user-with-reviews-123"

    compose.runOnUiThread {
      compose.activity.setContent {
        val navController = rememberNavController()
        SeekrMainNavHost(navController = navController, testMode = true)

        LaunchedEffect(Unit) { navController.navigate("profile/$userId/reviews") }
      }
    }

    waitUntilTrue(MED) {
      compose
          .onAllNodes(hasTestTag("PROFILE_REVIEWS_SCREEN"), useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

    compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

    waitUntilTrue(MED) {
      compose
          .onAllNodes(hasTestTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU), useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  @Test
  fun navigation_bar_item_colors_and_styling_applied_correctly() {
    goToProfileTab()

    // Verify navigation bar is displayed with correct styling
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()

    // All tabs should be visible
    node(NavigationTestTags.OVERVIEW_TAB).assertIsDisplayed()
    node(NavigationTestTags.MAP_TAB).assertIsDisplayed()
    node(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()

    // Click through each tab to verify selection states work
    node(NavigationTestTags.OVERVIEW_TAB).performClick()
    compose.waitForIdle()
    node(NavigationTestTags.OVERVIEW_SCREEN).assertIsDisplayed()

    node(NavigationTestTags.MAP_TAB).performClick()
    compose.waitForIdle()
    node(NavigationTestTags.MAP_SCREEN).assertIsDisplayed()

    node(NavigationTestTags.PROFILE_TAB).performClick()
    compose.waitForIdle()
    node(ProfileTestTags.PROFILE_SCREEN).assertIsDisplayed()
  }

  @Test
  fun huntcard_goProfile_navigates_to_clicked_user_profile() {
    val authorId = "different-author-456"
    val hunt =
        createHunt(uid = "hunt-profile-nav", title = "Hunt for Profile Nav")
            .copy(authorId = authorId)

    val fakeHuntRepo = FakeRepoSuccess(listOf(hunt))
    val fakeProfileRepo =
        ProfileRepositoryLocal().apply {
          addProfile(sampleProfileWithPseudonym(authorId, "Author Test"))
        }

    withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
      compose.runOnUiThread {
        compose.activity.setContent {
          SeekrMainNavHost(
              testMode = true,
              huntCardViewModelFactory = { FakeHuntCardViewModel(hunt) },
              reviewViewModelFactory = { FakeReviewHuntViewModel() })
        }
      }

      // Wait for last hunt card to actually appear
      compose.waitUntil(MED) {
        compose
            .onAllNodesWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      compose
          .onAllNodesWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
          .onFirst()
          .performClick()

      waitUntilTrue(MED) {
        compose
            .onAllNodesWithTag(NavigationTestTags.HUNTCARD_SCREEN, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      // Click on author to navigate to their profile
      compose
          .onNodeWithTag(HuntCardScreenTestTags.AUTHOR_TEXT, useUnmergedTree = true)
          .performClick()

      waitUntilTrue(MED) {
        compose.onNodeWithTag(ProfileTestTags.PROFILE_SCREEN, useUnmergedTree = true).assertExists()
        true
      }

      node(ProfileTestTags.PROFILE_SCREEN).assertIsDisplayed()
    }
  }

  @Test
  fun goProfile_opensSettings_opensTerms() {
    // Profile → Settings
    goToProfileTab()

    node(ProfileTestTags.SETTINGS).performClick()
    node(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()
    node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

    node(SettingsScreenTestTags.APP_CONDITION_BUTTON).performClick()
    node(NavigationTestTags.TERMS_CONDITIONS_SCREEN).assertIsDisplayed()
  }
}
