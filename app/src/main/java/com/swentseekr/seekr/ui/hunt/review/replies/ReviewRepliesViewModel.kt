package com.swentseekr.seekr.ui.hunt.review.replies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.review.HuntReviewReply
import com.swentseekr.seekr.model.hunt.review.HuntReviewReplyRepository
import com.swentseekr.seekr.model.hunt.review.HuntReviewReplyRepositoryProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing replies for a *single* review card.
 *
 * Responsibilities:
 * - Load and listen to replies for the given review.
 * - Maintain a tree → flattened list of [ReplyNodeUiState].
 * - Track expansion state for "See replies" at the root and at each reply.
 * - Track inline reply text per target (root or specific reply).
 * - Send new replies and delete replies authored by the current user.
 */
class ReviewRepliesViewModel(
    private val reviewId: String,
    private val replyRepository: HuntReviewReplyRepository =
        HuntReviewReplyRepositoryProvider.repository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ReviewRepliesUiState(reviewId = reviewId))

  /** Public state consumed by the UI layer. */
  val uiState: StateFlow<ReviewRepliesUiState> = _uiState.asStateFlow()

  /** Latest raw replies snapshot coming from the repository. */
  private var allReplies: List<HuntReviewReply> = emptyList()

  /** IDs of replies whose children are currently expanded. */
  private val expandedReplyIds: MutableSet<String> = mutableSetOf()

  /** IDs of replies whose inline composer is currently open. */
  private val composerOpenReplyIds: MutableSet<String> = mutableSetOf()

  /** Prevents starting multiple collectors for the same ViewModel. */
  private var hasStarted: Boolean = false

  /**
   * Starts loading replies and listening for updates for this review. Should typically be called
   * once from the composable using this ViewModel.
   */
  fun start() {
    if (hasStarted) return
    hasStarted = true

    viewModelScope.launch(dispatcher) {
      _uiState.update { it.copy(isLoading = true) }

      replyRepository.listenToReplies(reviewId).collect { raw ->
        allReplies = raw
        _uiState.update { it.copy(isLoading = false, errorMessage = null) }
        rebuildFromRawReplies(raw)
      }
    }
  }

  /** Explicitly refreshes the replies snapshot (e.g. pull-to-refresh). */
  fun refresh() {
    viewModelScope.launch(dispatcher) {
      _uiState.update { it.copy(isLoading = true) }
      try {
        val raw = replyRepository.getRepliesForReview(reviewId)
        allReplies = raw
        rebuildFromRawReplies(raw)
        _uiState.update { it.copy(isLoading = false, errorMessage = null) }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
              isLoading = false,
              errorMessage = e.message ?: "Failed to refresh replies",
          )
        }
      }
    }
  }

  /**
   * Updates the text of the inline composer bound to [target].
   *
   * For [ReplyTarget.RootReview] this drives the composer at the bottom of the review card. For
   * [ReplyTarget.Reply] this drives the composer shown under a specific reply.
   */
  fun onReplyTextChanged(target: ReplyTarget, newText: String) {
    _uiState.update { state ->
      when (target) {
        is ReplyTarget.RootReview -> state.copy(rootReplyText = newText)
        is ReplyTarget.Reply -> {
          val updated = state.childReplyTexts.toMutableMap()
          updated[target.parentReplyId] = newText
          state.copy(childReplyTexts = updated)
        }
      }
    }
  }

  /**
   * Toggles the expanded/collapsed state of children under the given reply.
   *
   * If [parentReplyId] is null, this toggles the root review's "See replies" section.
   */
  fun onToggleReplies(parentReplyId: String?) {
    if (parentReplyId == null) {
      _uiState.update { it.copy(isRootExpanded = !it.isRootExpanded) }
    } else {
      if (!expandedReplyIds.add(parentReplyId)) {
        expandedReplyIds.remove(parentReplyId)
      }
    }
    rebuildFromRawReplies(allReplies)
  }

  /**
   * Toggles whether the inline reply composer is visible for the given [target]. Typically called
   * when the user taps "Reply" on the review or a reply.
   *
   * Root review: we assume the root composer is always visible, so this is a no-op.
   */
  fun onToggleComposer(target: ReplyTarget) {
    when (target) {
      is ReplyTarget.RootReview -> {
        // Root composer is always visible in current design, ignore.
      }
      is ReplyTarget.Reply -> {
        val id = target.parentReplyId
        if (!composerOpenReplyIds.add(id)) {
          composerOpenReplyIds.remove(id)
        }
        rebuildFromRawReplies(allReplies)
      }
    }
  }

  /**
   * Validates and sends the reply text currently associated with [target].
   * - Builds a [HuntReviewReply] with appropriate parentReplyId.
   * - Uses the current user as [HuntReviewReply.authorId].
   * - Clears the inline composer text on success.
   */
  fun sendReply(target: ReplyTarget) {
    val currentUserId = currentUserIdOrNull()
    if (currentUserId == null) {
      _uiState.update { it.copy(errorMessage = "You must be signed in to reply.") }
      return
    }

    val state = _uiState.value
    val text =
        when (target) {
          is ReplyTarget.RootReview -> state.rootReplyText.trim()
          is ReplyTarget.Reply -> state.childReplyTexts[target.parentReplyId]?.trim().orEmpty()
        }

    if (text.isBlank()) {
      _uiState.update { it.copy(errorMessage = "Reply cannot be empty.") }
      return
    }

    val parentReplyId =
        when (target) {
          is ReplyTarget.RootReview -> null
          is ReplyTarget.Reply -> target.parentReplyId
        }

    val newReply =
        HuntReviewReply(
            replyId = replyRepository.getNewReplyId(),
            reviewId = reviewId,
            parentReplyId = parentReplyId,
            authorId = currentUserId,
            comment = text,
            createdAt = System.currentTimeMillis(),
            updatedAt = null,
            isDeleted = false,
        )

    viewModelScope.launch(dispatcher) {
      _uiState.update { it.copy(isSendingReply = true, errorMessage = null) }
      try {
        replyRepository.addReply(newReply)

        // Clear composer text for this target.
        _uiState.update { s ->
          when (target) {
            is ReplyTarget.RootReview -> s.copy(rootReplyText = "")
            is ReplyTarget.Reply -> {
              val updated = s.childReplyTexts.toMutableMap()
              updated.remove(target.parentReplyId)
              s.copy(childReplyTexts = updated)
            }
          }
        }

        // Close inline composer for this reply target.
        if (target is ReplyTarget.Reply) {
          composerOpenReplyIds.remove(target.parentReplyId)
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
              errorMessage = e.message ?: "Failed to send reply.",
          )
        }
      } finally {
        _uiState.update { it.copy(isSendingReply = false) }
      }
    }
  }

  /**
   * Deletes the reply with the given [replyId] if the current user is its author.
   *
   * This:
   * - Checks ownership.
   * - Calls [HuntReviewReplyRepository.deleteReply].
   * - Leaves descendants intact; they remain as replies to a now-missing parent.
   */
  fun deleteReply(replyId: String) {
    val currentUserId = currentUserIdOrNull()
    if (currentUserId == null) {
      _uiState.update { it.copy(errorMessage = "You must be signed in to delete a reply.") }
      return
    }

    val reply = allReplies.find { it.replyId == replyId }
    if (reply == null) {
      _uiState.update { it.copy(errorMessage = "Reply not found.") }
      return
    }

    if (reply.authorId != currentUserId) {
      _uiState.update { it.copy(errorMessage = "You can only delete your own replies.") }
      return
    }

    viewModelScope.launch(dispatcher) {
      try {
        replyRepository.deleteReply(replyId)
      } catch (e: Exception) {
        _uiState.update { it.copy(errorMessage = e.message ?: "Failed to delete reply.") }
      }
    }
  }

  /** Clears the current error message (if any) from [ReviewRepliesUiState.errorMessage]. */
  fun clearError() {
    _uiState.update { it.copy(errorMessage = null) }
  }

  /**
   * Rebuilds the internal tree + flattened list of [ReplyNodeUiState] from a raw list of
   * [HuntReviewReply] coming from the repository.
   *
   * This method:
   * - Computes parent → children relationships.
   * - Applies expansion state to decide which nodes are visible.
   * - Computes depth and children counts.
   */
  internal fun rebuildFromRawReplies(rawReplies: List<HuntReviewReply>) {
    // Build parent -> children map
    val currentState = _uiState.value
    val rootExpanded = currentState.isRootExpanded
    val childrenByParent: Map<String?, List<HuntReviewReply>> =
        rawReplies
            .groupBy { it.parentReplyId }
            .mapValues { (_, list) -> list.sortedBy { it.createdAt } }

    // Precompute total descendant counts per replyId
    val memoChildrenCount = mutableMapOf<String, Int>()

    fun countDescendants(replyId: String): Int {
      memoChildrenCount[replyId]?.let {
        return it
      }
      val directChildren = childrenByParent[replyId].orEmpty()
      val total =
          directChildren.size + directChildren.sumOf { child -> countDescendants(child.replyId) }
      memoChildrenCount[replyId] = total
      return total
    }

    rawReplies.forEach { countDescendants(it.replyId) }

    val currentUserId = currentUserIdOrNull()
    val flattened = mutableListOf<ReplyNodeUiState>()

    fun traverse(parentId: String?, depth: Int) {
      val children = childrenByParent[parentId].orEmpty()
      for (child in children) {
        val id = child.replyId

        // If root is collapsed, we don't show any of its children.
        if (parentId == null && !rootExpanded) {
          continue
        }

        val isExpanded = expandedReplyIds.contains(id)
        val isComposerOpen = composerOpenReplyIds.contains(id)
        val totalChildrenCount = memoChildrenCount[id] ?: 0
        val isMine = currentUserId != null && currentUserId == child.authorId

        flattened +=
            ReplyNodeUiState(
                reply = child,
                depth = depth,
                isExpanded = isExpanded,
                isComposerOpen = isComposerOpen,
                isMine = isMine,
                totalChildrenCount = totalChildrenCount,
            )

        if (isExpanded) {
          traverse(id, depth + 1)
        }
      }
    }

    traverse(parentId = null, depth = 0)

    val totalReplyCount = rawReplies.size
    _uiState.update { it.copy(replies = flattened, totalReplyCount = totalReplyCount) }
  }
  /**
   * Returns the ID of the currently authenticated user, or null if not logged in. Used for "isMine"
   * and ownership checks.
   */
  internal fun currentUserIdOrNull(): String? {
    return auth.currentUser?.uid
  }
}
