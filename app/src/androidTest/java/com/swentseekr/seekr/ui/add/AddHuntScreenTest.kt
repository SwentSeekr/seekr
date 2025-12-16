package com.swentseekr.seekr.ui.add

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.HuntScreenTestTags
import com.swentseekr.seekr.ui.hunt.add.AddHuntScreen
import com.swentseekr.seekr.ui.hunt.add.AddHuntViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// Shared text constant for the Add Hunt screen
private val add_hunt_text = "Add your Hunt"

/**
 * UI tests for the Add Hunt screen.
 *
 * This test suite verifies screen initialization, navigation behavior,
 * form validation, save action enablement, and ViewModel-driven state
 * handling during the hunt creation flow.
 */

class AddHuntScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private fun setContent(
      viewModel: AddHuntViewModel,
      onGoBack: () -> Unit = {},
      onDone: () -> Unit = {}
  ) {
    composeRule.setContent {
      MaterialTheme {
        AddHuntScreen(addHuntViewModel = viewModel, onGoBack = onGoBack, onDone = onDone)
      }
    }
  }

  @Test
  fun starts_with_fields_and_back_navigates() {
    val vm = AddHuntViewModel()
    var backCalled = false
    setContent(vm, onGoBack = { backCalled = true })

    composeRule.onNodeWithText(add_hunt_text).assertExists()
    composeRule.onNodeWithTag(HuntScreenTestTags.BUTTON_SELECT_LOCATION).assertExists()
    composeRule.onNodeWithContentDescription(AddTestConstants.BACK_LABEL).performClick()
    assertTrue(backCalled)
  }

  @Test
  fun save_enabled_when_state_valid_and_not_logged_in_shows_error_and_does_not_call_onDone() {
    val vm = AddHuntViewModel()
    var doneCalled = false
    setContent(vm, onDone = { doneCalled = true })

    // Preload 2 points directly to avoid map
    composeRule.runOnUiThread {
      vm.setPoints(listOf(Location(0.0, 0.0, "A"), Location(1.0, 1.0, "B")))
    }

    // Fill required fields
    composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_TITLE).performTextInput("Title")
    composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_DESCRIPTION).performTextInput("Desc")
    composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_TIME).performTextInput("1.25")
    composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_DISTANCE).performTextInput("3.4")

    // Select status
    val statusField = composeRule.onNodeWithTag(HuntScreenTestTags.DROPDOWN_STATUS)
    statusField.performClick()
    composeRule.onNodeWithText(HuntStatus.values().first().name).performClick()

    // Select difficulty
    val diffField = composeRule.onNodeWithTag(HuntScreenTestTags.DROPDOWN_DIFFICULTY)
    diffField.performClick()
    composeRule.onNodeWithText(Difficulty.values().first().name).performClick()

    // Save should be enabled
    val save = composeRule.onNodeWithTag(HuntScreenTestTags.HUNT_SAVE)
    save.assertIsEnabled()

    // Not logged in -> addHunt returns false, error is emitted then cleared by effect
    save.performClick()
    composeRule.waitForIdle()

    assertFalse(doneCalled)
    assertNull(vm.uiState.value.errorMsg)
  }

  @Test
  fun invalid_state_triggered_error_is_cleared_by_effect() {
    val vm = AddHuntViewModel()
    setContent(vm)

    // Ensure invalid state (default)
    // Calling addHunt() directly sets error in VM; effect should clear it
    composeRule.runOnUiThread {
      val result = vm.submit()
      assertFalse(result)
    }

    // After composition effect runs, error gets cleared
    composeRule.waitForIdle()
    assertNull(vm.uiState.value.errorMsg)

    // Fix title validation message path and ensure UI updates don’t crash
    val title = composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_TITLE)
    title.performTextInput("A")
    title.performTextClearance()
    composeRule
        .onNodeWithTag(HuntScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertExists()
  }
}
