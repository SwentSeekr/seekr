package com.swentseekr.seekr.ui.hunt.review

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AddReviewScreenDefaults {
  val ChangeAlpha: Float = 0.3f
  val ChangeAlphaMedium: Float = 0.5f

  val ColumnPadding: Dp = 20.dp
  val ColumnMediumPadding: Dp = 16.dp
  val ColumnVArrangement: Dp = 20.dp
  val ColumnLittleVArr: Dp = 8.dp
  val ColumnMediumVArr: Dp = 16.dp

  val RowHArrangement: Dp = 12.dp
  val RowStarArrangement: Dp = 4.dp

  val CardCornerRadius: Dp = 20.dp
  val CardMedCornerRadius: Dp = 16.dp
  val CardNoElevation: Dp = 0.dp
  val CardLittleElevation: Dp = 2.dp

  val ButtonTonalHeight: Dp = 52.dp
  val ButtonTonalCornerRadius: Dp = 12.dp
  val ButtonWeight: Float = 1f
  val ButtonElevation: Dp = 4.dp

  val TextPadding: Dp = 12.dp
  val TitleFontSize = 24.sp

  val ImageSize: Dp = 120.dp
  val ImageCorners: Dp = 12.dp
  val ImageShadow: Dp = 4.dp

  val SurfacePadding: Dp = 4.dp
  val SurfaceSize: Dp = 28.dp
  val SurfaceCorners: Dp = 8.dp

  val IconSize: Dp = 20.dp
  val IconBig: Dp = 40.dp

  val SpacerHeightSmall: Dp = 8.dp
  val SpacerHeightMedium: Dp = 16.dp

  const val MaxStars = 5

  val CommentFieldHeight: Dp = 250.dp
  val CommentFieldCornerRadius: Dp = 16.dp

  val SelectedStarColor = Color(0xFFFFC107)
  val UnselectedStarColor = Color.Gray
  val TopBarBackgroundColor: Color = Color.LightGray
  val CommentLineHeight = 20.sp
  const val CommentMaxLines = 15
  const val Rating: Double = 0.0
}

object AddReviewScreenStrings {
  const val UnknownAuthor = "Unknown Author"
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
  const val RemovePhotoContentDescription = "Remove photo"

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
  const val ErrorDeletingPhoto = "Error deleting photo:"

  const val ErrorAddingPhoto = "Failed to upload photo:"
  const val ErrorDeletingImages = "Failed to delete image:"
  const val ErrorCancleImage = "Failed to cancel image selection."
  const val ErrorClearSubmitReview = "Cannot clear form, review not submitted successfully."
  const val Empty = ""

  const val NEW_REVIEW_TITLE = "New review added"
  const val NEW_REVIEW_MESSAGE = "You added a new review!"

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
