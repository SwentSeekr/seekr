package com.swentseekr.seekr.model.hunt

interface HuntReviewRepository {
  fun getNewUid(): String

  suspend fun getReviewHunt(reviewId: String): HuntReview?

  suspend fun addReviewHunt(review: HuntReview)

  suspend fun updateReviewHunt(reviewId: String, newReview: HuntReview)

  suspend fun deleteReviewHunt(reviewId: String)

  suspend fun getHuntReviews(huntId: String): List<HuntReview>
}
