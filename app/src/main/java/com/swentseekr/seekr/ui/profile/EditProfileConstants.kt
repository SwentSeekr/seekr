package com.swentseekr.seekr.ui.profile

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Centralized constants for UI dimensions. */
object EditProfileNumberConstants {
  val PROFILE_PICTURE_SIZE = 80.dp
  val BIO_FIELD_MIN_HEIGHT = 120.dp
  val SCREEN_PADDING = 24.dp
  val SPACER_MEDIUM = 12.dp
  val SPACER_LARGE = 16.dp
  val ADD_ICON_FONT_SIZE = 24.sp
}

object EditProfileStrings {
  const val DIALOG_TITLE = "Choose Image"
  const val DIALOG_MESSAGE = "Pick a source for your new profile picture"

  const val BUTTON_GALLERY = "Gallery"
  const val BUTTON_CAMERA = "Camera"
  const val BUTTON_REMOVE_PICTURE = "Remove Picture"
  const val BUTTON_CANCEL = "Cancel"
  const val BUTTON_SAVE = "Save"
  const val BUTTON_SAVING = "Saving..."

  const val FIELD_LABEL_PSEUDONYM = "Pseudonym"
  const val FIELD_LABEL_BIO = "Bio"

  const val ERROR_PSEUDONYM_EMPTY = "Pseudonym cannot be empty"
  const val ERROR_PSEUDONYM_MAX = "Max 30 characters allowed"
  const val ERROR_BIO_MAX = "Max 200 characters allowed"

  const val SUCCESS_UPDATE = "Profile updated!"
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

object EditProfileImageConstants {
  const val PROFILE_PIC_DEFAULT = 0
  const val MAX_PSEUDONYM_LENGTH = 30
  const val MAX_BIO_LENGTH = 30
}
