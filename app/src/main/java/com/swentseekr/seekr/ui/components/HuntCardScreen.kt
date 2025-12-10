package com.swentseekr.seekr.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.swentseekr.seekr.model.hunt.review.HuntReviewReplyRepositoryProvider
import com.swentseekr.seekr.ui.hunt.review.ReviewHuntViewModel
import com.swentseekr.seekr.ui.hunt.review.replies.ReviewRepliesSection
import com.swentseekr.seekr.ui.hunt.review.replies.ReviewRepliesViewModel
import com.swentseekr.seekr.ui.hunt.review.replies.ReviewRepliesViewModelFactory
import com.swentseekr.seekr.ui.huntcardview.HuntCardUiState
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel
import com.swentseekr.seekr.ui.profile.ProfilePicture

/**
 * Main screen displaying all the details of a Hunt, including:
 * - hero image carousel with difficulty badge and like button,
 * - statistics (distance, duration),
 * - description section,
 * - map preview with the starting point,
 * - action button (edit or add review depending on author),
 * - list of existing reviews or an empty state.
 *
 * This function is responsible for:
 * - loading the hunt data,
 * - loading the author profile,
 * - loading reviews,
 * - identifying whether the current user is the hunt's author,
 * - choosing the correct action button (edit vs add review).
 *
 * @param huntId The ID of the hunt to load.
 * @param modifier Optional modifier for layout.
 * @param huntCardViewModel ViewModel that provides hunt details and like/review logic.
 * @param reviewViewModel ViewModel that handles review-specific logic.
 * @param onGoBack Callback executed when the user taps the back button.
 * @param goProfile Callback to navigate to the author's profile.
 * @param beginHunt Callback to start the hunt.
 * @param addReview Callback to navigate to the "add review" screen.
 * @param editHunt Callback to navigate to the "edit hunt" screen (if author).
 * @param navController Navigation controller used to display review images.
 */
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

  LoadHuntCardScreenData(huntId = huntId, uiState = uiState, huntCardViewModel = huntCardViewModel)

  val hunt = uiState.hunt
  val authorId = hunt?.authorId ?: ""

  val authorProfile = uiState.authorProfile
  val reviews = uiState.reviewList

  val currentUserId = uiState.currentUserId

  val isAuthor = currentUserId == authorId

  val hasUserReview = currentUserId != null && reviews.any { it.authorId == currentUserId }
  val canUserAddReview = !isAuthor && !hasUserReview

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
                        tint = MaterialTheme.colorScheme.onPrimary)
                  }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onSurface))
      },
      floatingActionButton = {
        FloatingActionButton(
            onClick = beginHunt,
            modifier =
                Modifier.testTag(HuntCardScreenTestTags.BEGIN_BUTTON)
                    .size(
                        HuntCardScreenDefaults.IconSize32 *
                            HuntCardScreenDefaults.BeginButtonSizeMultiplier),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape) {
              Icon(
                  imageVector = Icons.Filled.PlayArrow,
                  contentDescription = HuntCardScreenStrings.BeginHunt,
                  modifier = Modifier.size(HuntCardScreenDefaults.IconSize32))
            }
      },
      modifier = modifier.fillMaxSize(),
      containerColor = HuntCardScreenDefaults.ScreenBackground) { innerPadding ->
        if (hunt == null) {
          Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
          }
          return@Scaffold
        }

        LazyColumn(
            modifier =
                Modifier.padding(innerPadding).testTag(HuntCardScreenTestTags.HUNT_CARD_LIST)) {
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

              if (isAuthor || canUserAddReview) {
                item {
                  ModernActionButtons(
                      isCurrentId = isAuthor, buttonIcon = actionIcon, onActionClick = actionButton)
                }
              }

              // Reviews header
              item {
                Text(
                    HuntCardScreenStrings.Reviews,
                    fontSize = HuntCardScreenDefaults.SmallFontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = HuntCardScreenDefaults.Padding20)
                            .padding(
                                top = HuntCardScreenDefaults.Padding40,
                                bottom = HuntCardScreenDefaults.Padding16))
              }

              // Reviews list
              if (reviews.isEmpty()) {
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

              item {
                Spacer(
                    modifier =
                        Modifier.height(
                            HuntCardScreenDefaults.Padding40 *
                                HuntCardScreenDefaults.EndListSpacerMultiplier))
              }
            }
      }
}

