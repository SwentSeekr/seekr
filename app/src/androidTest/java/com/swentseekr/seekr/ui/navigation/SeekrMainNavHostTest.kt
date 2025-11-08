package com.swentseekr.seekr.ui.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
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

    // --- tiny click helpers to robustly fire in-screen actions (tag, content-desc, or text) ---
    private fun tryClickByTag(vararg tags: String): Boolean =
        tags.any { tag -> runCatching { node(tag).performClick(); true }.getOrNull() == true }

    private fun tryClickByDesc(vararg descs: String): Boolean =
        descs.any { d ->
            runCatching {
                compose.onNodeWithContentDescription(d, useUnmergedTree = true).performClick(); true
            }.getOrNull() == true
        }

    private fun tryClickByText(vararg texts: String): Boolean =
        texts.any { t ->
            runCatching {
                compose.onNodeWithText(t, useUnmergedTree = true).performClick(); true
            }.getOrNull() == true
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
            }.getOrNull() == true
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
            }.getOrNull() == true
        }
        node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()

        // system back
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.waitUntil(3_000) {
            runCatching {
                node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
                true
            }.getOrNull() == true
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
            }.getOrNull() == true
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
            val barVisible = runCatching {
                node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed(); true
            }.getOrNull() == true

            val addHuntGone = compose
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
    fun edit_hunt_inScreen_back_and_done_restore_correct_destinations() {
        // Open one of "My hunts" → EditHunt
        node(NavigationTestTags.PROFILE_TAB).performClick()
        compose.waitForIdle()
        node("HUNT_CARD_0").performClick()
        node(NavigationTestTags.EDIT_HUNT_SCREEN).assertIsDisplayed()

        // First: in-screen back (exercises EditHunt onGoBack)
        clickAny(
            { tryClickByTag("EDIT_HUNT_BACK", "EditHuntBack") },
            { tryClickByDesc("Back", "Navigate up") },
            { tryClickByText("Back") }
        )
        // We should be back on a tab (Profile) → bottom bar visible
        compose.waitUntil(3_000) {
            runCatching {
                node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed(); true
            }.getOrNull() == true
        }

        // Open EditHunt again to exercise onDone
        node("HUNT_CARD_0").performClick()
        node(NavigationTestTags.EDIT_HUNT_SCREEN).assertIsDisplayed()

        // Trigger in-screen 'Done/Save' (exercises EditHunt onDone → popUpTo(Profile))
        clickAny(
            { tryClickByTag("EDIT_HUNT_DONE", "EditHuntDone", "PRIMARY_ACTION") },
            { tryClickByDesc("Done", "Save") },
            { tryClickByText("Done", "Save") }
        )

        // Expect bottom bar visible again (back on Profile)
        compose.waitUntil(3_000) {
            runCatching {
                node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed(); true
            }.getOrNull() == true
        }
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
            { tryClickByText("Back") }
        )

        // Back to a tab destination → bottom bar visible
        node(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    }
}
