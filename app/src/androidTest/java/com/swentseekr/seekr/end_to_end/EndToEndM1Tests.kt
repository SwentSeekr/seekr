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
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.addhunt.AddHuntScreenTestTags
import com.swentseekr.seekr.ui.addhunt.AddHuntViewModel
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
 * Full E2E flow:
 * Overview → Profile → Add Hunt → Save → returns to Overview.
 *
 * This test skips sign-in and works entirely offline.
 */
@RunWith(AndroidJUnit4::class)
class EndToEndM1Tests {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setupFirebase() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
  }

  @Test
  fun addHunt_validFlow_navigatesBackToOverview() {
    // Step 1 - Launch the full navigation host
    composeTestRule.setContent { SeekrMainNavHost() }

    // Step 2 - Overview visible initially
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN).assertIsDisplayed()

    // Step 3 - Navigate to Profile tab
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_SCREEN).assertIsDisplayed()

    // Step 4 - Navigate to Add Hunt
    composeTestRule.onNodeWithTag(ProfileTestTags.ADD_HUNT).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.ADD_HUNT_SCREEN).assertIsDisplayed()

    // Step 5 - Fill required form fields
    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.INPUT_HUNT_TITLE)
      .performTextInput("E2E Test Hunt")
    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.INPUT_HUNT_DESCRIPTION)
      .performTextInput("This hunt is added during an end-to-end test.")
    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.INPUT_HUNT_TIME)
      .performTextInput("2.0")

    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.DROPDOWN_STATUS).performClick()
    composeTestRule.onAllNodes(hasText(HuntStatus.FUN.name)).onFirst().performClick()

    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.DROPDOWN_DIFFICULTY).performClick()
    composeTestRule.onAllNodes(hasText(Difficulty.EASY.name)).onFirst().performClick()

    // Step 6 - Inject two mock points directly into the ViewModel
    composeTestRule.activity.runOnUiThread {
      val addHuntViewModel = AddHuntViewModel()
      addHuntViewModel.setPoints(
        listOf(
          Location(0.0, 0.0, "Start Point"),
          Location(1.0, 1.0, "End Point")
        )
      )
      // Optionally: attach to current composition if needed
    }

    // Step 7 - Click Save Hunt
    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.HUNT_SAVE).performClick()
    composeTestRule.waitForIdle()

    // Step 8 - Wait until Overview screen reappears
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule.onAllNodesWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN)
        .fetchSemanticsNodes().isNotEmpty()
    }

    // Step 9 - Verify Overview visible again
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN).assertIsDisplayed()
  }
}
