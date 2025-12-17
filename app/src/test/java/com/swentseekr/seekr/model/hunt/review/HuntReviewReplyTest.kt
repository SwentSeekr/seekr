package com.swentseekr.seekr.model.hunt.review

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the HuntReviewReply data model.
 *
 * This test suite verifies correct field initialization, copy behavior, and equality semantics of
 * the HuntReviewReply class.
 */
class HuntReviewReplyTest {

  @Test
  fun constructor_setsFieldsCorrectly() {
    val reply =
        HuntReviewReply(
            replyId = "r1",
            reviewId = "rev1",
            parentReplyId = "parent1",
            authorId = "userA",
            comment = "Nice!",
            createdAt = 123L,
            updatedAt = 456L,
            isDeleted = true,
        )

    assertEquals("r1", reply.replyId)
    assertEquals("rev1", reply.reviewId)
    assertEquals("parent1", reply.parentReplyId)
    assertEquals("userA", reply.authorId)
    assertEquals("Nice!", reply.comment)
    assertEquals(123L, reply.createdAt)
    assertEquals(456L, reply.updatedAt)
    assertTrue(reply.isDeleted)
  }

  @Test
  fun copyAndEquals_behaveAsExpected() {
    val base = HuntReviewReply(replyId = "r1", reviewId = "rev1", comment = "Hi")
    val copy = base.copy(comment = "Hi again")

    assertNotEquals(base, copy)
    assertEquals("Hi again", copy.comment)
    assertEquals("r1", copy.replyId)
    assertEquals("rev1", copy.reviewId)
  }
}