/**
 * Handles all asynchronous data-loading logic required by the HuntCard screen.
 *
 * This helper centralizes the `LaunchedEffect` calls that load:
 * - the current user ID,
 * - the selected hunt's details,
 * - the hunt author's profile,
 * - all other reviews for the hunt.
 *
 * It ensures that data is only fetched when needed and reacts to state changes such as the current
 * user ID or when the hunt becomes available in `uiState`.
 *
 * @param huntId The ID of the hunt whose data should be loaded.
 * @param uiState The current state of the HuntCard screen, used to react to user and hunt changes.
 * @param huntCardViewModel The ViewModel responsible for fetching hunt, profile, and review data.
 */
@Composable
private fun LoadHuntCardScreenData(
    huntId: String,
    uiState: HuntCardUiState,
    huntCardViewModel: HuntCardViewModel
) {
  LaunchedEffect(Unit) { huntCardViewModel.loadCurrentUserID() }

  LaunchedEffect(huntId, uiState.currentUserId) {
    uiState.currentUserId?.let { huntCardViewModel.loadHunt(huntId) }
  }

  LaunchedEffect(uiState.hunt?.authorId) {
    uiState.hunt?.authorId?.let { huntCardViewModel.loadAuthorProfile(it) }
  }

  LaunchedEffect(huntId) { huntCardViewModel.loadOtherReview(huntId) }
}

/**
 * Displays the top "hero" section of the screen:
 * - image carousel,
 * - difficulty badge,
 * - like button,
 * - hunt title and author link.
 *
 * It acts as the visual entry point of the HuntCard screen.
 *
 * @param hunt The hunt to display.
 * @param authorName The displayed name of the hunt's author.
 * @param huntId The ID of the hunt (needed for like interaction).
 * @param huntCardViewModel ViewModel handling like toggling.
 * @param goProfile Callback to navigate to the author’s profile.
 */
@Composable
fun ModernHeroImageSection(
    hunt: Hunt,
    authorName: String,
    huntId: String,
    huntCardViewModel: HuntCardViewModel,
    goProfile: (String) -> Unit
) {
  Box(modifier = Modifier.fillMaxWidth().aspectRatio(HuntCardScreenDefaults.AspectRatioHero)) {
    HuntImageCarousel(hunt = hunt, modifier = Modifier.fillMaxWidth())

    ModernDifficultyBadge(
        difficulty = hunt.difficulty,
        modifier = Modifier.align(Alignment.TopStart).padding(HuntCardScreenDefaults.Padding16))

    LikeButton(
        huntCardViewModel = huntCardViewModel,
        huntId = huntId,
        modifier = Modifier.align(Alignment.TopEnd).padding(HuntCardScreenDefaults.Padding16))

    Column(
        modifier =
            Modifier.align(Alignment.BottomStart).padding(HuntCardScreenDefaults.Padding20)) {
          Text(
              text = hunt.title,
              fontSize = HuntCardScreenDefaults.TitleFontSize,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimary,
              lineHeight = HuntCardScreenDefaults.LineHeight,
              modifier = Modifier.testTag(HuntCardScreenTestTags.TITLE_TEXT))

          Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding8))

          Text(
              text = "${HuntCardScreenStrings.By} $authorName",
              fontSize = HuntCardScreenDefaults.AuthorFontSize,
              color =
                  MaterialTheme.colorScheme.onPrimary.copy(alpha = HuntCardScreenDefaults.Alpha),
              fontWeight = FontWeight.Medium,
              modifier =
                  Modifier.clickable { goProfile(hunt.authorId) }
                      .testTag(HuntCardScreenTestTags.AUTHOR_TEXT))
        }
  }
}

/**
 * Shows two horizontal stat cards for:
 * - distance,
 * - duration.
 *
 * Uses ModernStatCard internally for consistent styling.
 *
 * @param hunt The hunt containing the stats to display.
 */
@Composable
fun ModernStatsSection(hunt: Hunt) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(
                  horizontal = HuntCardScreenDefaults.Padding20,
                  vertical = HuntCardScreenDefaults.Padding20),
      horizontalArrangement = Arrangement.SpaceEvenly) {
        ModernStatCard(
            label = HuntCardScreenStrings.DistanceLabel,
            value = "${hunt.distance}",
            unit = HuntCardScreenStrings.DistanceUnit,
            modifier = Modifier.weight(HuntCardScreenDefaults.CardWeight))

        Spacer(modifier = Modifier.width(HuntCardScreenDefaults.Padding12))

        ModernStatCard(
            label = HuntCardScreenStrings.DurationLabel,
            value = "${hunt.time}",
            unit = HuntCardScreenStrings.HourUnit,
            modifier = Modifier.weight(HuntCardScreenDefaults.CardWeight))
      }
}

