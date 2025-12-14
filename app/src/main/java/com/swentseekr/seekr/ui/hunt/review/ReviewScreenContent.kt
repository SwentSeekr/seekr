package com.swentseekr.seekr.ui.hunt.review

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel

/**
 * A composable that manages the content and state of a review screen. It handles both adding and
 * editing reviews for a given hunt, including:
 * - Loading hunt and author data.
 * - Managing rating, text, and photo inputs.
 * - Handling submission, cancellation, and navigation callbacks.
 *
 * @param title The title displayed at the top of the review screen.
 * @param huntId The unique ID of the hunt being reviewed.
 * @param onGoBack Callback invoked when the user wants to navigate back.
 * @param onDone Callback invoked when the user completes and submits the review.
 * @param onCancel Callback invoked when the user cancels the review process.
 * @param reviewViewModel ViewModel managing review state (rating, text, photos).
 * @param huntCardViewModel ViewModel managing hunt and author profile data.
 */
@Composable
fun ReviewScreenContent(
    title: String,
    huntId: String,
    reviewId: String,
    onGoBack: () -> Unit,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    reviewViewModel: ReviewHuntViewModel,
    huntCardViewModel: HuntCardViewModel
) {

  val uiState by reviewViewModel.uiState.collectAsState()
  val uiStateHuntCard by huntCardViewModel.uiState.collectAsState()
  LaunchedEffect(huntId) { reviewViewModel.loadHunt(huntId) }
  // Only load review if reviewId is not empty (edit mode)
  LaunchedEffect(reviewId) {
    if (reviewId.isNotEmpty()) {
      reviewViewModel.loadReview(reviewId)
    }
  }

  val hunt2 = uiState.hunt
  val authorId = hunt2?.authorId ?: ""
  LaunchedEffect(authorId) { huntCardViewModel.loadAuthorProfile(authorId) }
  val authorProfile = uiStateHuntCard.authorProfile
  val hunt = uiState.hunt
  val imagePickerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetMultipleContents(),
          onResult = { uris -> uris.forEach { uri -> reviewViewModel.addPhoto(uri.toString()) } })

  val author = authorProfile?.author?.pseudonym ?: (AddReviewScreenStrings.UNKNOWN)
  val context = LocalContext.current

  BaseReviewScreen(
      title = title,
      huntTitle = uiState.hunt?.title ?: AddReviewScreenStrings.LOADING,
      authorName = author,
      rating = uiState.rating,
      reviewText = uiState.reviewText,
      photos = uiState.photos,
      isReviewTextError = uiState.invalidReviewText != null,
      isDoneEnabled = uiState.isValid,
      onRatingChanged = { reviewViewModel.updateRating(it.toDouble()) },
      onReviewTextChanged = { reviewViewModel.setReviewText(it) },
      onAddPhotos = { imagePickerLauncher.launch(AddReviewScreenStrings.IMAGE_MIME_TYPE) },
      onRemovePhoto = { index -> reviewViewModel.removePhoto(uiState.photos[index]) },
      onGoBack = onGoBack,
      onCancel = {
        reviewViewModel.clearFormNoSubmission()
        onCancel()
      },
      onDone = {
        hunt?.let { reviewViewModel.submitCurrentUserReview(it, context) }
        onDone()
      })
}
