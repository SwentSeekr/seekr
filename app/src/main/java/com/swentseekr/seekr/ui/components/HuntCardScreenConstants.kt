package com.swentseekr.seekr.ui.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Provides default UI values, localized strings, and test tags used across the Hunt Card screen.
 *
 * This file contains three main objects:
 * - **HuntCardScreenDefaults**: Centralizes all styling constants such as dimensions, colors,
 *   elevations, font sizes, scaling values, and layout configurations used by the Hunt Card UI.
 *   These defaults help maintain visual consistency and simplify UI updates.
 * - **HuntCardScreenStrings**: Contains user-facing text constants and accessibility descriptions
 *   related to the Hunt Card screen. Keeping strings here supports reusability and improves
 *   maintainability, especially for localization and accessibility.
 * - **HuntCardScreenTestTags**: Defines semantic tags used for UI testing with Compose Test APIs.
 *   These tags ensure stable and descriptive identifiers for automated tests targeting visual
 *   components such as buttons, text fields, maps, image carousels, and review elements.
 *
 * Together, these objects form a cohesive configuration layer that standardizes appearance,
 * behavior, accessibility, and testability for the Hunt Card screen components.
 */
object HuntCardScreenDefaults {
  // ---------------
  // Card elevation and shape
  // ---------------
  val CardElevation: Dp = 2.dp
  val CornerRadius: Dp = 12.dp
  val ReviewCardCornerRadius: Dp = 16.dp
  val ZeroElevation: Dp = 0.dp

  // ---------------
  // Profile and avatar
  // ---------------
  val ProfilePictureSize: Dp = 32.dp
  const val INITIAL_LETTER_COUNT: Int = 1
  const val NO_PROFILE_PICTURE_RES_ID: Int = 0

  // ---------------
  // Image carousel layout
  // ---------------
  val ImageCarouselHeight: Dp = 120.dp
  val AspectRatioHero: Float = 16f / 9f
  val ImageIndicatorDotSize: Dp = 6.dp
  val ImageIndicatorDotSelectedSize: Dp = 8.dp
  val ImageIndicatorDotSpacing: Dp = 4.dp
  val ImageIndicatorTopPadding: Dp = 4.dp
  const val IMAGE_INDICATOR_LAST_INDEX_OFFSET: Int = 1

  // ---------------
  // Image carousel animation
  // ---------------
  const val IMAGE_CAROUSEL_MIN_SCALE: Float = 0.85f
  const val IMAGE_CAROUSEL_MAX_SCALE: Float = 1.08f
  const val IMAGE_CAROUSEL_SIDE_ROTATION_DEGREE: Float = 10f
  const val IMAGE_CAROUSEL_ROTATION_CENTER_DEGREES: Float = 0f
  const val IMAGE_CAROUSEL_OVERLAYER_MAX_ALPHA: Float = 0.35f
  const val IMAGE_CAROUSEL_OVERLAYER_MIN_ALPHA: Float = 0f
  const val IMAGE_CAROUSEL_DISTANCE_FACTOR: Float = 8f
  const val IMAGE_CAROUSEL_INTERPOLATION_MIN_FRACTION: Float = 0f
  const val IMAGE_CAROUSEL_INTERPOLATION_MAX_FRACTION: Float = 1f
  const val FULL_SCREEN_IMAGE_HEIGHT_FRACTION: Float = 0.9f

  // ---------------
  // Padding and spacing
  // ---------------
  val Padding40: Dp = 40.dp
  val Padding20: Dp = 20.dp
  val Padding16: Dp = 16.dp
  val Padding12: Dp = 12.dp
  val Padding8: Dp = 8.dp
  val Padding4: Dp = 4.dp
  val Padding2: Dp = 2.dp
  val Spacing6: Dp = 6.dp

  // ---------------
  // Icon sizes
  // ---------------
  val IconSize18: Dp = 18.dp
  val IconSize24: Dp = 24.dp
  val IconSize32: Dp = 32.dp
  val IconSize48: Dp = 48.dp

  // ---------------
  // Map and layout
  // ---------------
  val MapHeight250: Dp = 250.dp
  val CardWeight: Float = 1f
  const val WEIGHT_LOCATION_MODIFIER: Float = 1f
  const val WEIGHT_TIME_MODIFIER: Float = 1f

  // ---------------
  // Typography
  // ---------------

  val LineHeight = 32.sp
  val DescriptionLineHeight = 22.sp
  val OtherLineHeight = 20.sp

  // ---------------
  // Alpha and scaling
  // ---------------
  const val ALPHA = 0.9f
  const val ALPHA3 = 0.3f
  const val ALPHA7 = 0.7f
  val Zoom: Float = 12f
  const val TEXT_COLOR_FACTOR: Float = 0.9f
  const val BACKGROUND_ALPHA: Float = 0.8f

  // ---------------
  // Misc logic constants
  // ---------------
  const val BEGIN_BUTTON_SIZE_MULTIPLIER: Int = 2
  const val END_LIST_SPACER_MULTIPLIER: Int = 2
  const val START: Float = 200f

