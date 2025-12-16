package com.swentseekr.seekr.ui.hunt.review

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AddReviewScreenDefaults {
  val ChangeAlpha: Float = 0.3f

  val ColumnPadding: Dp = 20.dp
  val ColumnVArrangement: Dp = 20.dp

  val RowHArrangement: Dp = 12.dp
  val RowStarArrangement: Dp = 4.dp

  val CardCornerRadius: Dp = 20.dp
  val CardElevation: Dp = 0.dp

  val ButtonTonalHeight: Dp = 52.dp
  val ButtonTonalCornerRadius: Dp = 12.dp
  val ButtonWeight: Float = 1f
  val ButtonElevation: Dp = 4.dp

  val TitleFontSize = 24.sp

  val ImageSize: Dp = 120.dp
  val ImageCorners: Dp = 12.dp
  val ImageShadow: Dp = 4.dp

  val SurfacePadding: Dp = 4.dp
  val SurfaceSize: Dp = 28.dp
  val SurfaceCorners: Dp = 8.dp
  val SurfaceIconSize: Dp = 14.dp

  val IconSize: Dp = 20.dp
  val IconBig: Dp = 40.dp

  val SpacerHeightSmall: Dp = 8.dp
  val SpacerHeightMedium: Dp = 16.dp

  const val MAX_STARS = 5
  const val MIN_STARS = 0

  val CommentFieldHeight: Dp = 250.dp
  val CommentFieldCornerRadius: Dp = 16.dp

  val SelectedStarColor = Color(0xFFFFC107)
  val UnselectedStarColor = Color.Gray
  val CommentLineHeight = 20.sp
  const val COMMENT_MAX_LINES = 15
  const val RATING: Double = 0.0

  const val DEFAULT_RATING: Int = 0
  const val FIRST_STAR_INDEX: Int = 1
  const val RATING_STEP: Int = 1
  const val STAR_SELECTED_SCALE: Float = 1.1f
  const val STAR_UNSELECTED_SCALE: Float = 1f
  const val MIN_RATING: Double = 0.0
  const val MAX_RATING: Double = 0.0

  // Header & photos
  val HeaderInnerSpacing: Dp = 8.dp
  val HeaderSubtitleSpacerHeight: Dp = 4.dp

  val PhotosSpacerHeight: Dp = 8.dp
  const val COMMENT_CHAR_COUNT_ALPHA: Float = 0.7f
  val TrailingTileAlpha: Float = 0.6f
}

object AddReviewScreenStrings {
  const val TITLE = "Add Review Hunt"
  const val BACK_CONTENT_DESCRIPTION = "Back"
  const val RATING_PREFIX = "Your rating: "
  const val COMMENT_LABEL = "Comment"
  const val COMMENT_PLACEHOLDER = "Leave a comment..."
  const val ADD_PHOTO_CONTENT_DESCRIPTION = "Add Photo"
  const val ADD_PICTURES_BUTTON_LABEL = "Add Pictures"
  const val CANCEL_BUTTON_LABEL = "Cancel"
  const val DONE_BUTTON_LABEL = "Done"
  const val IMAGE_MIME_TYPE = "image/*"
  const val SELECTED_IMAGE_CONTENT_DESCRIPTION_PREFIX = "Selected Image "
  const val STAR_CONTENT_DESCRIPTION_PREFIX = "Star "
  const val BY = "by"
  const val REVIEW_VIEW_MODEL = "ReviewHuntViewModel"
  const val REMOVE_PHOTO_CONTENT_DESCRIPTION = "Remove photo"

  const val USER_0 = "0"
  const val LOADING = "Loading..."

  const val ERROR_SUBMISSION = "At least one field is not valid"
  const val ERROR_LOADING_HUNT = "Error loading Hunt by ID:"
  const val ERROR_LOADING_PROFILE = "Error loading user profile for User ID:"
  const val HUNT_CARD_VIEW_MODEL = "HuntCardViewModel"
  const val ERROR_REVIEW_HUNT = "Error review Hunt"
  const val FAIL_SUBMIT_REVIEW = "Failed to submit review:"
  const val NO_CURRENT_USER = "None (B2)"
  const val ERROR_DELETE_REVIEW = "You can only delete your own review."
  const val FAIL_DELETE_HUNT = "Failed to delete Hunt:"
  const val ERROR_DELETE_HUNT = "Error deleting Review for hunt"
  const val REVIEW_NOT_EMPTY = "The review cannot be empty"
  const val INVALID_RATING = "Rating must be between 1 and 5"
  const val ERROR_DELETING_PHOTO = "Error deleting photo:"

  const val ERROR_ADDING_PHOTO = "Failed to upload photo:"
  const val ERROR_DELETING_IMAGES = "Failed to delete image:"
  const val ERROR_CANCEL_IMAGE = "Failed to cancel image selection."
  const val ERROR_CLEAR_SUBMIT_REVIEW = "Cannot clear form, review not submitted successfully."

  const val NEW_REVIEW_TITLE = "New review added"
  const val NEW_REVIEW_MESSAGE = "You added a new review!"

  const val PHOTOS_LABEL_PREFIX = "Photos ("
  const val PHOTOS_LABEL_SUFFIX = ")"
  const val ADD_MORE_PHOTOS_BUTTON_LABEL = "Add more"
  const val UPDATE_REVIEW = "Review Updated"
  const val UPDATE_REVIEW_SUCCESS = "Your review has been updated successfully"
  const val UPDATE_REVIEW_FAIL = "Error updating review"
  const val UPDATE_REVIEW_FAIL_SET_MSG = "Failed to update review:"
  const val ADD_KEY = "add_"
  const val EDIT_KEY = "edit_"
  const val EDIT_TITLE = "Edit Review Hunt"
  const val UNKNOWN = "Unknown Author"

  fun ratingSummary(rating: Int, maxStars: Int): String = "$RATING_PREFIX$rating /$maxStars"

  fun commentLengthLabel(length: Int): String = "$length chars"

  fun photosHeader(count: Int): String = "$PHOTOS_LABEL_PREFIX$count$PHOTOS_LABEL_SUFFIX"
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
  const val ADD_PHOTO_BUTTON_TEST_TAG = "AddPhotoButton"
  const val PHOTOS_LAZY_ROW_TAG = "PhotosLazyRow"
  private const val REMOVE_PHOTO_PREFIX = "RemovePhoto_"

  fun starTag(index: Int) = "Star_$index"

  fun removePhotoTag(index: Int) = "$REMOVE_PHOTO_PREFIX$index"
}
