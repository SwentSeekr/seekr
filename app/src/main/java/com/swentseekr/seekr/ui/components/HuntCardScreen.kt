package com.swentseekr.seekr.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.ui.hunt.review.ReviewHuntViewModel
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel
import com.swentseekr.seekr.ui.profile.ProfilePicture

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
    navController: NavHostController
) {
  val uiState by huntCardViewModel.uiState.collectAsState()

  // --- Load core data ---
  LaunchedEffect(huntId) { huntCardViewModel.loadHunt(huntId) }
  val hunt = uiState.hunt
  val authorId = hunt?.authorId ?: ""

  LaunchedEffect(authorId) { huntCardViewModel.loadAuthorProfile(authorId) }
  val authorProfile = uiState.authorProfile

  LaunchedEffect(huntId) { huntCardViewModel.loadOtherReview(huntId) }
  val reviews = uiState.reviewList

  LaunchedEffect(Unit) { huntCardViewModel.loadCurrentUserID() }
  val currentUserId = uiState.currentUserId

  // --- Logic from old main ---
  val isAuthor = currentUserId == authorId
  val actionButton = if (isAuthor) editHunt else addReview
  val actionIcon = if (isAuthor) Icons.Filled.Edit else Icons.Filled.Star
  val authorName = authorProfile?.author?.pseudonym ?: HuntCardScreenStrings.UnknownAuthor

  Scaffold(
      topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(
                  modifier = Modifier.testTag(HuntCardScreenTestTags.GO_BACK_BUTTON),
                  onClick = onGoBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = HuntCardScreenStrings.BackContentDescription,
                        tint = Color.White)
                  }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A)))
      },
      floatingActionButton = {
        FloatingActionButton(
            onClick = beginHunt,
            modifier = Modifier.testTag(HuntCardScreenTestTags.BEGIN_BUTTON).size(64.dp),
            containerColor = Color(0xFF00C853),
            contentColor = Color.White,
            shape = CircleShape) {
              Icon(
                  imageVector = Icons.Filled.PlayArrow,
                  contentDescription = HuntCardScreenStrings.BeginHunt,
                  modifier = Modifier.size(32.dp))
            }
      },
      modifier = modifier.fillMaxSize(),
      containerColor = Color(0xFFF8F9FA)) { innerPadding ->

        // LOADING STATE
        if (hunt == null) {
          Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00C853))
          }
          return@Scaffold
        }

        // FULL SCREEN CONTENT
        LazyColumn(modifier = Modifier.padding(innerPadding).testTag("HUNT_CARD_LIST")) {
          item {
            ModernHeroImageSection(
                hunt = hunt,
                authorName = authorName,
                huntId = huntId,
                huntCardViewModel = huntCardViewModel,
                goProfile = goProfile)
          }

          item { ModernStatsSection(hunt = hunt) }
          item { ModernDescriptionSection(description = hunt.description) }

          item { ModernMapSection(hunt = hunt) }

          item {
            ModernActionButtons(
                isCurrentId = isAuthor, buttonIcon = actionIcon, onActionClick = actionButton)
          }

          // REVIEWS HEADER
          item {
            Text(
                HuntCardScreenStrings.Reviews,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 32.dp, bottom = 16.dp))
          }

          // REVIEWS LIST
          if (reviews.isNullOrEmpty()) {
            item { ModernEmptyReviewsState() }
          } else {
            items(reviews) { review ->
              ModernReviewCard(
                  review = review,
                  reviewHuntViewModel = reviewViewModel,
                  currentUserId = currentUserId,
                  navController = navController,
                  onDeleteReview = { reviewId ->
                    huntCardViewModel.deleteReview(
                        review.huntId, reviewId, review.authorId, currentUserId)
                  })
            }
          }

          item { Spacer(modifier = Modifier.height(80.dp)) }
        }
      }
}

@Composable
fun ModernHeroImageSection(
    hunt: Hunt,
    authorName: String,
    huntId: String,
    huntCardViewModel: HuntCardViewModel,
    goProfile: (String) -> Unit
) {
  Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
    HuntImageCarousel(hunt = hunt, modifier = Modifier.fillMaxWidth())

    // Difficulty badge
    ModernDifficultyBadge(
        difficulty = hunt.difficulty, modifier = Modifier.align(Alignment.TopStart).padding(16.dp))

    // Like button
    LikeButton(
        huntCardViewModel = huntCardViewModel,
        huntId = huntId,
        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp))

    // Title + Author
    Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
      Text(
          text = hunt.title,
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          lineHeight = 32.sp,
          modifier = Modifier.testTag(HuntCardScreenTestTags.TITLE_TEXT))

      Spacer(modifier = Modifier.height(8.dp))

      Text(
          text = "${HuntCardScreenStrings.By} $authorName",
          fontSize = 16.sp,
          color = Color.White.copy(alpha = 0.9f),
          fontWeight = FontWeight.Medium,
          modifier =
              Modifier.clickable { goProfile(hunt.authorId) }
                  .testTag(HuntCardScreenTestTags.AUTHOR_TEXT))
    }
  }
}

@Composable
fun ModernStatsSection(hunt: com.swentseekr.seekr.model.hunt.Hunt) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
      horizontalArrangement = Arrangement.SpaceEvenly) {
        ModernStatCard(
            label = "Distance",
            value = "${hunt.distance}",
            unit = HuntCardScreenStrings.DistanceUnit,
            modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(12.dp))
        ModernStatCard(
            label = "Duration",
            value = "${hunt.time}",
            unit = HuntCardScreenStrings.HourUnit,
            modifier = Modifier.weight(1f))
      }
}

