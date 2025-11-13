package com.swentseekr.seekr.ui.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.profile.createHunt
import com.swentseekr.seekr.ui.components.HuntCardScreen
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

    /** Helper to open the Profile tab and wait for compose to settle. */
    private fun goToProfileTab() {
        node(NavigationTestTags.PROFILE_TAB).performClick()
        compose.waitForIdle()
    }

    private fun firstNode(tag: String) =
        compose.onAllNodes(hasTestTag(tag), useUnmergedTree = true).onFirst()


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

        // Tap in-screen back (by tag)
        compose.onNodeWithTag(SettingsScreenTestTags.BACK_BUTTON, useUnmergedTree = true).performClick()

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
                .onNodeWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
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
                .assertIsDisplayed()
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
            node("HUNT_CARD_0").assertIsDisplayed().performClick()
            waitUntilTrue(MED) {
                node(NavigationTestTags.EDIT_HUNT_SCREEN).assertIsDisplayed()
                true
            }

            // Bottom bar should be hidden on EditHunt.
            node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

            // Try to SAVE to trigger onDone() → back to Profile.
            compose.onNodeWithTag(HuntScreenTestTags.HUNT_SAVE, useUnmergedTree = true).performClick()

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
        }
    }


    @Test
    fun huntcard_add_review_button_navigates_to_addreview_and_bottom_bar_hidden_then_back() {
        val hunt = createHunt(uid = "rev-1", title = "Reviewable Hunt")

        withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
            compose.runOnUiThread { compose.activity.setContent { SeekrMainNavHost(testMode = true) } }

            // From Overview open a hunt to reach HuntCard
            waitUntilTrue(LONG) {
                compose.onNodeWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
                    .assertExists()
                true
            }
            compose.onNodeWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
                .performClick()

            // On HuntCard
            firstNode(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed()
            node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

            waitUntilTrue(LONG) {
                node(HuntCardScreenTestTags.REVIEW_BUTTON).assertIsDisplayed()
                true
            }
            node(HuntCardScreenTestTags.REVIEW_BUTTON).assertIsDisplayed().performClick()

            // AddReview visible
            waitUntilTrue(SHORT) {
                firstNode(NavigationTestTags.REVIEW_HUNT_SCREEN).assertIsDisplayed()
                true
            }
            node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

            // Back to HuntCard
            compose.onNodeWithTag(AddReviewScreenTestTags.GO_BACK_BUTTON, useUnmergedTree = true)
                .performClick()
            firstNode(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed()

        }
    }

    @Test
    fun addreview_cancel_and_done_each_pop_back_to_huntcard() {
        val hunt = createHunt(uid = "rev-2", title = "Reviewable 2")

        withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
            compose.runOnUiThread { compose.activity.setContent { SeekrMainNavHost(testMode = true) } }

            // Open HuntCard
            waitUntilTrue(MED) {
                compose.onNodeWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
                    .assertExists()
                true
            }
            compose.onNodeWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
                .performClick()
            // After opening card
            firstNode(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed()

// To AddReview
            node(HuntCardScreenTestTags.REVIEW_BUTTON).performClick()
            firstNode(NavigationTestTags.REVIEW_HUNT_SCREEN).assertIsDisplayed()

// Cancel → back to HuntCard
            compose.onNodeWithTag(AddReviewScreenTestTags.CANCEL_BUTTON, useUnmergedTree = true).performClick()
            firstNode(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed()

// Again to AddReview
            node(HuntCardScreenTestTags.REVIEW_BUTTON).performClick()
            firstNode(NavigationTestTags.REVIEW_HUNT_SCREEN).assertIsDisplayed()

// Done → back to HuntCard
            compose.onNodeWithTag(AddReviewScreenTestTags.DONE_BUTTON, useUnmergedTree = true).performClick()
            firstNode(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed()

        }
    }

    @Test
    fun settings_edit_profile_button_navigates_to_editprofile_cancel_returns_to_settings_then_back_to_tabs() {
        // Go to Settings
        goToProfileTab()
        node(ProfileTestTags.SETTINGS).performClick()
        node(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()
        node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

        // Click "Edit Profile" (new line in SeekrMainNavHost wiring)
        compose.onNodeWithTag(SettingsScreenTestTags.EDIT_PROFILE_BUTTON, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        // On EditProfile route now
        compose.onAllNodes(
            hasTestTag(NavigationTestTags.EDIT_PROFILE_SCREEN), useUnmergedTree = true
        ).onFirst().assertIsDisplayed()
        node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

        // Cancel inside EditProfile → popBack to Settings
        compose.onNodeWithTag(EditProfileTestTags.CANCEL_BUTTON, useUnmergedTree = true)
            .performClick()
        node(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()
        node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

        // Back (in-screen back button in Settings) → return to tabs, bottom bar visible
        compose.onNodeWithTag(SettingsScreenTestTags.BACK_BUTTON, useUnmergedTree = true)
            .performClick()
        node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    }

    @Test
    fun huntcard_to_addreview_uses_route_arg_and_survives_recompose() {
        // Ensure that route arg forwarding (huntId) is exercised and screen survives recompositions.
        val hunt = createHunt(uid = "rev-arg", title = "Arg Hunt")

        withFakeRepo(FakeRepoSuccess(listOf(hunt))) {
            compose.runOnUiThread { compose.activity.setContent { SeekrMainNavHost(testMode = true) } }

            // Navigate to HuntCard
            waitUntilTrue(MED) {
                compose.onNodeWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
                    .assertExists()
                true
            }
            compose.onNodeWithTag(OverviewScreenTestTags.LAST_HUNT_CARD, useUnmergedTree = true)
                .performClick()
            firstNode(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed()

// To AddReview
            node(HuntCardScreenTestTags.REVIEW_BUTTON).performClick()
            firstNode(NavigationTestTags.REVIEW_HUNT_SCREEN).assertIsDisplayed()

// Pop and re-enter
            compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            firstNode(NavigationTestTags.HUNTCARD_SCREEN).assertIsDisplayed()
            node(HuntCardScreenTestTags.REVIEW_BUTTON).performClick()
            firstNode(NavigationTestTags.REVIEW_HUNT_SCREEN).assertIsDisplayed()

        }
    }
}
