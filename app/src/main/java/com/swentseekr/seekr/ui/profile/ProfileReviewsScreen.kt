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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.swentseekr.seekr.ui.components.MAX_RATING
import com.swentseekr.seekr.ui.components.Rating
import com.swentseekr.seekr.ui.components.RatingType
import com.swentseekr.seekr.ui.components.ReviewCard
import com.swentseekr.seekr.ui.hunt.review.ReviewHuntViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileReviewsScreen(
    userId: String,
    profileViewModel: ProfileViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    navController: NavHostController,
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val profile = uiState.profile
    val totalReviews by profileViewModel.totalReviews.collectAsState()
    val reviews by profileViewModel.reviewsState.collectAsState()
    val reviewHuntViewModel: ReviewHuntViewModel = viewModel()


    LaunchedEffect(userId) {
        profileViewModel.loadProfile(userId)
    }
    LaunchedEffect(profile) {
        profile?.let { profileViewModel.loadAllReviewsForProfile(it) }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reviews") },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (profile == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Top summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Rating(rating = profile.author.reviewRate, RatingType.STAR)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (totalReviews == 1)
                            "${String.format("%.1f",profile.author.reviewRate)}/${MAX_RATING} - $totalReviews review"
                        else
                            "${String.format("%.1f", profile.author.reviewRate)}/${MAX_RATING} - $totalReviews reviews",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Divider()

                // ProfileReviewsScreen
                LaunchedEffect(reviews) {
                    reviews.forEach { review ->
                        reviewHuntViewModel.loadHunt(review.huntId)
                        reviewHuntViewModel.loadAuthorProfile(review.authorId)
                    }
                }


                // Scrollable list of reviews
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    if (reviews.isEmpty()) {
                        item {
                            Text(
                                text = "No reviews yet",
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(reviews) { review ->
                            ReviewCard(
                                review = review,
                                reviewHuntViewModel = reviewHuntViewModel,
                                currentUserId = profileViewModel.currentUid,
                                navController = navController,
                                onDeleteReview = { reviewId ->
                                    reviewHuntViewModel.deleteReview(reviewId, review.authorId,                 currentUserId = profileViewModel.currentUid
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}