@Composable
fun ModernStatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
  Card(
      modifier = modifier,
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                  text = label,
                  fontSize = 12.sp,
                  color = Color(0xFF666666),
                  fontWeight = FontWeight.Medium)
              Spacer(modifier = Modifier.height(4.dp))
              Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A))
                Text(
                    text = " $unit",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 2.dp))
              }
            }
      }
}

@Composable
fun ModernDescriptionSection(description: String) {
  Card(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
              text = "Description",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1A1A1A))
          Spacer(modifier = Modifier.height(12.dp))
          Text(
              text = description,
              fontSize = 15.sp,
              lineHeight = 22.sp,
              color = Color(0xFF444444),
              modifier = Modifier.testTag(HuntCardScreenTestTags.DESCRIPTION_TEXT))
        }
      }
}

@Composable
fun ModernMapSection(hunt: com.swentseekr.seekr.model.hunt.Hunt) {
  Card(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
              text = "Starting Point",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1A1A1A))
          Spacer(modifier = Modifier.height(12.dp))

          val startPosition = LatLng(hunt.start.latitude, hunt.start.longitude)
          val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(startPosition, 12f)
          }

          Box(
              modifier =
                  Modifier.fillMaxWidth()
                      .height(250.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .testTag(HuntCardScreenTestTags.MAP_CONTAINER)) {
                GoogleMap(
                    modifier = Modifier.matchParentSize(),
                    cameraPositionState = cameraPositionState) {
                      Marker(
                          state = MarkerState(position = startPosition),
                          title =
                              "${HuntCardScreenStrings.ReviewMarkerTitlePrefix}${hunt.start.name}",
                          snippet = hunt.start.name.ifBlank { null })
                    }
              }
        }
      }
}

@Composable
fun ModernActionButtons(
    isCurrentId: Boolean,
    buttonIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onActionClick: () -> Unit
) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.End) {
        Button(
            onClick = onActionClick,
            modifier = Modifier.testTag(HuntCardScreenTestTags.REVIEW_BUTTON),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = if (isCurrentId) Color(0xFF2196F3) else Color(0xFFFFA726)),
            shape = RoundedCornerShape(12.dp)) {
              Icon(
                  imageVector = buttonIcon,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                  text =
                      if (isCurrentId) HuntCardScreenStrings.EditHunt
                      else HuntCardScreenStrings.AddReview,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.SemiBold)
            }
      }
}

@Composable
fun ModernEmptyReviewsState() {
  Box(
      modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
      contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
              imageVector = Icons.Filled.Star,
              contentDescription = null,
              modifier = Modifier.size(48.dp),
              tint = Color(0xFFCCCCCC))
          Spacer(modifier = Modifier.height(12.dp))
          Text(
              text = HuntCardScreenStrings.NoReviews,
              modifier = Modifier.testTag("NO_REVIEWS_TEXT"))
        }
      }
}

@Composable
fun ModernReviewCard(
    review: HuntReview,
    reviewHuntViewModel: ReviewHuntViewModel,
    currentUserId: String?,
    navController: NavHostController,
    onDeleteReview: (String) -> Unit
) {
  val uiState by reviewHuntViewModel.uiState.collectAsState()

  LaunchedEffect(review.huntId) { reviewHuntViewModel.loadHunt(review.huntId) }
  val authorId = review.authorId
  LaunchedEffect(authorId) { reviewHuntViewModel.loadAuthorProfile(authorId) }
  val authorProfile = uiState.authorProfile

  val isCurrentId = currentUserId == authorId

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 8.dp)
              .testTag(HuntCardScreenTestTags.REVIEW_CARD),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            ProfilePicture(
                profilePictureRes = authorProfile?.author?.profilePicture ?: 0,
                modifier = Modifier.size(40.dp).clip(CircleShape))
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              val author = authorProfile?.author?.pseudonym ?: "Unknown Author"
              Text(
                  text = author,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = Color(0xFF1A1A1A))
              Spacer(modifier = Modifier.height(4.dp))
              Rating(review.rating, RatingType.STAR)
            }

            if (isCurrentId) {
              IconButton(
                  onClick = { onDeleteReview(review.reviewId) },
                  modifier = Modifier.testTag(HuntCardScreenTestTags.DELETE_REVIEW_BUTTON)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = HuntCardScreenStrings.ReviewDeleteButton,
                        tint = Color(0xFFEF5350))
                  }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
              text = review.comment,
              fontSize = 14.sp,
              lineHeight = 20.sp,
              color = Color(0xFF444444))

          if (review.photos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                  reviewHuntViewModel.loadReviewImages(review.photos)
                  navController.navigate("reviewImages")
                },
                modifier = Modifier.testTag("SEE_PICTURES_BUTTON"),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5F5F5), contentColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(8.dp)) {
                  Text("See Pictures (${review.photos.size})", fontSize = 13.sp)
                }
          }
        }
      }
}

@Composable
fun LikeButton(
    huntCardViewModel: HuntCardViewModel,
    huntId: String,
    modifier: Modifier = Modifier
) {
  val uiState by huntCardViewModel.uiState.collectAsState()
  val isLiked = uiState.isLiked

  Surface(modifier = modifier, shape = CircleShape, color = Color.White.copy(alpha = 0.9f)) {
    IconButton(
        onClick = { huntCardViewModel.onLikeClick(huntId) },
        modifier = Modifier.testTag(HuntCardScreenTestTags.LIKE_BUTTON)) {
          Icon(
              imageVector = Icons.Default.Favorite,
              contentDescription = HuntCardScreenStrings.LikeButton,
              tint = if (isLiked) Color(0xFFFF5252) else Color(0xFFCCCCCC),
              modifier = Modifier.size(24.dp))
        }
  }
}
