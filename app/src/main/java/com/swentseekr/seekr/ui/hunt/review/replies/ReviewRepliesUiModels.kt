package com.swentseekr.seekr.ui.hunt.review.replies

import com.swentseekr.seekr.model.hunt.review.HuntReviewReply

/**
 * Identifies the logical target of a reply in a thread.
 *
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
 * UI-ready representation of a reply node inside a threaded discussion.
 * This is what the card/replies section will render.
 */
data class ReplyNodeUiState(
    /** Underlying domain model for this reply. */
    val reply: HuntReviewReply,
    /** Depth in the visual tree; 0 = direct reply to review, 1 = reply to that reply, etc. */
    val depth: Int,
    /** True if this reply's child thread is currently expanded in the UI. */
    val isExpanded: Boolean,
    /** True if this reply currently shows its inline reply composer. */
    val isComposerOpen: Boolean,
    /** True if the current user is the author of this reply (show delete button). */
    val isMine: Boolean,
    /** Number of direct + nested children under this reply (for "See X replies"). */
    val totalChildrenCount: Int,
)

/**
 * UI state for the replies section of a single review card.
 * This is scoped to one review.
 */
data class ReviewRepliesUiState(
    /** ID of the review whose thread is being represented. */
    val reviewId: String,
    /** True while replies for this review are being loaded. */
    val isLoading: Boolean = false,
    /** Flattened list of reply nodes, already ordered and with depth. */
    val replies: List<ReplyNodeUiState> = emptyList(),
    /** True while a reply is currently being sent. */
    val isSendingReply: Boolean = false,
    /** Current text in the inline composer for the root review. */
    val rootReplyText: String = "",
    /**
     * Map from parent reply ID to current composer text for that reply.
     * This drives the inline reply fields under each reply.
     */
    val childReplyTexts: Map<String, String> = emptyMap(),
    /** Optional error message to briefly show near the replies section. */
    val errorMessage: String? = null,
    val isRootExpanded: Boolean = false,
    val totalReplyCount: Int = 0,
)
