package com.swentseekr.seekr.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.ui.components.MAX_RATING
import com.swentseekr.seekr.ui.components.ModernReviewCard
import com.swentseekr.seekr.ui.components.Rating
import com.swentseekr.seekr.ui.components.RatingType
import com.swentseekr.seekr.ui.hunt.review.ReviewHuntViewModel
import com.swentseekr.seekr.ui.profile.ProfileReviewsScreenConstant.COLUMN_PADDING
import com.swentseekr.seekr.ui.profile.ProfileReviewsScreenConstant.DETAIL_ROUTE
import com.swentseekr.seekr.ui.profile.ProfileReviewsScreenConstant.DIVIDER
import com.swentseekr.seekr.ui.profile.ProfileReviewsScreenConstant.HEADER_KEY
import com.swentseekr.seekr.ui.profile.ProfileReviewsScreenConstant.HORIZONTAL_DIVIDER_PADDING
import com.swentseekr.seekr.ui.profile.ProfileReviewsScreenConstant.MULTIPLE_REVIEWS
import com.swentseekr.seekr.ui.profile.ProfileReviewsScreenConstant.RATING_FORMAT
import com.swentseekr.seekr.ui.profile.ProfileReviewsScreenConstant.SINGLE_REVIEW
import com.swentseekr.seekr.ui.profile.ProfileReviewsScreenConstant.SPACER_HEIGHT
import com.swentseekr.seekr.ui.profile.ProfileReviewsScreenConstant.STRING_FORMAT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileReviewsScreen(
    userId: String,
    profileViewModel: ProfileViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    editReview: () -> Unit = {},
    navController: NavHostController,
    testProfile: Profile? = null,
    testReviews: List<HuntReview>? = null,
    reviewHuntViewModel: ReviewHuntViewModel = viewModel(), // for testing
    testHuntsById: Map<String, Hunt>? = null // for testing
) {
  val uiState by profileViewModel.uiState.collectAsState()
  val profile = testProfile ?: uiState.profile
  val reviews = testReviews ?: profileViewModel.reviewsState.collectAsState().value
  val totalReviews = reviews.size
  val reviewHuntViewModel: ReviewHuntViewModel = reviewHuntViewModel
  val huntsById = testHuntsById ?: reviewHuntViewModel.huntsById.collectAsState().value
  val reviewHuntState by reviewHuntViewModel.uiState.collectAsState()

  LaunchedEffect(userId) { if (testProfile == null) profileViewModel.loadProfile(userId) }
  LaunchedEffect(profile) {
    if (testProfile == null && profile != null) profileViewModel.loadAllReviewsForProfile(profile)
  }

  LaunchedEffect(reviews) {
    reviews.forEach { review ->
      reviewHuntViewModel.loadHunt(review.huntId)
      reviewHuntViewModel.loadAuthorProfile(review.authorId)
    }
  }
  Scaffold(
      modifier = Modifier.testTag(ProfileReviewsTestTags.SCREEN),
      topBar = {
        TopAppBar(
            modifier = Modifier.testTag(ProfileReviewsTestTags.TOP_BAR),
            title = { Text(ProfileReviewsScreenConstant.REVIEWS) },
            navigationIcon = {
              IconButton(
                  onClick = onGoBack,
                  modifier = Modifier.testTag(ProfileReviewsTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = ProfileReviewsScreenConstant.BACK)
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
                        .padding(ProfileReviewsScreenConstant.Padding16)
                        .testTag(ProfileReviewsTestTags.RATING_SUMMARY),
                verticalAlignment = Alignment.CenterVertically) {
                  Rating(rating = profile.author.reviewRate, RatingType.STAR)
                  Spacer(modifier = Modifier.width(ProfileReviewsScreenConstant.Padding08))
                  Text(
                      text =
                          String.format(
                              if (totalReviews == ProfileReviewsScreenConstant.ONE_REVIEW)
                                  SINGLE_REVIEW
                              else MULTIPLE_REVIEWS,
                              String.format(STRING_FORMAT, profile.author.reviewRate),
                              MAX_RATING,
                              totalReviews),
                      style = MaterialTheme.typography.bodyMedium,
                      modifier = Modifier.testTag(ProfileReviewsTestTags.RATING_TEXT))
                }

            HorizontalDivider(
                modifier = Modifier.testTag(ProfileReviewsTestTags.DIVIDER),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color)

            // ProfileReviewsScreen
            LaunchedEffect(reviews) {
              reviews
                  .map { it.huntId }
                  .distinct()
                  .forEach { huntId -> reviewHuntViewModel.loadHunt(huntId) }
              reviews.forEach { review -> reviewHuntViewModel.loadAuthorProfile(review.authorId) }
            }

            val groupedReviews = reviews.groupBy { it.huntId }

            // Scrollable list of reviews
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag(ProfileReviewsTestTags.REVIEWS_LIST),
                contentPadding = PaddingValues(vertical = ProfileReviewsScreenConstant.Padding08)) {
                  if (reviews.isEmpty()) {
                    item {
                      Text(
                          text = ProfileReviewsScreenConstant.NO_REVIEW,
                          modifier =
                              Modifier.fillMaxWidth()
                                  .padding(ProfileReviewsScreenConstant.Padding16)
                                  .testTag(ProfileReviewsTestTags.EMPTY_MESSAGE),
                          textAlign = TextAlign.Center)
                    }
                  } else {
                    groupedReviews.forEach { (huntId, huntReviews) ->
                      val hunt = huntsById[huntId]

                      // --- SECTION HEADER ---
                      if (hunt != null) {
                        val headerKey = String.format(HEADER_KEY, huntId)
                        val detailRoute = String.format(DETAIL_ROUTE, hunt.uid)
                        item(key = headerKey) {
                          Column(
                              Modifier.fillMaxWidth()
                                  .clickable { navController.navigate(detailRoute) }
                                  .padding(COLUMN_PADDING.dp)
                                  .testTag(headerKey)) {
                                Text(
                                    text = hunt.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(SPACER_HEIGHT.dp))
                                Text(
                                    text =
                                        String.format(
                                            RATING_FORMAT,
                                            String.format(STRING_FORMAT, hunt.reviewRate),
                                            MAX_RATING),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary)
                              }
                        }
                      }
                      items(huntReviews, key = { it.reviewId }) { review ->
                        Box(
                            modifier =
                                Modifier.testTag(
                                    ProfileReviewsTestTags.reviewCardTag(review.reviewId))) {
                              ModernReviewCard(
                                  review = review,
                                  reviewHuntViewModel = reviewHuntViewModel,
                                  currentUserId = profileViewModel.currentUid,
                                  navController = navController,
                                  onDeleteReview = { reviewId ->
                                    reviewHuntViewModel.deleteReview(
                                        reviewId,
                                        review.authorId,
                                        currentUserId = profileViewModel.currentUid)
                                  },
                                  onEdit = { editReview },
                                  authorProfile = reviewHuntState.authorProfiles[review.authorId])
                            }
                      }
                      val huntDivider = String.format(DIVIDER, huntId)
                      item(key = huntDivider) {
                        HorizontalDivider(
                            modifier =
                                Modifier.padding(vertical = HORIZONTAL_DIVIDER_PADDING.dp)
                                    .testTag(huntDivider))
                      }
                    }
                  }
                }
          }
        }
      }
}
