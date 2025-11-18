package com.swentseekr.seekr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.DifficultyColor
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.ui.hunt.review.ReviewHuntViewModel
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel
import com.swentseekr.seekr.ui.profile.ProfilePicture
import com.swentseekr.seekr.ui.theme.RedLike

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuntCardScreen(
    huntId: String,
    modifier: Modifier = Modifier,
    huntCardViewModel: HuntCardViewModel = viewModel(),
    reviewViewModel: ReviewHuntViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    beginHunt: () -> Unit = {},
    addReview: () -> Unit = {},
    editHunt: () -> Unit = {},
    testmode: Boolean = false,
) {
  val uiState by huntCardViewModel.uiState.collectAsState()

  // Load when arriving / when id changes
  LaunchedEffect(huntId) { huntCardViewModel.loadHunt(huntId) }
  val hunt2 = uiState.hunt
  val authorId = hunt2?.authorId ?: ""

  LaunchedEffect(authorId) { huntCardViewModel.loadAuthorProfile(authorId) }
  val authorProfile = uiState.authorProfile

  LaunchedEffect(huntId) { huntCardViewModel.loadOtherReview(huntId) }
  val reviews = uiState.reviewList

  LaunchedEffect(Unit) { huntCardViewModel.loadCurrentUserID() }
  val currentUserId = uiState.currentUserId

  val isCurrentId = currentUserId == authorId // verify if current user is author
  val buttonFunctionEdit = if (isCurrentId) editHunt else addReview
  val buttonText =
      if (isCurrentId) HuntCardScreenStrings.EditHunt else HuntCardScreenStrings.AddReview

  val author = authorProfile?.author?.pseudonym ?: ("Unknown Author")

  Scaffold(
      // BAR GOBACK ARROW
      topBar = {
        TopAppBar(
            title = { Text("") },
            navigationIcon = {
              IconButton(
                  modifier = Modifier.testTag(HuntCardScreenTestTags.GO_BACK_BUTTON),
                  onClick = onGoBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = HuntCardScreenStrings.BackContentDescription)
                  }
            },
            modifier = Modifier.background(HuntCardScreenDefaults.TopBarColor))
      },
      modifier = modifier.fillMaxSize()) { innerPadding ->
        val hunt = hunt2
        if (hunt == null) {
          Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
        } else {

          // COLUMN FOR INFORMATIONS
          LazyColumn {
            item {
              Card(
                  modifier =
                      modifier
                          .fillMaxWidth()
                          .padding(innerPadding)
                          .padding(horizontal = HuntCardScreenDefaults.ScreenPaddingHorizontal)
                          .padding(
                              top = HuntCardScreenDefaults.ScreenPaddingTop,
                              bottom = HuntCardScreenDefaults.ScreenPaddingBottom)
                          .border(
                              HuntCardScreenDefaults.CardBorderWidth,
                              MaterialTheme.colorScheme.primary,
                              RoundedCornerShape(HuntCardScreenDefaults.CornerRadius))
                          .height(HuntCardScreenDefaults.ScreenHuntCardHeight),
                  colors =
                      CardDefaults.cardColors(
                          containerColor = HuntCardScreenDefaults.CardBackgroundColor),
                  shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)) {
                    Column(
                        modifier =
                            Modifier.padding(HuntCardScreenDefaults.CardInnerPadding)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                        verticalArrangement =
                            Arrangement.spacedBy(HuntCardScreenDefaults.InfoColumnPadding)) {
                          // ROW WITH IMAGE, TITLE, AUTHOR, DIFFICULTY, DISTANCE, TIME

                          Column(
                              modifier =
                                  modifier
                                      .padding(HuntCardScreenDefaults.InfoColumnPadding)
                                      .fillMaxWidth()
                                      .fillMaxSize()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                  Text(
                                      hunt.title,
                                      fontSize = HuntCardScreenDefaults.TitleFontSize,
                                      fontWeight = FontWeight.Bold,
                                      textAlign = TextAlign.Center,
                                      modifier =
                                          Modifier.weight(HuntCardScreenDefaults.TitleWeight)
                                              .padding(HuntCardScreenDefaults.InfoTextPadding)
                                              .testTag(HuntCardScreenTestTags.TITLE_TEXT))
                                  LikeButton(huntCardViewModel, huntId)
                                }
                                Text(
                                    "by $author",
                                    modifier =
                                        Modifier.padding(
                                                horizontal = HuntCardScreenDefaults.InfoTextPadding)
                                            .testTag(HuntCardScreenTestTags.AUTHOR_TEXT))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                  AsyncImage(
                                      model = hunt.mainImageUrl.ifEmpty { R.drawable.empty_image },
                                      contentDescription =
                                          HuntCardScreenStrings.HuntPictureDescription,
                                      modifier =
                                          Modifier.padding(
                                                  horizontal = HuntCardScreenDefaults.BadgePadding)
                                              .size(HuntCardScreenDefaults.ImageSize)
                                              .clip(RectangleShape)
                                              .testTag(HuntCardScreenTestTags.IMAGE),
                                      placeholder = painterResource(R.drawable.empty_image),
                                      error = painterResource(R.drawable.empty_image))
                                  Column(
                                      modifier = Modifier.fillMaxWidth(),
                                      horizontalAlignment = Alignment.CenterHorizontally,
                                      verticalArrangement = Arrangement.Center) {
                                        StatsBox(
                                            hunt.difficulty.toString(),
                                            DifficultyColor(hunt.difficulty),
                                            modifier)
                                        Spacer(
                                            modifier =
                                                modifier.height(
                                                    HuntCardScreenDefaults.BadgePadding))
                                        StatsBox(
                                            "${hunt.distance} ${HuntCardScreenStrings.DistanceUnit}",
                                            Color.White,
                                            modifier)
                                        Spacer(
                                            modifier =
                                                modifier.height(
                                                    HuntCardScreenDefaults.BadgePadding))
                                        StatsBox(
                                            "${hunt.time} ${HuntCardScreenStrings.TimeUnit}",
                                            Color.White,
                                            modifier)
                                      }
                                }
                              }

                          // DESCRIPTION

                          Text(
                              hunt.description, // +"  "+ currentUserId,
                              modifier =
                                  Modifier.padding(HuntCardScreenDefaults.SectionSpacing)
                                      .testTag(HuntCardScreenTestTags.DESCRIPTION_TEXT))

                          // MAP WITH START POINT

                          var mapLoaded by remember { mutableStateOf(true) }

                          if (mapLoaded) {
                            val startPosition = LatLng(hunt.start.latitude, hunt.start.longitude)
                            val cameraPositionState = rememberCameraPositionState {
                              position =
                                  CameraPosition.fromLatLngZoom(
                                      startPosition, HuntCardScreenDefaults.MapZoom)
                            }

                            Box(
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .height(HuntCardScreenDefaults.MapHeight)
                                        .padding(HuntCardScreenDefaults.MapPadding)
                                        .testTag(HuntCardScreenTestTags.MAP_CONTAINER)) {
                                  GoogleMap(
                                      modifier = Modifier.matchParentSize(),
                                      cameraPositionState = cameraPositionState) {
                                        Marker(
                                            state = MarkerState(position = startPosition),
                                            title =
                                                "${HuntCardScreenStrings.ReviewMarkerTitlePrefix} ${hunt.start.name}",
                                            snippet = HuntCardScreenStrings.ReviewHint)
                                      }
                                }
                          }

                          // BOUTON BEGIN HUNT

                          Row(
                              modifier = modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.SpaceEvenly,
                          ) {
                            Button(
                                beginHunt,
                                modifier =
                                    modifier
                                        .defaultMinSize(
                                            minWidth = HuntCardScreenDefaults.ButtonWidth)
                                        .wrapContentWidth()
                                        .testTag(HuntCardScreenTestTags.BEGIN_BUTTON)) {
                                  Text(HuntCardScreenStrings.BeginHunt)
                                }
                            Button(
                                buttonFunctionEdit,
                                modifier =
                                    modifier
                                        .defaultMinSize(
                                            minWidth = HuntCardScreenDefaults.ButtonWidth)
                                        .wrapContentWidth()
                                        .testTag(HuntCardScreenTestTags.REVIEW_BUTTON)) {
                                  Text(
                                      buttonText,
                                      Modifier.padding(HuntCardScreenDefaults.InfoTextPadding))
                                }
                          }
                        }
                  }
            }

            item {
              Text(
                  HuntCardScreenStrings.Reviews,
                  fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.Center,
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(
                              top = HuntCardScreenDefaults.ReviewCardPadding,
                              bottom = HuntCardScreenDefaults.ReviewCardVerticalPadding))
            }
            if (reviews == null || reviews.isEmpty()) {
              item {
                Text(
                    HuntCardScreenStrings.NoReviews,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(
                                top = HuntCardScreenDefaults.NoReviewPadding,
                                bottom = HuntCardScreenDefaults.NoReviewPadding))
              }
            } else {
              items(reviews) { review ->
                ReviewCard(review, reviewViewModel, huntCardViewModel, currentUserId)
              }
            }
          }
        }
      }
}

