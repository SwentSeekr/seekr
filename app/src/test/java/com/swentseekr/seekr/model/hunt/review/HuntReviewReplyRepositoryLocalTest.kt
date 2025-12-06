package com.swentseekr.seekr.model.hunt.review

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HuntReviewReplyRepositoryLocalTest {

  private lateinit var repository: HuntReviewReplyRepositoryLocal
  private lateinit var reply1: HuntReviewReply
  private lateinit var reply2: HuntReviewReply

  @Before
  fun setUp() {
    repository = HuntReviewReplyRepositoryLocal()

    reply1 =
        HuntReviewReply(
            replyId = "r1",
            reviewId = "rev123",
            parentReplyId = null,
            authorId = "userA",
            comment = "Top-level reply",
            createdAt = 1L,
        )

    reply2 =
        HuntReviewReply(
            replyId = "r2",
            reviewId = "rev123",
            parentReplyId = "r1",
            authorId = "userB",
            comment = "Nested reply",
            createdAt = 2L,
        )
  }

  @Test
  fun getNewReplyId_generatesUniqueIds() {
    val id1 = repository.getNewReplyId()
    val id2 = repository.getNewReplyId()
    assertNotEquals(id1, id2)
  }

  @Test
  fun addReply_addsSuccessfully() = runTest {
    repository.addReply(reply1)

    val replies = repository.getRepliesForReview("rev123")
    assertEquals(1, replies.size)
    assertEquals(reply1, replies[0])
  }

  @Test(expected = IllegalArgumentException::class)
  fun addReply_blankId_throwsException() = runTest {
    val invalid = reply1.copy(replyId = "")
    repository.addReply(invalid)
  }

  @Test
  fun getRepliesForReview_filtersByReviewId() = runTest {
    val otherReview = reply1.copy(replyId = "r3", reviewId = "rev999")

    repository.addReply(reply1)
    repository.addReply(reply2)
    repository.addReply(otherReview)

    val replies = repository.getRepliesForReview("rev123")

    assertEquals(2, replies.size)
    assertTrue(replies.contains(reply1))
    assertTrue(replies.contains(reply2))
    assertFalse(replies.contains(otherReview))
  }

  @Test
  fun getRepliesForReview_returnsEmptyList_whenNone() = runTest {
    val replies = repository.getRepliesForReview("nope")
    assertTrue(replies.isEmpty())
  }

  @Test
  fun updateReply_updatesSuccessfully() = runTest {
    repository.addReply(reply1)

    val updated = reply1.copy(comment = "Updated", isDeleted = true)
    repository.updateReply("r1", updated)

    val stored = repository.getRepliesForReview("rev123").first()
    assertEquals("Updated", stored.comment)
    assertTrue(stored.isDeleted)
    assertEquals("r1", stored.replyId) // id is preserved
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateReply_invalidId_throwsException() = runTest {
    repository.updateReply("does_not_exist", reply1)
  }

  @Test
  fun deleteReply_removesSuccessfully() = runTest {
    repository.addReply(reply1)
    assertEquals(1, repository.getRepliesForReview("rev123").size)

    repository.deleteReply("r1")

    val replies = repository.getRepliesForReview("rev123")
    assertTrue(replies.isEmpty())
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteReply_invalidId_throwsException() = runTest { repository.deleteReply("does_not_exist") }

  @Test
  fun listenToReplies_emitsCurrentStateOnce() = runTest {
    repository.addReply(reply1)
    repository.addReply(reply2)

    val flow = repository.listenToReplies("rev123")
    val emitted = flow.first()

    assertEquals(2, emitted.size)
    assertTrue(emitted.contains(reply1))
    assertTrue(emitted.contains(reply2))
  }
}
