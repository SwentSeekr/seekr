package com.swentseekr.seekr.ui.components

import androidx.compose.ui.graphics.Color
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
  val CardElevation: Dp = 2.dp
  val CornerRadius: Dp = 12.dp
  val ProfilePictureSize: Dp = 32.dp
  val ImageCarouselHeight: Dp = 120.dp
  const val IMAGE_CAROUSEL_MIN_SCALE: Float = 0.85f
  const val IMAGE_CAROUSEL_MAX_SCALE: Float = 1.08f
  const val IMAGE_CAROUSEL_SIDE_ROTATION_DEGREE: Float = 10f
  const val IMAGE_CAROUSEL_OVERLAYER_MAX_ALPHA: Float = 0.35f
  const val IMAGE_CAROUSEL_OVERLAYER_MIN_ALPHA: Float = 0f
  const val IMAGE_CAROUSEL_DISTANCE_FACTOR: Float = 8f
  const val IMAGE_CAROUSEL_INTERPOLATION_MIN_FRACTION: Float = 0f
  const val IMAGE_CAROUSEL_INTERPOLATION_MAX_FRACTION: Float = 1f
  const val FULL_SCREEN_IMAGE_HEIGHT_FRACTION: Float = 0.9f
  const val TEXT_COLOR_FACTOR: Float = 0.9f
  const val WEIGHT_LOCATION_MODIFIER: Float = 1f
  const val WEIGHT_TIME_MODIFIER: Float = 1f

  val ImageIndicatorDotSize: Dp = 6.dp
  val ImageIndicatorDotSelectedSize: Dp = 8.dp
  val ImageIndicatorDotSpacing: Dp = 4.dp
  val ImageIndicatorTopPadding: Dp = 4.dp
  const val IMAGE_CAROUSEL_ROTATION_CENTER_DEGREES: Float = 0f
  const val IMAGE_INDICATOR_LAST_INDEX_OFFSET: Int = 1

  val Padding20: Dp = 20.dp
  val Padding12: Dp = 12.dp
  val Padding16: Dp = 16.dp
  val Padding8: Dp = 8.dp
  val Padding4: Dp = 4.dp
  val Padding40: Dp = 40.dp
  val Padding2: Dp = 2.dp
  val Spacing6: Dp = 6.dp

  val IconSize18: Dp = 18.dp
  val IconSize24: Dp = 24.dp
  val IconSize32: Dp = 32.dp
  val IconSize48: Dp = 48.dp

  val MapHeight250: Dp = 250.dp
  val AspectRatioHero: Float = 16f / 9f

  val Alpha: Float = 0.9f

  val TitleFontSize = 28.sp
  val AuthorFontSize = 16.sp
  val LineHeight = 32.sp

  val CardWeight: Float = 1f
  val MediumFontSize = 24.sp
  val SmallFontSize = 18.sp
  val DescriptionFontSize = 15.sp
  val DescriptionLineHeight = 22.sp

  val Zoom: Float = 12f
  val OtherLineHeight = 20.sp
  val MinFontSize = 13.sp

  val ImageIndicatorUnselectedColor: Color = Color.LightGray
  val ImageIndicatorSelectedColor: Color = Color.DarkGray
  val OrangeButton: Color = Color(0xFFFFA726)

  val ScreenBackground: Color = Color(0xFFF8F9FA)
  val LightGray: Color = Color(0xFFCCCCCC)

  val ParagraphGray: Color = Color(0xFF444444)

  val CardSoftGray: Color = Color(0xFFF5F5F5)

  // Specific colors
  val ErrorRed: Color = Color(0xFFEF5350)
  val LikeRedStrong: Color = Color(0xFFFF5252)

  // New: review card visuals & logic constants
  val ReviewCardCornerRadius: Dp = 16.dp
  val ZeroElevation: Dp = 0.dp
  const val DeleteIconAlpha: Float = 0.85f

  const val NoRepliesCount: Int = 0
  const val SingleReplyCount: Int = 1
  const val INITIAL_LETTER_COUNT: Int = 1
  const val NO_PROFILE_PICTURE_RES_ID: Int = 0

  const val BEGIN_BUTTON_SIZE_MULTIPLIER: Int = 2
  const val END_LIST_SPACER_MULTIPLIER: Int = 2
  const val RepliesLabelAlpha: Float = 0.8f
  const val BACKGROUND_ALPHA: Float = 0.8f
  const val START: Float = 200f
}

