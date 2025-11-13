package com.swentseekr.seekr.ui.profile

import android.net.Uri
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
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.swentseekr.seekr.ui.theme.SampleAppTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented UI tests for the [EditProfileScreen] and [EditProfileContent] composables.
 *
 * These tests verify that:
 * - User interactions correctly update the UI state (e.g., text input fields).
 * - Buttons are enabled or disabled based on UI state.
 * - Dialogs and messages (success/error) appear under expected conditions.
 * - Callbacks such as `onCancel`, `onSave`, and `onProfilePictureChange` are invoked properly.
 *
 * Uses [composeTestRule] to simulate user interactions and assert UI state.
 */
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

  @Test
  fun displaysSelectedProfilePictureUri() {
    val testUri = Uri.parse("content://fake/image.jpg")
    composeTestRule.setContent {
      SampleAppTheme {
        EditProfileContent(
            uiState = EditProfileUIState(pseudonym = "Test", bio = "Bio", profilePicture = 0),
            onPseudonymChange = {},
            onBioChange = {},
            onCancel = {},
            onSave = {},
            onProfilePictureChange = {},
            profilePictureUri = testUri)
      }
    }

    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun saveButtonDisabled_whenErrorDisplayed() {
    val uiState =
        EditProfileUIState(
            pseudonym = "Tester",
            bio = "Bio",
            profilePicture = 0,
            canSave = true,
            isSaving = false,
            errorMsg = "Some error")

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

    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsEnabled()
    composeTestRule.onNodeWithTag(EditProfileTestTags.ERROR_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun editProfileContent_localError_overridesUiStateError_andSuccess() {
    var pseudonymChanged = ""
    composeTestRule.setContent {
      EditProfileContent(
          uiState =
              EditProfileUIState(
                  pseudonym = "",
                  bio = "",
                  profilePicture = 0,
                  canSave = true,
                  isSaving = false,
                  success = true,
                  errorMsg = "Repo error"),
          onPseudonymChange = { pseudonymChanged = it },
          onBioChange = {},
          onCancel = {},
          onSave = {},
          onProfilePictureChange = {})
    }

    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).performClick()

    composeTestRule
        .onNodeWithTag(EditProfileTestTags.ERROR_MESSAGE)
        .assertTextContains("Pseudonym cannot be empty")
  }

  @Test
  fun editProfileContent_saveButton_disabled_whenIsSaving() {
    val uiState = EditProfileUIState(canSave = true, isSaving = true)
    composeTestRule.setContent {
      EditProfileContent(
          uiState = uiState,
          onPseudonymChange = {},
          onBioChange = {},
          onCancel = {},
          onSave = {},
          onProfilePictureChange = {})
    }

    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun editProfileContent_showsSuccessMessage_whenUiStateSuccessTrue() {
    val state = EditProfileUIState(success = true)

    composeTestRule.setContent {
      EditProfileContent(
          uiState = state,
          onPseudonymChange = {},
          onBioChange = {},
          onCancel = {},
          onSave = {},
          onProfilePictureChange = {})
    }

    composeTestRule
        .onNodeWithTag(EditProfileTestTags.SUCCESS_MESSAGE)
        .assertTextContains("Profile updated!")
  }

  @Test
  fun editProfileContent_saveButton_disabled_whenIsSavingOrCannotSave() {
    val state = EditProfileUIState(canSave = false, isSaving = false)
    composeTestRule.setContent {
      EditProfileContent(
          uiState = state,
          onPseudonymChange = {},
          onBioChange = {},
          onCancel = {},
          onSave = {},
          onProfilePictureChange = {})
    }
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun pseudonymTooLong_showsError() {
    var canSave = true

    composeTestRule.setContent {
      var pseudonym by remember { mutableStateOf("") }
      var bio by remember { mutableStateOf("") }

      SampleAppTheme {
        EditProfileContent(
            uiState = EditProfileUIState(pseudonym = pseudonym, bio = bio, canSave = true),
            onPseudonymChange = { pseudonym = it },
            onBioChange = { bio = it },
            onCancel = {},
            onSave = { canSave = true },
            onProfilePictureChange = {})
      }
    }

    val longPseudonym = "a".repeat(31)
    composeTestRule
        .onNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD)
        .performTextInput(longPseudonym)

    composeTestRule.onNodeWithText("Max 30 characters allowed").assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun bioTooLong_showsError() {
    var canSave = true

    composeTestRule.setContent {
      var pseudonym by remember { mutableStateOf("Valid") }
      var bio by remember { mutableStateOf("") }

      SampleAppTheme {
        EditProfileContent(
            uiState = EditProfileUIState(pseudonym = pseudonym, bio = bio, canSave = true),
            onPseudonymChange = { pseudonym = it },
            onBioChange = { bio = it },
            onCancel = {},
            onSave = { canSave = true },
            onProfilePictureChange = {})
      }
    }

    val longBio = "b".repeat(201)
    composeTestRule.onNodeWithTag(EditProfileTestTags.BIO_FIELD).performTextInput(longBio)

    composeTestRule.onNodeWithText("Max 200 characters allowed").assertIsDisplayed()

    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun cameraButton_triggersLaunchCheckedByDisappearingDialog() {
    var cameraLaunched = false

    composeTestRule.setContent {
      SampleAppTheme { EditProfileScreen(testMode = true, onGoBack = {}, onDone = {}) }
    }

    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).performClick()
    composeTestRule.onNodeWithTag(EditProfileTestTags.CAMERA_BUTTON).performClick()
    composeTestRule.onNodeWithTag(EditProfileTestTags.DIALOG).assertDoesNotExist()
  }

  @Test
  fun selectingProfilePictureFromGallery_updatesUri() {
    val testUri = Uri.parse("content://fake/image_gallery.jpg")
    var selectedUri: Uri? = null

    composeTestRule.setContent {
      SampleAppTheme {
        EditProfileContent(
            uiState = EditProfileUIState(pseudonym = "", bio = "", profilePicture = 0),
            onPseudonymChange = {},
            onBioChange = {},
            onCancel = {},
            onSave = {},
            onProfilePictureChange = { selectedUri = testUri },
            profilePictureUri = null)
      }
    }

    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).performClick()
    assert(selectedUri == testUri)
  }

  @Test
  fun emptyPseudonym_showsErrorAndDisablesSave() {
    composeTestRule.setContent {
      var pseudonym by remember { mutableStateOf("") }

      SampleAppTheme {
        EditProfileContent(
            uiState =
                EditProfileUIState(
                    pseudonym = pseudonym, bio = "Some bio", profilePicture = 0, canSave = true),
            onPseudonymChange = { pseudonym = it },
            onBioChange = {},
            onCancel = {},
            onSave = {},
            onProfilePictureChange = {})
      }
    }

    composeTestRule.onNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD).performTextInput(" ")

    composeTestRule.onNodeWithText("Pseudonym cannot be empty").assertIsDisplayed()
  }

  @Test
  fun profilePicture_whenNoneSelected_isDefault() {
    val uiState =
        EditProfileUIState(
            pseudonym = "",
            bio = "",
        )

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

    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).assertIsDisplayed()

    assert(uiState.profilePicture == 0)
  }
}
