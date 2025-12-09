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
    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
  }
}
