package com.swentseekr.seekr.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object HuntCardScreenDefaults {
  const val MaxStarNumber = 5
  val TopBarColor: Color = Color.LightGray
  val spacerHeightSmall = 3.dp
  val CardBorderWidthSmall: Dp = 2.dp
  val CardElevation: Dp = 2.dp
  val ColumnStartingPadding: Dp = 12.dp
  val ColumnEndingPadding: Dp = 12.dp
  val ColumnTopPadding: Dp = 8.dp
  val ColumnBottomPadding: Dp = 8.dp
  val ScreenPaddingHorizontal: Dp = 16.dp
  val ScreenPaddingTop: Dp = 8.dp
  val CardPadding: Dp = 8.dp
  val CardWidthFraction: Float = 0.85f
  val ScreenPaddingBottom: Dp = 16.dp
  val ScreenHuntCardHeight: Dp = 700.dp
  val LikeButtonSize: Dp = 32.dp
  val LikeButtonPadding: Dp = 4.dp
  val CardInnerPadding: Dp = 12.dp
  val SectionSpacing: Dp = 8.dp
  val InfoColumnPadding: Dp = 8.dp
  val InfoTextPadding: Dp = 4.dp
  val ImageSize: Dp = 100.dp
  val ImagePadding: Dp = 4.dp
  val ImageRoundness: Dp = 8.dp
  val BadgeHeight: Dp = 20.dp
  val BadgeWidth: Dp = 80.dp
  val BadgePadding: Dp = 4.dp
  val BadgeTextPadding: Dp = 2.dp
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

  val NoReviewPadding: Dp = 30.dp
  val TitleFontSize = 20.sp
  const val MapZoom = 12f
  const val TitleWeight = 1f
  const val SampleReviewCount = 10
  val PrimaryBorderColor: Color = Color(0xFF60BA37)
  val CardBackgroundColor: Color = Color(0xFFF8DEB6)
  val DifficultyBadgeColor: Color = Color.Green
  val NeutralBadgeColor: Color = Color.White

  val ProfilePictureSize: Dp = 32.dp
  val SmallSpacerPadding: Dp = 8.dp
  val BigSpacerPadding: Dp = 90.dp
  val DeleteReviewButtonSize: Dp = 32.dp
  val DeleteReviewButtonPadding: Dp = 4.dp
}

object HuntCardScreenStrings {
  const val BackContentDescription = "Back"
  const val LikeButton = "Like Button"
  const val HuntPictureDescription = "Hunt Picture"
  const val DistanceUnit = "km"
  const val TimeUnit = "h"
  const val BeginHunt = "Begin Hunt"
  const val AddReview = "Add Review"
  const val EditHunt = "Edit Hunt"
  const val Reviews = "Reviews : "
  const val NoReviews = "No reviews yet."
  const val ReviewTitlePrefix = "Rating:"
  const val ReviewHint = "Point de départ de la chasse"
  const val ReviewMarkerTitlePrefix = "Départ : "
}

object HuntCardScreenTestTags {
  const val GO_BACK_BUTTON = "GoBackButton"
  const val TITLE_TEXT = "TitleText"
  const val AUTHOR_TEXT = "AuthorText"
  const val IMAGE = "HuntImage"
  const val DIFFICULTY_BOX = "DifficultyBox"
  const val DISTANCE_BOX = "DistanceBox"
  const val TIME_BOX = "TimeBox"
  const val DESCRIPTION_TEXT = "DescriptionText"
  const val MAP_CONTAINER = "MapContainer"
  const val BEGIN_BUTTON = "BeginButton"
  const val REVIEW_BUTTON = "ReviewButton"
  const val REVIEW_CARD = "ReviewCard"
  const val DELETE_REVIEW_BUTTON = "DeleteReviewButton"
  const val LIKE_BUTTON = "HuntCard_LikeButton"
}
