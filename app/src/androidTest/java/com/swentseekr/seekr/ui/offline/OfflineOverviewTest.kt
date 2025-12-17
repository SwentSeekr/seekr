package com.swentseekr.seekr.ui.offline

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the OfflineOverviewScreen composable.
 *
 * This test suite verifies that the offline overview screen components are displayed correctly and
 * that user interactions, such as clicking the show downloaded hunts button, function as expected.
 */
@RunWith(AndroidJUnit4::class)
class OfflineOverviewTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun offlineOverviewScreenDisplaysOverviewMessageAndButton() {
    composeTestRule.setContent { TestOfflineOverviewScreen(onShowDownloadedHunts = {}) }

    // Check main info text
    composeTestRule.onNodeWithText(OfflineConstants.OFFLINE_OVERVIEW_MESSAGE).assertIsDisplayed()

    // Check button text
    composeTestRule
        .onNodeWithText(OfflineConstants.SHOW_DOWNLOADED_HUNTS_BUTTON)
        .assertIsDisplayed()
  }

  @Test
  fun offlineOverviewScreenClickingButtonTriggersCallback() {
    var clicked = false

    composeTestRule.setContent {
      TestOfflineOverviewScreen(onShowDownloadedHunts = { clicked = true })
    }

    composeTestRule.onNodeWithText(OfflineConstants.SHOW_DOWNLOADED_HUNTS_BUTTON).performClick()

    assertTrue(clicked)
  }
}

@Composable
private fun TestOfflineOverviewScreen(onShowDownloadedHunts: () -> Unit) {
  // Provide a MaterialTheme so OfflineOverviewScreen can use MaterialTheme.colorScheme safely
  MaterialTheme {
    OfflineOverviewScreen(
        onShowDownloadedHunts = onShowDownloadedHunts,
    )
  }
}
