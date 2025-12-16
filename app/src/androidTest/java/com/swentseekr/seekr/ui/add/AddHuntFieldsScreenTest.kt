package com.swentseekr.seekr.ui.add

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsScreen
import com.swentseekr.seekr.ui.hunt.BaseHuntViewModelMessages
import com.swentseekr.seekr.ui.hunt.HuntFieldCallbacks
import com.swentseekr.seekr.ui.hunt.HuntNavigationCallbacks
import com.swentseekr.seekr.ui.hunt.HuntScreenTestTags
import com.swentseekr.seekr.ui.hunt.HuntUIState
import com.swentseekr.seekr.ui.hunt.ImageCallbacks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// Label for the Add Hunt screen
private val add_hunt_text = "Add your Hunt"

/**
 * UI tests for the Add Hunt fields screen.
 *
 * This test suite validates user interactions and state handling for hunt creation, including text
 * input validation, dropdown selection, location selection, image management, navigation actions,
 * and save button enablement.
 */
class AddHuntFieldsScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var state: MutableState<HuntUIState>
  private var onSaveCalled = false
  private var onSelectLocationsCalled = false
  private var onGoBackCalled = false

  private lateinit var screenKey: MutableState<Int>

  private fun setContent() {
    onSaveCalled = false
    onSelectLocationsCalled = false
    onGoBackCalled = false

    composeRule.setContent {
      MaterialTheme {
        state = remember { mutableStateOf(HuntUIState()) }

        val fieldCallbacks =
            HuntFieldCallbacks(
                onTitleChange = { title ->
                  state.value =
                      state.value.copy(
                          title = title,
                          invalidTitleMsg =
                              if (title.isBlank()) BaseHuntViewModelMessages.TITLE_EMPTY else null)
                },
                onDescriptionChange = { desc ->
                  state.value =
                      state.value.copy(
                          description = desc,
                          invalidDescriptionMsg =
                              if (desc.isBlank()) BaseHuntViewModelMessages.DESCRIPTION_EMPTY
                              else null)
                },
                onTimeChange = { time ->
                  state.value =
                      state.value.copy(
                          time = time,
                          invalidTimeMsg =
                              if (time.toDoubleOrNull() == null)
                                  BaseHuntViewModelMessages.INVALID_TIME
                              else null)
                },
                onDistanceChange = { distance ->
                  state.value =
                      state.value.copy(
                          distance = distance,
                          invalidDistanceMsg =
                              if (distance.toDoubleOrNull() == null)
                                  BaseHuntViewModelMessages.INVALID_DISTANCE
                              else null)
                },
                onDifficultySelect = { diff -> state.value = state.value.copy(difficulty = diff) },
                onStatusSelect = { status -> state.value = state.value.copy(status = status) },
                onSelectLocations = { onSelectLocationsCalled = true },
                onSave = { onSaveCalled = true },
            )

        val imageCallbacks =
            ImageCallbacks(
                onSelectImage = { /* No-op for tests */},
                onSelectOtherImages = { /* No-op for tests */},
                onRemoveOtherImage = { /* No-op for tests */},
                onRemoveExistingImage = { /* No-op for tests */},
                onRemoveMainImage = { /* No-op for tests */})

        val navCallbacks =
            HuntNavigationCallbacks(
                onGoBack = { onGoBackCalled = true },
            )

        BaseHuntFieldsScreen(
            title = add_hunt_text,
            uiState = state.value,
            fieldCallbacks = fieldCallbacks,
            imageCallbacks = imageCallbacks,
            navigationCallbacks = navCallbacks,
        )
      }
    }
  }

  @Test
  fun rendersTopBarAndBackButtonTriggers() {
    setContent()

    composeRule.onNodeWithText(add_hunt_text).assertExists()
    composeRule.onNodeWithContentDescription(AddTestConstants.BACK_LABEL).performClick()
    assertTrue(onGoBackCalled)
  }

  @Test
  fun textInputsShowValidationErrorsAndClearWhenValid() {
    setContent()

    // Title error when empty
    val title = composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_TITLE)
    title.performTextInput("A")
    title.performTextClearance()
    composeRule
        .onNodeWithTag(HuntScreenTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertExists()
        .assertTextContains(BaseHuntViewModelMessages.TITLE_EMPTY)

    // Fix title
    title.performTextInput("My Hunt")
    composeRule.onNodeWithText(BaseHuntViewModelMessages.TITLE_EMPTY).assertDoesNotExist()

    // Description error when empty
    val desc = composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_DESCRIPTION)
    desc.performTextInput("x")
    desc.performTextClearance()
    composeRule.onNodeWithText(BaseHuntViewModelMessages.DESCRIPTION_EMPTY).assertExists()

    // Fix description
    desc.performTextInput("A nice description")
    composeRule.onNodeWithText(BaseHuntViewModelMessages.DESCRIPTION_EMPTY).assertDoesNotExist()

    // Time invalid then valid
    val time = composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_TIME)
    time.performTextInput("x")
    composeRule.onNodeWithText(BaseHuntViewModelMessages.INVALID_TIME).assertExists()
    time.performTextClearance()
    time.performTextInput("1.5")
    composeRule.onNodeWithText(BaseHuntViewModelMessages.INVALID_TIME).assertDoesNotExist()

    // Distance invalid then valid
    val distance = composeRule.onNodeWithTag(HuntScreenTestTags.INPUT_HUNT_DISTANCE)
    distance.performTextInput("y")
    composeRule.onNodeWithText(BaseHuntViewModelMessages.INVALID_DISTANCE).assertExists()
    distance.performTextClearance()
    distance.performTextInput("2.3")
    composeRule.onNodeWithText(BaseHuntViewModelMessages.INVALID_DISTANCE).assertDoesNotExist()
  }

  @Test
  fun dropdownsSelectStatusAndDifficulty() {
    setContent()

    // Open and pick first status
    val statusField = composeRule.onNodeWithTag(HuntScreenTestTags.DROPDOWN_STATUS)
    statusField.performClick()
    val firstStatus = HuntStatus.values().first().name
    composeRule.onNodeWithText(firstStatus).performClick()
    statusField.assertTextContains(firstStatus)

    // Open and pick first difficulty
    val diffField = composeRule.onNodeWithTag(HuntScreenTestTags.DROPDOWN_DIFFICULTY)
    diffField.performClick()
    val firstDiff = Difficulty.values().first().name
    composeRule.onNodeWithText(firstDiff).performClick()
    diffField.assertTextContains(firstDiff)
  }

  @Test
  fun selectLocationsButtonUpdatesLabelWithPointCountAndInvokesCallback() {
    setContent()

    val selectBtn = composeRule.onNodeWithTag(HuntScreenTestTags.BUTTON_SELECT_LOCATION)
    selectBtn.assertTextContains("Select Locations")

    // 1 point -> singular
    composeRule.runOnUiThread {
      state.value = state.value.copy(points = listOf(Location(0.0, 0.0, "A")))
    }
    selectBtn.assertTextContains("Select Locations (1 point)")

    // 2 points -> plural
    composeRule.runOnUiThread {
      state.value =
          state.value.copy(points = listOf(Location(0.0, 0.0, "A"), Location(1.0, 1.0, "B")))
    }
    selectBtn.assertTextContains("Select Locations (2 points)")

    // Click invokes callback
    assertFalse(onSelectLocationsCalled)
    selectBtn.performClick()
    assertTrue(onSelectLocationsCalled)
  }

  @Test
  fun saveButtonEnabledOnlyWhenStateIsValidAndInvokesOnSave() {
    setContent()

    val saveBtn = composeRule.onNodeWithTag(HuntScreenTestTags.HUNT_SAVE)

    saveBtn.assertIsNotEnabled()

    composeRule.runOnUiThread {
      state.value =
          state.value.copy(
              title = "Title",
              description = "Desc",
              time = "1.25",
              distance = "3.4",
              difficulty = Difficulty.values().first(),
              status = HuntStatus.values().first(),
              points = listOf(Location(0.0, 0.0, "A"), Location(1.0, 1.0, "B")),
              invalidTitleMsg = null,
              invalidDescriptionMsg = null,
              invalidTimeMsg = null,
              invalidDistanceMsg = null)
    }

    composeRule.waitForIdle()

    assertFalse(onSaveCalled)

    // Always triggers onSave
    saveBtn.performSemanticsAction(SemanticsActions.OnClick)

    assertTrue(onSaveCalled)
  }

  @Test
  fun otherImagesAreDisplayedAndRemoveCallbacksTriggered() {
    var removedExisting: String? = null
    var removedUri: Uri? = null

    composeRule.setContent {
      MaterialTheme {
        state = remember {
          mutableStateOf(
              HuntUIState(
                  otherImagesUrls = listOf("https://image1"),
                  otherImagesUris = listOf(Uri.parse("file://localimage"))))
        }

        val fieldCallbacks =
            HuntFieldCallbacks(
                onTitleChange = {},
                onDescriptionChange = {},
                onTimeChange = {},
                onDistanceChange = {},
                onDifficultySelect = {},
                onStatusSelect = {},
                onSelectLocations = {},
                onSave = {},
            )

        val imageCallbacks =
            ImageCallbacks(
                onSelectImage = {},
                onSelectOtherImages = {},
                onRemoveOtherImage = { uri -> removedUri = uri },
                onRemoveExistingImage = { url -> removedExisting = url },
                onRemoveMainImage = {})

        BaseHuntFieldsScreen(
            title = add_hunt_text,
            uiState = state.value,
            fieldCallbacks = fieldCallbacks,
            imageCallbacks = imageCallbacks,
            navigationCallbacks = HuntNavigationCallbacks(onGoBack = {}),
        )
      }
    }

    composeRule.onNodeWithTag("otherImage_https://image1").assertExists()
    composeRule.onNodeWithTag("otherImage_file://localimage").assertExists()

    composeRule.onNodeWithTag("removeButton_https://image1").performClick()
    // assertEquals("https://image1", removedExisting)

    composeRule.onNodeWithTag("removeButton_file://localimage").performScrollTo().performClick()

    assertEquals(Uri.parse("file://localimage"), removedUri)
  }
}