  val boarderStrokeWidth = 1.dp
}

object HuntCardScreenStrings {
  // --------------- Accessibility & Button Labels ---------------
  const val BACK_CONTENT_DESCRIPTION = "Back"
  const val LIKE_BUTTON = "Like Button"
  const val HUNT_PICTURE_DESCRIPTION = "Hunt Picture"
  const val BEGIN_HUNT = "Begin Hunt"
  const val ADD_REVIEW = "Add Review"
  const val EDIT_HUNT = "Edit Hunt"

  // --------------- Units & Stats Labels ---------------
  const val DISTANCE_UNIT = "km"
  const val TIME_UNIT = "h"
  const val HOUR_UNIT = "h"
  const val DISTANCE_LABEL = "Distance"
  const val DURATION_LABEL = "Duration"

  // --------------- Author & User Info ---------------
  const val BY = "by"
  const val UNKNOWN_AUTHOR = "Unknown Author"
  const val CURRENT_USER_INITIAL_LABEL = "Y"

  // --------------- Section Titles & Messages ---------------
  const val REVIEWS = "Reviews : "
  const val NO_REVIEW = "No reviews yet."
  const val DESCRIPTION_LABEL = "Description"
  const val START_POINT_LABEL = "Starting Point"
  const val FULL_SCREEN_DESCRIPTION = "Full-screen hunt image"
  const val MENU = "Menu"
  const val EDIT = "Edit"
  const val DELETE = "Delete"
  const val SEE_PICTURES_STRING = "See Pictures"

  // --------------- Map & Marker Labels ---------------
  const val REVIEW_MARKER_PREFIX = "Start: "

  // --------------- Review & Reply Keys ---------------
  const val REPLIES_VIEW_MODEL_PREFIX = "replies_"
  const val REVIEW_IMAGE_PREFIX = "reviewImages/"
}

/**
 * Contains user-facing strings and accessibility labels for the Hunt Card screen.
 *
 * These constants are used for:
 * - Button labels
 * - Content descriptions (accessibility)
 * - Section titles and UI text
 *
 * Centralizing strings here:
 * - Improves maintainability
 * - Simplifies localization
 * - Ensures consistent wording across the UI
 *
 * Note: This object does not replace Android string resources, but serves as a structured
 * intermediary for Compose UI components.
 */
object HuntCardScreenTestTags {

  // --------------- Screen & Navigation ---------------
  const val GO_BACK_BUTTON = "GoBackButton"
  const val LAST_SPACER = "HuntCard_LastSpacer"

  // --------------- Dot Menu ---------------
  const val DOTBUTOON = "DOT_MENU_BUTTON"
  const val EDIT_BUTTON = "DOT_MENU_EDIT"
  const val DELETE_BUTTON = "DOT_MENU_DELETE"

  // --------------- Text Fields & Labels ---------------
  const val TITLE_TEXT = "TitleText"
  const val AUTHOR_TEXT = "AuthorText"
  const val DESCRIPTION_TEXT = "DescriptionText"

  // --------------- Map & Image Carousel ---------------
  const val MAP_CONTAINER = "MapContainer"
  const val IMAGE_CAROUSEL_CONTAINER = "HuntCardImageCarouselContainer"
  const val IMAGE_PAGER = "HuntCardImagePager"
  const val IMAGE_PAGE_PREFIX = "HuntCardImagePage_"
  const val IMAGE_FULLSCREEN = "HuntCardImageFullscreen"
  const val IMAGE_INDICATOR_ROW = "HuntCardImageIndicatorRow"
  const val IMAGE_INDICATOR_DOT_PREFIX = "HuntCardImageIndicatorDot_"

  // --------------- Buttons & Actions ---------------
  const val BEGIN_BUTTON = "BeginButton"
  const val REVIEW_BUTTON = "ReviewButton"
  const val EDIT_HUNT_BUTTON = "EditHuntButton"
  const val DELETE_REVIEW_BUTTON = "DeleteReviewButton"
  const val LIKE_BUTTON = "HuntCard_LikeButton"
  const val SEE_PICTURES_BUTTON = "SEE_PICTURES_BUTTON"

  // --------------- Reviews & Comments ---------------
  const val REVIEW_CARD = "ReviewCard"
  const val NO_REVIEWS_TEXT = "NO_REVIEWS_TEXT"
  const val REVIEW_COMMENT = "REVIEW_COMMENT"
  const val MODERN_EMPTY_REVIEWS_STATE = "MODERN_EMPTY_REVIEWS_STATE"
  const val REVIEW_PROFILE_INITIALS = "REVIEW_PROFILE_INITIALS"

  // --------------- Lists ---------------
  const val HUNT_CARD_LIST = "HUNT_CARD_LIST"

  // --------------- Loading Indicators ---------------
  const val CIRCULAR_PROGRESS_INDICATOR = "CIRCULAR_PROGRESS_INDICATOR"
}
