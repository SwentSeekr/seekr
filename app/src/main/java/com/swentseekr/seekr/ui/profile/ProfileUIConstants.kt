package com.swentseekr.seekr.ui.profile

import androidx.compose.ui.graphics.Color
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

/**
 * Centralized UI constants extracted from ProfileScreen to remove hardcoded values while preserving
 * identical UI for tests.
 */
object ProfileUIConstantsDefaults {

  // ---- GENERAL PADDINGS ----
  val Padding4 = 4.dp
  val Padding8 = 8.dp
  val Padding12 = 12.dp
  val Padding16 = 16.dp
  val Padding20 = 20.dp
  val Padding24 = 24.dp
  val Padding32 = 32.dp
  val Padding60 = 60.dp

  // ---- SIZES ----
  val Size2 = 2.dp
  val Size8 = 8.dp
  val Size16 = 16.dp
  val Size20 = 20.dp
  val Size24 = 24.dp
  val Size28 = 28.dp
  val Size40 = 40.dp
  val Size64 = 64.dp
  val Size70 = 70.dp

  // ---- FONT SIZES ----
  val Font12 = 12.sp
  val Font14 = 14.sp
  val Font16 = 16.sp
  val Font18 = 18.sp
  val Font20 = 20.sp
  val Font22 = 22.sp

  // ---- COLORS ----
  val LightGrayBackground = Color(0xFFF8F9FA)
  val LoadingGray = Color(0xFF666666)
  val LoadingIndicatorGreen = Color(0xFF00C853)
  val ErrorRed = Color(0xFFEF5350)
  val ToolbarGreen = Color(0xFF00C853)
  val TabInactiveGray = Color(0xFF999999)
  val IconGray = Color(0xFFCCCCCC)

  // ---- PROFILE HEADER ----
  val ProfileHeaderGradientEnd = Color(0xFFE8847C)

  // ---- TEST STRINGS ----
  const val TabMyHuntsLabel = "My Hunts"
  const val TabDoneLabel = "Done"
  const val TabLikedLabel = "Liked"

  // ---- EMPTY STATE ----
  val EmptyIconSize = 64.dp
  val EmptyTextColor = Color(0xFF999999)
  const val EmptyText = "No hunts yet"

  // ---- ALPHA VALUES ----
  const val AlphaLight = 0.1f
  const val AlphaMedium = 0.85f
  const val AlphaLow = 0.2f
  // ---- WEIGHTS ----
  const val Weight = 1f
}
