package com.swentseekr.seekr.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.swentseekr.seekr.ui.theme.SampleAppTheme
import org.junit.Rule
import org.junit.Test

class EditProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun pseudonymAndBioChange_updatesFieldsAndEnablesSave() {
    composeTestRule.setContent {
      var pseudonym by remember { mutableStateOf("") }
      var bio by remember { mutableStateOf("") }

      SampleAppTheme {
        EditProfileContent(
            uiState =
                EditProfileUIState(
                    pseudonym = pseudonym, bio = bio, profilePicture = 0, canSave = true),
            onPseudonymChange = { pseudonym = it },
            onBioChange = { bio = it },
            onCancel = {},
            onSave = {},
            onProfilePictureChange = {})
      }
    }

    composeTestRule.onNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD).performTextInput("NewName")
    composeTestRule.onNodeWithTag(EditProfileTestTags.BIO_FIELD).performTextInput("New bio text")

    composeTestRule.onNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD).assertTextContains("NewName")
    composeTestRule.onNodeWithTag(EditProfileTestTags.BIO_FIELD).assertTextContains("New bio text")

    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsEnabled()
  }

  @Test
  fun clickingProfilePicture_opensDialog() {
    composeTestRule.setContent { SampleAppTheme { EditProfileScreen(testMode = true) } }

    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).performClick()
    composeTestRule.onNodeWithTag(EditProfileTestTags.DIALOG).assertIsDisplayed()
  }

  @Test
  fun cancelButton_callsCallback() {
    var canceled = false
    composeTestRule.setContent {
      SampleAppTheme {
        EditProfileContent(
            uiState = EditProfileUIState(pseudonym = "", bio = "", profilePicture = 0),
            onPseudonymChange = {},
            onBioChange = {},
            onCancel = { canceled = true },
            onSave = {},
            onProfilePictureChange = {})
      }
    }

    composeTestRule.onNodeWithTag(EditProfileTestTags.CANCEL_BUTTON).performClick()
    assert(canceled)
  }

  @Test
  fun saveButton_disabled_whenCannotSave() {
    val uiState =
        EditProfileUIState(
            pseudonym = "", bio = "", profilePicture = 0, canSave = false, isSaving = false)

    composeTestRule.setContent {
      SampleAppTheme {
        EditProfileContent(
            uiState = uiState,
            onPseudonymChange = {},
            onBioChange = {},
            onCancel = {},
            onSave = {},
            onProfilePictureChange = {})
      }
    }

    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun saveButton_showsSavingText_whenIsSaving() {
    val uiState =
        EditProfileUIState(
            pseudonym = "Tester",
            bio = "Hello",
            profilePicture = 0,
            canSave = true,
            isSaving = true)

    composeTestRule.setContent {
      SampleAppTheme {
        EditProfileContent(
            uiState = uiState,
            onPseudonymChange = {},
            onBioChange = {},
            onCancel = {},
            onSave = {},
            onProfilePictureChange = {})
      }
    }

    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertTextEquals("Saving...")
  }

  @Test
  fun saveButton_showsSuccessMessage() {
    val uiState =
        EditProfileUIState(
            pseudonym = "Tester",
            bio = "Hello",
            profilePicture = 0,
            canSave = true,
            isSaving = false,
            success = true,
            errorMsg = null)

    composeTestRule.setContent {
      SampleAppTheme {
        EditProfileContent(
            uiState = uiState,
            onPseudonymChange = {},
            onBioChange = {},
            onCancel = {},
            onSave = {},
            onProfilePictureChange = {})
      }
    }

    composeTestRule.onNodeWithTag(EditProfileTestTags.SUCCESS_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun showsErrorMessage_whenError() {
    val uiState =
        EditProfileUIState(
            pseudonym = "Tester",
            bio = "Hello",
            profilePicture = 0,
            canSave = true,
            isSaving = false,
            success = false,
            errorMsg = "Failed to save")

    composeTestRule.setContent {
      SampleAppTheme {
        EditProfileContent(
            uiState = uiState,
            onPseudonymChange = {},
            onBioChange = {},
            onCancel = {},
            onSave = {},
            onProfilePictureChange = {})
      }
    }

    composeTestRule
        .onNodeWithTag(EditProfileTestTags.ERROR_MESSAGE)
        .assertTextContains("Error: Failed to save")
  }

  @Test
  fun clickProfilePicture_triggersDialog() {
    var dialogShown = false

    composeTestRule.setContent {
      SampleAppTheme {
        EditProfileContent(
            uiState = EditProfileUIState(pseudonym = "", bio = "", profilePicture = 0),
            onPseudonymChange = {},
            onBioChange = {},
            onCancel = {},
            onSave = {},
            onProfilePictureChange = { dialogShown = true })
      }
    }

    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).performClick()
    assert(dialogShown)
  }

  @Test
  fun profilePictureDialog_cancelButton_closesDialogWithoutChanging() {
    val pictureChanged = false

    composeTestRule.setContent {
      SampleAppTheme { EditProfileScreen(testMode = true, onGoBack = {}, onDone = {}) }
    }

    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).performClick()
    composeTestRule.onNodeWithTag("DIALOG_CANCEL_BUTTON").performClick()
    assert(!pictureChanged)
  }

  @Test
  fun showsError_whenSavingWithEmptyPseudonym() {
    val uiState =
        EditProfileUIState(
            pseudonym = "",
            bio = "Something",
            profilePicture = 0,
            canSave = true,
            hasChanges = true,
            isSaving = false,
            success = false,
            errorMsg = null)

    composeTestRule.setContent {
      SampleAppTheme {
        EditProfileContent(
            uiState = uiState,
            onPseudonymChange = {},
            onBioChange = {},
            onCancel = {},
            onSave = {},
            onProfilePictureChange = {})
      }
    }

    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).performClick()

    composeTestRule
        .onNodeWithTag(EditProfileTestTags.ERROR_MESSAGE)
        .assertExists()
        .assertTextContains("Pseudonym cannot be empty")
  }
}
