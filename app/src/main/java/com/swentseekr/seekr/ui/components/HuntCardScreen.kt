package com.swentseekr.seekr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
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
    goProfile: (String) -> Unit = {},
    beginHunt: () -> Unit = {},
    addReview: () -> Unit = {},
    editHunt: () -> Unit = {},
    // goImages: () -> Unit = {}
    navController: NavHostController
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

  val author = authorProfile?.author?.pseudonym ?: HuntCardScreenStrings.UnknownAuthor

  Scaffold(
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
      modifier = modifier.fillMaxSize().testTag(HuntCardScreenTestTags.HUNTCARD_SCREEN)) {
          innerPadding ->
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

                          HuntHeaderSection(
                              hunt = hunt,
                              authorName = author,
                              huntId = huntId,
                              huntCardViewModel = huntCardViewModel,
                              goProfile = goProfile,
                              modifier = modifier,
                          )

                          // DESCRIPTION

                          HuntDescriptionSection(hunt.description)

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
                ReviewCard(review, reviewViewModel, huntCardViewModel, currentUserId, navController)
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
fun HuntDescriptionSection(description: String, modifier: Modifier = Modifier) {
  Text(
      description,
      modifier =
          modifier
              .padding(HuntCardScreenDefaults.SectionSpacing)
              .testTag(HuntCardScreenTestTags.DESCRIPTION_TEXT))
}

@Composable
fun ReviewCard(
    review: HuntReview,
    reviewHuntViewModel: ReviewHuntViewModel,
    huntCardViewModel: HuntCardViewModel,
    currentUserId: String?,
    // goImages: () -> Unit = {},
    navController: NavHostController
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
              .padding(horizontal = HuntCardScreenDefaults.ScreenPaddingHorizontal)
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
            profilePictureRes =
                authorProfile?.author?.profilePicture ?: HuntCardScreenDefaults.NoPicture,
            modifier = Modifier.size(HuntCardScreenDefaults.ProfilePictureSize))
        Spacer(modifier = Modifier.padding(horizontal = HuntCardScreenDefaults.SmallSpacerPadding))

        Text(
            "${HuntCardScreenStrings.By} $author",
            fontWeight = FontWeight.Bold,
        )
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
                contentDescription = HuntCardScreenStrings.ReviewDeleteButton,
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
      Text(review.photos.size.toString())

      if (review.photos.isNotEmpty()) {
        Button(
            onClick = {
              reviewHuntViewModel.loadReviewImages(review.photos)
              navController.navigate("reviewImages")
            },
            modifier = Modifier.align(Alignment.End)) {
              Text("See Pictures")
            }
      }
    }
  }
}

@Composable
fun HuntHeaderSection(
    hunt: com.swentseekr.seekr.model.hunt.Hunt,
    authorName: String,
    huntId: String,
    goProfile: (String) -> Unit = {},
    huntCardViewModel: HuntCardViewModel,
    modifier: Modifier = Modifier,
) {
  Column(
      modifier =
          modifier.padding(HuntCardScreenDefaults.InfoColumnPadding).fillMaxWidth().fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
              hunt.title,
              fontSize = HuntCardScreenDefaults.TitleFontSize,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
              modifier =
                  Modifier.weight(HuntCardScreenDefaults.TitleWeight)
                      .padding(HuntCardScreenDefaults.InfoTextPadding)
                      .testTag(HuntCardScreenTestTags.TITLE_TEXT),
          )

          // Like button next to the title – this is what your test clicks
          LikeButton(
              huntCardViewModel = huntCardViewModel,
              huntId = huntId,
          )
        }

        Text(
            "${HuntCardScreenStrings.By} $authorName",
            modifier =
                Modifier.padding(horizontal = HuntCardScreenDefaults.InfoTextPadding)
                    .clickable(onClick = { goProfile(hunt.authorId) })
                    .testTag(HuntCardScreenTestTags.AUTHOR_TEXT),
        )

        Spacer(modifier = Modifier.height(HuntCardScreenDefaults.AuthorImageSpacing))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
          // IMAGE CAROUSEL (keep your new design)
          HuntImageCarousel(
              hunt = hunt,
              modifier =
                  Modifier.weight(HuntCardScreenDefaults.ImageCarouselWeight)
                      .padding(end = HuntCardScreenDefaults.ImageCarouselPadding),
          )

          // STATS
          Column(
              modifier = Modifier.weight(HuntCardScreenDefaults.StatsColumnWeight),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement =
                  Arrangement.spacedBy(
                      HuntCardScreenDefaults.BadgePadding, Alignment.CenterVertically),
          ) {
            StatsBox(
                hunt.difficulty.toString(),
                DifficultyColor(hunt.difficulty),
                modifier = Modifier,
            )
            StatsBox(
                "${hunt.distance} ${HuntCardScreenStrings.DistanceUnit}",
                HuntCardScreenDefaults.NeutralBadgeColor,
                modifier = Modifier,
            )
            StatsBox(
                "${hunt.time} ${HuntCardScreenStrings.HourUnit}",
                HuntCardScreenDefaults.NeutralBadgeColor,
                modifier = Modifier,
            )
          }
        }
      }
}
