package com.swentseekr.seekr.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swentseekr.seekr.ui.theme.GrayBackgound
import com.swentseekr.seekr.ui.theme.Green
import com.swentseekr.seekr.ui.theme.MediumYellow
import com.swentseekr.seekr.ui.theme.White

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
  const val ImageIndicatorLastIndexOffset = 1

  val Padding20 = 20.dp
  val Padding12 = 12.dp
  val Padding16 = 16.dp
  val Padding8 = 8.dp
  val Padding4 = 4.dp
  val Padding40 = 40.dp
  val Padding2 = 2.dp

  val IconSize18 = 18.dp
  val IconSize24 = 24.dp
  val IconSize32 = 32.dp
  val IconSize48 = 48.dp

  val MapHeight250 = 250.dp
  val AspectRatioHero = 16f / 9f

  val Alpha = 0.9f

  val TitleFontSize = 28.sp
  val AuthorFontSize = 16.sp
  val LineHeight = 32.sp

  val CardWeight = 1f
  val MediumFontSize = 24.sp
    val SmallFontSize = 18.sp
  val DescriptionFontSize = 15.sp
  val DescriptionLineHeight = 22.sp

  val Zoom = 12f
  val OtherLineHeight = 20.sp
  val MinFontSize = 13.sp


  val ImageIndicatorUnselectedColor: Color = Color.LightGray
  val ImageIndicatorSelectedColor: Color = Color.DarkGray
  val BlueButton: Color =  Color(0xFF2196F3)
  val OrangeButton: Color = Color(0xFFFFA726)
  val WhiteTint : Color = Color(0xFFCCCCCC)
  val OrangeTint : Color = Color(0xFFEF5350)
  val DarkGray : Color = Color(0xFF444444)
  val RedLike : Color = Color(0xFFCCCCCC)
  val GreyDefault : Color = Color(0xFFFF5252)
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
}

object HuntCardScreenTestTags {
  const val HUNTCARD_SCREEN = "HuntCardScreen"
  const val GO_BACK_BUTTON = "GoBackButton"
  const val TITLE_TEXT = "TitleText"
  const val AUTHOR_TEXT = "AuthorText"
  const val DESCRIPTION_TEXT = "DescriptionText"
  const val MAP_CONTAINER = "MapContainer"
  const val BEGIN_BUTTON = "BeginButton"
  const val REVIEW_BUTTON = "ReviewButton"
  const val REVIEW_CARD = "ReviewCard"
  const val IMAGE_CAROUSEL_CONTAINER = "HuntCardImageCarouselContainer"
  const val IMAGE_PAGER = "HuntCardImagePager"
  const val IMAGE_PAGE_PREFIX = "HuntCardImagePage_"
  const val IMAGE_FULLSCREEN = "HuntCardImageFullscreen"
  const val IMAGE_INDICATOR_ROW = "HuntCardImageIndicatorRow"
  const val IMAGE_INDICATOR_DOT_PREFIX = "HuntCardImageIndicatorDot_"
  const val DELETE_REVIEW_BUTTON = "DeleteReviewButton"
  const val LIKE_BUTTON = "HuntCard_LikeButton"
}
