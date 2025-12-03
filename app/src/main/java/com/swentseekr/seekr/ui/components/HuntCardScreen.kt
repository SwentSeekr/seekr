package com.swentseekr.seekr.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.swentseekr.seekr.model.hunt.HUNTS_COLLECTION_PATH
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

    // --- Logic ---
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
                        onClick = onGoBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = HuntCardScreenStrings.BackContentDescription,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = beginHunt,
                modifier = Modifier
                    .testTag(HuntCardScreenTestTags.BEGIN_BUTTON)
                    .size(HuntCardScreenDefaults.IconSize32 * 2),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = HuntCardScreenStrings.BeginHunt,
                    modifier = Modifier.size(HuntCardScreenDefaults.IconSize32)
                )
            }
        },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.onPrimary
    ) { innerPadding ->

        if (hunt == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .testTag("HUNT_CARD_LIST")
        ) {
            item {
                ModernHeroImageSection(
                    hunt = hunt,
                    authorName = authorName,
                    huntId = huntId,
                    huntCardViewModel = huntCardViewModel,
                    goProfile = goProfile
                )
            }

            item { ModernStatsSection(hunt = hunt) }
            item { ModernDescriptionSection(description = hunt.description) }
            item { ModernMapSection(hunt = hunt) }

            item {
                ModernActionButtons(
                    isCurrentId = isAuthor,
                    buttonIcon = actionIcon,
                    onActionClick = actionButton
                )
            }
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(HuntCardScreenDefaults.AspectRatioHero)
    ) {
        HuntImageCarousel(
            hunt = hunt,
            modifier = Modifier.fillMaxWidth()
        )

        ModernDifficultyBadge(
            difficulty = hunt.difficulty,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(HuntCardScreenDefaults.Padding16)
        )

        LikeButton(
            huntCardViewModel = huntCardViewModel,
            huntId = huntId,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(HuntCardScreenDefaults.Padding16)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(HuntCardScreenDefaults.Padding20)
        ) {
            Text(
                text = hunt.title,
                fontSize = HuntCardScreenDefaults.TitleFontSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                lineHeight = HuntCardScreenDefaults.LineHeight,
                modifier = Modifier.testTag(HuntCardScreenTestTags.TITLE_TEXT)
            )

            Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding8))

            Text(
                text = "${HuntCardScreenStrings.By} $authorName",
                fontSize = HuntCardScreenDefaults.AuthorFontSize,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = HuntCardScreenDefaults.Alpha),
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { goProfile(hunt.authorId) }
                    .testTag(HuntCardScreenTestTags.AUTHOR_TEXT)
            )
        }
    }
}

@Composable
fun ModernStatsSection(hunt: Hunt) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = HuntCardScreenDefaults.Padding20,
                vertical = HuntCardScreenDefaults.Padding20
            ),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ModernStatCard(
            label = HuntCardScreenStrings.DistanceLabel,
            value = "${hunt.distance}",
            unit = HuntCardScreenStrings.DistanceUnit,
            modifier = Modifier.weight(HuntCardScreenDefaults.CardWeight)
        )

        Spacer(modifier = Modifier.width(HuntCardScreenDefaults.Padding12))

        ModernStatCard(
            label = HuntCardScreenStrings.DurationLabel,
            value = "${hunt.time}",
            unit = HuntCardScreenStrings.HourUnit,
            modifier = Modifier.weight(HuntCardScreenDefaults.CardWeight)
        )
    }
}

@Composable
fun ModernStatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = HuntCardScreenDefaults.CardElevation),
        shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HuntCardScreenDefaults.Padding16),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding4))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = HuntCardScreenDefaults.MediumFontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = " $unit",
                    fontSize = HuntCardScreenDefaults.AuthorFontSize,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(bottom = HuntCardScreenDefaults.Padding2)
                )
            }
        }
    }
}

@Composable
fun ModernDescriptionSection(description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = HuntCardScreenDefaults.Padding20,
                vertical = HuntCardScreenDefaults.Padding12
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = HuntCardScreenDefaults.CardElevation),
        shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)
    ) {
        Column(modifier = Modifier.padding(HuntCardScreenDefaults.Padding20)) {
            Text(
                text = HuntCardScreenStrings.DescriptionLabel,
                fontSize = HuntCardScreenDefaults.SmallFontSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding12))

            Text(
                text = description,
                fontSize = HuntCardScreenDefaults.DescriptionFontSize,
                lineHeight = HuntCardScreenDefaults.DescriptionLineHeight,
                color = HuntCardScreenDefaults.DarkGray,
                modifier = Modifier.testTag(HuntCardScreenTestTags.DESCRIPTION_TEXT)
            )
        }
    }
}

