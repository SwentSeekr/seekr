package com.swentseekr.seekr.ui.hunt.review

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AddReviewScreenDefaults {
  val SpacePadding: Dp = 16.dp
  val TitleFontSize = 24.sp
  val SubtitleFontSize = 14.sp
  const val MaxStars = 5
  val CommentFieldHeight: Dp = 350.dp
  val SelectedStarColor = Color(0xFFFFC107)
  val UnselectedStarColor = Color.Gray
}

object AddReviewScreenTestTags {
  const val GO_BACK_BUTTON = "HuntCardReview_GoBackButton"
  const val INFO_COLUMN = "HuntCardReview_InfoColumn"
  const val RATING_BAR = "HuntCardReview_RatingBar"
  const val COMMENT_TEXT_FIELD = "HuntCardReview_CommentTextField"
  const val BUTTONS_ROW = "HuntCardReview_ButtonsRow"
  const val CANCEL_BUTTON = "HuntCardReview_CancelButton"
  const val DONE_BUTTON = "HuntCardReview_DoneButton"
  const val ERROR_MESSAGE = "HuntCardReview_ErrorMessage"

  fun starTag(index: Int) = "Star_$index"
}
