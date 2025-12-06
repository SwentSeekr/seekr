package com.swentseekr.seekr.model.hunt.review

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

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
    every { snapshot.getString("reviewId") } returns "rev123"
    every { snapshot.getString("parentReplyId") } returns "parent1"
    every { snapshot.getString("authorId") } returns "userA"
    every { snapshot.getString("comment") } returns "Hi"
    every { snapshot.getLong("createdAt") } returns 123L
    every { snapshot.getLong("updatedAt") } returns 456L
    every { snapshot.getBoolean("isDeleted") } returns true

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
    every { snapshot.getString("reviewId") } returns null // required but missing
    every { snapshot.getString("authorId") } returns "userA" // doesn’t matter

    val result = repository.documentToReply(snapshot)

    assertNull(result)
  }

  @Test
  fun documentToReply_handlesException_andReturnsNull() {
    val snapshot = mockk<DocumentSnapshot>()

    every { snapshot.id } returns "r1"
    // Force an exception when trying to read a field:
    every { snapshot.getString("reviewId") } throws RuntimeException("boom")

    val result = repository.documentToReply(snapshot)

    assertNull(result)
  }
}
