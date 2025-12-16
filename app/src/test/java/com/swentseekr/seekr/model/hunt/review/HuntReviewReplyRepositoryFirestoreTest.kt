package com.swentseekr.seekr.model.hunt.review

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for HuntReviewReplyRepositoryFirestore document mapping.
 *
 * This test suite verifies correct parsing of Firestore DocumentSnapshot
 * into reply domain models, including handling of missing fields and
 * exception safety during deserialization.
 */

class HuntReviewReplyRepositoryFirestoreTest {

  private lateinit var firestore: FirebaseFirestore
  private lateinit var repository: HuntReviewReplyRepositoryFirestore

  @Before
  fun setUp() {
    firestore = mockk(relaxed = true)
    repository = HuntReviewReplyRepositoryFirestore(firestore)
  }

  @Test
  fun documentToReply_parsesFieldsCorrectly() {
    val snapshot = mockk<DocumentSnapshot>()

    every { snapshot.id } returns "r1"
    every { snapshot.getString(HuntReviewReplyFirestoreConstants.FIELD_REVIEW_ID) } returns "rev123"
    every { snapshot.getString(HuntReviewReplyFirestoreConstants.FIELD_PARENT_REPLY_ID) } returns
        "parent1"
    every { snapshot.getString(HuntReviewReplyFirestoreConstants.FIELD_AUTHOR_ID) } returns "userA"
    every { snapshot.getString(HuntReviewReplyFirestoreConstants.FIELD_COMMENT) } returns "Hi"
    every { snapshot.getLong(HuntReviewReplyFirestoreConstants.FIELD_CREATED_AT) } returns 123L
    every { snapshot.getLong(HuntReviewReplyFirestoreConstants.FIELD_UPDATED_AT) } returns 456L
    every { snapshot.getBoolean(HuntReviewReplyFirestoreConstants.FIELD_IS_DELETED) } returns true

    val result = repository.documentToReply(snapshot)

    assertNotNull(result)
    result!!
    assertEquals("r1", result.replyId)
    assertEquals("rev123", result.reviewId)
    assertEquals("parent1", result.parentReplyId)
    assertEquals("userA", result.authorId)
    assertEquals("Hi", result.comment)
    assertEquals(123L, result.createdAt)
    assertEquals(456L, result.updatedAt)
    assertTrue(result.isDeleted)
  }

  @Test
  fun documentToReply_missingRequiredFields_returnsNull() {
    val snapshot = mockk<DocumentSnapshot>()

    every { snapshot.id } returns "r1"
    every { snapshot.getString(HuntReviewReplyFirestoreConstants.FIELD_REVIEW_ID) } returns null
    every { snapshot.getString(HuntReviewReplyFirestoreConstants.FIELD_AUTHOR_ID) } returns "userA"
    val result = repository.documentToReply(snapshot)

    assertNull(result)
  }

  @Test
  fun documentToReply_handlesException_andReturnsNull() {
    val snapshot = mockk<DocumentSnapshot>()

    every { snapshot.id } returns "r1"
    every { snapshot.getString(HuntReviewReplyFirestoreConstants.FIELD_REVIEW_ID) } throws
        RuntimeException("boom")

    val result = repository.documentToReply(snapshot)

    assertNull(result)
  }
}
