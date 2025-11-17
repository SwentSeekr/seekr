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
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File

/** Centralized constants for UI dimensions. */
object EditProfileConstants {
  val PROFILE_PICTURE_SIZE = 80.dp
  val BIO_FIELD_MIN_HEIGHT = 120.dp
  val SCREEN_PADDING = 24.dp
  val SPACER_SMALL = 8.dp
  val SPACER_MEDIUM = 12.dp
  val SPACER_LARGE = 16.dp
  val ADD_ICON_FONT_SIZE = 24.sp
}

/**
 * Centralized test tags for identifying composable nodes in tests.
 *
 * Used in instrumented and UI tests to find elements using `composeTestRule.onNodeWithTag(...)`.
 */
object EditProfileTestTags {
  const val SCREEN = "EDIT_PROFILE_SCREEN"
  const val PROFILE_PICTURE = "PROFILE_PICTURE"
  const val DIALOG = "PROFILE_PICTURE_DIALOG"
  const val GALLERY_BUTTON = "GALLERY_BUTTON"
  const val CAMERA_BUTTON = "CAMERA_BUTTON"
  const val PSEUDONYM_FIELD = "PSEUDONYM_FIELD"
  const val BIO_FIELD = "BIO_FIELD"
  const val CANCEL_BUTTON = "CANCEL_BUTTON"
  const val SAVE_BUTTON = "SAVE_BUTTON"
  const val ERROR_MESSAGE = "ERROR_MESSAGE"
  const val SUCCESS_MESSAGE = "SUCCESS_MESSAGE"
}

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

  // var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

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

  fun createTempImageUri(): Uri {
    val imageFile = File(context.cacheDir, "temp_profile.jpg")
    if (!imageFile.exists()) imageFile.createNewFile()
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
  }

  fun launchCamera() {
    try {
      val uri = createImageUri(context)
      cameraPhotoUri.value = uri
      cameraLauncher.launch(uri)
    } catch (e: Exception) {
      Log.e("CameraLaunch", "Failed to launch camera: ${e.message}", e)
    }
  }

  LaunchedEffect(Unit) {
    if (!testMode && userId != null) editProfileViewModel.loadProfile()
    editProfileViewModel.uiState.collect { if (it.success) onDone() }
  }

  if (showDialog) {
    AlertDialog(
        modifier = Modifier.testTag(EditProfileTestTags.DIALOG),
        onDismissRequest = { showDialog = false },
        title = { Text("Choose Image") },
        text = { Text("Pick a source for your new profile picture") },
        confirmButton = {
          Column(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    modifier = Modifier.fillMaxWidth().testTag(EditProfileTestTags.GALLERY_BUTTON),
                    onClick = {
                      galleryLauncher.launch("image/*")
                      showDialog = false
                    }) {
                      Text("Gallery")
                    }

                Button(
                    modifier = Modifier.fillMaxWidth().testTag(EditProfileTestTags.CAMERA_BUTTON),
                    onClick = {
                      launchCamera()
                      showDialog = false
                    }) {
                      Text("Camera")
                    }

                if (uiState.profilePicture != 0 ||
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
                        Text("Remove Picture", color = MaterialTheme.colorScheme.onErrorContainer)
                      }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth().testTag("DIALOG_CANCEL_BUTTON"),
                    onClick = { showDialog = false }) {
                      Text("Cancel")
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

fun createImageUri(context: Context): Uri {
  val contentValues = ContentValues().apply { put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") }
  return context.contentResolver.insert(
      MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
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
              .padding(EditProfileConstants.SCREEN_PADDING)
              .testTag(EditProfileTestTags.SCREEN),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier.size(EditProfileConstants.PROFILE_PICTURE_SIZE)
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
                    text = "+",
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimary),
                    fontSize = EditProfileConstants.ADD_ICON_FONT_SIZE,
                    modifier =
                        Modifier.align(Alignment.BottomEnd)
                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                            .padding(4.dp))
              }
            }

        Spacer(modifier = Modifier.height(EditProfileConstants.SPACER_LARGE))

        OutlinedTextField(
            value = uiState.pseudonym,
            onValueChange = { newValue ->
              if (!isLoading) {
                onPseudonymChange(newValue)
                pseudonymError =
                    when {
                      newValue.isBlank() -> "Pseudonym cannot be empty"
                      newValue.length > 30 -> "Max 30 characters allowed"
                      else -> null
                    }
              }
            },
            label = { Text("Pseudonym") },
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

        Spacer(modifier = Modifier.height(EditProfileConstants.SPACER_MEDIUM))

        OutlinedTextField(
            value = uiState.bio,
            onValueChange = { newValue ->
              if (!isLoading) {
                onBioChange(newValue)
                bioError =
                    when {
                      newValue.length > 200 -> "Max 200 characters allowed"
                      else -> null
                    }
              }
            },
            label = { Text("Bio") },
            enabled = !isLoading,
            modifier =
                Modifier.fillMaxWidth()
                    .heightIn(min = EditProfileConstants.BIO_FIELD_MIN_HEIGHT)
                    .testTag(EditProfileTestTags.BIO_FIELD))
        if (bioError != null) {
          Text(
              text = bioError!!,
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.align(Alignment.Start))
        }

        Spacer(modifier = Modifier.height(EditProfileConstants.SPACER_LARGE))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
          OutlinedButton(
              modifier = Modifier.testTag(EditProfileTestTags.CANCEL_BUTTON),
              onClick = onCancel,
              enabled = !isLoading) {
                Text("Cancel")
              }
          Button(
              modifier = Modifier.testTag(EditProfileTestTags.SAVE_BUTTON),
              onClick = {
                if (uiState.pseudonym.isBlank()) {
                  localError = "Pseudonym cannot be empty"
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
                Text(if (uiState.isSaving) "Saving..." else "Save")
              }
        }

        Spacer(modifier = Modifier.height(EditProfileConstants.SPACER_MEDIUM))

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
                  "Profile updated!",
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.testTag(EditProfileTestTags.SUCCESS_MESSAGE))
        }
      }
}
