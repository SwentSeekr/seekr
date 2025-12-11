package com.swentseekr.seekr.model.hunt.review

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * In memory implementation of [HuntReviewReplyRepository].
 *
 * This repository keeps replies in a simple mutable map and generates monotonically increasing
 * reply identifiers using a local counter. It is primarily intended for tests and local usage.
 */
class HuntReviewReplyRepositoryLocal : HuntReviewReplyRepository {

  /** Backing store for all replies, indexed by their reply identifier. */
  private val replies = mutableMapOf<String, HuntReviewReply>()

  /** Monotonically increasing counter used to generate new reply identifiers. */
  private var idCounter = INITIAL_REPLY_ID_COUNTER

  /**
   * Generates a new unique reply identifier.
   *
   * The identifier is created by incrementing an internal counter and returning its value as a
   * string.
   *
   * @return a new unique reply identifier.
   */
  override fun getNewReplyId(): String {
    idCounter += REPLY_ID_INCREMENT
    return idCounter.toString()
  }

  /**
   * Returns all replies associated with the given review.
   *
   * @param reviewId identifier of the review whose replies are requested.
   * @return a list of replies that belong to the specified review.
   */
  override suspend fun getRepliesForReview(reviewId: String): List<HuntReviewReply> {
    return replies.values.filter { it.reviewId == reviewId }
  }

  /**
   * Adds a new reply to the repository.
   *
   * The reply must already have a non blank identifier.
   *
   * @param reply the reply to add.
   * @throws IllegalArgumentException if [reply.replyId] is blank.
   */
  override suspend fun addReply(reply: HuntReviewReply) {
    require(reply.replyId.isNotBlank()) { ERROR_REPLY_ID_BLANK }
    replies[reply.replyId] = reply
  }

  /**
   * Replaces an existing reply with a new instance.
   *
   * The stored reply keeps the provided [replyId] even if [newReply] contains a different
   * identifier.
   *
   * @param replyId identifier of the reply to update.
   * @param newReply new reply content that should replace the existing one.
   * @throws IllegalArgumentException if no reply with the given [replyId] exists.
   */
  override suspend fun updateReply(replyId: String, newReply: HuntReviewReply) {
    require(replies.containsKey(replyId)) { ERROR_REPLY_NOT_FOUND_TEMPLATE.format(replyId) }
    replies[replyId] = newReply.copy(replyId = replyId)
  }

  /**
   * Deletes the reply with the given identifier.
   *
   * @param replyId identifier of the reply to delete.
   * @throws IllegalArgumentException if no reply with the given [replyId] exists.
   */
  override suspend fun deleteReply(replyId: String) {
    require(replies.containsKey(replyId)) { ERROR_REPLY_NOT_FOUND_TEMPLATE.format(replyId) }
    replies.remove(replyId)
  }

  /**
   * Returns a [Flow] that emits a single snapshot of the replies for the given review and then
   * completes.
   *
   * This is a minimal implementation intended for tests and simple use cases where only a one shot
   * view of the current replies is needed.
   *
   * @param reviewId identifier of the review whose replies should be emitted.
   * @return a flow that emits a single list of replies for the given review.
   */
  override fun listenToReplies(reviewId: String): Flow<List<HuntReviewReply>> = flow {
    emit(getRepliesForReview(reviewId))
  }
}
