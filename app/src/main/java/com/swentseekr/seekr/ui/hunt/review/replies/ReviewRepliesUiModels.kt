package com.swentseekr.seekr.ui.hunt.review.replies

import com.swentseekr.seekr.model.hunt.review.HuntReviewReply

/**
 * Identifies the logical target of a reply in a thread.
 * - [RootReview] means "reply to the review itself".
 * - [Reply] means "reply to a specific existing reply".
 */
sealed class ReplyTarget {

  /**
   * Target representing the root review card.
   *
   * @property reviewId ID of the review being replied to.
   */
  data class RootReview(
      val reviewId: String,
  ) : ReplyTarget()

  /**
   * Target representing a reply to another reply.
   *
   * @property reviewId ID of the review the whole thread belongs to.
   * @property parentReplyId ID of the reply this new reply will be attached to.
   */
  data class Reply(
      val reviewId: String,
      val parentReplyId: String,
  ) : ReplyTarget()
}

/**
 * UI-ready representation of a reply node inside a threaded discussion. This is what the
 * card/replies section will render.
 */
data class ReplyNodeUiState(
    val reply: HuntReviewReply,
    val depth: Int,
    val isExpanded: Boolean,
    val isComposerOpen: Boolean,
    val isMine: Boolean,
    val totalChildrenCount: Int,
)

/** UI state for the replies section of a single review card. This is scoped to one review. */
data class ReviewRepliesUiState(
    val reviewId: String,
    val isLoading: Boolean = false,
    val replies: List<ReplyNodeUiState> = emptyList(),
    val isSendingReply: Boolean = false,
    val rootReplyText: String = ReviewRepliesUiModelConstants.ROOT_BASE_REPLY_TEXT,
    val childReplyTexts: Map<String, String> = emptyMap(),
    val errorMessage: String? = null,
    val isRootExpanded: Boolean = false,
    val totalReplyCount: Int = ReviewRepliesUiModelConstants.TOTAL_REPLY_BASE_COUNT,
)