@Composable
fun ModernMapSection(hunt: Hunt) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = HuntCardScreenDefaults.Padding20,
                vertical = HuntCardScreenDefaults.Padding12
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = HuntCardScreenDefaults.CardElevation),
        shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)
    ) {
        Column(modifier = Modifier.padding(HuntCardScreenDefaults.Padding20)) {

            Text(
                text = HuntCardScreenStrings.StartingPointLabel,
                fontSize = HuntCardScreenDefaults.SmallFontSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding12))

            val startPosition = LatLng(hunt.start.latitude, hunt.start.longitude)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(startPosition, HuntCardScreenDefaults.Zoom)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HuntCardScreenDefaults.MapHeight250)
                    .clip(RoundedCornerShape(HuntCardScreenDefaults.CornerRadius))
                    .testTag(HuntCardScreenTestTags.MAP_CONTAINER)
            ) {
                GoogleMap(
                    modifier = Modifier.matchParentSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = MarkerState(position = startPosition),
                        title = "${HuntCardScreenStrings.ReviewMarkerTitlePrefix}${hunt.start.name}",
                        snippet = hunt.start.name.ifBlank { null }
                    )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = HuntCardScreenDefaults.Padding20,
                vertical = HuntCardScreenDefaults.Padding12
            ),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            onClick = onActionClick,
            modifier = Modifier.testTag(HuntCardScreenTestTags.REVIEW_BUTTON),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCurrentId)
                    HuntCardScreenDefaults.BlueButton
                else
                    HuntCardScreenDefaults.OrangeButton
            ),
            shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)
        ) {
            Icon(
                imageVector = buttonIcon,
                contentDescription = null,
                modifier = Modifier.size(HuntCardScreenDefaults.IconSize18)
            )

            Spacer(modifier = Modifier.width(HuntCardScreenDefaults.Padding8))

            Text(
                text = if (isCurrentId)
                    HuntCardScreenStrings.EditHunt
                else
                    HuntCardScreenStrings.AddReview,
                fontSize = HuntCardScreenDefaults.DescriptionFontSize,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ModernEmptyReviewsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = HuntCardScreenDefaults.Padding40),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(HuntCardScreenDefaults.IconSize48),
                tint = HuntCardScreenDefaults.WhiteTint
            )

            Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding12))

            Text(
                text = HuntCardScreenStrings.NoReviews,
                modifier = Modifier.testTag("NO_REVIEWS_TEXT")
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = HuntCardScreenDefaults.Padding20,
                vertical = HuntCardScreenDefaults.Padding8
            )
            .testTag(HuntCardScreenTestTags.REVIEW_CARD),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = HuntCardScreenDefaults.CardElevation),
        shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)
    ) {
        Column(modifier = Modifier.padding(HuntCardScreenDefaults.Padding16)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                ProfilePicture(
                    profilePictureRes = authorProfile?.author?.profilePicture ?: 0,
                    modifier = Modifier
                        .size(HuntCardScreenDefaults.ProfilePictureSize)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(HuntCardScreenDefaults.Padding12))

                Column(modifier = Modifier.weight(HuntCardScreenDefaults.CardWeight)) {
                    val authorName =
                        authorProfile?.author?.pseudonym ?: HuntCardScreenStrings.UnknownAuthor

                    Text(
                        text = authorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = HuntCardScreenDefaults.DescriptionFontSize,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding4))

                    Rating(review.rating, RatingType.STAR)
                }

                if (isCurrentId) {
                    IconButton(
                        onClick = { onDeleteReview(review.reviewId) },
                        modifier = Modifier.testTag(HuntCardScreenTestTags.DELETE_REVIEW_BUTTON)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = HuntCardScreenStrings.ReviewDeleteButton,
                            tint = HuntCardScreenDefaults.OrangeTint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding12))

            Text(
                text = review.comment,
                fontSize = HuntCardScreenDefaults.DescriptionFontSize,
                lineHeight = HuntCardScreenDefaults.OtherLineHeight,
                color = HuntCardScreenDefaults.DarkGray
            )

            if (review.photos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding12))

                Button(
                    onClick = {
                        reviewHuntViewModel.loadReviewImages(review.photos)
                        navController.navigate("reviewImages")
                    },
                    modifier = Modifier.testTag("SEE_PICTURES_BUTTON"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)
                ) {
                    Text("See Pictures (${review.photos.size})", fontSize = HuntCardScreenDefaults.MinFontSize)
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

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = HuntCardScreenDefaults.Alpha)
    ) {
        IconButton(
            onClick = { huntCardViewModel.onLikeClick(huntId) },
            modifier = Modifier.testTag(HuntCardScreenTestTags.LIKE_BUTTON)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = HuntCardScreenStrings.LikeButton,
                tint = if (isLiked)
                    HuntCardScreenDefaults.RedLike
                else
                    HuntCardScreenDefaults.GreyDefault,
                modifier = Modifier.size(HuntCardScreenDefaults.IconSize24)
            )
        }
    }
}

