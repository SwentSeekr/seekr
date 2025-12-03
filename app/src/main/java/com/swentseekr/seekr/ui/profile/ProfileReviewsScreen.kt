package com.swentseekr.seekr.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.ui.components.MAX_RATING
import com.swentseekr.seekr.ui.components.Rating
import com.swentseekr.seekr.ui.components.RatingType
import com.swentseekr.seekr.ui.components.ReviewCard
import com.swentseekr.seekr.ui.hunt.review.ReviewHuntViewModel

object ProfileReviewsTestTags {
  const val SCREEN = "PROFILE_REVIEWS_SCREEN"
  const val LOADING = "PROFILE_REVIEWS_LOADING"
  const val TOP_BAR = "PROFILE_REVIEWS_TOP_BAR"
  const val BACK_BUTTON = "PROFILE_REVIEWS_BACK_BUTTON"
  const val RATING_SUMMARY = "PROFILE_REVIEWS_RATING_SUMMARY"
  const val RATING_TEXT = "PROFILE_REVIEWS_RATING_TEXT"
  const val DIVIDER = "PROFILE_REVIEWS_DIVIDER"
  const val REVIEWS_LIST = "PROFILE_REVIEWS_LIST"
  const val EMPTY_MESSAGE = "PROFILE_REVIEWS_EMPTY_MESSAGE"

  fun reviewCardTag(reviewId: String) = "REVIEW_CARD_$reviewId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileReviewsScreen(
    userId: String,
    profileViewModel: ProfileViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    navController: NavHostController,
    testProfile: Profile? = null,
    testReviews: List<HuntReview>? = null
) {
  val uiState by profileViewModel.uiState.collectAsState()
  val profile = testProfile ?: uiState.profile
  val reviews = testReviews ?: profileViewModel.reviewsState.collectAsState().value
  val totalReviews = reviews.size
  val reviewHuntViewModel: ReviewHuntViewModel = viewModel()

  LaunchedEffect(userId) { if (testProfile == null) profileViewModel.loadProfile(userId) }
  LaunchedEffect(profile) {
    if (testProfile == null && profile != null) profileViewModel.loadAllReviewsForProfile(profile)
  }

  Scaffold(
      modifier = Modifier.testTag(ProfileReviewsTestTags.SCREEN),
      topBar = {
        TopAppBar(
            modifier = Modifier.testTag(ProfileReviewsTestTags.TOP_BAR),
            title = { Text("Reviews") },
            navigationIcon = {
              IconButton(
                  onClick = onGoBack,
                  modifier = Modifier.testTag(ProfileReviewsTestTags.BACK_BUTTON)) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                  }
            })
      }) { padding ->
        if (profile == null) {
          Box(
              modifier =
                  Modifier.fillMaxSize().padding(padding).testTag(ProfileReviewsTestTags.LOADING),
              contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
              }
        } else {
          Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Top summary
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(16.dp)
                        .testTag(ProfileReviewsTestTags.RATING_SUMMARY),
                verticalAlignment = Alignment.CenterVertically) {
                  Rating(rating = profile.author.reviewRate, RatingType.STAR)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                      text =
                          if (totalReviews == 1)
                              "${String.format("%.1f",profile.author.reviewRate)}/${MAX_RATING} - $totalReviews review"
                          else
                              "${String.format("%.1f", profile.author.reviewRate)}/${MAX_RATING} - $totalReviews reviews",
                      style = MaterialTheme.typography.bodyMedium,
                      modifier = Modifier.testTag(ProfileReviewsTestTags.RATING_TEXT))
                }

            Divider(modifier = Modifier.testTag(ProfileReviewsTestTags.DIVIDER))

            // ProfileReviewsScreen
            LaunchedEffect(reviews) {
              reviews.forEach { review ->
                reviewHuntViewModel.loadHunt(review.huntId)
                reviewHuntViewModel.loadAuthorProfile(review.authorId)
              }
            }

            // Scrollable list of reviews
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag(ProfileReviewsTestTags.REVIEWS_LIST),
                contentPadding = PaddingValues(vertical = 8.dp)) {
                  if (reviews.isEmpty()) {
                    item {
                      Text(
                          text = "No reviews yet",
                          modifier =
                              Modifier.fillMaxWidth()
                                  .padding(16.dp)
                                  .testTag(ProfileReviewsTestTags.EMPTY_MESSAGE),
                          textAlign = TextAlign.Center)
                    }
                  } else {
                    items(reviews) { review ->
                      Box(
                          modifier =
                              Modifier.testTag(
                                  ProfileReviewsTestTags.reviewCardTag(review.reviewId))) {
                            ReviewCard(
                                review = review,
                                reviewHuntViewModel = reviewHuntViewModel,
                                currentUserId = profileViewModel.currentUid,
                                navController = navController,
                                onDeleteReview = { reviewId ->
                                  reviewHuntViewModel.deleteReview(
                                      reviewId,
                                      review.authorId,
                                      currentUserId = profileViewModel.currentUid)
                                })
                          }
                    }
                  }
                }
          }
        }
      }
}
