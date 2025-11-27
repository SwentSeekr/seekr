package com.swentseekr.seekr.ui.profile

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants.MAX_BIO_LENGTH
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants.MAX_PSEUDONYM_LENGTH
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants.PROFILE_PIC_DEFAULT
import com.swentseekr.seekr.ui.profile.EditProfileStrings.BUTTON_CAMERA
import com.swentseekr.seekr.ui.profile.EditProfileStrings.BUTTON_CANCEL
import com.swentseekr.seekr.ui.profile.EditProfileStrings.BUTTON_GALLERY
import com.swentseekr.seekr.ui.profile.EditProfileStrings.BUTTON_REMOVE_PICTURE
import com.swentseekr.seekr.ui.profile.EditProfileStrings.BUTTON_SAVE
import com.swentseekr.seekr.ui.profile.EditProfileStrings.BUTTON_SAVING
import com.swentseekr.seekr.ui.profile.EditProfileStrings.DIALOG_MESSAGE
import com.swentseekr.seekr.ui.profile.EditProfileStrings.DIALOG_TITLE
import com.swentseekr.seekr.ui.profile.EditProfileStrings.ERROR_BIO_MAX
import com.swentseekr.seekr.ui.profile.EditProfileStrings.ERROR_PSEUDONYM_EMPTY
import com.swentseekr.seekr.ui.profile.EditProfileStrings.ERROR_PSEUDONYM_MAX
import com.swentseekr.seekr.ui.profile.EditProfileStrings.FIELD_LABEL_BIO
import com.swentseekr.seekr.ui.profile.EditProfileStrings.FIELD_LABEL_PSEUDONYM
import com.swentseekr.seekr.ui.profile.EditProfileStrings.SUCCESS_UPDATE

/**
 * The main screen composable for editing a user’s profile.
 *
 * Displays editable fields for pseudonym and bio, allows changing the profile picture, and handles
 * saving or canceling changes.
 *
 * @param userId The ID of the user whose profile is being edited.
 * @param editProfileViewModel The [EditProfileViewModel] that holds UI state and logic.
 * @param onGoBack Callback when the user cancels or navigates back.
 * @param onDone Callback when saving completes successfully.
 * @param testMode If true, disables loading user data and simplifies behavior for testing.
 */
