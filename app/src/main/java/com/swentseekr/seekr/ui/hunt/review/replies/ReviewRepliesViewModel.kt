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
 * ViewModel managing replies for a single review card.
 *
 * Handles fetching, sending, deleting, and updating replies in a thread-like structure.
 * Maintains UI state including expanded replies, open composers, and error messages.
 *
 * @param reviewId The ID of the review this ViewModel is associated with.
 * @param replyRepository Repository used for fetching, adding, and deleting replies.
 * @param dispatcher Coroutine dispatcher used for asynchronous operations.
 * @param currentUserIdProvider Lambda that provides the current authenticated user's ID, or null if not signed in.
 */
class ReviewRepliesViewModel(
    private val reviewId: String,
    private val replyRepository: HuntReviewReplyRepository =
        HuntReviewReplyRepositoryProvider.repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val currentUserIdProvider: () -> String? = {
      FirebaseAuth.getInstance().currentUser?.uid
    },
) : ViewModel() {

  private val _uiState = MutableStateFlow(ReviewRepliesUiState(reviewId = reviewId))
  val uiState: StateFlow<ReviewRepliesUiState> = _uiState.asStateFlow()

  private var allReplies: List<HuntReviewReply> = emptyList()
  private val expandedReplyIds: MutableSet<String> = mutableSetOf()
  private val composerOpenReplyIds: MutableSet<String> = mutableSetOf()
  private var hasStarted: Boolean = false

  /**
   * Initializes listening for replies on this review.
   *
   * Sets up a coroutine to collect live updates from the repository.
   * Updates the UI state with loading/error information.
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

  /**
   * Updates the reply text for a given target (root review or child reply).
   *
   * @param target The target reply or root review to update text for.
   * @param newText The new text to set in the composer.
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
   * Toggles the expanded/collapsed state of a reply thread.
   *
   * @param parentReplyId The parent reply ID to toggle; null toggles the root review thread.
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
   * Toggles the inline composer visibility for a specific reply.
   *
   * @param target The reply target for which the composer should be toggled.
   */
  fun onToggleComposer(target: ReplyTarget) {
    when (target) {
      is ReplyTarget.RootReview -> {
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
   * Sends a reply to the specified target (root review or child reply).
   *
   * Handles validation, empty text errors, and signing in checks.
   * Updates the UI state with sending progress and resets composer text upon success.
   *
   * @param target The target to which the reply should be sent.
   */
  fun sendReply(target: ReplyTarget) {
    val currentUserId = currentUserIdOrNull()
    if (currentUserId == null) {
      _uiState.update { it.copy(errorMessage = ReviewRepliesStrings.ERROR_SIGN_IN_TO_REPLY) }
      return
    }

    val state = _uiState.value
    val text =
        when (target) {
          is ReplyTarget.RootReview -> state.rootReplyText.trim()
          is ReplyTarget.Reply -> state.childReplyTexts[target.parentReplyId]?.trim().orEmpty()
        }

    if (text.isBlank()) {
      _uiState.update { it.copy(errorMessage = ReviewRepliesStrings.ERROR_EMPTY_REPLY) }
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

        if (target is ReplyTarget.Reply) {
          composerOpenReplyIds.remove(target.parentReplyId)
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
              errorMessage = e.message ?: ReviewRepliesStrings.ERROR_SEND_REPLY,
          )
        }
      } finally {
        _uiState.update { it.copy(isSendingReply = false) }
      }
    }
  }

  /**
   * Deletes a reply authored by the current user.
   *
   * @param replyId The ID of the reply to delete.
   */
  fun deleteReply(replyId: String) {
    val currentUserId = currentUserIdOrNull()
    if (currentUserId == null) {
      _uiState.update { it.copy(errorMessage = ReviewRepliesStrings.ERROR_SIGN_IN_TO_DELETE) }
      return
    }

    val reply = allReplies.find { it.replyId == replyId }
    if (reply == null) {
      _uiState.update { it.copy(errorMessage = ReviewRepliesStrings.ERROR_REPLY_NOT_FOUND) }
      return
    }

    if (reply.authorId != currentUserId) {
      _uiState.update { it.copy(errorMessage = ReviewRepliesStrings.ERROR_DELETE_NOT_OWNER) }
      return
    }

    viewModelScope.launch(dispatcher) {
      try {
        replyRepository.deleteReply(replyId)
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
              errorMessage = e.message ?: ReviewRepliesStrings.ERROR_DELETE_REPLY,
          )
        }
      }
    }
  }

  /**
   * Rebuilds the flattened list of [ReplyNodeUiState] from raw replies.
   *
   * Takes into account expanded replies, composer visibility, and calculates
   * total children count for proper thread display.
   *
   * @param rawReplies The raw list of replies fetched from the repository.
   */
  internal fun rebuildFromRawReplies(rawReplies: List<HuntReviewReply>) {
    val currentState = _uiState.value
    val rootExpanded = currentState.isRootExpanded
    val childrenByParent: Map<String?, List<HuntReviewReply>> =
        rawReplies
            .groupBy { it.parentReplyId }
            .mapValues { (_, list) -> list.sortedBy { it.createdAt } }

    val memoChildrenCount = mutableMapOf<String, Int>()

    /**
     * Counts the total number of descendants for a given reply ID recursively.
     *
     * @param replyId The reply ID for which to count all descendant replies.
     * @return The total number of descendant replies.
     */
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

    /**
     * Recursively traverses the reply tree to build a flattened list of [ReplyNodeUiState].
     *
     * Handles expanded/collapsed states, child reply counts, and composer visibility.
     *
     * @param parentId The parent reply ID to start traversing from (null for root-level replies).
     * @param depth The current nesting depth for proper indentation/display.
     */
    fun traverse(parentId: String?, depth: Int) {
      val children = childrenByParent[parentId].orEmpty()
      for (child in children) {
        if (parentId == null && !rootExpanded) {
          continue
        }

        val id = child.replyId
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
   * Retrieves the current user ID or null if the user is not signed in.
   *
   * @return The current Firebase Auth user ID or null.
   */
  internal fun currentUserIdOrNull(): String? = currentUserIdProvider()
}
