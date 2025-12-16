package com.swentseekr.seekr.ui.profile

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
  const val SINGLE_REVIEW = 1

  const val ONE_DECIMAL_FORMAT = "%.1f"
  const val SINGLE_REVIEW_LABEL = "- %d review"
  const val MULTIPLE_REVIEWS_LABEL = "- %d reviews"
  const val HUNTS_DONE_LABEL = "%d Hunts done"
  const val EMPTY_REVIEW_RATE = 0.0
}

/** Centralized constants for UI dimensions. */
object EditProfileNumberConstants {
  val ALPHA_CHANGE = 0.3f
  val ALPHA_MID = 0.5f

  val PROFILE_PICTURE_SIZE = 140.dp
  val PROFILE_PICTURE_SHADOW = 12.dp
  val PROFILE_SURFACE = 40.dp
  val PROFILE_ELEVATION = 6.dp

  val ICON_PADDING = 8.dp

  val BIO_FIELD_MIN_HEIGHT = 120.dp
  val SCREEN_PADDING = 24.dp

  val SPACER_TINY = 4.dp
  val SPACER_MEDIUM = 12.dp
  val SPACER_LARGE = 16.dp
  val SPACER_SMALL = 8.dp

  val SIZE_LARGE = 20.dp

  val PADDING_GIGANTIC = 20.dp
  val PADDING_BIG = 16.dp
  val PADDING_SMALL = 4.dp

  val VERTICAL_ARR = 12.dp
  val VERTICAL_ARR_MEDIUM = 16.dp
  val VERTICAL_ARR_LARGE = 20.dp
  val HORIZONTAL_ARR = 12.dp

  val BUTTON_HEIGHT_DP = 50.dp
  val BUTTON_HEIGHT = 52.dp
  val BUTTON_WEIGHT = 1f

  val ROUND_CORNER_BIG = 20.dp
  val ROUND_CORNER_MID = 12.dp

  val PROGRESS_INDIC = 32.dp
  val PROGRESS_STROKE = 3.dp

  val ELEVATION_SMALL = 2.dp
  val ELEVATION_MEDIUM = 4.dp

  const val PROFILE_PICTURE_SIZE_DP = 140

  const val PROFILE_PIC_DEFAULT = 0
  const val MAX_PSEUDONYM_LENGTH = 30
  const val MAX_BIO_LENGTH = 30
  const val MIN_PSEUDONYM_LENGTH = 3

  val STROKE_WIDTH = 2.dp
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

  const val CHECKING_AVAILABILITY = "Checking availability..."
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

  val Padding60 = 60.dp

  // ---- SIZES ----

  val Size20 = 20.dp
  val Size24 = 24.dp
  val Size28 = 28.dp
  val Size70 = 70.dp

  // ---- PROFILE HEADER ----
  val ProfileHeaderGradientEnd = Color(0xFFE8847C)

  // ---- TEST STRINGS ----
  const val TabMyHuntsLabel = "My Hunts"
  const val TabDoneLabel = "Done"
  const val TabLikedLabel = "Liked"

  // ---- EMPTY STATE ----
  val EmptyIconSize = 64.dp
  const val EmptyText = "No hunts yet"

  // ---- ALPHA VALUES ----
  const val AlphaLight = 0.1f
  const val AlphaMedium = 0.85f
  const val AlphaLow = 0.2f
  // ---- WEIGHTS ----
  const val Weight = 1f
}

object ProfileReviewsScreenConstant {
  const val SPACER_HEIGHT = 4
  const val HORIZONTAL_DIVIDER_PADDING = 8
  const val COLUMN_PADDING = 16
  const val SINGLE_REVIEW = "%s/%s - %d review"
  const val MULTIPLE_REVIEWS = "%s/%s - %d reviews"
  const val STRING_FORMAT = "%.1f"
  const val HEADER_KEY = "hunt_header_%s"
  const val DETAIL_ROUTE = "hunt/%s"
  const val RATING_FORMAT = "%s/%s"
  const val DIVIDER = "divider_%s"
}