@Composable
fun EditProfileScreen(
    userId: String? = null,
    editProfileViewModel: EditProfileViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onDone: () -> Unit = {},
    testMode: Boolean = false,
) {

  val uiState by editProfileViewModel.uiState.collectAsState()
  val context = LocalContext.current
  var cameraError by remember { mutableStateOf<String?>(null) }

  var showDialog by remember { mutableStateOf(false) }

  val galleryLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { editProfileViewModel.updateProfilePictureUri(it) }
      }

  var cameraPhotoUri = remember { mutableStateOf<Uri?>(null) }
  val cameraLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraPhotoUri.value != null) {
          editProfileViewModel.updateProfilePictureUri(cameraPhotoUri.value!!)
        }
      }

  fun launchCamera() {
    val uri = createImageUri(context)
    if (uri == null) {
      cameraError = EditProfileStrings.CAMERA_PERMISSION_ERROR
      return
    }
    try {
      cameraPhotoUri.value = uri
      cameraLauncher.launch(uri)
    } catch (e: Exception) {
      cameraError = "Failed to launch camera: ${e.message}"
      Log.e(EditProfileStrings.CAMERA_LAUNCH, "Failed to launch camera: ${e.message}", e)
    }
  }

  LaunchedEffect(Unit) {
    if (!testMode && userId != null) editProfileViewModel.loadProfile()
    editProfileViewModel.uiState.collect { if (it.success) onDone() }
  }

  if (cameraError != null) {
    AlertDialog(
        onDismissRequest = { cameraError = null },
        title = { Text(EditProfileStrings.ERROR) },
        text = { Text(cameraError!!) },
        confirmButton = {
          Button(onClick = { cameraError = null }) { Text(EditProfileStrings.OK) }
        })
  }

  if (showDialog) {
    AlertDialog(
        modifier = Modifier.testTag(EditProfileTestTags.DIALOG),
        onDismissRequest = { showDialog = false },
        title = { Text(DIALOG_TITLE) },
        text = { Text(DIALOG_MESSAGE) },
        confirmButton = {
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = EditProfileNumberConstants.SPACER_SMALL),
              verticalArrangement = Arrangement.spacedBy(EditProfileNumberConstants.SPACER_SMALL)) {
                Button(
                    modifier = Modifier.fillMaxWidth().testTag(EditProfileTestTags.GALLERY_BUTTON),
                    onClick = {
                      galleryLauncher.launch(EditProfileStrings.IMAGE)
                      showDialog = false
                    }) {
                      Text(BUTTON_GALLERY)
                    }

                Button(
                    modifier = Modifier.fillMaxWidth().testTag(EditProfileTestTags.CAMERA_BUTTON),
                    onClick = {
                      launchCamera()
                      showDialog = false
                    }) {
                      Text(BUTTON_CAMERA)
                    }

                if (uiState.profilePicture != PROFILE_PIC_DEFAULT ||
                    uiState.profilePictureUri != null ||
                    uiState.profilePictureUrl.isNotEmpty()) {
                  Button(
                      modifier = Modifier.fillMaxWidth(),
                      colors =
                          ButtonDefaults.buttonColors(
                              containerColor = MaterialTheme.colorScheme.errorContainer),
                      onClick = {
                        editProfileViewModel.removeProfilePicture()
                        showDialog = false
                      }) {
                        Text(
                            BUTTON_REMOVE_PICTURE,
                            color = MaterialTheme.colorScheme.onErrorContainer)
                      }
                }

                Spacer(modifier = Modifier.height(EditProfileNumberConstants.SPACER_SMALL))

                OutlinedButton(
                    modifier =
                        Modifier.fillMaxWidth().testTag(EditProfileTestTags.DIALOG_CANCEL_BUTTON),
                    onClick = { showDialog = false }) {
                      Text(BUTTON_CANCEL)
                    }
              }
        })
  }

  EditProfileContent(
      uiState = uiState,
      onPseudonymChange = editProfileViewModel::updatePseudonym,
      onBioChange = editProfileViewModel::updateBio,
      onCancel = {
        editProfileViewModel.cancelChanges()
        onGoBack()
      },
      onSave = {
        editProfileViewModel.saveProfile()
        if (uiState.success) onDone()
      },
      onProfilePictureChange = { showDialog = true },
      profilePictureUri = uiState.profilePictureUri)
}

fun createImageUri(context: Context): Uri? {
  val contentValues =
      ContentValues().apply {
        put(MediaStore.Images.Media.MIME_TYPE, EditProfileStrings.IMAGE_JPEG)
      }
  return try {
    context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
  } catch (e: Exception) {
    Log.e(EditProfileStrings.IMAGE_URI, EditProfileStrings.ERROR_URI, e)
    null
  }
}

/**
 * Composable that displays the editable content of the Edit Profile screen.
 *
 * Handles text input for pseudonym and bio, shows error/success messages, and provides buttons for
 * saving, canceling, or changing the profile picture.
 *
 * @param uiState The current UI state containing field values and flags.
 * @param onPseudonymChange Callback when the pseudonym text changes.
 * @param onBioChange Callback when the bio text changes.
 * @param onCancel Callback when the cancel button is clicked.
 * @param onSave Callback when the save button is clicked.
 * @param onProfilePictureChange Callback when the profile picture is clicked.
 * @param profilePictureUri Optional [Uri] for a locally selected image preview.
 */
