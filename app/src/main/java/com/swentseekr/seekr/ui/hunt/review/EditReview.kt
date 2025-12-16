package com.swentseekr.seekr.ui.hunt.review

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel

/**
 * This composable displays the UI for editing an existing hunt review. It reuses the
 * [ReviewScreenContent] composable to handle:
 * - Display of the current review information.
 * - Rating adjustment.
 * - Comment editing.
 * - Photo management.
 *
 * The screen also provides callbacks for navigation and completion actions.
 *
 * @param huntId The unique identifier of the hunt associated with the review.
 * @param reviewId The unique identifier of the review being edited.
 * @param huntCardViewModel The [HuntCardViewModel] instance used to fetch and update review data.
 *   Defaults to the scoped ViewModel obtained via [viewModel()].
 * @param onGoBack Lambda to be invoked when the user navigates back without saving changes.
 * @param onDone Lambda to be invoked when the user successfully finishes editing the review.
 * @param onCancel Lambda to be invoked when the user cancels editing the review.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReviewScreen(
    huntId: String,
    reviewId: String,
    huntCardViewModel: HuntCardViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onDone: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
  val vm: ReviewHuntViewModel = viewModel(key = "${AddReviewScreenStrings.EDIT_KEY}$reviewId")
  ReviewScreenContent(
      title = AddReviewScreenStrings.EDIT_TITLE,
      huntId = huntId,
      reviewId = reviewId,
      onGoBack = onGoBack,
      onDone = onDone,
      onCancel = onCancel,
      reviewViewModel = vm,
      huntCardViewModel = huntCardViewModel)
}
