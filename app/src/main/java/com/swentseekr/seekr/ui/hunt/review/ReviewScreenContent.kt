package com.swentseekr.seekr.ui.hunt.review

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel

@Composable
fun ReviewScreenContent(
    title: String,
    huntId: String,
    onGoBack: () -> Unit,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    reviewViewModel: ReviewHuntViewModel,
    huntCardViewModel: HuntCardViewModel
) {

  val uiState by reviewViewModel.uiState.collectAsState()
  val uiStateHuntCard by huntCardViewModel.uiState.collectAsState()
  LaunchedEffect(huntId) { reviewViewModel.loadHunt(huntId) }
  val hunt2 = uiState.hunt
  val authorId = hunt2?.authorId ?: ""
  LaunchedEffect(authorId) { huntCardViewModel.loadAuthorProfile(authorId) }
  val authorProfile = uiStateHuntCard.authorProfile
  val hunt = uiState.hunt
  var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
  val imagePickerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetMultipleContents(),
          onResult = { uris ->
            selectedImages = uris
            uris.forEach { uri -> reviewViewModel.addPhoto(uri.toString()) }
          })

  val author = authorProfile?.author?.pseudonym ?: ("Unknown Author")
  val context = LocalContext.current

  BaseReviewScreen(
      title = title,
      huntTitle = uiState.hunt?.title ?: "",
      authorName = author,
      rating = uiState.rating,
      reviewText = uiState.reviewText,
      photos = uiState.photos,
      isReviewTextError = uiState.invalidReviewText != null,
      isDoneEnabled = uiState.isValid,
      onRatingChanged = { reviewViewModel.updateRating(it.toDouble()) },
      onReviewTextChanged = { reviewViewModel.setReviewText(it) },
      onAddPhotos = { imagePickerLauncher.launch(AddReviewScreenStrings.ImageMimeType) },
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
