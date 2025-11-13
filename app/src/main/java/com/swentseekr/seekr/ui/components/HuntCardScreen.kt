package com.swentseekr.seekr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuntCardScreen(
    huntId: String,
    modifier: Modifier = Modifier,
    huntCardViewModel: HuntCardViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    beginHunt: () -> Unit = {},
    addReview: () -> Unit = {},
    testmode: Boolean = false,
) {
  val uiState by huntCardViewModel.uiState.collectAsState()

  // Load when arriving / when id changes
  LaunchedEffect(huntId) { huntCardViewModel.loadHunt(huntId) }
  LaunchedEffect(huntId) { huntCardViewModel.loadOtherReview(huntId) }

  val hunt2 = uiState.hunt
  // val reviews = uiState.reviewList
  val reviews =
      List(HuntCardScreenDefaults.SampleReviewCount) { index ->
        HuntReview(
            reviewId = "review$index",
            authorId = "author$index",
            huntId = "hunt123",
            rating = 4.0 + (index % 2),
            comment = "This is review number $index",
            photos = emptyList())
      }

  val author = "SpikeMan"
  val hunt =
      Hunt(
          uid = "hunt123",
          start = Location(40.7128, -74.0060, "New York"),
          end = Location(40.730610, -73.935242, "Brooklyn"),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = "City Exploration",
          description =
              "Discover hidden gems in the city åß∂ƒ@ªº∆¬±“#Ç[]|{}≠¿´‘¶–…«¬∆øπ§¢æ‘¡°œ∑€®️†Ωn hello kitty",
          time = 2.5,
          distance = 5.0,
          difficulty = Difficulty.DIFFICULT,
          authorId = "0",
          mainImageUrl = R.drawable.ic_launcher_foreground.toString(),
          reviewRate = 4.5)
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
                          HuntCardScreenDefaults.PrimaryBorderColor,
                          RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)),
              colors =
                  CardDefaults.cardColors(
                      containerColor = HuntCardScreenDefaults.CardBackgroundColor),
              shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)) {
                LazyColumn(
                    modifier =
                        Modifier.padding(HuntCardScreenDefaults.CardInnerPadding).fillMaxWidth(),
                    verticalArrangement =
                        Arrangement.spacedBy(HuntCardScreenDefaults.SectionSpacing)) {
                      // ROW WITH IMAGE, TITLE, AUTHOR, DIFFICULTY, DISTANCE, TIME
                      item {
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
                                        Modifier.weight(1f)
                                            .padding(HuntCardScreenDefaults.InfoTextPadding)
                                            .testTag(HuntCardScreenTestTags.TITLE_TEXT))
                              }
                              Text(
                                  "by $author",
                                  modifier =
                                      Modifier.padding(
                                              horizontal = HuntCardScreenDefaults.InfoTextPadding)
                                          .testTag(HuntCardScreenTestTags.AUTHOR_TEXT))
                              Row {
                                AsyncImage(
                                    model = hunt.mainImageUrl.ifEmpty { R.drawable.empty_image },
                                    contentDescription =
                                        HuntCardScreenStrings.HuntPictureDescription,
                                    modifier =
                                        Modifier.padding(
                                                horizontal = HuntCardScreenDefaults.InfoTextPadding)
                                            .size(HuntCardScreenDefaults.ImageSize)
                                            .clip(RectangleShape)
                                            .testTag(HuntCardScreenTestTags.IMAGE),
                                    placeholder = painterResource(R.drawable.empty_image),
                                    error = painterResource(R.drawable.empty_image))
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally) {
                                      Box(
                                          modifier =
                                              Modifier.padding(HuntCardScreenDefaults.BadgePadding)
                                                  .background(
                                                      HuntCardScreenDefaults.DifficultyBadgeColor)
                                                  .height(HuntCardScreenDefaults.BadgeHeight)
                                                  .width(HuntCardScreenDefaults.BadgeWidth)
                                                  .clip(RectangleShape)
                                                  .testTag(HuntCardScreenTestTags.DIFFICULTY_BOX),
                                      ) {
                                        Text(
                                            hunt.difficulty.toString(),
                                            modifier =
                                                Modifier.align(Alignment.Center)
                                                    .padding(
                                                        HuntCardScreenDefaults.BadgeTextPadding))
                                      }

                                      Box(
                                          modifier =
                                              Modifier.padding(HuntCardScreenDefaults.BadgePadding)
                                                  .background(
                                                      HuntCardScreenDefaults.NeutralBadgeColor)
                                                  .height(HuntCardScreenDefaults.BadgeHeight)
                                                  .width(HuntCardScreenDefaults.BadgeWidth)
                                                  .clip(RectangleShape)
                                                  .testTag(HuntCardScreenTestTags.DISTANCE_BOX),
                                      ) {
                                        Text(
                                            "${hunt.distance} ${HuntCardScreenStrings.DistanceUnit}",
                                            modifier =
                                                Modifier.align(Alignment.Center)
                                                    .padding(
                                                        HuntCardScreenDefaults.BadgeTextPadding))
                                      }
                                      Box(
                                          modifier =
                                              Modifier.padding(HuntCardScreenDefaults.BadgePadding)
                                                  .background(
                                                      HuntCardScreenDefaults.NeutralBadgeColor)
                                                  .height(HuntCardScreenDefaults.BadgeHeight)
                                                  .width(HuntCardScreenDefaults.BadgeWidth)
                                                  .clip(RectangleShape)
                                                  .testTag(HuntCardScreenTestTags.TIME_BOX),
                                      ) {
                                        Text(
                                            "${hunt.time} ${HuntCardScreenStrings.TimeUnit}",
                                            modifier =
                                                Modifier.align(Alignment.Center)
                                                    .padding(
                                                        HuntCardScreenDefaults.BadgeTextPadding))
                                      }
                                    }
                              }
                            }
                      }

                      // DESCRIPTION
                      item {
                        Text(
                            hunt.description,
                            modifier =
                                Modifier.padding(HuntCardScreenDefaults.InfoColumnPadding)
                                    .testTag(HuntCardScreenTestTags.DESCRIPTION_TEXT))
                      }
                      // MAP WITH START POINT
                      item {
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
                                              "${HuntCardScreenStrings.ReviewMarkerTitlePrefix}${hunt.start.name}",
                                          snippet = HuntCardScreenStrings.ReviewHint)
                                    }
                              }
                        }
                      }
                      // BOUTON BEGIN HUNT
                      item {
                        Row(
                            modifier = modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                          Button(
                              beginHunt,
                              modifier =
                                  modifier
                                      .width(HuntCardScreenDefaults.ButtonWidth)
                                      .testTag(HuntCardScreenTestTags.BEGIN_BUTTON)) {
                                Text(HuntCardScreenStrings.BeginHunt)
                              }
                          Button(
                              addReview,
                              modifier =
                                  modifier
                                      .width(HuntCardScreenDefaults.ButtonWidth)
                                      .testTag(HuntCardScreenTestTags.REVIEW_BUTTON)) {
                                Text(HuntCardScreenStrings.AddReview)
                              }
                        }
                      }
                    }
              }

          LazyColumn(
              modifier =
                  modifier
                      .fillMaxWidth()
                      .padding(innerPadding)
                      .padding(horizontal = HuntCardScreenDefaults.ScreenPaddingHorizontal)
                      .padding(
                          top = HuntCardScreenDefaults.ScreenPaddingTop,
                          bottom = HuntCardScreenDefaults.ScreenPaddingBottom)) {
                items(reviews) { review -> ReviewCard(review) }
              }
        }
      }
}

@Composable
fun ReviewCard(review: HuntReview) {
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
      Text(
          "${HuntCardScreenStrings.ReviewTitlePrefix} ${review.rating}/${HuntCardScreenDefaults.MaxStarNumber}",
          fontWeight = FontWeight.Bold)
      Text(review.comment)
    }
  }
}

/**
 * @Preview
 * @Composable fun HuntCardScreenPreview() { HuntCardScreen() }
 */
