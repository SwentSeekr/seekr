package com.swentseekr.seekr.ui.preview

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.HuntUIState
import com.swentseekr.seekr.ui.hunt.preview.PreviewHuntScreen
import com.swentseekr.seekr.ui.hunt.preview.PreviewHuntScreenTestTags
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
/**
 * UI tests for the PreviewHuntScreen composable.
 *
 * This test suite verifies that the preview hunt screen components are displayed
 * correctly based on various UI states and that user interactions, such as
 * clicking the back and confirm buttons, function as expected.
 */
class PreviewHuntScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun createFakeVM(initialState: HuntUIState = HuntUIState()) =
      FakePreviewHuntViewModel(sourceState = MutableStateFlow(initialState))

  private fun createValidUiState(): HuntUIState {
    val base = HuntUIState()
    return base.copy(
        title = "My Preview Hunt",
        description = "A nice description for coverage.",
        distance = "3.5",
        time = "1.0",
        difficulty = Difficulty.INTERMEDIATE,
        status = HuntStatus.FUN,
        mainImageUrl = "https://example.com/main.jpg",
        otherImagesUris =
            listOf(
                Uri.parse("https://example.com/image1"), Uri.parse("https://example.com/image2")),
        points =
            listOf(
                Location(latitude = 46.0, longitude = 6.0, name = "Start"),
                Location(latitude = 46.1, longitude = 6.1, name = "End")))
  }

  /**
   * Full happy-path preview:
   * - Valid state with images, points, difficulty, status
   * - Scrolls down to ensure all sections are actually composed and displayed
   */
  @Test
  fun previewScreen_showsAllSections_withScrolling_whenStateValid() {
    val validState = createValidUiState()
    val vm = createFakeVM(validState)

    composeTestRule.setContent { PreviewHuntScreen(viewModel = vm, onConfirm = {}, onGoBack = {}) }

    // Root screen
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.PREVIEW_HUNT_SCREEN).assertIsDisplayed()

    // Hero section (should be visible at top)
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_AUTHOR_PREVIEW).assertIsDisplayed()

    // Stats section (should be visible near top)
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_TIME).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_DISTANCE).assertIsDisplayed()

    // Description card - scroll to it first
    composeTestRule
        .onNodeWithTag(PreviewHuntScreenTestTags.HUNT_DESCRIPTION)
        .performScrollTo()
        .assertIsDisplayed()

    // Status & points section - scroll to these as well
    composeTestRule
        .onNodeWithTag(PreviewHuntScreenTestTags.HUNT_STATUS)
        .performScrollTo()
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(PreviewHuntScreenTestTags.HUNT_POINTS)
        .performScrollTo()
        .assertIsDisplayed()

    // Confirm button - scroll to it and verify it's enabled
    composeTestRule
        .onNodeWithTag(PreviewHuntScreenTestTags.CONFIRM_BUTTON)
        .performScrollTo()
        .assertIsEnabled()
  }

  /**
   * Back and confirm callbacks:
   * - Ensures that both buttons invoke the correct lambdas.
   * - Scrolls to confirm button before clicking it.
   */
  @Test
  fun previewScreen_backAndConfirmCallbacks_areInvoked() {
    val validState = createValidUiState()
    val vm = createFakeVM(validState)

    var backClicked = false
    var confirmClicked = false

    composeTestRule.setContent {
      PreviewHuntScreen(
          viewModel = vm, onConfirm = { confirmClicked = true }, onGoBack = { backClicked = true })
    }

    // Back button at the top
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.BACK_BUTTON).performClick()
    assertTrue(backClicked)

    // Scroll to confirm button and click it
    composeTestRule
        .onNodeWithTag(PreviewHuntScreenTestTags.CONFIRM_BUTTON)
        .performScrollTo()
        .assertIsEnabled()
        .performClick()

    assertTrue(confirmClicked)
  }
}
