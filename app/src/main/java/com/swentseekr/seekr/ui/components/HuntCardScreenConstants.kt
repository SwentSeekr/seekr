package com.swentseekr.seekr.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object HuntCardScreenDefaults {
  const val MaxStarNumber = 5
  val TopBarColor: Color = Color.LightGray
  val ScreenPaddingHorizontal: Dp = 16.dp
  val ScreenPaddingTop: Dp = 8.dp
  val ScreenPaddingBottom: Dp = 16.dp
  val CardInnerPadding: Dp = 12.dp
  val SectionSpacing: Dp = 8.dp
  val InfoColumnPadding: Dp = 8.dp
  val InfoTextPadding: Dp = 4.dp
  val ImageSize: Dp = 100.dp
  val BadgeHeight: Dp = 20.dp
  val BadgeWidth: Dp = 80.dp
  val BadgePadding: Dp = 4.dp
  val BadgeTextPadding: Dp = 2.dp
  val MapHeight: Dp = 400.dp
  val MapPadding: Dp = 8.dp
  val CardBorderWidth: Dp = 2.dp
  val CornerRadius: Dp = 12.dp
  val ButtonWidth: Dp = 120.dp
  val ReviewCardVerticalPadding: Dp = 4.dp
  val ReviewCardPadding: Dp = 8.dp
  val TitleFontSize = 20.sp
  const val MapZoom = 12f
  const val SampleReviewCount = 10
  val PrimaryBorderColor: Color = Color(0xFF60BA37)
  val CardBackgroundColor: Color = Color(0xFFF8DEB6)
  val DifficultyBadgeColor: Color = Color.Green
  val NeutralBadgeColor: Color = Color.White
}

object HuntCardScreenStrings {
  const val BackContentDescription = "Back"
  const val HuntPictureDescription = "Hunt Picture"
  const val DistanceUnit = "km"
  const val TimeUnit = "min"
  const val BeginHunt = "Begin Hunt"
  const val AddReview = "Add Review"
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
}
