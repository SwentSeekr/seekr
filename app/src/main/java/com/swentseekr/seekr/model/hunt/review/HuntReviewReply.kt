package com.swentseekr.seekr.model.hunt.review

/**
 * Single reply inside a review thread.
 *
 * @property replyId unique ID of this reply
 * @property reviewId ID of the root review this reply belongs to
 * @property parentReplyId ID of the parent reply, or null if replying directly to the review
 * @property authorId ID of the user who wrote this reply
 * @property comment reply text
 * @property createdAt creation timestamp (e.g. epoch millis)
 * @property updatedAt last update timestamp, if edited
 * @property isDeleted whether this reply has been soft-deleted
 */
data class HuntReviewReply(
    val replyId: String = "",
    val reviewId: String = "",
    val parentReplyId: String? = null,
    val authorId: String = "",
    val comment: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long? = null,
    val isDeleted: Boolean = false,
)
