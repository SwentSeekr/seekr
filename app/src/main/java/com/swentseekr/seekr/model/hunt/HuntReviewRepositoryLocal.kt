package com.swentseekr.seekr.model.hunt

open class HuntReviewRepositoryLocal : HuntReviewRepository {
  private val reviews = mutableListOf<HuntReview>()
  private var id = 0

  override fun getNewUid(): String {
    return (id++).toString()
  }

  override suspend fun getReviewHunt(reviewId: String): HuntReview {
    for (i in reviews.indices) {
      if (reviews[i].reviewId == reviewId) {
        return reviews[i]
      }
    }
    throw IllegalArgumentException("Hunt with ID $reviewId is not found")
  }

  override suspend fun addReviewHunt(review: HuntReview) {
    reviews.add(review)
  }

  override suspend fun updateReviewHunt(reviewId: String, newReview: HuntReview) {
    for (i in reviews.indices) {
      if (reviews[i].reviewId == reviewId) {
        reviews[i] = newReview
        return
      }
    }
    throw IllegalArgumentException("Review with ID $reviewId is not found")
  }

  override suspend fun deleteReviewHunt(reviewId: String) {
    val wasRemoved = reviews.removeIf { it.reviewId == reviewId }
    if (!wasRemoved) {
      throw IllegalArgumentException("Review with ID $reviewId is not found")
    }
  }

  override suspend fun getHuntReviews(huntId: String): List<HuntReview> {
    return reviews.filter { it.huntId == huntId }
  }
}