/**
 * Displays a single stat inside a rounded card, with:
 * - a label (e.g., "Distance"),
 * - a bold value,
 * - a smaller unit (e.g., "km").
 *
 * Used for distance and duration in the stats section.
 *
 * @param label The name of the statistic.
 * @param value The numeric value of the stat.
 * @param unit The unit displayed after the value.
 * @param modifier Optional layout modifier.
 */
@Composable
fun ModernStatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
  Card(
      modifier = modifier,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
      elevation =
          CardDefaults.cardElevation(defaultElevation = HuntCardScreenDefaults.CardElevation),
      shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(HuntCardScreenDefaults.Padding16),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                  text = label,
                  fontSize = HuntCardScreenDefaults.SmallFontSize,
                  color = MaterialTheme.colorScheme.onSecondary,
                  fontWeight = FontWeight.Medium)

              Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding4))

              Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = HuntCardScreenDefaults.MediumFontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = " $unit",
                    fontSize = HuntCardScreenDefaults.AuthorFontSize,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(bottom = HuntCardScreenDefaults.Padding2))
              }
            }
      }
}

/**
 * Displays the hunt description in a rounded card with a section header.
 *
 * Includes padding, title, and styled paragraph text.
 *
 * @param description The full text description of the hunt.
 */
@Composable
fun ModernDescriptionSection(description: String) {
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(
                  horizontal = HuntCardScreenDefaults.Padding20,
                  vertical = HuntCardScreenDefaults.Padding12),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
      elevation =
          CardDefaults.cardElevation(defaultElevation = HuntCardScreenDefaults.CardElevation),
      shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)) {
        Column(modifier = Modifier.padding(HuntCardScreenDefaults.Padding20)) {
          Text(
              text = HuntCardScreenStrings.DescriptionLabel,
              fontSize = HuntCardScreenDefaults.SmallFontSize,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface)

          Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding12))

          Text(
              text = description,
              fontSize = HuntCardScreenDefaults.DescriptionFontSize,
              lineHeight = HuntCardScreenDefaults.DescriptionLineHeight,
              color = HuntCardScreenDefaults.ParagraphGray,
              modifier = Modifier.testTag(HuntCardScreenTestTags.DESCRIPTION_TEXT))
        }
      }
}

/**
 * Displays a Google Maps preview showing the hunt’s starting point. Includes:
 * - a section title,
 * - a styled Card container,
 * - a map with an anchored marker.
 *
 * @param hunt The hunt containing the starting coordinates and name.
 */
@Composable
fun ModernMapSection(hunt: Hunt) {
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(
                  horizontal = HuntCardScreenDefaults.Padding20,
                  vertical = HuntCardScreenDefaults.Padding12),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
      elevation =
          CardDefaults.cardElevation(defaultElevation = HuntCardScreenDefaults.CardElevation),
      shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)) {
        Column(modifier = Modifier.padding(HuntCardScreenDefaults.Padding20)) {
          Text(
              text = HuntCardScreenStrings.StartingPointLabel,
              fontSize = HuntCardScreenDefaults.SmallFontSize,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface)

          Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding12))

          val startPosition = LatLng(hunt.start.latitude, hunt.start.longitude)
          val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(startPosition, HuntCardScreenDefaults.Zoom)
          }

          Box(
              modifier =
                  Modifier.fillMaxWidth()
                      .height(HuntCardScreenDefaults.MapHeight250)
                      .clip(RoundedCornerShape(HuntCardScreenDefaults.CornerRadius))
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

/**
 * Displays a single action button aligned on the right.
 *
 * The button’s:
 * - label,
 * - icon,
 * - color are determined by the parent depending on whether the user is the hunt’s author.
 *
 * It can either show:
 * - "Edit Hunt",
 * - "Add Review".
 *
 * @param isCurrentId True if the current user is the author of the hunt.
 * @param buttonIcon The icon displayed inside the button.
 * @param onActionClick Callback triggered when the button is clicked.
 */
@Composable
fun ModernActionButtons(
    isCurrentId: Boolean,
    buttonIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onActionClick: () -> Unit
) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(
                  horizontal = HuntCardScreenDefaults.Padding20,
                  vertical = HuntCardScreenDefaults.Padding12),
      horizontalArrangement = Arrangement.End) {
        Button(
            onClick = onActionClick,
            modifier = Modifier.testTag(HuntCardScreenTestTags.REVIEW_BUTTON),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        if (isCurrentId) MaterialTheme.colorScheme.secondary
                        else HuntCardScreenDefaults.OrangeButton),
            shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)) {
              Icon(
                  imageVector = buttonIcon,
                  contentDescription = null,
                  modifier = Modifier.size(HuntCardScreenDefaults.IconSize18))

              Spacer(modifier = Modifier.width(HuntCardScreenDefaults.Padding8))

              Text(
                  text =
                      if (isCurrentId) HuntCardScreenStrings.EditHunt
                      else HuntCardScreenStrings.AddReview,
                  fontSize = HuntCardScreenDefaults.DescriptionFontSize,
                  fontWeight = FontWeight.SemiBold)
            }
      }
}

