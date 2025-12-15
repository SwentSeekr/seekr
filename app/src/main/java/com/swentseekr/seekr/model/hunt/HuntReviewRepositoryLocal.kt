package com.swentseekr.seekr.model.hunt

/**
 * In-memory implementation of [HuntReviewRepository] for testing or local development.
 *
 * Stores hunt reviews in a mutable list and generates incremental IDs for new reviews. Does not
 * persist data beyond the app runtime.
 */
open class HuntReviewRepositoryLocal : HuntReviewRepository {
  private val reviews = mutableListOf<HuntReview>()
  private var id = 0

  /** Generates a new unique review ID as a string. */
  override fun getNewUid(): String {
    return (id++).toString()
  }

  /** Retrieves a review by its ID or throws [IllegalArgumentException] if not found. */
  override suspend fun getReviewHunt(reviewId: String): HuntReview {
    for (i in reviews.indices) {
      if (reviews[i].reviewId == reviewId) {
        return reviews[i]
      }
    }
    throw IllegalArgumentException(
        "${HuntReviewRepositoryLocalConstantsString.HUNT_START} $reviewId ${HuntReviewRepositoryLocalConstantsString.NOT_FOUND}")
  }

  /** Adds a new review to the in-memory list. */
  override suspend fun addReviewHunt(review: HuntReview) {
    reviews.add(review)
  }

  /** Updates an existing review by ID or throws [IllegalArgumentException] if not found. */
  override suspend fun updateReviewHunt(reviewId: String, newReview: HuntReview) {
    for (i in reviews.indices) {
      if (reviews[i].reviewId == reviewId) {
        reviews[i] = newReview
        return
      }
    }
    throw IllegalArgumentException(
        "${HuntReviewRepositoryLocalConstantsString.REVIEW_START} $reviewId ${HuntReviewRepositoryLocalConstantsString.NOT_FOUND}")
  }

  /** Deletes a review by ID or throws [IllegalArgumentException] if not found. */
  override suspend fun deleteReviewHunt(reviewId: String) {
    val wasRemoved = reviews.removeIf { it.reviewId == reviewId }
    if (!wasRemoved) {
      throw IllegalArgumentException(
          "${HuntReviewRepositoryLocalConstantsString.REVIEW_START} $reviewId ${HuntReviewRepositoryLocalConstantsString.NOT_FOUND}")
    }
  }

  /** Retrieves all reviews associated with the given hunt ID. */
  override suspend fun getHuntReviews(huntId: String): List<HuntReview> {
    return reviews.filter { it.huntId == huntId }
  }
}
