package com.swentseekr.seekr.ui.hunt.review

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

  val author = authorProfile?.author?.pseudonym ?: ("Unknown Author")
  val context = LocalContext.current

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = AddReviewScreenStrings.Title,
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold)
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
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface))
      },
      modifier = modifier.fillMaxWidth()) { innerPadding ->
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag(AddReviewScreenTestTags.INFO_COLUMN),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
              Spacer(modifier = modifier.height(8.dp))

              Card(
                  modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                  shape = RoundedCornerShape(20.dp),
                  colors =
                      CardDefaults.cardColors(
                          containerColor =
                              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                  elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                          Text(
                              text = hunt?.title ?: AddReviewScreenStrings.LoadingPlaceholder,
                              style = MaterialTheme.typography.headlineSmall,
                              fontWeight = FontWeight.Bold)
                          Text(
                              text = "${AddReviewScreenStrings.By} $author",
                              style = MaterialTheme.typography.bodyLarge,
                              color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                  }

              Card(
                  modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                  shape = RoundedCornerShape(20.dp),
                  colors =
                      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary),
                  elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                          Text(
                              AddReviewScreenStrings.RateThisHunt,
                              style = MaterialTheme.typography.titleMedium,
                              fontWeight = FontWeight.SemiBold)

                          StarRatingBar(
                              rating = uiState.rating.toInt(),
                              maxStars = maxStar,
                              onRatingChanged = { newRating ->
                                reviewViewModel.updateRating(newRating.toDouble())
                              })

                          AnimatedVisibility(
                              visible = uiState.rating > 0,
                              enter = fadeIn() + scaleIn(),
                              exit = fadeOut() + scaleOut()) {
                                Text(
                                    AddReviewScreenStrings.ratingSummary(
                                        uiState.rating.toInt(), maxStar),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary)
                              }
                        }
                  }

              OutlinedTextField(
                  value = uiState.reviewText,
                  onValueChange = { reviewViewModel.setReviewText(it) },
                  modifier =
                      modifier
                          .fillMaxWidth()
                          .height(250.dp)
                          .testTag(AddReviewScreenTestTags.COMMENT_TEXT_FIELD),
                  label = { Text(AddReviewScreenStrings.CommentLabel) },
                  placeholder = { Text(AddReviewScreenStrings.CommentPlaceholder) },
                  isError = uiState.invalidReviewText != null,
                  supportingText = {
                    AnimatedVisibility(
                        visible = uiState.invalidReviewText != null,
                        enter = fadeIn(),
                        exit = fadeOut()) {
                          uiState.invalidReviewText?.let {
                            Text(
                                it,
                                modifier = Modifier.testTag(AddReviewScreenTestTags.ERROR_MESSAGE),
                                color = MaterialTheme.colorScheme.error)
                          }
                        }
                  },
                  textStyle =
                      LocalTextStyle.current.copy(
                          lineHeight = AddReviewScreenDefaults.CommentLineHeight),
                  singleLine = false,
                  maxLines = AddReviewScreenDefaults.CommentMaxLines,
                  shape = RoundedCornerShape(16.dp),
                  colors =
                      OutlinedTextFieldDefaults.colors(
                          unfocusedBorderColor =
                              MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                          focusedBorderColor = MaterialTheme.colorScheme.primary,
                          unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                          focusedContainerColor = MaterialTheme.colorScheme.surface))

              FilledTonalButton(
                  onClick = { imagePickerLauncher.launch(AddReviewScreenStrings.ImageMimeType) },
                  modifier = Modifier.fillMaxWidth().height(52.dp),
                  shape = RoundedCornerShape(12.dp)) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = AddReviewScreenStrings.AddPhotoContentDescription,
                        modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text(
                        AddReviewScreenStrings.AddPicturesButtonLabel,
                        style = MaterialTheme.typography.titleSmall)
                  }

              AnimatedVisibility(
                  visible = uiState.photos.isNotEmpty(),
                  enter = fadeIn() + scaleIn(),
                  exit = fadeOut() + scaleOut()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                          Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Photos (${uiState.photos.size})",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 12.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.testTag("PhotosLazyRow")) {
                                  items(uiState.photos.size) { index ->
                                    Box {
                                      AsyncImage(
                                          model = uiState.photos[index],
                                          contentDescription =
                                              "${AddReviewScreenStrings.SelectedImageContentDescriptionPrefix}$index",
                                          modifier =
                                              Modifier.size(120.dp)
                                                  .clip(RoundedCornerShape(12.dp))
                                                  .shadow(4.dp, RoundedCornerShape(12.dp)),
                                          placeholder = painterResource(R.drawable.empty_image),
                                          error = painterResource(R.drawable.empty_image))
                                      Surface(
                                          modifier =
                                              Modifier.align(Alignment.TopEnd)
                                                  .padding(4.dp)
                                                  .size(28.dp)
                                                  .clickable {
                                                    reviewViewModel.removePhoto(
                                                        uiState.photos[index])
                                                  }
                                                  .testTag("RemovePhoto$index"),
                                          shape = RoundedCornerShape(8.dp),
                                          color = MaterialTheme.colorScheme.errorContainer) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription =
                                                    AddReviewScreenStrings
                                                        .RemovePhotoContentDescription,
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.padding(4.dp))
                                          }
                                    }
                                  }
                                }
                          }
                        }
                  }

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                  modifier = modifier.fillMaxWidth().testTag(AddReviewScreenTestTags.BUTTONS_ROW),
                  horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                          reviewViewModel.clearFormNoSubmission()
                          onCancel()
                        },
                        modifier =
                            modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag(AddReviewScreenTestTags.CANCEL_BUTTON),
                        shape = RoundedCornerShape(12.dp)) {
                          Text(AddReviewScreenStrings.CancelButtonLabel)
                        }
                    Button(
                        onClick = {
                          hunt?.let { reviewViewModel.submitCurrentUserReview(it, context) }
                          onDone()
                        },
                        enabled = uiState.isValid,
                        modifier =
                            modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag(AddReviewScreenTestTags.DONE_BUTTON),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)) {
                          Text(AddReviewScreenStrings.DoneButtonLabel)
                        }
                  }

              Spacer(modifier = Modifier.height(16.dp))
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
  Row(
      modifier = Modifier.testTag(AddReviewScreenTestTags.RATING_BAR),
      horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..starCount) {
          val scale by
              animateFloatAsState(
                  targetValue = if (i <= rating) 1.1f else 1f,
                  animationSpec =
                      spring(
                          dampingRatio = Spring.DampingRatioMediumBouncy,
                          stiffness = Spring.StiffnessMedium))

          Icon(
              imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
              contentDescription = "${AddReviewScreenStrings.StarContentDescriptionPrefix}$i",
              tint =
                  if (i <= rating) AddReviewScreenDefaults.SelectedStarColor
                  else AddReviewScreenDefaults.UnselectedStarColor,
              modifier =
                  Modifier.size(40.dp)
                      .scale(scale)
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
