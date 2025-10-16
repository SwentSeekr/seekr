package com.swentseekr.seekr.end_to_end

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.ui.addhunt.AddHuntScreenTestTags
import com.swentseekr.seekr.ui.addhunt.AddPointsMapScreenTestTags
import com.swentseekr.seekr.ui.navigation.NavigationTestTags
import com.swentseekr.seekr.ui.navigation.SeekrMainNavHost
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags
import com.swentseekr.seekr.ui.profile.ProfileTestTags
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 🚀 Full End-to-End UI flow: Overview → Profile → Add Hunt → Select Points → Save → Overview
 *
 * This test:
 * - Runs the entire navigation flow without requiring Firebase authentication.
 * - Uses `testMode = true` to skip network / Firebase calls.
 * - Verifies that navigation, form input, and UI state transitions work correctly.
 */
@RunWith(AndroidJUnit4::class)
class EndToEndM1Tests {

  // Compose test rule: launches a real activity and manages the Compose lifecycle.
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  /**
   * Setup step run before each test. Initializes Firebase only if it hasn’t been initialized
   * already. (Prevents crashes when ViewModels internally reference Firebase.)
   */
  @Before
  fun setupFirebase() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
  }

  @Test
  fun addHunt_validFlow_selectsPointsOnMap_andNavigatesBackToOverview() {
    // Step 1 – Launch the full navigation host in test mode (bypasses Firebase & repo)
    composeTestRule.setContent { SeekrMainNavHost(testMode = true) }

    // Step 2 – Verify that the Overview screen is visible by default
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN).assertIsDisplayed()

    // Step 3 – Navigate to the Profile tab in the bottom navigation bar
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_SCREEN).assertIsDisplayed()

    // Step 4 – Click on the "Add Hunt" button in the Profile screen
    composeTestRule.onNodeWithTag(ProfileTestTags.ADD_HUNT).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.ADD_HUNT_SCREEN).assertIsDisplayed()

    // Step 5 – Fill in the required Add Hunt form fields
    composeTestRule
        .onNodeWithTag(AddHuntScreenTestTags.INPUT_HUNT_TITLE)
        .performTextInput("E2E Test Hunt")

    composeTestRule
        .onNodeWithTag(AddHuntScreenTestTags.INPUT_HUNT_DESCRIPTION)
        .performTextInput("This hunt is added during an end-to-end test.")

    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.INPUT_HUNT_TIME).performTextInput("2.0")

    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.INPUT_HUNT_DISTANCE).performTextInput("5.5")

    // Select hunt status and difficulty
    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.DROPDOWN_STATUS).performClick()
    composeTestRule.onAllNodes(hasText(HuntStatus.FUN.name)).onFirst().performClick()

    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.DROPDOWN_DIFFICULTY).performClick()
    composeTestRule.onAllNodes(hasText(Difficulty.EASY.name)).onFirst().performClick()

    // Step 6 – Click the “Select Locations” button to open the map screen
    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.BUTTON_SELECT_LOCATION).performClick()

    // Wait until the map screen is displayed
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithTag(AddPointsMapScreenTestTags.MAP_VIEW)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Step 7 – In test mode, points are auto-generated → just confirm
    composeTestRule
        .onNodeWithTag(AddPointsMapScreenTestTags.CONFIRM_BUTTON)
        .assertIsEnabled()
        .performClick()

    composeTestRule.waitForIdle()

    // Step 8 – Click “Save Hunt”
    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.HUNT_SAVE).performClick()

    // Step 9 – Wait until the Overview screen becomes visible again
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
          .onAllNodesWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Step 10 – Verify that the Overview screen is indeed visible
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN).assertIsDisplayed()
  }
}
