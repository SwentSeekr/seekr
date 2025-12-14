package com.swentseekr.seekr.model.hunt

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

const val HUNT_REVIEW_COLLECTION_PATH = "huntsReviews"

/**
 * Firestore implementation of [HuntReviewRepository].
 *
 * Stores and retrieves [HuntReview] objects in Firestore.
 *
 * @property db The Firestore instance used to store and retrieve hunt reviews.
 */
class HuntReviewRepositoryFirestore(private val db: FirebaseFirestore) : HuntReviewRepository {
  override fun getNewUid(): String {
    return db.collection(HUNT_REVIEW_COLLECTION_PATH).document().id
  }

  /**
   * Retrieves a single review by its unique ID.
   *
   * @param reviewId The ID of the review to retrieve.
   * @return The [HuntReview] associated with the given ID.
   * @throws IllegalArgumentException if no review exists with the given ID.
   */
  override suspend fun getReviewHunt(reviewId: String): HuntReview {
    val document = db.collection(HUNT_REVIEW_COLLECTION_PATH).document(reviewId).get().await()
    return documentToHuntReview(document)
        ?: throw IllegalArgumentException("Hunt with Id ${reviewId} is not found")
  }

  /**
   * Adds a new review to Firestore.
   *
   * Preconditions:
   * - The review must already have a non-blank `reviewId`.
   *
   * @param review The review to add.
   * @throws IllegalArgumentException if the review ID is blank.
   */
  override suspend fun addReviewHunt(review: HuntReview) {
    require(review.reviewId.isNotBlank()) { "Review ID cannot be blank." }
    db.collection(HUNT_REVIEW_COLLECTION_PATH).document(review.reviewId).set(review).await()
  }

  /**
   * Updates an existing review in Firestore.
   *
   * @param reviewId The ID of the review to update.
   * @param newReview The updated review data.
   */
  override suspend fun updateReviewHunt(reviewId: String, newReview: HuntReview) {
    db.collection(HUNT_REVIEW_COLLECTION_PATH).document(reviewId).set(newReview).await()
  }

  /**
   * Deletes a review from Firestore.
   *
   * @param reviewId The ID of the review to delete.
   */
  override suspend fun deleteReviewHunt(reviewId: String) {
    db.collection(HUNT_REVIEW_COLLECTION_PATH).document(reviewId).delete().await()
  }

  /**
   * Retrieves all reviews associated with a specific hunt.
   *
   * Preconditions:
   * - A user must be logged in.
   *
   * @param huntId The ID of the hunt whose reviews should be retrieved.
   * @return A list of [HuntReview] objects linked to the hunt.
   * @throws IllegalStateException if no user is currently logged in.
   */
  override suspend fun getHuntReviews(huntId: String): List<HuntReview> {
    FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("User not logged in")
    val snapshot =
        db.collection(HUNT_REVIEW_COLLECTION_PATH).whereEqualTo("huntId", huntId).get().await()
    return snapshot.mapNotNull { documentToHuntReview(it) }
  }

  /**
   * Converts a Firestore [DocumentSnapshot] into a [HuntReview].
   *
   * Returns `null` if required fields are missing or if an error occurs during conversion.
   *
   * @param document The Firestore document snapshot.
   * @return A [HuntReview] instance, or `null` if conversion fails.
   */
  private fun documentToHuntReview(document: DocumentSnapshot): HuntReview? {
    return try {
      val reviewID = document.id
      val authorID = document.getString("authorId") ?: return null
      val huntID = document.getString("huntId") ?: return null
      val rating = document.getDouble("rating") ?: return null
      val comment = document.getString("comment") ?: return null
      val photos = document.get("photos") as? List<String> ?: emptyList()

      HuntReview(
          reviewId = reviewID,
          authorId = authorID,
          huntId = huntID,
          rating = rating,
          comment = comment,
          photos = photos)
    } catch (e: Exception) {
      Log.e("HuntReviewRepositoryFirestore", "Error converting document to HuntReview", e)
      null
    }
  }
}
