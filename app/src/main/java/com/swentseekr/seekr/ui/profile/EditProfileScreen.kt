package com.swentseekr.seekr.ui.profile

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.ui.auth.OnboardingFlowDimensions
import com.swentseekr.seekr.ui.auth.OnboardingFlowStrings
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants.MAX_BIO_LENGTH
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
import com.swentseekr.seekr.ui.profile.EditProfileStrings.FIELD_LABEL_BIO
import com.swentseekr.seekr.ui.profile.EditProfileStrings.FIELD_LABEL_PSEUDONYM
import com.swentseekr.seekr.ui.profile.EditProfileStrings.SUCCESS_UPDATE

val UI_C = EditProfileNumberConstants

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
        title = { Text(DIALOG_TITLE, style = MaterialTheme.typography.headlineSmall) },
        text = { Text(DIALOG_MESSAGE) },
        confirmButton = {
          Column(
              modifier = Modifier.fillMaxWidth().padding(horizontal = UI_C.PADDING_BIG),
              verticalArrangement = Arrangement.spacedBy(UI_C.VERTICAL_ARR)) {
                FilledTonalButton(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(UI_C.BUTTON_HEIGHT_DP)
                            .testTag(EditProfileTestTags.GALLERY_BUTTON),
                    onClick = {
                      galleryLauncher.launch(EditProfileStrings.IMAGE)
                      showDialog = false
                    },
                    shape = RoundedCornerShape(UI_C.ROUND_CORNER_MID)) {
                      Text(BUTTON_GALLERY)
                    }

                FilledTonalButton(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(UI_C.BUTTON_HEIGHT_DP)
                            .testTag(EditProfileTestTags.CAMERA_BUTTON),
                    onClick = {
                      launchCamera()
                      showDialog = false
                    },
                    shape = RoundedCornerShape(UI_C.ROUND_CORNER_MID)) {
                      Text(BUTTON_CAMERA)
                    }

                if (uiState.profilePicture != PROFILE_PIC_DEFAULT ||
                    uiState.profilePictureUri != null ||
                    uiState.profilePictureUrl.isNotEmpty()) {
                  Button(
                      modifier = Modifier.fillMaxWidth().height(UI_C.BUTTON_HEIGHT_DP),
                      shape = RoundedCornerShape(UI_C.ROUND_CORNER_MID),
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

                Spacer(modifier = Modifier.height(UI_C.SPACER_TINY))

                OutlinedButton(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(UI_C.BUTTON_HEIGHT_DP)
                            .testTag(EditProfileTestTags.DIALOG_CANCEL_BUTTON),
                    onClick = { showDialog = false },
                    shape = RoundedCornerShape(UI_C.ROUND_CORNER_MID)) {
                      Text(BUTTON_CANCEL)
                    }
              }
        })
  }

  EditProfileContent(
      uiState = uiState,
      onPseudonymChange = editProfileViewModel::validatePseudonym,
      pseudonymError = uiState.pseudonymError,
      isCheckingPseudonym = uiState.isCheckingPseudonym,
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

@Composable
fun EditProfileContent(
    uiState: EditProfileUIState,
    onPseudonymChange: (String) -> Unit,
    pseudonymError: String?,
    isCheckingPseudonym: Boolean,
    onBioChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onProfilePictureChange: () -> Unit,
    profilePictureUri: Uri? = null
) {
  var bioError by remember { mutableStateOf<String?>(null) }
  var localError by remember { mutableStateOf<String?>(null) }
  var pseudonym = uiState.pseudonym

  val isLoading = uiState.isLoading

  val scale by animateFloatAsState(if (isLoading) 0.95f else 1f)

  Column(
      modifier =
          Modifier.fillMaxSize()
              .background(
                  Brush.verticalGradient(
                      colors =
                          listOf(
                              MaterialTheme.colorScheme.surface,
                              MaterialTheme.colorScheme.surfaceVariant.copy(
                                  alpha = UI_C.ALPHA_CHANGE))))
              .padding(UI_C.SCREEN_PADDING)
              .testTag(EditProfileTestTags.SCREEN),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(UI_C.VERTICAL_ARR_LARGE)) {
        Spacer(modifier = Modifier.height(UI_C.SPACER_LARGE))

        Box(
            modifier =
                Modifier.size(UI_C.PROFILE_PICTURE_SIZE)
                    .scale(scale)
                    .shadow(UI_C.PROFILE_PICTURE_SHADOW, CircleShape)
                    .clickable { onProfilePictureChange() }
                    .testTag(EditProfileTestTags.PROFILE_PICTURE)) {
              ProfilePicture(
                  profilePictureRes = uiState.profilePicture,
                  profilePictureUri = profilePictureUri,
                  profilePictureUrl = uiState.profilePictureUrl,
                  modifier = Modifier.fillMaxSize())

              if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).size(UI_C.PROGRESS_INDIC),
                    strokeWidth = UI_C.PROGRESS_STROKE)
              } else {
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).size(UI_C.PROFILE_SURFACE),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = UI_C.PROFILE_ELEVATION) {
                      Icon(
                          imageVector = Icons.Filled.Create,
                          contentDescription = "Edit",
                          tint = MaterialTheme.colorScheme.onPrimary,
                          modifier = Modifier.padding(UI_C.ICON_PADDING))
                    }
              }
            }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(UI_C.ROUND_CORNER_BIG),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = UI_C.ELEVATION_SMALL)) {
              Column(
                  modifier = Modifier.padding(UI_C.PADDING_GIGANTIC),
                  verticalArrangement = Arrangement.spacedBy(UI_C.VERTICAL_ARR_MEDIUM)) {
                    OutlinedTextField(
                        value = pseudonym,
                        onValueChange = {
                          pseudonym = it
                          onPseudonymChange(it)
                        },
                        label = { Text(FIELD_LABEL_PSEUDONYM) },
                        enabled = !isLoading,
                        isError = pseudonymError != null,
                        supportingText = {
                          when {
                            isCheckingPseudonym -> {
                              Row(
                                  verticalAlignment = Alignment.CenterVertically,
                                  horizontalArrangement =
                                      Arrangement.spacedBy(
                                          OnboardingFlowDimensions.SPACING_SMALL)) {
                                    CircularProgressIndicator(
                                        modifier =
                                            Modifier.size(OnboardingFlowDimensions.SIZE_MEDIUM),
                                        strokeWidth = OnboardingFlowDimensions.STROKE_WIDTH)
                                    Text(
                                        OnboardingFlowStrings.CHECKING_AVAILABILITY,
                                        style = MaterialTheme.typography.bodySmall)
                                  }
                            }
                            pseudonymError != null -> {
                              Text(pseudonymError, color = MaterialTheme.colorScheme.error)
                            }
                          }
                        },
                        trailingIcon = {
                          AnimatedVisibility(
                              visible = isCheckingPseudonym, enter = fadeIn(), exit = fadeOut()) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(OnboardingFlowDimensions.SIZE_LARGE),
                                    strokeWidth = OnboardingFlowDimensions.STROKE_WIDTH)
                              }
                        },
                        modifier =
                            Modifier.fillMaxWidth().testTag(EditProfileTestTags.PSEUDONYM_FIELD),
                        shape = RoundedCornerShape(UI_C.ROUND_CORNER_MID),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor =
                                    MaterialTheme.colorScheme.outline.copy(alpha = UI_C.ALPHA_MID),
                                focusedBorderColor = MaterialTheme.colorScheme.primary))

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
                                .heightIn(min = UI_C.BIO_FIELD_MIN_HEIGHT)
                                .testTag(EditProfileTestTags.BIO_FIELD),
                        shape = RoundedCornerShape(UI_C.ROUND_CORNER_MID),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor =
                                    MaterialTheme.colorScheme.outline.copy(alpha = UI_C.ALPHA_MID),
                                focusedBorderColor = MaterialTheme.colorScheme.primary))

                    AnimatedVisibility(
                        visible = bioError != null,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()) {
                          bioError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = UI_C.PADDING_SMALL))
                          }
                        }
                  }
            }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UI_C.HORIZONTAL_ARR)) {
              OutlinedButton(
                  modifier =
                      Modifier.weight(UI_C.BUTTON_WEIGHT)
                          .height(UI_C.BUTTON_HEIGHT)
                          .testTag(EditProfileTestTags.CANCEL_BUTTON),
                  onClick = onCancel,
                  enabled = !isLoading,
                  shape = RoundedCornerShape(UI_C.ROUND_CORNER_MID)) {
                    Text(BUTTON_CANCEL)
                  }
              Button(
                  modifier =
                      Modifier.weight(UI_C.BUTTON_WEIGHT)
                          .height(UI_C.BUTTON_HEIGHT)
                          .testTag(EditProfileTestTags.SAVE_BUTTON),
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
                          !uiState.isSaving,
                  shape = RoundedCornerShape(UI_C.ROUND_CORNER_MID),
                  elevation =
                      ButtonDefaults.buttonElevation(defaultElevation = UI_C.ELEVATION_MEDIUM)) {
                    Text(if (uiState.isSaving) BUTTON_SAVING else BUTTON_SAVE)
                  }
            }

        AnimatedVisibility(
            visible = localError != null || uiState.errorMsg != null || uiState.success,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()) {
              Card(
                  colors =
                      CardDefaults.cardColors(
                          containerColor =
                              when {
                                localError != null || uiState.errorMsg != null ->
                                    MaterialTheme.colorScheme.errorContainer
                                uiState.success -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surface
                              }),
                  shape = RoundedCornerShape(UI_C.ROUND_CORNER_MID)) {
                    when {
                      localError != null ->
                          Text(
                              text = localError!!,
                              color = MaterialTheme.colorScheme.onErrorContainer,
                              modifier =
                                  Modifier.padding(UI_C.PADDING_BIG)
                                      .testTag(EditProfileTestTags.ERROR_MESSAGE))
                      uiState.errorMsg != null ->
                          Text(
                              "Error: ${uiState.errorMsg}",
                              color = MaterialTheme.colorScheme.onErrorContainer,
                              modifier =
                                  Modifier.padding(UI_C.PADDING_BIG)
                                      .testTag(EditProfileTestTags.ERROR_MESSAGE))
                      uiState.success ->
                          Text(
                              SUCCESS_UPDATE,
                              color = MaterialTheme.colorScheme.onPrimaryContainer,
                              modifier =
                                  Modifier.padding(UI_C.PADDING_BIG)
                                      .testTag(EditProfileTestTags.SUCCESS_MESSAGE))
                    }
                  }
            }
      }
}
