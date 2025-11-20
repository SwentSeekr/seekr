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
  const val MaxStarNumber = 5
  val TopBarColor: Color = GrayBackgound
  val ScreenPaddingHorizontal: Dp = 16.dp
  val ScreenPaddingTop: Dp = 8.dp
  val ScreenPaddingBottom: Dp = 16.dp
  val CardInnerPadding: Dp = 12.dp
  val SectionSpacing: Dp = 8.dp
  val InfoColumnPadding: Dp = 8.dp
  val InfoTextPadding: Dp = 4.dp
  val BadgePadding: Dp = 4.dp
  val MapHeight: Dp = 350.dp
  val MapPadding: Dp = 8.dp
  val CardBorderWidth: Dp = 2.dp
  val CornerRadius: Dp = 12.dp
  val statBoxCornerRadius: Dp = 5.dp
  val statBoxHeight: Dp = 25.dp
  val statBoxWidth: Dp = 90.dp
  val statBoxPadding: Dp = 1.dp
  val ButtonWidth: Dp = 100.dp
  val ReviewCardVerticalPadding: Dp = 4.dp
  val ReviewCardPadding: Dp = 8.dp
  val TitleFontSize = 20.sp
  const val MapZoom = 12f
  val PrimaryBorderColor: Color = Green
  val CardBackgroundColor: Color = MediumYellow
  val NeutralBadgeColor: Color = White
  // spacing between author text and image row
  val AuthorImageSpacing: Dp = 8.dp
  val ImageCarouselHeight: Dp = 120.dp
  val ImageCarouselPadding: Dp = 8.dp
  val ImageCarouselCornerRadius: Dp = 8.dp
  val ImageCarouselWhiteFrame: Dp = 2.dp
  val ImageCarouselPagerContentPadding: Dp = 12.dp
  val ImageCarouselPageSpacing: Dp = 8.dp
  const val ImageCarouselMinScale: Float = 0.85f
  const val ImageCarouselMaxScale: Float = 1.08f
  const val ImageCarouselSideRotationDegrees: Float = 10f
  const val ImageCarouselOverlayMaxAlpha: Float = 0.35f
  const val ImageCarouselOverlayMinAlpha: Float = 0f
  const val ImageCarouselCameraDistanceFactor: Float = 8f
  const val ImageCarouselInterpolationMinFraction: Float = 0f
  const val ImageCarouselInterpolationMaxFraction: Float = 1f
  val ImageCarouselShadowElevation: Dp = 4.dp
  const val ImageCarouselWeight: Float = 2f
  const val StatsColumnWeight: Float = 1f
  const val FullScreenImageHeightFraction: Float = 0.9f
  val ImageIndicatorDotSize: Dp = 6.dp
  val ImageIndicatorDotSelectedSize: Dp = 8.dp
  val ImageIndicatorDotSpacing: Dp = 4.dp
  val ImageIndicatorTopPadding: Dp = 4.dp
  const val ImageCarouselRotationCenterDegrees: Float = 0f
  const val ImageIndicatorLastIndexOffset = 1
  val ImageIndicatorUnselectedColor: Color = Color.LightGray
  val ImageIndicatorSelectedColor: Color = Color.DarkGray
}

object HuntCardScreenStrings {
  const val BackContentDescription = "Back"
  const val DistanceUnit = "km"
  const val HourUnit = "h"
  const val BeginHunt = "Begin Hunt"
  const val AddReview = "Add Review"
  const val EditHunt = "Edit Hunt"
  const val ReviewTitlePrefix = "Rating:"
  const val ReviewHint = "Hunt starting point"
  const val ReviewMarkerTitlePrefix = "Start: "
  const val FullScreenImageDescription = "Full-screen hunt image"
  const val HuntPicturePageDescriptionPrefix = "Hunt picture "
  const val UnknownAuthor = "Unknown Author"
}

object HuntCardScreenTestTags {
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
}
