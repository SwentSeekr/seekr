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
  const val ImageCarouselMinScale: Float = 0.85f
  const val ImageCarouselMaxScale: Float = 1.08f
  const val ImageCarouselSideRotationDegrees: Float = 10f
  const val ImageCarouselOverlayMaxAlpha: Float = 0.35f
  const val ImageCarouselOverlayMinAlpha: Float = 0f
  const val ImageCarouselCameraDistanceFactor: Float = 8f
  const val ImageCarouselInterpolationMinFraction: Float = 0f
  const val ImageCarouselInterpolationMaxFraction: Float = 1f
  const val FullScreenImageHeightFraction: Float = 0.9f
  val ImageIndicatorDotSize: Dp = 6.dp
  val ImageIndicatorDotSelectedSize: Dp = 8.dp
  val ImageIndicatorDotSpacing: Dp = 4.dp
  val ImageIndicatorTopPadding: Dp = 4.dp
  const val ImageCarouselRotationCenterDegrees: Float = 0f
  const val ImageIndicatorLastIndexOffset: Int = 1

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
  const val InitialLetterCount: Int = 1
  const val NoProfilePictureResId: Int = 0

  const val BeginButtonSizeMultiplier: Int = 2
  const val EndListSpacerMultiplier: Int = 2
  const val RepliesLabelAlpha: Float = 0.8f
}

object HuntCardScreenStrings {
  const val BackContentDescription = "Back"
  const val LikeButton = "Like Button"
  const val HuntPictureDescription = "Hunt Picture"
  const val DistanceUnit = "km"
  const val TimeUnit = "h"
  const val By = "by"
  const val HourUnit = "h"
  const val BeginHunt = "Begin Hunt"
  const val AddReview = "Add Review"
  const val EditHunt = "Edit Hunt"
  const val Reviews = "Reviews : "
  const val ReviewDeleteButton = "Delete Review"
  const val NoReviews = "No reviews yet."
  const val FullScreenImageDescription = "Full-screen hunt image"
  const val ReviewMarkerTitlePrefix = "Start: "
  const val UnknownAuthor = "Unknown Author"
  const val DescriptionLabel = "Description"
  const val StartingPointLabel = "Starting Point"
  const val DistanceLabel = "Distance"
  const val DurationLabel = "Duration"

  // New strings for ModernReviewCard / replies
  const val SeePictures = "See Pictures"
  const val HideReplies = "Hide replies"
  const val ViewRepliesPrefix = "View"
  const val ReplySingular = "reply"
  const val ReplyPlural = "replies"
  const val NoRepliesYet = "No replies yet"
  const val CurrentUserInitialLabel = "Y"

  const val RepliesViewModelKeyPrefix = "replies_"
  const val ReviewImagesRoutePrefix = "reviewImages/"
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
