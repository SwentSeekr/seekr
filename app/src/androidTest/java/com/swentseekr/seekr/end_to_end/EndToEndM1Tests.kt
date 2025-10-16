package com.swentseekr.seekr.end_to_end

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.ui.addhunt.AddHuntScreenTestTags
import com.swentseekr.seekr.ui.map.MapScreenTestTags
import com.swentseekr.seekr.ui.navigation.NavigationTestTags
import com.swentseekr.seekr.ui.navigation.SeekrMainNavHost
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags
import com.swentseekr.seekr.ui.profile.ProfileTestTags
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end test of the main Seekr navigation flow.
 *
 * This test does NOT depend on sign-in; it simply verifies that navigation between Overview → Map →
 * Profile → Add Hunt works as expected.
 */
@RunWith(AndroidJUnit4::class)
class EndToEndM1Tests {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
  }


  @Test
  fun navigateBetweenScreens_andVerifyUIElements() {

    // launch the main navigation host
    composeTestRule.setContent { SeekrMainNavHost() }

    // --- Overview screen should be visible by default ---
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.OVERVIEW_TAB).assertIsDisplayed()
    // Optionally check an Overview-specific element if you have one (e.g. a list)
    // composeTestRule.onNodeWithTag("OVERVIEW_LIST").assertIsDisplayed()

    // --- Navigate to Map ---
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_SCREEN).assertIsDisplayed()

    // --- Navigate to Profile ---
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_SCREEN).assertIsDisplayed()

    // --- From Profile to Add Hunt ---
    // If your ProfileScreen has a button with tag "ADD_HUNT_BUTTON"
    composeTestRule.onNodeWithTag(ProfileTestTags.ADD_HUNT).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(NavigationTestTags.ADD_HUNT_SCREEN).assertIsDisplayed()

    // --- Fill out and submit the Add Hunt form ---
    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.INPUT_HUNT_TITLE)
      .performTextInput("E2E Test Hunt")
    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.INPUT_HUNT_DESCRIPTION)
      .performTextInput("End-to-end test description")
    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.INPUT_HUNT_TIME)
      .performTextInput("1.0")
    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.INPUT_HUNT_DISTANCE)
      .performTextInput("2.0")

    // Select status
    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.DROPDOWN_STATUS).performClick()
    composeTestRule.onAllNodes(hasText("FUN")).onFirst().performClick()

    // Select difficulty
    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.DROPDOWN_DIFFICULTY).performClick()
    composeTestRule.onAllNodes(hasText("EASY")).onFirst().performClick()

    // For this test, skip point selection — assume points are optional or mocked
    // If required: simulate “Select Locations” click
    // composeTestRule.onNodeWithTag(AddHuntScreenTestTags.BUTTON_SELECT_LOCATION).performClick()
    // Simulate returning to form, etc.

    // ✅ Save Hunt
    composeTestRule.onNodeWithTag(AddHuntScreenTestTags.HUNT_SAVE).performClick()
    composeTestRule.waitForIdle()

    // ✅ Expect redirect back to Overview screen
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN).fetchSemanticsNodes()
        .isNotEmpty()
    }

    // ✅ Verify Hunt list shows the newly added Hunt
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.HUNT_LIST).assertIsDisplayed()
    composeTestRule.onAllNodes(hasText("E2E Test Hunt")).onFirst().assertIsDisplayed()
  }
}