/**
 * Displays the placeholder UI shown when a hunt has no reviews.
 *
 * Consists of:
 * - a star icon,
 * - a message ("No reviews yet").
 *
 * Used inside the reviews list section.
 */
@Composable
fun ModernEmptyReviewsState() {
  Box(
      modifier = Modifier.fillMaxWidth().padding(vertical = HuntCardScreenDefaults.Padding40),
      contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
              imageVector = Icons.Filled.Star,
              contentDescription = null,
              modifier = Modifier.size(HuntCardScreenDefaults.IconSize48),
              tint = HuntCardScreenDefaults.LightGray)

          Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding12))

          Text(
              text = HuntCardScreenStrings.NoReviews,
              modifier = Modifier.testTag(HuntCardScreenTestTags.NO_REVIEWS_TEXT))
        }
      }
}

/**
 * Displays a single review inside a card, including:
 * - reviewer's profile picture and name,
 * - rating stars,
 * - review comment,
 * - optional "See Pictures" button,
 * - delete button if the review belongs to the current user.
 *
 * Automatically loads the review author's profile via the ReviewHuntViewModel.
 *
 * @param review The review data to display.
 * @param reviewHuntViewModel ViewModel managing review-specific state.
 * @param currentUserId The ID of the logged-in user.
 * @param navController Navigation controller used for opening review images.
 * @param onDeleteReview Called when the user deletes their own review.
 */
