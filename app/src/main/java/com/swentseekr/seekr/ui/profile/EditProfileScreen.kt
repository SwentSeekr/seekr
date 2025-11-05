package com.swentseekr.seekr.ui.profile

import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.ui.theme.SampleAppTheme
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

  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

  var showDialog by remember { mutableStateOf(false) }

  val galleryLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) selectedImageUri = uri
      }

  var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
  val cameraLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) selectedImageUri = cameraPhotoUri
      }

  fun createTempImageUri(): Uri {
    val imageFile = File(context.cacheDir, "temp_profile.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
  }

  LaunchedEffect(userId) {
    if (!testMode && userId != null) editProfileViewModel.loadProfile(userId)
  }

  if (showDialog) {
    AlertDialog(
        modifier = Modifier.testTag(EditProfileTestTags.DIALOG),
        onDismissRequest = { showDialog = false },
        title = { Text("Choose Image") },
        text = { Text("Pick a source for your new profile picture") },
        confirmButton = {
          Button(
              modifier = Modifier.testTag(EditProfileTestTags.GALLERY_BUTTON),
              onClick = {
                galleryLauncher.launch("image/*")
                showDialog = false
              }) {
                Text("Gallery")
              }
        },
        dismissButton = {
          Row {
            Button(
                modifier = Modifier.testTag(EditProfileTestTags.CAMERA_BUTTON),
                onClick = {
                  cameraPhotoUri = createTempImageUri()
                  cameraLauncher.launch(cameraPhotoUri)
                  showDialog = false
                }) {
                  Text("Camera")
                }

            Spacer(modifier = Modifier.width(EditProfileConstants.SPACER_SMALL))

            Button(
                modifier = Modifier.testTag("DIALOG_CANCEL_BUTTON"),
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
      profilePictureUri = selectedImageUri)
}

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
  var localError by remember { mutableStateOf<String?>(null) }
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
                  modifier = Modifier.fillMaxSize())
              Text(
                  text = "+",
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  fontSize = EditProfileConstants.ADD_ICON_FONT_SIZE,
                  modifier =
                      Modifier.align(Alignment.BottomEnd)
                          .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                          .padding(4.dp))
            }

        Spacer(modifier = Modifier.height(EditProfileConstants.SPACER_LARGE))

        OutlinedTextField(
            value = uiState.pseudonym,
            onValueChange = onPseudonymChange,
            label = { Text("Pseudonym") },
            modifier = Modifier.fillMaxWidth().testTag(EditProfileTestTags.PSEUDONYM_FIELD))

        OutlinedTextField(
            value = uiState.bio,
            onValueChange = onBioChange,
            label = { Text("Bio") },
            modifier =
                Modifier.fillMaxWidth()
                    .heightIn(min = EditProfileConstants.BIO_FIELD_MIN_HEIGHT)
                    .testTag(EditProfileTestTags.BIO_FIELD))

        Spacer(modifier = Modifier.height(EditProfileConstants.SPACER_LARGE))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
          OutlinedButton(
              modifier = Modifier.testTag(EditProfileTestTags.CANCEL_BUTTON), onClick = onCancel) {
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
              enabled = uiState.canSave && !uiState.isSaving) {
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditProfileScreenPreview() {
  val fakeState =
      EditProfileUIState(
          pseudonym = "Explorer42",
          bio = "Adventurer and puzzle lover.",
          profilePicture = 0,
          canSave = true,
          hasChanges = true,
          isSaving = false,
          success = false,
          errorMsg = null)

  SampleAppTheme {
    EditProfileContent(
        uiState = fakeState,
        onPseudonymChange = {},
        onBioChange = {},
        onCancel = {},
        onSave = {},
        onProfilePictureChange = {},
        profilePictureUri = null)
  }
}
