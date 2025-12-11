package com.swentseekr.seekr.ui.hunt.review

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel

val UICons = AddReviewScreenDefaults

/**
 * This is the main composable for the "Add Review" screen where users can:
 * - Rate a hunt using stars.
 * - Add comments/review text.
 * - Upload photos.
 * - Submit, cancel, or go back.
 *
 * @param huntId The unique ID of the hunt being reviewed.
 * @param reviewViewModel The ViewModel responsible for managing review-related UI state.
 * @param huntCardViewModel The ViewModel responsible for fetching hunt and author details.
 * @param onGoBack Callback triggered when the user presses the "Go Back" button.
 * @param onDone Callback triggered when the user successfully submits the review.
 * @param onCancel Callback triggered when the user cancels the review process.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    huntId: String,
    // reviewViewModel: ReviewHuntViewModel = viewModel(),
    huntCardViewModel: HuntCardViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onDone: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
  val vm: ReviewHuntViewModel = viewModel(key = "add_$huntId")
  ReviewScreenContent(
      title = AddReviewScreenStrings.Title,
      huntId = huntId,
      reviewId = "",
      onGoBack = onGoBack,
      onDone = onDone,
      onCancel = onCancel,
      reviewViewModel = vm,
      huntCardViewModel = huntCardViewModel)
}

/**
 * A reusable composable for displaying a row of selectable stars. Supports interactive rating with
 * animations.
 *
 * @param maxStars Maximum number of stars in the rating bar. Defaults to
 *   [AddReviewScreenDefaults.MaxStars].
 * @param rating Current rating value (0 means no stars selected). Defaults to 0.
 * @param onRatingChanged Callback invoked when the user taps a star. Provides the updated rating
 *   value.
 */
@Composable
fun StarRatingBar(
    maxStars: Int = AddReviewScreenDefaults.MaxStars,
    rating: Int = AddReviewScreenDefaults.DefaultRating,
    onRatingChanged: (Int) -> Unit
) {
  val starCount =
      if (maxStars > AddReviewScreenDefaults.MinStars) maxStars
      else AddReviewScreenDefaults.MaxStars

  Row(
      modifier = Modifier.testTag(AddReviewScreenTestTags.RATING_BAR),
      horizontalArrangement = Arrangement.spacedBy(UICons.RowStarArrangement)) {
        for (i in AddReviewScreenDefaults.FirstStarIndex..starCount) {
          val scale by
              animateFloatAsState(
                  targetValue =
                      if (i <= rating) AddReviewScreenDefaults.StarSelectedScale
                      else AddReviewScreenDefaults.StarUnselectedScale,
                  animationSpec =
                      spring(
                          dampingRatio = Spring.DampingRatioMediumBouncy,
                          stiffness = Spring.StiffnessMedium))

          Icon(
              imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
              contentDescription = "${AddReviewScreenStrings.StarContentDescriptionPrefix}$i",
              tint =
                  if (i <= rating) AddReviewScreenDefaults.SelectedStarColor
                  else AddReviewScreenDefaults.UnselectedStarColor,
              modifier =
                  Modifier.size(UICons.IconBig)
                      .scale(scale)
                      .clickable {
                        if (i == rating) {
                          onRatingChanged(i - AddReviewScreenDefaults.RatingStep)
                        } else {
                          onRatingChanged(i)
                        }
                      }
                      .testTag(AddReviewScreenTestTags.starTag(i)))
        }
      }
}
