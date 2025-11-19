package com.swentseekr.seekr.ui.hunt.review

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AddReviewScreenDefaults {
  val SpacePadding: Dp = 16.dp
  val InfoVerticalPadding: Dp = 4.dp
  val FieldVerticalPadding: Dp = 8.dp
  val PhotoSectionSpacing: Dp = 12.dp
  val PhotosSpacing: Dp = 8.dp
  val StarPadding: Dp = 4.dp
  val AddPhotosIconSize: Dp = 20.dp
  val ImageSize: Dp = 120.dp
  val CommentFieldCornerRadius: Dp = 12.dp
  val CommentFieldWidthFraction = 0.9f
  val TitleFontSize = 24.sp
  val SubtitleFontSize = 14.sp
  const val MaxStars = 5
  val CommentFieldHeight: Dp = 350.dp
  val SelectedStarColor = Color(0xFFFFC107)
  val UnselectedStarColor = Color.Gray
  val TopBarBackgroundColor: Color = Color.LightGray
  val CommentLineHeight = 20.sp
  const val CommentMaxLines = 15
}

object AddReviewScreenStrings {
  const val Title = "Add Review Hunt"
  const val BackContentDescription = "Back"
  const val RateThisHunt = "Rate this Hunt:"
  const val RatingPrefix = "Your rating: "
  const val CommentLabel = "Comment"
  const val CommentPlaceholder = "Leave a comment..."
  const val AddPhotoContentDescription = "Add Photo"
  const val AddPicturesButtonLabel = "Add Pictures"
  const val CancelButtonLabel = "Cancel"
  const val DoneButtonLabel = "Done"
  const val LoadingPlaceholder = "Loading..."
  const val ImageMimeType = "image/*"
  const val SelectedImageContentDescriptionPrefix = "Selected Image "
  const val StarContentDescriptionPrefix = "Star "
  const val By = "by"
  const val ReviewViewModel = "ReviewHuntViewModel"

  const val User0 = "0"

  const val ErrorSubmisson = "At least one field is not valid"
  const val ErrorLoadingHunt = "Error loading Hunt by ID:"
  const val ErrorLoadingProfil = "Error loading user profile for User ID:"
  const val HuntCardViewModel = "HuntCardViewModel"
  const val ErrorReviewHunt = "Error review Hunt"
  const val FailSubmitReview = "Failed to submit review:"
  const val NoCurrentUser = "None (B2)"
  const val ErrorDeleteReview = "You can only delete your own review."
  const val FailDeleteHunt = "Failed to delete Hunt:"
  const val ErrorDeleteHunt = "Error deleting Review for hunt"
  const val ReviewNotEmpty = "The review cannot be empty"
  const val InvalidRating = "Rating must be between 1 and 5"

  const val ErrorClearSubmitReview = "Cannot clear form, review not submitted successfully."

  fun ratingSummary(rating: Int, maxStars: Int): String = "$RatingPrefix$rating /$maxStars"
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
