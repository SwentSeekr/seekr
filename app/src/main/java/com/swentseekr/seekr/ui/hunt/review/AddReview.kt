package com.swentseekr.seekr.ui.hunt.review

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.swentseekr.seekr.R
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    huntId: String,
    modifier: Modifier = Modifier,
    reviewViewModel: ReviewHuntViewModel = viewModel(),
    huntCardViewModel: HuntCardViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onDone: () -> Unit = {},
    onCancel: () -> Unit = {},
    onSelectImage: (Uri?) -> Unit = {},
) {
  val uiState by reviewViewModel.uiState.collectAsState()
  val uiStateHuntCard by huntCardViewModel.uiState.collectAsState()
  LaunchedEffect(huntId) { reviewViewModel.loadHunt(huntId) }
  val hunt2 = uiState.hunt
  val authorId = hunt2?.authorId ?: ""
  LaunchedEffect(authorId) { huntCardViewModel.loadAuthorProfile(authorId) }
  val authorProfile = uiStateHuntCard.authorProfile
  val hunt = uiState.hunt
  val maxStar = AddReviewScreenDefaults.MaxStars
  var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
  val imagePickerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetMultipleContents(),
          onResult = { uris ->
            selectedImages = uris
            uris.forEach { uri -> reviewViewModel.addPhoto(uri.toString()) }
          })

  val author = authorProfile?.author?.pseudonym ?: (AddReviewScreenStrings.UnknownAuthor)

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = AddReviewScreenStrings.Title,
                  fontSize = AddReviewScreenDefaults.TitleFontSize,
                  fontWeight = FontWeight.Bold,
                  modifier = modifier.padding(vertical = AddReviewScreenDefaults.SpacePadding))
            },
            navigationIcon = {
              IconButton(
                  modifier = modifier.testTag(AddReviewScreenTestTags.GO_BACK_BUTTON),
                  onClick = onGoBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = AddReviewScreenStrings.BackContentDescription)
                  }
            },
            modifier = modifier.background(AddReviewScreenDefaults.TopBarBackgroundColor))
      },
      modifier = modifier.fillMaxWidth()) { innerPadding ->
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .testTag(AddReviewScreenTestTags.INFO_COLUMN),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              Spacer(modifier = modifier.height(AddReviewScreenDefaults.SpacePadding))

              Spacer(modifier = modifier.height(AddReviewScreenDefaults.SpacePadding))
              Text(
                  text = hunt?.title ?: AddReviewScreenStrings.LoadingPlaceholder,
                  fontSize = AddReviewScreenDefaults.TitleFontSize,
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.titleLarge,
                  modifier =
                      modifier.padding(vertical = AddReviewScreenDefaults.InfoVerticalPadding))
              Text(
                  text = "${AddReviewScreenStrings.By} $author",
                  fontSize = AddReviewScreenDefaults.SubtitleFontSize,
              )
              Spacer(modifier = modifier.height(AddReviewScreenDefaults.SpacePadding))

              Text(
                  AddReviewScreenStrings.RateThisHunt,
                  style = MaterialTheme.typography.titleMedium,
                  fontSize = AddReviewScreenDefaults.SubtitleFontSize)
              StarRatingBar(
                  rating = uiState.rating.toInt(),
                  maxStars = maxStar,
                  onRatingChanged = { newRating ->
                    reviewViewModel.updateRating(newRating.toDouble())
                  })

              Text(AddReviewScreenStrings.ratingSummary(uiState.rating.toInt(), maxStar))

              Box(
                  modifier =
                      modifier.fillMaxWidth(AddReviewScreenDefaults.CommentFieldWidthFraction)) {
                    OutlinedTextField(
                        value = uiState.reviewText,
                        onValueChange = { reviewViewModel.setReviewText(it) },
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .height(AddReviewScreenDefaults.CommentFieldHeight)
                                .padding(vertical = AddReviewScreenDefaults.FieldVerticalPadding)
                                .testTag(AddReviewScreenTestTags.COMMENT_TEXT_FIELD),
                        label = { Text(AddReviewScreenStrings.CommentLabel) },
                        placeholder = { Text(AddReviewScreenStrings.CommentPlaceholder) },
                        isError = uiState.invalidReviewText != null,
                        supportingText = {
                          uiState.invalidReviewText?.let {
                            Text(
                                it,
                                modifier = Modifier.testTag(AddReviewScreenTestTags.ERROR_MESSAGE))
                          }
                        },
                        textStyle =
                            LocalTextStyle.current.copy(
                                lineHeight = AddReviewScreenDefaults.CommentLineHeight),
                        singleLine = false,
                        maxLines = AddReviewScreenDefaults.CommentMaxLines,
                        shape =
                            RoundedCornerShape(AddReviewScreenDefaults.CommentFieldCornerRadius))
                  }

              Spacer(modifier = Modifier.height(AddReviewScreenDefaults.FieldVerticalPadding))

              Button(
                  onClick = { imagePickerLauncher.launch(AddReviewScreenStrings.ImageMimeType) },
                  modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = AddReviewScreenStrings.AddPhotoContentDescription,
                        modifier = Modifier.size(AddReviewScreenDefaults.AddPhotosIconSize))
                    Text(
                        AddReviewScreenStrings.AddPicturesButtonLabel,
                        modifier =
                            Modifier.padding(start = AddReviewScreenDefaults.FieldVerticalPadding))
                  }

              if (uiState.photos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AddReviewScreenDefaults.PhotoSectionSpacing))
                LazyRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(AddReviewScreenDefaults.PhotosSpacing),
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = AddReviewScreenDefaults.SpacePadding)) {
                      items(uiState.photos.size) { index ->
                        AsyncImage(
                            model = uiState.photos[index],
                            contentDescription =
                                "${AddReviewScreenStrings.SelectedImageContentDescriptionPrefix}$index",
                            modifier =
                                Modifier.size(AddReviewScreenDefaults.ImageSize)
                                    .clip(
                                        RoundedCornerShape(
                                            AddReviewScreenDefaults.CommentFieldCornerRadius)),
                            placeholder = painterResource(R.drawable.empty_image),
                            error = painterResource(R.drawable.empty_image))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription =
                                AddReviewScreenStrings.RemovePhotoContentDescription,
                            modifier =
                                Modifier.size(AddReviewScreenDefaults.CloseImageSize)
                                    .clickable {
                                      reviewViewModel.removePhoto(uiState.photos[index])
                                    }
                                    .testTag("RemovePhoto$index"))
                      }
                    }
              }

              Row(
                  modifier =
                      modifier
                          .fillMaxWidth()
                          .align(Alignment.CenterHorizontally)
                          .testTag(AddReviewScreenTestTags.BUTTONS_ROW),
                  horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = {
                          reviewViewModel.clearFormNoSubmission()
                          onCancel()
                        },
                        modifier = modifier.testTag(AddReviewScreenTestTags.CANCEL_BUTTON)) {
                          Text(AddReviewScreenStrings.CancelButtonLabel)
                        }
                    Button(
                        onClick = {
                          hunt?.let { reviewViewModel.submitCurrentUserReview(it) }
                          onDone()
                        },
                        enabled = uiState.isValid,
                        modifier = modifier.testTag(AddReviewScreenTestTags.DONE_BUTTON)) {
                          Text(AddReviewScreenStrings.DoneButtonLabel)
                        }
                  }
            }
      }
}

@Composable
fun StarRatingBar(
    maxStars: Int = AddReviewScreenDefaults.MaxStars,
    rating: Int = 0,
    onRatingChanged: (Int) -> Unit
) {
  val starCount = if (maxStars > 0) maxStars else AddReviewScreenDefaults.MaxStars
  Row(modifier = Modifier.testTag(AddReviewScreenTestTags.RATING_BAR)) {
    for (i in 1..starCount) {

      Icon(
          imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
          contentDescription = "${AddReviewScreenStrings.StarContentDescriptionPrefix}$i",
          tint =
              if (i <= rating) AddReviewScreenDefaults.SelectedStarColor
              else AddReviewScreenDefaults.UnselectedStarColor,
          modifier =
              Modifier.padding(AddReviewScreenDefaults.StarPadding)
                  .clickable {
                    if (i == rating) {
                      onRatingChanged(i - 1)
                    } else {
                      onRatingChanged(i)
                    }
                  }
                  .testTag(AddReviewScreenTestTags.starTag(i)))
    }
  }
}

@Preview
@Composable
fun AddReviewScreenPreview() {
  AddReviewScreen("hunt123")
}
