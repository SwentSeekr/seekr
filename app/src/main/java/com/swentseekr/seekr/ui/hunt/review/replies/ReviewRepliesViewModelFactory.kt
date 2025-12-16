package com.swentseekr.seekr.ui.hunt.review.replies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swentseekr.seekr.model.hunt.review.HuntReviewReplyRepository

/**
 * Simple [ViewModelProvider.Factory] for creating [ReviewRepliesViewModel] instances with a
 * specific [reviewId].
 *
 * This allows each review card in the list to obtain its own scoped [ReviewRepliesViewModel].
 */
class ReviewRepliesViewModelFactory(
    private val reviewId: String,
    private val repository: HuntReviewReplyRepository,
) : ViewModelProvider.Factory {

  /**
   * Creates a new [ReviewRepliesViewModel] for the given [modelClass], preconfigured with
   * [reviewId] and [repository].
   *
   * @param T The type of [ViewModel] being requested.
   * @param modelClass The [Class] object of the [ViewModel] type.
   * @return A new instance of [ReviewRepliesViewModel].
   * @throws IllegalArgumentException if [modelClass] is not assignable from
   *   [ReviewRepliesViewModel].
   */
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(ReviewRepliesViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return ReviewRepliesViewModel(
          reviewId = reviewId,
          replyRepository = repository,
      )
          as T
    }

    throw IllegalArgumentException(
        ReviewRepliesStrings.ERROR_UNKNOWN_VIEW_MODEL_CLASS_PREFIX + modelClass.name)
  }
}
