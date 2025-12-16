package com.swentseekr.seekr.ui.offline

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.model.profile.createHunt
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags
import com.swentseekr.seekr.utils.HuntTestConstants.HUNT_UID_1
import com.swentseekr.seekr.utils.HuntTestConstants.HUNT_UID_2
import com.swentseekr.seekr.utils.HuntTestConstants.TITLE_1
import com.swentseekr.seekr.utils.HuntTestConstants.TITLE_2
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the OfflineOverviewHuntsScreen composable.
 *
 * This test suite verifies that the screen correctly renders a list of hunts and handles user
 * interactions such as clicking on hunt items.
 */
@RunWith(AndroidJUnit4::class)
class OfflineOverviewHuntsScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun offlineOverviewHuntsScreen_rendersAndHandlesHuntClick_forNonEmptyList() {
    // Build a small list of hunts using the existing factory.
    val hunts =
        listOf(
            createHunt(uid = HUNT_UID_1, title = TITLE_1),
            createHunt(uid = HUNT_UID_2, title = TITLE_2))

    var clickedId: String? = null

    composeRule.setContent {
      MaterialTheme {
        OfflineOverviewHuntsScreen(
            hunts = hunts,
            onHuntClick = { uid -> clickedId = uid },
        )
      }
    }

    // Root overview container is displayed
    composeRule.onNodeWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN).assertIsDisplayed()

    // List container is displayed
    composeRule.onNodeWithTag(OverviewScreenTestTags.HUNT_LIST).assertIsDisplayed()

    // Click the last hunt card to exercise the clickable { onHuntClick(hunt.uid) } line.
    composeRule.onNodeWithTag(OverviewScreenTestTags.LAST_HUNT_CARD).performClick()

    // Not strictly needed for coverage, but sanity-check the callback was invoked.
    assert(clickedId == HUNT_UID_2)
  }
}