@Composable
fun ModernReviewCard(
    review: HuntReview,
    reviewHuntViewModel: ReviewHuntViewModel,
    currentUserId: String?,
    navController: NavHostController,
    onDeleteReview: (String) -> Unit,
) {
  val uiState by reviewHuntViewModel.uiState.collectAsState()

  val authorId = review.authorId
  LaunchedEffect(authorId) { reviewHuntViewModel.loadAuthorProfile(authorId) }
  val authorProfile = uiState.authorProfiles[authorId]

  val isCurrentId = currentUserId == authorId

  // Replies ViewModel for this specific review
  val repliesViewModel: ReviewRepliesViewModel =
      viewModel(
          key = "${HuntCardScreenStrings.RepliesViewModelKeyPrefix}${review.reviewId}",
          factory =
              ReviewRepliesViewModelFactory(
                  reviewId = review.reviewId,
                  repository = HuntReviewReplyRepositoryProvider.repository,
              ),
      )

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(
                  horizontal = HuntCardScreenDefaults.Padding20,
                  vertical = HuntCardScreenDefaults.Padding8)
              .clickable {
                // You can leave this clickable for some future behavior or remove it if not needed
              }
              .testTag(HuntCardScreenTestTags.REVIEW_CARD),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation =
          CardDefaults.cardElevation(defaultElevation = HuntCardScreenDefaults.ZeroElevation),
      shape = RoundedCornerShape(HuntCardScreenDefaults.ReviewCardCornerRadius)) {
        Column(modifier = Modifier.padding(HuntCardScreenDefaults.Padding16)) {

          // --- Header: avatar + name + rating + optional delete ---
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            val profilePictureRes =
                authorProfile?.author?.profilePicture
                    ?: HuntCardScreenDefaults.NoProfilePictureResId

            if (profilePictureRes != HuntCardScreenDefaults.NoProfilePictureResId) {
              ProfilePicture(
                  profilePictureRes = profilePictureRes,
                  modifier =
                      Modifier.size(HuntCardScreenDefaults.ProfilePictureSize).clip(CircleShape))
            } else {
              Box(
                  modifier =
                      Modifier.size(HuntCardScreenDefaults.ProfilePictureSize)
                          .clip(CircleShape)
                          .background(
                              if (isCurrentId) MaterialTheme.colorScheme.primary
                              else MaterialTheme.colorScheme.tertiary),
                  contentAlignment = Alignment.Center) {
                    val initial =
                        authorId.take(HuntCardScreenDefaults.InitialLetterCount).uppercase()
                    Text(
                        text =
                            if (isCurrentId) HuntCardScreenStrings.CurrentUserInitialLabel
                            else initial,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White)
                  }
            }

            Spacer(modifier = Modifier.width(HuntCardScreenDefaults.Padding12))

            Column(modifier = Modifier.weight(HuntCardScreenDefaults.CardWeight)) {
              val authorName =
                  authorProfile?.author?.pseudonym ?: HuntCardScreenStrings.UnknownAuthor

              Text(
                  text = authorName,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = HuntCardScreenDefaults.DescriptionFontSize,
                  color = MaterialTheme.colorScheme.onSurface)

              Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding4))

              Rating(review.rating, RatingType.STAR)
            }

            if (isCurrentId) {
              IconButton(
                  onClick = { onDeleteReview(review.reviewId) },
                  modifier = Modifier.testTag(HuntCardScreenTestTags.DELETE_REVIEW_BUTTON)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = HuntCardScreenStrings.ReviewDeleteButton,
                        tint =
                            MaterialTheme.colorScheme.error.copy(
                                alpha = HuntCardScreenDefaults.DeleteIconAlpha))
                  }
            }
          }

          Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding8))

          // --- Comment text ---
          if (review.comment.isNotBlank()) {
            Text(
                text = review.comment,
                fontSize = HuntCardScreenDefaults.DescriptionFontSize,
                lineHeight = HuntCardScreenDefaults.OtherLineHeight,
                color = MaterialTheme.colorScheme.onSurface)
          }

          // --- Optional photos button ---
          if (review.photos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding8))

            Button(
                onClick = {
                  reviewHuntViewModel.loadReviewImages(review.photos)
                  navController.navigate(
                      "${HuntCardScreenStrings.ReviewImagesRoutePrefix}${review.reviewId}")
                },
                modifier = Modifier.testTag(HuntCardScreenTestTags.SEE_PICTURES_BUTTON),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = HuntCardScreenDefaults.CardSoftGray,
                        contentColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(HuntCardScreenDefaults.Padding8)) {
                  Text(
                      "${HuntCardScreenStrings.SeePictures} (${review.photos.size})",
                      fontSize = HuntCardScreenDefaults.MinFontSize)
                }
          }

          Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Spacing6))

          ReviewRepliesSection(
              viewModel = repliesViewModel,
              modifier = Modifier.padding(top = HuntCardScreenDefaults.Spacing6),
          )
        }
      }
}

/**
 * Displays a circular like/unlike button inside a translucent background.
 *
 * The button displays:
 * - red heart when liked,
 * - gray heart when unliked.
 *
 * Toggling is handled by the HuntCardViewModel.
 *
 * @param huntCardViewModel ViewModel handling like interactions.
 * @param huntId The ID of the hunt being liked.
 * @param modifier Optional modifier for styling and positioning.
 */
@Composable
fun LikeButton(
    huntCardViewModel: HuntCardViewModel,
    huntId: String,
    modifier: Modifier = Modifier
) {

  val likedHuntsCache by huntCardViewModel.likedHuntsCache.collectAsState()
  val isLiked = likedHuntsCache.contains(huntId)

  IconButton(
      onClick = { huntCardViewModel.onLikeClick(huntId) },
      modifier = modifier.testTag(HuntCardScreenTestTags.LIKE_BUTTON)) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = HuntCardScreenStrings.LikeButton,
            tint =
                if (isLiked) HuntCardScreenDefaults.LikeRedStrong
                else HuntCardScreenDefaults.LightGray,
            modifier = Modifier.size(HuntCardScreenDefaults.IconSize24))
      }
}
