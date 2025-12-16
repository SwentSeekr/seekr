package com.swentseekr.seekr.model.hunt

/**
 * Represents a review left by a user for a specific hunt.
 *
 * @property reviewId Unique identifier of the review.
 * @property authorId User ID of the author who submitted the review.
 * @property huntId ID of the hunt being reviewed.
 * @property rating Numerical rating given to the hunt (e.g., 0.0–5.0 scale).
 * @property comment Optional textual feedback provided by the user.
 * @property photos Optional list of URLs pointing to photos attached to the review.
 */
data class HuntReview(
    val reviewId: String = "",
    val authorId: String = "",
    val huntId: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val photos: List<String> = emptyList()
)