object HuntCardScreenStrings {
  const val BACK_CONTENT_DESCRIPTION = "Back"
  const val LIKE_BUTTON = "Like Button"
  const val HUNT_PICTURE_DESCRIPTION = "Hunt Picture"
  const val DISTANCE_UNIT = "km"
  const val TIME_UNIT = "h"
  const val BY = "by"
  const val HOUR_UNIT = "h"
  const val BEGIN_HUNT = "Begin Hunt"
  const val ADD_REVIEW = "Add Review"
  const val EDIT_HUNT = "Edit Hunt"
  const val REVIEWS = "Reviews : "
  const val ReviewDeleteButton = "Delete Review"
  const val NO_REVIEW = "No reviews yet."
  const val FULL_SCREEN_DESCRIPTION = "Full-screen hunt image"
  const val REVIEW_MARKER_PREFIX = "Start: "
  const val UNKNOWN_AUTHOR = "Unknown Author"
  const val DESCRIPTION_LABEL = "Description"
  const val START_POINT_LABEL = "Starting Point"
  const val DISTANCE_LABEL = "Distance"
  const val DURATION_LABEL = "Duration"

  // New strings for ModernReviewCard / replies
  const val SeePictures = "See Pictures"
  const val HideReplies = "Hide replies"
  const val ViewRepliesPrefix = "View"
  const val ReplySingular = "reply"
  const val ReplyPlural = "replies"
  const val NoRepliesYet = "No replies yet"
  const val CURRENT_USER_INITIAL_LABEL = "Y"

  const val REPLIES_VIEW_MODEL_PREFIX = "replies_"
  const val REVIEW_IMAGE_PREFIX = "reviewImages/"
}

object HuntCardScreenTestTags {
  const val HUNTCARD_SCREEN = "HuntCardScreen"
  const val DOTBUTOON = "DOT_MENU_BUTTON"
  const val EDIT_BUTTON = "DOT_MENU_EDIT"
  const val DELETE_BUTTON = "DOT_MENU_DELETE"
  const val GO_BACK_BUTTON = "GoBackButton"
  const val TITLE_TEXT = "TitleText"
  const val AUTHOR_TEXT = "AuthorText"
  const val DESCRIPTION_TEXT = "DescriptionText"
  const val MAP_CONTAINER = "MapContainer"
  const val BEGIN_BUTTON = "BeginButton"
  const val REVIEW_BUTTON = "ReviewButton"
  const val EDIT_HUNT_BUTTON = "EditHuntButton"
  const val REVIEW_CARD = "ReviewCard"
  const val IMAGE_CAROUSEL_CONTAINER = "HuntCardImageCarouselContainer"
  const val IMAGE_PAGER = "HuntCardImagePager"
  const val IMAGE_PAGE_PREFIX = "HuntCardImagePage_"
  const val IMAGE_FULLSCREEN = "HuntCardImageFullscreen"
  const val IMAGE_INDICATOR_ROW = "HuntCardImageIndicatorRow"
  const val IMAGE_INDICATOR_DOT_PREFIX = "HuntCardImageIndicatorDot_"
  const val DELETE_REVIEW_BUTTON = "DeleteReviewButton"
  const val LIKE_BUTTON = "HuntCard_LikeButton"
  const val LAST_SPACER = "HuntCard_LastSpacer"

  // New test tags moved from inline strings
  const val HUNT_CARD_LIST = "HUNT_CARD_LIST"
  const val SEE_PICTURES_BUTTON = "SEE_PICTURES_BUTTON"
  const val NO_REVIEWS_TEXT = "NO_REVIEWS_TEXT"
  const val REVIEW_COMMENT = "REVIEW_COMMENT"
  const val MODERN_EMPTY_REVIEWS_STATE = "MODERN_EMPTY_REVIEWS_STATE"
  const val CIRCULAR_PROGRESS_INDICATOR = "CIRCULAR_PROGRESS_INDICATOR"
  const val REVIEW_PROFILE_INITIALS = "REVIEW_PROFILE_INITIALS"
}
