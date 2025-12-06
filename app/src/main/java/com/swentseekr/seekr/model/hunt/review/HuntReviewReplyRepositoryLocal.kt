package com.swentseekr.seekr.model.hunt.review

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** In-memory implementation of [HuntReviewReplyRepository]. */
class HuntReviewReplyRepositoryLocal : HuntReviewReplyRepository {

  private val replies = mutableMapOf<String, HuntReviewReply>()
  private var idCounter = 0L

  override fun getNewReplyId(): String {
    idCounter += 1
    return idCounter.toString()
  }

  override suspend fun getRepliesForReview(reviewId: String): List<HuntReviewReply> {
    return replies.values.filter { it.reviewId == reviewId }
  }

  override suspend fun addReply(reply: HuntReviewReply) {
    require(reply.replyId.isNotBlank()) { "Reply ID cannot be blank." }
    replies[reply.replyId] = reply
  }

  override suspend fun updateReply(replyId: String, newReply: HuntReviewReply) {
    if (!replies.containsKey(replyId)) {
      throw IllegalArgumentException("Reply with id $replyId does not exist")
    }
    replies[replyId] = newReply.copy(replyId = replyId)
  }

  override suspend fun deleteReply(replyId: String) {
    if (!replies.containsKey(replyId)) {
      throw IllegalArgumentException("Reply with id $replyId does not exist")
    }
    replies.remove(replyId)
  }

  /**
   * Simple implementation for tests: emits the current snapshot of replies for the given review
   * once.
   */
  override fun listenToReplies(reviewId: String): Flow<List<HuntReviewReply>> = flow {
    emit(getRepliesForReview(reviewId))
  }
}