@Composable
fun EditProfileContent(
    uiState: EditProfileUIState,
    onPseudonymChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onProfilePictureChange: () -> Unit,
    profilePictureUri: Uri? = null
) {
  var pseudonymError by remember { mutableStateOf<String?>(null) }
  var bioError by remember { mutableStateOf<String?>(null) }
  var localError by remember { mutableStateOf<String?>(null) }

  val isLoading = uiState.isLoading

  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(EditProfileNumberConstants.SCREEN_PADDING)
              .testTag(EditProfileTestTags.SCREEN),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier.size(EditProfileNumberConstants.PROFILE_PICTURE_SIZE)
                    .clickable { onProfilePictureChange() }
                    .testTag(EditProfileTestTags.PROFILE_PICTURE)) {
              ProfilePicture(
                  profilePictureRes = uiState.profilePicture,
                  profilePictureUri = profilePictureUri,
                  profilePictureUrl = uiState.profilePictureUrl,
                  modifier = Modifier.fillMaxSize())
              if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).size(24.dp))
              } else {
                Text(
                    text = EditProfileStrings.PLUS,
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimary),
                    fontSize = EditProfileNumberConstants.ADD_ICON_FONT_SIZE,
                    modifier =
                        Modifier.align(Alignment.BottomEnd)
                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                            .padding(EditProfileNumberConstants.PADDING_SMALL))
              }
            }

        Spacer(modifier = Modifier.height(EditProfileNumberConstants.SPACER_LARGE))

        OutlinedTextField(
            value = uiState.pseudonym,
            onValueChange = { newValue ->
              if (!isLoading) {
                onPseudonymChange(newValue)
                pseudonymError =
                    when {
                      newValue.isBlank() -> ERROR_PSEUDONYM_EMPTY
                      newValue.length > MAX_PSEUDONYM_LENGTH -> ERROR_PSEUDONYM_MAX
                      else -> null
                    }
              }
            },
            label = { Text(FIELD_LABEL_PSEUDONYM) },
            enabled = !isLoading,
            isError = pseudonymError != null,
            modifier = Modifier.fillMaxWidth().testTag(EditProfileTestTags.PSEUDONYM_FIELD))

        if (pseudonymError != null) {
          Text(
              text = pseudonymError!!,
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.align(Alignment.Start))
        }

        Spacer(modifier = Modifier.height(EditProfileNumberConstants.SPACER_MEDIUM))

        OutlinedTextField(
            value = uiState.bio,
            onValueChange = { newValue ->
              if (!isLoading) {
                onBioChange(newValue)
                bioError =
                    when {
                      newValue.length > MAX_BIO_LENGTH -> ERROR_BIO_MAX
                      else -> null
                    }
              }
            },
            label = { Text(FIELD_LABEL_BIO) },
            enabled = !isLoading,
            modifier =
                Modifier.fillMaxWidth()
                    .heightIn(min = EditProfileNumberConstants.BIO_FIELD_MIN_HEIGHT)
                    .testTag(EditProfileTestTags.BIO_FIELD))
        if (bioError != null) {
          Text(
              text = bioError!!,
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.align(Alignment.Start))
        }

        Spacer(modifier = Modifier.height(EditProfileNumberConstants.SPACER_LARGE))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
          OutlinedButton(
              modifier = Modifier.testTag(EditProfileTestTags.CANCEL_BUTTON),
              onClick = onCancel,
              enabled = !isLoading) {
                Text(BUTTON_CANCEL)
              }
          Button(
              modifier = Modifier.testTag(EditProfileTestTags.SAVE_BUTTON),
              onClick = {
                if (uiState.pseudonym.isBlank()) {
                  localError = ERROR_PSEUDONYM_EMPTY
                } else {
                  onSave()
                }
              },
              enabled =
                  !isLoading &&
                      uiState.canSave &&
                      pseudonymError == null &&
                      bioError == null &&
                      !uiState.isSaving) {
                Text(if (uiState.isSaving) BUTTON_SAVING else BUTTON_SAVE)
              }
        }

        Spacer(modifier = Modifier.height(EditProfileNumberConstants.SPACER_MEDIUM))

        when {
          localError != null ->
              Text(
                  text = localError!!,
                  color = MaterialTheme.colorScheme.error,
                  modifier = Modifier.testTag(EditProfileTestTags.ERROR_MESSAGE))
          uiState.errorMsg != null ->
              Text(
                  "Error: ${uiState.errorMsg}",
                  color = MaterialTheme.colorScheme.error,
                  modifier = Modifier.testTag(EditProfileTestTags.ERROR_MESSAGE))
          uiState.success ->
              Text(
                  SUCCESS_UPDATE,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.testTag(EditProfileTestTags.SUCCESS_MESSAGE))
        }
      }
}
