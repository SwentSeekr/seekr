package com.swentseekr.seekr.model.hunt

interface HuntReviewRepository {
  fun getNewUid(): String

  suspend fun getReviewHunt(reviewID: String): HuntReview?

  suspend fun addReviewHunt(review: HuntReview)

  suspend fun updateReviewHunt(reviewID: String, newReview: HuntReview)

  suspend fun deleteReviewHunt(reviewID: String)

  suspend fun getHuntReviews(huntID: String): List<HuntReview>
}
