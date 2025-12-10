package com.swentseekr.seekr.model.hunt.review

import com.swentseekr.seekr.ui.hunt.review.replies.ReplyTarget
import com.swentseekr.seekr.ui.hunt.review.replies.ReviewRepliesStrings
import com.swentseekr.seekr.ui.hunt.review.replies.ReviewRepliesViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlin.test.Test
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewRepliesViewModelTest {

  // 1) Main dispatcher rule so viewModelScope (which uses Main) works in unit tests
  @get:Rule val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

  private lateinit var fakeRepository: FakeHuntReviewReplyRepository

  private val reviewId = "review-1"

  private fun createViewModel(
      currentUserId: String? = null,
  ): ReviewRepliesViewModel {
    return ReviewRepliesViewModel(
        reviewId = reviewId,
        replyRepository = fakeRepository,
        // Use the SAME dispatcher as runTest + Main
        dispatcher = mainDispatcherRule.testDispatcher,
        currentUserIdProvider = { currentUserId },
    )
  }

  @Before
  fun setup() {
    fakeRepository = FakeHuntReviewReplyRepository()
  }

  @Test
  fun onReplyTextChanged_updatesRootAndChildTexts() =
      runTest(mainDispatcherRule.testDispatcher) {
        val vm = createViewModel(currentUserId = "user-1")

        vm.onReplyTextChanged(
            ReplyTarget.RootReview(reviewId = reviewId),
            "Hello root",
        )
        assertEquals("Hello root", vm.uiState.value.rootReplyText)

        vm.onReplyTextChanged(
            ReplyTarget.Reply(
                reviewId = reviewId,
                parentReplyId = "r1",
            ),
            "Hello child",
        )
        val childText = vm.uiState.value.childReplyTexts["r1"]
        assertEquals("Hello child", childText)
      }

  @Test
  fun sendReply_withoutLoggedInUser_setsSignInError() =
      runTest(mainDispatcherRule.testDispatcher) {
        val vm = createViewModel(currentUserId = null)

        vm.onReplyTextChanged(
            ReplyTarget.RootReview(reviewId = reviewId),
            "Test reply",
        )

        // Root reply target, because we are replying to the review itself
        vm.sendReply(ReplyTarget.RootReview(reviewId = reviewId))

        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(ReviewRepliesStrings.ErrorSignInToReply, state.errorMessage)
        assertTrue(fakeRepository.addedReplies.isEmpty())
      }

  @Test
  fun sendReply_withBlankText_setsEmptyReplyError() =
      runTest(mainDispatcherRule.testDispatcher) {
        val vm = createViewModel(currentUserId = "user-1")

        vm.onReplyTextChanged(
            ReplyTarget.RootReview(reviewId = reviewId),
            "   ", // blank
        )
        vm.sendReply(ReplyTarget.RootReview(reviewId = reviewId))

        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(ReviewRepliesStrings.ErrorEmptyReply, state.errorMessage)
        assertTrue(fakeRepository.addedReplies.isEmpty())
      }

  @Test
  fun sendReply_rootReply_success_addsReplyAndClearsText() =
      runTest(mainDispatcherRule.testDispatcher) {
        val vm = createViewModel(currentUserId = "user-1")

        vm.onReplyTextChanged(
            ReplyTarget.RootReview(reviewId = reviewId),
            "Hi there",
        )
        vm.sendReply(ReplyTarget.RootReview(reviewId = reviewId))

        advanceUntilIdle()

        assertEquals(1, fakeRepository.addedReplies.size)
        val added = fakeRepository.addedReplies.first()
        assertEquals(reviewId, added.reviewId)
        assertNull(added.parentReplyId, "Root reply should have null parent")
        assertEquals("Hi there", added.comment)
        assertEquals("user-1", added.authorId)

        val state = vm.uiState.value
        assertEquals("", state.rootReplyText)
        assertFalse(state.isSendingReply)
      }

  @Test
  fun deleteReply_notOwner_setsError() =
      runTest(mainDispatcherRule.testDispatcher) {
        val vm = createViewModel(currentUserId = "user-1")

        val reply =
            HuntReviewReply(
                replyId = "r1",
                reviewId = reviewId,
                parentReplyId = null,
                authorId = "other-user",
                comment = "Hi",
                createdAt = 1L,
                updatedAt = null,
                isDeleted = false,
            )

        // Make sure the VM has this reply in its state
        vm.start()
        fakeRepository.emit(listOf(reply))
        advanceUntilIdle()

        vm.deleteReply("r1")

        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(ReviewRepliesStrings.ErrorDeleteNotOwner, state.errorMessage)
        assertTrue(fakeRepository.deletedReplyIds.isEmpty())
      }

  @Test
  fun deleteReply_owner_callsRepository() =
      runTest(mainDispatcherRule.testDispatcher) {
        val vm = createViewModel(currentUserId = "user-1")

        val reply =
            HuntReviewReply(
                replyId = "r1",
                reviewId = reviewId,
                parentReplyId = null,
                authorId = "user-1",
                comment = "Hi",
                createdAt = 1L,
                updatedAt = null,
                isDeleted = false,
            )

        vm.start()
        fakeRepository.emit(listOf(reply))
        advanceUntilIdle()

        vm.deleteReply("r1")

        advanceUntilIdle()

        assertEquals(listOf("r1"), fakeRepository.deletedReplyIds)
      }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {

  override fun starting(description: Description) {
    Dispatchers.setMain(testDispatcher)
  }

  override fun finished(description: Description) {
    Dispatchers.resetMain()
  }
}

/** Very small fake repo for tests, no Mockito needed. */
@OptIn(ExperimentalCoroutinesApi::class)
internal class FakeHuntReviewReplyRepository : HuntReviewReplyRepository {

  private val shared = MutableSharedFlow<List<HuntReviewReply>>(replay = 1)

  // In-memory store for replies
  private val replies = mutableMapOf<String, HuntReviewReply>()

  // For assertions
  val addedReplies = mutableListOf<HuntReviewReply>()
  val deletedReplyIds = mutableListOf<String>()

  override fun getNewReplyId(): String = "fake-${addedReplies.size + 1}"

  override suspend fun getRepliesForReview(reviewId: String): List<HuntReviewReply> {
    return replies.values.filter { it.reviewId == reviewId }
  }

  override suspend fun addReply(reply: HuntReviewReply) {
    addedReplies += reply
    replies[reply.replyId] = reply
    shared.emit(replies.values.toList())
  }

  override suspend fun updateReply(replyId: String, newReply: HuntReviewReply) {
    replies[replyId] = newReply.copy(replyId = replyId)
    shared.emit(replies.values.toList())
  }

  override suspend fun deleteReply(replyId: String) {
    deletedReplyIds += replyId
    replies.remove(replyId)
    shared.emit(replies.values.toList())
  }

  override fun listenToReplies(reviewId: String): Flow<List<HuntReviewReply>> = shared

  // Helper for tests to push an arbitrary snapshot:
  suspend fun emit(list: List<HuntReviewReply>) {
    replies.clear()
    list.forEach { replies[it.replyId] = it }
    shared.emit(list)
  }
}
