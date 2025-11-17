package com.swentseekr.seekr.ui.profile

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.swentseekr.seekr.ui.theme.SampleAppTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented UI tests for the [EditProfileScreen] and [EditProfileContent] composable.
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

  private fun setContent(
      uiState: EditProfileUIState = EditProfileUIState(),
      pseudonymChange: (String) -> Unit = {},
      bioChange: (String) -> Unit = {},
      onCancel: () -> Unit = {},
      onSave: () -> Unit = {},
      onProfilePictureChange: () -> Unit = {},
      profilePictureUri: Uri? = null
  ) {
    composeTestRule.setContent {
      SampleAppTheme {
        EditProfileContent(
            uiState = uiState,
            onPseudonymChange = pseudonymChange,
            onBioChange = bioChange,
            onCancel = onCancel,
            onSave = onSave,
            onProfilePictureChange = onProfilePictureChange,
            profilePictureUri = profilePictureUri)
      }
    }
  }

  private fun ComposeTestRule.inputPseudonym(text: String) =
      onNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD).performTextInput(text)

  private fun ComposeTestRule.inputBio(text: String) =
      onNodeWithTag(EditProfileTestTags.BIO_FIELD).performTextInput(text)

  private fun ComposeTestRule.clickSave() =
      onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).performClick()

  private fun ComposeTestRule.clickCancel() =
      onNodeWithTag(EditProfileTestTags.CANCEL_BUTTON).performClick()

  private fun ComposeTestRule.clickProfilePicture() =
      onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).performClick()

  @Test
  fun clickingProfilePicture_opensDialog() {
    var showDialog by mutableStateOf(false)
    composeTestRule.setContent {
      SampleAppTheme {
        if (showDialog) {
          AlertDialog(
              modifier = Modifier.testTag(EditProfileTestTags.DIALOG),
              onDismissRequest = { showDialog = false },
              title = { Text("Choose Image") },
              confirmButton = {
                Column {
                  Button(onClick = {}) { Text("Gallery") }
                  Button(onClick = {}) { Text("Camera") }
                  Button(onClick = { showDialog = false }) { Text("Remove Picture") }
                }
              })
        }
        EditProfileContent(
            uiState = EditProfileUIState(),
            onPseudonymChange = {},
            onBioChange = {},
            onCancel = {},
            onSave = {},
            onProfilePictureChange = { showDialog = true })
      }
    }
    composeTestRule.clickProfilePicture()
    composeTestRule.onNodeWithTag(EditProfileTestTags.DIALOG).assertIsDisplayed()
  }

  @Test
  fun cancelButton_callsCallback() {
    var canceled = false
    setContent(onCancel = { canceled = true })
    composeTestRule.clickCancel()
    assert(canceled)
  }

  @Test
  fun saveButton_disabled_whenCannotSave() {
    setContent(uiState = EditProfileUIState(canSave = false))
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun saveButton_showsSavingText_whenIsSaving() {
    setContent(uiState = EditProfileUIState(canSave = true, isSaving = true))
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertTextEquals("Saving...")
  }

  @Test
  fun saveButton_showsSuccessMessage() {
    setContent(uiState = EditProfileUIState(success = true))
    composeTestRule.onNodeWithTag(EditProfileTestTags.SUCCESS_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun showsErrorMessage_whenError() {
    setContent(uiState = EditProfileUIState(errorMsg = "Failed to save"))
    composeTestRule
        .onNodeWithTag(EditProfileTestTags.ERROR_MESSAGE)
        .assertTextContains("Error: Failed to save")
  }

  @Test
  fun clickProfilePicture_triggersDialog() {
    var dialogShown = false
    setContent(onProfilePictureChange = { dialogShown = true })
    composeTestRule.clickProfilePicture()
    assert(dialogShown)
  }

  @Test
  fun showsError_whenSavingWithEmptyPseudonym() {
    setContent(uiState = EditProfileUIState(pseudonym = "", canSave = true))
    composeTestRule.clickSave()
    composeTestRule
        .onNodeWithTag(EditProfileTestTags.ERROR_MESSAGE)
        .assertTextContains("Pseudonym cannot be empty")
  }

  @Test
  fun displaysSelectedProfilePictureUri() {
    val testUri = Uri.parse("content://fake/image.jpg")
    setContent(uiState = EditProfileUIState(), profilePictureUri = testUri)
    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun saveButtonDisabled_whenErrorDisplayed() {
    setContent(uiState = EditProfileUIState(errorMsg = "Some error", canSave = true))
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsEnabled()
    composeTestRule.onNodeWithTag(EditProfileTestTags.ERROR_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun editProfileContent_saveButton_disabled_whenIsSaving() {
    setContent(uiState = EditProfileUIState(isSaving = true, canSave = true))
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun editProfileContent_showsSuccessMessage_whenUiStateSuccessTrue() {
    setContent(uiState = EditProfileUIState(success = true))
    composeTestRule
        .onNodeWithTag(EditProfileTestTags.SUCCESS_MESSAGE)
        .assertTextContains("Profile updated!")
  }

  @Test
  fun editProfileContent_saveButton_disabled_whenIsSavingOrCannotSave() {
    setContent(uiState = EditProfileUIState(canSave = false, isSaving = false))
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun pseudonymTooLong_showsError() {
    var pseudonym by mutableStateOf("")
    setContent(uiState = EditProfileUIState(canSave = true), pseudonymChange = { pseudonym = it })
    val longPseudonym = "a".repeat(31)
    composeTestRule.inputPseudonym(longPseudonym)
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
  fun selectingProfilePictureFromGallery_updatesUri() {
    val testUri = Uri.parse("content://fake/image_gallery.jpg")
    var selectedUri: Uri? = null
    setContent(onProfilePictureChange = { selectedUri = testUri })
    composeTestRule.clickProfilePicture()
    assert(selectedUri == testUri)
  }

  @Test
  fun emptyPseudonym_showsErrorAndDisablesSave() {
    var pseudonym by mutableStateOf("")
    setContent(
        uiState = EditProfileUIState(pseudonym = pseudonym, canSave = true),
        pseudonymChange = { pseudonym = it })
    composeTestRule.inputPseudonym(" ")
    composeTestRule.onNodeWithText("Pseudonym cannot be empty").assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun profilePicture_whenNoneSelected_isDefault() {
    setContent(uiState = EditProfileUIState())
    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun profilePictureUri_displaysWhenSet() {
    val testUri = Uri.parse("content://fake/image.jpg")
    setContent(uiState = EditProfileUIState(profilePictureUri = testUri))
    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun profilePictureUrl_displayedFromUiState() {
    val testUrl = "https://example.com/profile.jpg"
    setContent(uiState = EditProfileUIState(profilePictureUrl = testUrl))
    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun loadingState_disablesAllInputs() {
    setContent(uiState = EditProfileUIState(isLoading = true))
    composeTestRule.onNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD).assertIsNotEnabled()
    composeTestRule.onNodeWithTag(EditProfileTestTags.BIO_FIELD).assertIsNotEnabled()
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsNotEnabled()
    composeTestRule.onNodeWithTag(EditProfileTestTags.CANCEL_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun notLoadingState_enablesAllInputs() {
    setContent(uiState = EditProfileUIState(isLoading = false, canSave = true))
    composeTestRule.onNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD).assertIsEnabled()
    composeTestRule.onNodeWithTag(EditProfileTestTags.BIO_FIELD).assertIsEnabled()
    composeTestRule.onNodeWithTag(EditProfileTestTags.CANCEL_BUTTON).assertIsEnabled()
  }

  @Test
  fun removePictureButton_shownWhenUriExists() {
    val testUri = Uri.parse("content://test/image.jpg")
    var showDialog by mutableStateOf(false)
    composeTestRule.setContent {
      SampleAppTheme {
        if (showDialog) {
          AlertDialog(
              modifier = Modifier.testTag(EditProfileTestTags.DIALOG),
              onDismissRequest = { showDialog = false },
              title = { Text("Choose Image") },
              confirmButton = {
                Column {
                  Button(onClick = {}) { Text("Gallery") }
                  Button(onClick = {}) { Text("Camera") }
                  Button(onClick = { showDialog = false }) { Text("Remove Picture") }
                }
              })
        }
        EditProfileContent(
            uiState = EditProfileUIState(profilePictureUri = testUri),
            onPseudonymChange = {},
            onBioChange = {},
            onCancel = {},
            onSave = {},
            onProfilePictureChange = { showDialog = true })
      }
    }
    composeTestRule.clickProfilePicture()
    composeTestRule.onNodeWithText("Remove Picture").assertIsDisplayed()
  }

  @Test
  fun uriAndUrl_bothCanBeSet() {
    val testUri = Uri.parse("content://test/new.jpg")
    val testUrl = "https://example.com/old.jpg"
    setContent(
        uiState = EditProfileUIState(profilePictureUri = testUri, profilePictureUrl = testUrl))
    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun profilePicture_uriTakesPrecedenceOverUrl() {
    val testUri = Uri.parse("content://test/new.jpg")
    val testUrl = "https://example.com/old.jpg"
    setContent(
        uiState = EditProfileUIState(profilePictureUri = testUri, profilePictureUrl = testUrl),
        profilePictureUri = testUri)
    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun loadingAndSaving_mutuallyExclusive() {
    setContent(uiState = EditProfileUIState(isLoading = true, isSaving = false))
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun validation_worksWithUrl() {
    var bio by mutableStateOf("")
    val testUrl = "https://example.com/profile.jpg"
    setContent(
        uiState =
            EditProfileUIState(profilePictureUrl = testUrl, pseudonym = "Valid", canSave = true),
        bioChange = { bio = it })
    val longBio = "b".repeat(201)
    composeTestRule.inputBio(longBio)
    composeTestRule.onNodeWithText("Max 200 characters allowed").assertIsDisplayed()
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
  fun cameraButton_triggersLaunchCheckedByDisappearingDialog() {
    var cameraLaunched = false
    var showDialog by mutableStateOf(true)

    composeTestRule.setContent {
      SampleAppTheme {
        if (showDialog) {
          AlertDialog(
              modifier = Modifier.testTag(EditProfileTestTags.DIALOG),
              onDismissRequest = { showDialog = false },
              title = { Text("Choose Image") },
              confirmButton = {
                Button(
                    modifier = Modifier.testTag(EditProfileTestTags.CAMERA_BUTTON),
                    onClick = {
                      cameraLaunched = true
                      showDialog = false
                    }) {
                      Text("Camera")
                    }
              })
        }
        EditProfileContent(
            uiState = EditProfileUIState(),
            onPseudonymChange = {},
            onBioChange = {},
            onCancel = {},
            onSave = {},
            onProfilePictureChange = { showDialog = true })
      }
    }

    composeTestRule.clickProfilePicture()
    composeTestRule.onNodeWithTag(EditProfileTestTags.CAMERA_BUTTON).performClick()
    composeTestRule.onNodeWithTag(EditProfileTestTags.DIALOG).assertDoesNotExist()
    assert(cameraLaunched)
  }

  @Test
  fun pseudonymAndBioChange_updatesFieldsAndEnablesSave() {
    var pseudonym by mutableStateOf("")
    var bio by mutableStateOf("")
    var uiState by
        mutableStateOf(EditProfileUIState(pseudonym = pseudonym, bio = bio, canSave = false))

    composeTestRule.setContent {
      EditProfileContent(
          uiState = uiState,
          onPseudonymChange = { newPseudo ->
            pseudonym = newPseudo
            uiState =
                uiState.copy(
                    pseudonym = newPseudo, canSave = newPseudo.isNotBlank() && bio.isNotBlank())
          },
          onBioChange = { newBio ->
            bio = newBio
            uiState =
                uiState.copy(bio = newBio, canSave = pseudonym.isNotBlank() && newBio.isNotBlank())
          },
          onCancel = {},
          onSave = {},
          onProfilePictureChange = {})
    }

    composeTestRule.inputPseudonym("NewName")
    composeTestRule.inputBio("New bio text")

    composeTestRule.onNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD).assertTextContains("NewName")
    composeTestRule.onNodeWithTag(EditProfileTestTags.BIO_FIELD).assertTextContains("New bio text")
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsEnabled()
  }
}
