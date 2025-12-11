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
 * @param huntId The ID of the hunt being reviewed.
 * @param reviewViewModel ViewModel managing the review state. Defaults to a local instance.
 * @param huntCardViewModel ViewModel managing the hunt card state. Defaults to a local instance.
 * @param onGoBack Callback invoked when the user wants to navigate back.
 * @param onDone Callback invoked when the user saves the edited review.
 * @param onCancel Callback invoked when the user cancels editing the review.
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
  val vm: ReviewHuntViewModel = viewModel(key = "edit_$reviewId")
  ReviewScreenContent(
      title = "Edit Review Hunt",
      huntId = huntId,
      reviewId = reviewId,
      onGoBack = onGoBack,
      onDone = onDone,
      onCancel = onCancel,
      reviewViewModel = vm,
      huntCardViewModel = huntCardViewModel)
}
