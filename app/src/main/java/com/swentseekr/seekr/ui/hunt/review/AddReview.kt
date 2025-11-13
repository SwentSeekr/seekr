package com.swentseekr.seekr.ui.hunt.review

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
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
import androidx.compose.runtime.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.swentseekr.seekr.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    huntId: String,
    modifier: Modifier = Modifier,
    reviewViewModel: ReviewHuntViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onDone: () -> Unit = {},
    onCancel: () -> Unit = {},
    onSelectImage: (Uri?) -> Unit = {},
) {
  val uiState by reviewViewModel.uiState.collectAsState()
  LaunchedEffect(huntId) { reviewViewModel.loadHunt(huntId) }
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

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "Add Review Hunt",
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
                        contentDescription = "Back")
                  }
            },
            modifier = modifier.background(Color.LightGray))
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
                  text = hunt?.title ?: "Loading...",
                  fontSize = AddReviewScreenDefaults.TitleFontSize,
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.titleLarge,
                  modifier = modifier.padding(vertical = 4.dp))
              Text(
                  text = hunt?.uid ?: "Loading...",
                  fontSize = AddReviewScreenDefaults.SubtitleFontSize,
              )
              Spacer(modifier = modifier.height(AddReviewScreenDefaults.SpacePadding))

              Text(
                  "Rate this Hunt:",
                  style = MaterialTheme.typography.titleMedium,
                  fontSize = AddReviewScreenDefaults.SubtitleFontSize)
              StarRatingBar(
                  rating = uiState.rating.toInt(),
                  maxStars = maxStar,
                  onRatingChanged = { newRating ->
                    reviewViewModel.updateRating(newRating.toDouble())
                  })

              Text("Your rating: ${uiState.rating.toInt()} /$maxStar ")

              Box(modifier = modifier.fillMaxWidth(0.9f).padding()) {
                OutlinedTextField(
                    value = uiState.reviewText,
                    onValueChange = { reviewViewModel.setReviewText(it) },
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .height(AddReviewScreenDefaults.CommentFieldHeight)
                            .padding(vertical = 8.dp)
                            .testTag(AddReviewScreenTestTags.COMMENT_TEXT_FIELD),
                    label = { Text("Comment") },
                    placeholder = { Text("Leave a comment...") },
                    isError = uiState.invalidReviewText != null,
                    supportingText = {
                      uiState.invalidReviewText?.let {
                        Text(it, modifier = Modifier.testTag(AddReviewScreenTestTags.ERROR_MESSAGE))
                      }
                    },
                    textStyle = LocalTextStyle.current.copy(lineHeight = 20.sp),
                    singleLine = false,
                    maxLines = 15,
                    shape = RoundedCornerShape(12.dp))
              }

              Spacer(modifier = Modifier.height(8.dp))

              Button(
                  onClick = { imagePickerLauncher.launch("image/*") },
                  modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Photo",
                        modifier = Modifier.size(20.dp))
                    Text("Add Pictures", modifier = Modifier.padding(start = 8.dp))
                  }

              if (uiState.photos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                      items(uiState.photos.size) { index ->
                        AsyncImage(
                            model = uiState.photos[index],
                            contentDescription = "Selected Image $index",
                            modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)),
                            placeholder = painterResource(R.drawable.empty_image),
                            error = painterResource(R.drawable.empty_image))
                      }
                    }
              }

              Row(
                  modifier =
                      modifier
                          .fillMaxWidth()
                          .padding()
                          .align(Alignment.CenterHorizontally)
                          .testTag(AddReviewScreenTestTags.BUTTONS_ROW),
                  horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = {
                          reviewViewModel.clearFormCancel()
                          onCancel()
                        },
                        modifier = modifier.testTag(AddReviewScreenTestTags.CANCEL_BUTTON)) {
                          Text("Cancel")
                        }
                    Button(
                        onClick = {
                          hunt?.let { reviewViewModel.submitCurrentUserReview(it) }
                          onDone()
                        },
                        modifier = modifier.testTag(AddReviewScreenTestTags.DONE_BUTTON)) {
                          Text("Done")
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
          contentDescription = "Star $i",
          tint =
              if (i <= rating) AddReviewScreenDefaults.SelectedStarColor
              else AddReviewScreenDefaults.UnselectedStarColor,
          modifier =
              Modifier.padding(4.dp)
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
