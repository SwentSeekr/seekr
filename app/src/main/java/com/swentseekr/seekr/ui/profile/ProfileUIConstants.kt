package com.swentseekr.seekr.ui.profile

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Centralized constants, strings, and test tags used across both the Profile screen and Edit
 * Profile screen.
 *
 * NOTE: Despite the "EditProfile" prefix in object names, these objects are referenced in multiple
 * places, not just the edit screen.
 *
 * This file is intentionally not split to avoid breaking existing usage across many references in
 * the project.
 */
object ProfileScreenConstants {
  val ICON_BUTTON_SIZE_DP = 40.dp
  const val ICON_BUTTON_WHITE_ALPHA = 0.9f
  const val ICON_BUTTON_GOBACK = "Go Back"
}

/** Centralized constants for UI dimensions. */
object EditProfileNumberConstants {
  val PROFILE_PICTURE_SIZE = 80.dp
  val BIO_FIELD_MIN_HEIGHT = 120.dp
  val SCREEN_PADDING = 24.dp
  val SPACER_MEDIUM = 12.dp
  val SPACER_LARGE = 16.dp
  val ADD_ICON_FONT_SIZE = 24.sp
  val SPACER_SMALL = 8.dp
  val PADDING_SMALL = 4.dp
  const val PROFILE_PICTURE_SIZE_DP = 100

  const val PROFILE_PIC_DEFAULT = 0
  const val MAX_PSEUDONYM_LENGTH = 30
  const val MAX_BIO_LENGTH = 30
  const val MIN_PSEUDONYM_LENGTH = 3
}

object EditProfileStrings {
  const val PLUS = "+"
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
  const val CAMERA_LAUNCH = "CameraLaunch"

  const val ERROR_PSEUDONYM_EMPTY = "Pseudonym cannot be empty"
  const val ERROR_PSEUDONYM_MAX = "Max 30 characters allowed"
  const val ERROR_BIO_MAX = "Max 200 characters allowed"

  const val SUCCESS_UPDATE = "Profile updated!"
  const val EMPTY_STRING = ""
  const val LOG_IN_ERROR = "User not logged in"
  const val LOAD_PROFILE_ERROR = "Failed to load profile"
  const val SAVE_PROFILE_ERROR = "Failed to save profile"
  const val NO_PROFILE = "Profile not found"
  const val LOAD_USER_ERROR = "User not loaded"
  const val CAMERA_PERMISSION_ERROR = "Could not create image file. Check storage permissions."
  const val ERROR = "Error"
  const val OK = "OK"
  const val IMAGE = "image/*"
  const val IMAGE_JPEG = "image/jpeg"
  const val IMAGE_URI = "CreateImageUri"
  const val ERROR_URI = "Failed to create image URI"
  const val SERVER_ERROR = "Some error"
  const val REPO_ERROR = "Repo error"
  const val NEW_TEST_NAME = "NewName"
  const val NEW_TEST_BIO = "New bio text"
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
  const val DIALOG_CANCEL_BUTTON = "DIALOG_CANCEL_BUTTON"
}
