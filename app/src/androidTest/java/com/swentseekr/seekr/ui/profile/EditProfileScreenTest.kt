package com.swentseekr.seekr.ui.profile

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.swentseekr.seekr.R
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants.MAX_BIO_LENGTH
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants.PROFILE_PIC_DEFAULT
import com.swentseekr.seekr.ui.profile.EditProfileStrings.DIALOG_TITLE
import com.swentseekr.seekr.ui.profile.EditProfileTestTags.DIALOG_CANCEL_BUTTON
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

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

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
              title = { Text(DIALOG_TITLE) },
              confirmButton = {
                Column {
                  Button(onClick = {}) { Text(EditProfileStrings.BUTTON_GALLERY) }
                  Button(onClick = {}) { Text(EditProfileStrings.BUTTON_CAMERA) }
                  Button(onClick = { showDialog = false }) {
                    Text(EditProfileStrings.BUTTON_REMOVE_PICTURE)
                  }
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
  fun saveButton_showsSavingText_whenIsSaving() {
    setContent(uiState = EditProfileUIState(canSave = true, isSaving = true))
    composeTestRule
        .onNodeWithTag(EditProfileTestTags.SAVE_BUTTON)
        .assertTextEquals(EditProfileStrings.BUTTON_SAVING)
  }

  @Test
  fun saveButton_showsSuccessMessage() {
    setContent(uiState = EditProfileUIState(success = true))
    composeTestRule.onNodeWithTag(EditProfileTestTags.SUCCESS_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun clickProfilePicture_triggersDialog() {

    composeTestRule.setContent {
      SampleAppTheme { EditProfileScreen(testMode = true, onGoBack = {}, onDone = {}) }
    }
    composeTestRule.clickProfilePicture()
    composeTestRule.onNodeWithTag(EditProfileTestTags.DIALOG).assertIsDisplayed()
  }

  @Test
  fun saveButtonEnabled_whenErrorDisplayed() {
    setContent(
        uiState =
            EditProfileUIState(
                errorMsg = EditProfileStrings.SERVER_ERROR, canSave = true)) // backend/server error
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsEnabled()
    composeTestRule.onNodeWithTag(EditProfileTestTags.ERROR_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun editProfileContent_saveButton_disabled_whenIsSaving() {
    setContent(uiState = EditProfileUIState(isSaving = true, canSave = true))
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun editProfileContent_saveButton_disabled_whenIsSavingOrCannotSave() {
    setContent(uiState = EditProfileUIState(canSave = false, isSaving = false))
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun bioTooLong_showsError() {

    var bio by mutableStateOf(EditProfileStrings.EMPTY_STRING)
    setContent(pseudonymChange = { bio = it })
    val longBio = "a".repeat(MAX_BIO_LENGTH + 1)
    composeTestRule.inputBio(longBio)
    composeTestRule.onNodeWithText(EditProfileStrings.ERROR_BIO_MAX).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun profilePicture_whenNoneSelected_isDefault() {
    val state = EditProfileUIState()

    setContent(uiState = state)

    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).assertIsDisplayed()
    assert(state.profilePicture == PROFILE_PIC_DEFAULT)
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
  fun profilePicture_uriTakesPrecedenceOverUrl() {
    val testUri = Uri.parse("content://test/new.jpg")
    val testUrl = "https://example.com/old.jpg"
    setContent(
        uiState = EditProfileUIState(profilePictureUri = testUri, profilePictureUrl = testUrl),
        profilePictureUri = testUri)
    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun profilePictureDialog_cancelButton_closesDialog() {

    composeTestRule.setContent {
      SampleAppTheme { EditProfileScreen(testMode = true, onGoBack = {}, onDone = {}) }
    }
    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).performClick()
    composeTestRule.onNodeWithTag(DIALOG_CANCEL_BUTTON).performClick()
    composeTestRule.onNodeWithTag(EditProfileTestTags.DIALOG).assertIsNotDisplayed()
  }

  @Test
  fun editProfileContent_localError_overridesUiStateError_andSuccess() {
    var pseudonymChanged = EditProfileStrings.EMPTY_STRING
    composeTestRule.setContent {
      EditProfileContent(
          uiState =
              EditProfileUIState(
                  pseudonym = EditProfileStrings.EMPTY_STRING,
                  bio = EditProfileStrings.EMPTY_STRING,
                  profilePicture = PROFILE_PIC_DEFAULT,
                  canSave = true,
                  isSaving = false,
                  success = true,
                  errorMsg = EditProfileStrings.REPO_ERROR),
          onPseudonymChange = { pseudonymChanged = it },
          onBioChange = {},
          onCancel = {},
          onSave = {},
          onProfilePictureChange = {})
    }
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).performClick()
    composeTestRule
        .onNodeWithTag(EditProfileTestTags.ERROR_MESSAGE)
        .assertTextContains(EditProfileStrings.ERROR_PSEUDONYM_EMPTY)
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
              title = { Text(DIALOG_TITLE) },
              confirmButton = {
                Button(
                    modifier = Modifier.testTag(EditProfileTestTags.CAMERA_BUTTON),
                    onClick = {
                      cameraLaunched = true
                      showDialog = false
                    }) {
                      Text(EditProfileStrings.BUTTON_CAMERA)
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
    var pseudonym by mutableStateOf(EditProfileStrings.EMPTY_STRING)
    var bio by mutableStateOf(EditProfileStrings.EMPTY_STRING)
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

    composeTestRule.inputPseudonym(EditProfileStrings.NEW_TEST_NAME)
    composeTestRule.inputBio(EditProfileStrings.NEW_TEST_BIO)
    composeTestRule
        .onNodeWithTag(EditProfileTestTags.PSEUDONYM_FIELD)
        .assertTextContains(EditProfileStrings.NEW_TEST_NAME)
    composeTestRule
        .onNodeWithTag(EditProfileTestTags.BIO_FIELD)
        .assertTextContains(EditProfileStrings.NEW_TEST_BIO)
    composeTestRule.onNodeWithTag(EditProfileTestTags.SAVE_BUTTON).assertIsEnabled()
  }

  @Test
  fun createImageUri_success_returnsUri() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val uri = createImageUri(context)
    assert(uri != null)
  }

  @Test
  fun galleryLauncher_nullUri_doesNothing() {
    composeTestRule.setContent { EditProfileScreen(testMode = true) }
    composeTestRule.onNodeWithTag(EditProfileTestTags.PROFILE_PICTURE).performClick()
    composeTestRule.onNodeWithTag(EditProfileTestTags.GALLERY_BUTTON).performClick()

    composeTestRule.onNodeWithTag(EditProfileTestTags.DIALOG).assertDoesNotExist()
  }

  @Test
  fun clickingGalleryButton_closesDialog() {
    composeTestRule.setContent { EditProfileScreen(testMode = true) }

    composeTestRule.clickProfilePicture()
    composeTestRule.onNodeWithTag(EditProfileTestTags.GALLERY_BUTTON).performClick()
    composeTestRule.onNodeWithTag(EditProfileTestTags.DIALOG).assertDoesNotExist()
  }

  @Test
  fun removeProfilePictureButton_shown_whenProfileNotDefault() {

    var showDialog by mutableStateOf(false)
    composeTestRule.setContent {
      SampleAppTheme {
        if (showDialog) {
          AlertDialog(
              modifier = Modifier.testTag(EditProfileTestTags.DIALOG),
              onDismissRequest = { showDialog = false },
              title = { Text(DIALOG_TITLE) },
              confirmButton = {
                Column {
                  Button(onClick = {}) { Text(EditProfileStrings.BUTTON_GALLERY) }
                  Button(onClick = {}) { Text(EditProfileStrings.BUTTON_CAMERA) }
                  Button(onClick = { showDialog = false }) {
                    Text(EditProfileStrings.BUTTON_REMOVE_PICTURE)
                  }
                }
              })
        }
        EditProfileContent(
            uiState = EditProfileUIState(profilePicture = R.drawable.logo_seekr),
            onPseudonymChange = {},
            onBioChange = {},
            onCancel = {},
            onSave = {},
            onProfilePictureChange = { showDialog = true })
      }
    }

    composeTestRule.clickProfilePicture()
    composeTestRule.onNodeWithText(EditProfileStrings.BUTTON_REMOVE_PICTURE).assertIsDisplayed()
  }
}
