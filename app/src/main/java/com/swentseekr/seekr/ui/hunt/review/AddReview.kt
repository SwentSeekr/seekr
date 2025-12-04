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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel

val UICons = AddReviewScreenDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    huntId: String,
    reviewViewModel: ReviewHuntViewModel = viewModel(),
    huntCardViewModel: HuntCardViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onDone: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
  ReviewScreenContent(
      title = AddReviewScreenStrings.Title,
      huntId = huntId,
      onGoBack = onGoBack,
      onDone = onDone,
      onCancel = onCancel,
      reviewViewModel = reviewViewModel,
      huntCardViewModel = huntCardViewModel)
}

@Composable
fun StarRatingBar(
    maxStars: Int = AddReviewScreenDefaults.MaxStars,
    rating: Int = 0,
    onRatingChanged: (Int) -> Unit
) {
  val starCount = if (maxStars > 0) maxStars else AddReviewScreenDefaults.MaxStars
  Row(
      modifier = Modifier.testTag(AddReviewScreenTestTags.RATING_BAR),
      horizontalArrangement = Arrangement.spacedBy(UICons.RowStarArrangement)) {
        for (i in 1..starCount) {
          val scale by
              animateFloatAsState(
                  targetValue = if (i <= rating) 1.1f else 1f,
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
                          onRatingChanged(i - 1)
                        } else {
                          onRatingChanged(i)
                        }
                      }
                      .testTag(AddReviewScreenTestTags.starTag(i)))
        }
      }
}

@Preview
@Composable
fun AddReviewScreenPreview() {
  AddReviewScreen("hunt123")
}
