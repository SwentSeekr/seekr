package com.swentseekr.seekr.ui.preview

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.swentseekr.seekr.ui.hunt.HuntUIState
import com.swentseekr.seekr.ui.hunt.preview.PreviewHuntScreen
import com.swentseekr.seekr.ui.hunt.preview.PreviewHuntScreenTestTags
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class PreviewHuntScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun createFakeVM() =
      FakePreviewHuntViewModel(sourceState = MutableStateFlow(HuntUIState()))

  private val vm = createFakeVM()

  @Test
  fun previewScreen_displaysAllFields_andConfirmEnabled() {
    val current = vm.uiState.value
    vm.updateState(
        current.copy(
            otherImagesUris =
                listOf(
                    Uri.parse("https://example.com/image1"),
                    Uri.parse("https://example.com/image2"))))

    composeTestRule.setContent { PreviewHuntScreen(viewModel = vm, onConfirm = {}, onGoBack = {}) }

    // Root screen + all main fields
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.PREVIEW_HUNT_SCREEN).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_AUTHOR_PREVIEW).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_DESCRIPTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_TIME).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_DISTANCE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_DIFFICULTY).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_STATUS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_POINTS).assertIsDisplayed()

    // Confirm button enabled with initial valid state
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.CONFIRM_BUTTON).assertIsEnabled()
  }

  @Test
  fun confirmButton_disabledWhenStateInvalid() {
    vm.updateState(PreviewTestConstants.invalidUiState)

    composeTestRule.setContent { PreviewHuntScreen(viewModel = vm, onConfirm = {}, onGoBack = {}) }

    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.CONFIRM_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun clickingBackButton_triggersCallback() {
    var backClicked = false

    composeTestRule.setContent {
      PreviewHuntScreen(viewModel = vm, onConfirm = {}, onGoBack = { backClicked = true })
    }

    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.BACK_BUTTON).performClick()
    assertTrue(backClicked)
  }
}
