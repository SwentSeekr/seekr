package com.swentseekr.seekr.model.hunt

/**
 * Repository interface for managing [HuntReview] entities.
 *
 * Defines CRUD operations for hunt reviews and allows retrieval of reviews by hunt or review ID.
 */
interface HuntReviewRepository {

  /**
   * Generates a new unique identifier for a review.
   *
   * @return A new unique [String] ID suitable for use as [HuntReview.reviewId].
   */
  fun getNewUid(): String

  /**
   * Retrieves a single review by its unique ID.
   *
   * @param reviewId The unique identifier of the review.
   * @return The [HuntReview] associated with the given ID.
   * @throws IllegalArgumentException if no review exists with the given ID.
   */
  suspend fun getReviewHunt(reviewId: String): HuntReview

  /**
   * Adds a new review to the repository.
   *
   * @param review The [HuntReview] to add.
   * @throws IllegalArgumentException if the `reviewId` is blank.
   */
  suspend fun addReviewHunt(review: HuntReview)

  /**
   * Updates an existing review with new data.
   *
   * @param reviewId The ID of the review to update.
   * @param newReview The updated [HuntReview] data.
   * @throws IllegalArgumentException if no review exists with the given ID.
   */
  suspend fun updateReviewHunt(reviewId: String, newReview: HuntReview)

  /**
   * Deletes a review by its unique ID.
   *
   * @param reviewId The ID of the review to delete.
   * @throws IllegalArgumentException if no review exists with the given ID.
   */
  suspend fun deleteReviewHunt(reviewId: String)

  /**
   * Retrieves all reviews associated with a specific hunt.
   *
   * @param huntId The ID of the hunt whose reviews should be retrieved.
   * @return A list of [HuntReview] objects for the given hunt.
   * @throws IllegalStateException if the user is not logged in (Firestore implementation).
   */
  suspend fun getHuntReviews(huntId: String): List<HuntReview>
}