@Composable
fun LikeButton(huntCardViewModel: HuntCardViewModel, huntId: String) {
  val uiState by huntCardViewModel.uiState.collectAsState()
  val isLiked = uiState.isLiked
  IconButton(
      onClick = { huntCardViewModel.onLikeClick(huntId) },
      modifier = Modifier.testTag(HuntCardScreenTestTags.LIKE_BUTTON)) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = HuntCardScreenStrings.LikeButton,
            tint = if (isLiked) RedLike else MaterialTheme.colorScheme.onBackground,
            modifier =
                Modifier.size(HuntCardScreenDefaults.LikeButtonSize)
                    .padding(start = HuntCardScreenDefaults.LikeButtonPadding))
      }
}

@Composable
fun ReviewCard(
    review: HuntReview,
    reviewHuntViewModel: ReviewHuntViewModel,
    huntCardViewModel: HuntCardViewModel,
    currentUserId: String?
) {

  val uiState by reviewHuntViewModel.uiState.collectAsState()
  // Load when arriving / when id changes
  LaunchedEffect(review.huntId) { reviewHuntViewModel.loadHunt(review.huntId) }
  val authorId = review.authorId
  LaunchedEffect(authorId) { reviewHuntViewModel.loadAuthorProfile(authorId) }
  val authorProfile = uiState.authorProfile

  val isCurrentId = currentUserId == authorId

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = HuntCardScreenDefaults.ReviewCardVerticalPadding)
              .border(
                  HuntCardScreenDefaults.CardBorderWidth,
                  HuntCardScreenDefaults.PrimaryBorderColor,
                  RoundedCornerShape(HuntCardScreenDefaults.CornerRadius))
              .testTag(HuntCardScreenTestTags.REVIEW_CARD),
      colors = CardDefaults.cardColors(containerColor = HuntCardScreenDefaults.CardBackgroundColor),
  ) {
    Column(modifier = Modifier.padding(HuntCardScreenDefaults.ReviewCardPadding)) {
      val author = authorProfile?.author?.pseudonym ?: ("Unknown Author")
      Row(verticalAlignment = Alignment.CenterVertically) {
        ProfilePicture(
            profilePictureRes = authorProfile?.author?.profilePicture ?: 0,
            modifier = Modifier.size(HuntCardScreenDefaults.ProfilePictureSize))
        Spacer(modifier = Modifier.padding(horizontal = HuntCardScreenDefaults.SmallSpacerPadding))

        Text("by ${author}", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.padding(horizontal = HuntCardScreenDefaults.BigSpacerPadding))
        if (isCurrentId) {
          IconButton(
              onClick = {
                huntCardViewModel.deleteReview(
                    review.huntId, review.reviewId, review.authorId, currentUserId)
              },
          ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Review",
                modifier =
                    Modifier.size(HuntCardScreenDefaults.DeleteReviewButtonSize)
                        .padding(start = HuntCardScreenDefaults.DeleteReviewButtonPadding)
                        .testTag(HuntCardScreenTestTags.DELETE_REVIEW_BUTTON))
          }
        }
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("${HuntCardScreenStrings.ReviewTitlePrefix}", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.padding(horizontal = HuntCardScreenDefaults.SmallSpacerPadding))
        Rating(review.rating, RatingType.STAR)
      }

      Text(review.comment)
    }
  }
}

@Preview
@Composable
fun HuntCardScreenPreview() {
  HuntCardScreen(
      huntId = "hunt123",
      testmode = true,
  )
}
