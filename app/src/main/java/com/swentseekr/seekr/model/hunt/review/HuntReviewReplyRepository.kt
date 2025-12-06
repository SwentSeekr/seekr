package com.swentseekr.seekr.model.hunt.review

import kotlinx.coroutines.flow.Flow

/** Abstraction for loading/storing threaded replies to a HuntReview. */
interface HuntReviewReplyRepository {

  /** Generates a new unique reply ID. */
  fun getNewReplyId(): String

  /** Returns all replies for a given review, flat list; ViewModel will build the tree. */
  suspend fun getRepliesForReview(reviewId: String): List<HuntReviewReply>

  /** Persists a new reply. */
  suspend fun addReply(reply: HuntReviewReply)

  /** Overwrites an existing reply with new data (for edits). */
  suspend fun updateReply(replyId: String, newReply: HuntReviewReply)

  /** Deletes a reply (hard delete). You can layer soft-delete in ViewModel/model if needed. */
  suspend fun deleteReply(replyId: String)

  /**
   * Optional: live updates for a review's replies. Emits a new list whenever Firestore snapshot
   * changes.
   */
  fun listenToReplies(reviewId: String): Flow<List<HuntReviewReply>>
}
