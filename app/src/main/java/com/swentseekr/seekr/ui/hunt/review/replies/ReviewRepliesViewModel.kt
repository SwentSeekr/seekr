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

/** ViewModel managing replies for a *single* review card. */
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

  /** Latest raw replies snapshot coming from the repository. */
  private var allReplies: List<HuntReviewReply> = emptyList()

  /** IDs of replies whose children are currently expanded. */
  private val expandedReplyIds: MutableSet<String> = mutableSetOf()

  /** IDs of replies whose inline composer is currently open. */
  private val composerOpenReplyIds: MutableSet<String> = mutableSetOf()

  /** Prevents starting multiple collectors for the same ViewModel. */
  private var hasStarted: Boolean = false

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
              errorMessage = e.message ?: ReviewRepliesStrings.ERROR_SEND_REPLY,
          )
        }
      } finally {
        _uiState.update { it.copy(isSendingReply = false) }
      }
    }
  }

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

  fun clearError() {
    _uiState.update { it.copy(errorMessage = null) }
  }

  internal fun rebuildFromRawReplies(rawReplies: List<HuntReviewReply>) {
    val currentState = _uiState.value
    val rootExpanded = currentState.isRootExpanded
    val childrenByParent: Map<String?, List<HuntReviewReply>> =
        rawReplies
            .groupBy { it.parentReplyId }
            .mapValues { (_, list) -> list.sortedBy { it.createdAt } }

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

  internal fun currentUserIdOrNull(): String? = currentUserIdProvider()
}
