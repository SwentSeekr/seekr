package com.swentseekr.seekr.ui.preview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.swentseekr.seekr.ui.hunt.HuntUIState
import com.swentseekr.seekr.ui.hunt.preview.PreviewHuntScreen
import com.swentseekr.seekr.ui.hunt.preview.PreviewHuntScreenTestTags
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class PreviewHuntScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun createFakeVM() =
      FakePreviewHuntViewModel(sourceState = MutableStateFlow(HuntUIState()))

  @Test
  fun previewScreen_displaysAllFields_andConfirmEnabled() {
    val vm = createFakeVM()

    composeTestRule.setContent { PreviewHuntScreen(viewModel = vm, onConfirm = {}, onGoBack = {}) }

    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_DESCRIPTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_TIME).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_DISTANCE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_DIFFICULTY).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_STATUS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.HUNT_POINTS).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.CONFIRM_BUTTON).assertIsEnabled()
  }

  @Test
  fun confirmButton_disabledWhenStateInvalid() {
    val vm = createFakeVM()
    vm.setState(
        HuntUIState(
            title = "",
            description = "",
            time = "",
            distance = "",
            difficulty = null,
            status = null,
            points = emptyList()))

    composeTestRule.setContent { PreviewHuntScreen(viewModel = vm, onConfirm = {}, onGoBack = {}) }

    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.CONFIRM_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun clickingBackButton_triggersCallback() {
    var backClicked = false
    val vm = createFakeVM()

    composeTestRule.setContent {
      PreviewHuntScreen(viewModel = vm, onConfirm = {}, onGoBack = { backClicked = true })
    }

    composeTestRule.onNodeWithTag(PreviewHuntScreenTestTags.BACK_BUTTON).performClick()
    assert(backClicked)
  }
}
