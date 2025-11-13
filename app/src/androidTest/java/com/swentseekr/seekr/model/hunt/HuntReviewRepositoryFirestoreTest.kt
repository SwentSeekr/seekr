package com.swentseekr.seekr.model.hunt

import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import com.swentseekr.seekr.utils.FirebaseTestEnvironment.clearEmulatorData
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HuntReviewRepositoryFirestoreTest {
  private val repository: HuntReviewRepository = HuntReviewRepositoryProvider.repository

  var review =
      HuntReview(
          reviewId = "testReviewID",
          authorId = "testAuthorID",
          huntId = "testHuntID",
          rating = 4.5,
          comment = "Great hunt!",
          photos = listOf("http://example.com/photo1.jpg", "http://example.com/photo2.jpg"))

  @Before
  fun setUp() {
    FirebaseTestEnvironment.setup()
    runTest {
      if (FirebaseTestEnvironment.isEmulatorActive()) {
        clearEmulatorData()
      }
      FirebaseAuth.getInstance().signInAnonymously().await()
      review = review.copy(authorId = FirebaseAuth.getInstance().currentUser?.uid ?: "0")
    }
  }

  @After
  fun tearDown() = runTest {
    if (FirebaseTestEnvironment.isEmulatorActive()) {
      clearEmulatorData()
    }
    FirebaseAuth.getInstance().signOut()
  }

  @Test
  fun generatesNewUniqueIds() = runTest {
    val firstId = repository.getNewUid()
    val secondId = repository.getNewUid()
    assertNotEquals(firstId, secondId)
    assertTrue(firstId.isNotEmpty())
    assertTrue(secondId.isNotEmpty())
  }

  @Test
  fun canAddHuntsToRepository() = runTest {
    repository.addReviewHunt(review)
    val reviews = repository.getHuntReviews("testHuntID")
    assertTrue(reviews.isNotEmpty())
    assertEquals(1, reviews.size)
    assertEquals(review, reviews[0])
  }

  @Test
  fun canAddMultipleHuntsToRepository() = runTest {
    val review2 = review.copy(reviewId = "review2", comment = "Not bad")
    repository.addReviewHunt(review)
    repository.addReviewHunt(review2)
    val reviews = repository.getHuntReviews("testHuntID")
    assertEquals(2, reviews.size)
    assert(reviews.contains(review))
    assert(reviews.contains(review2))
  }

  @Test
  fun canRetrieveAReview() = runTest {
    repository.addReviewHunt(review)
    val storedReview = repository.getReviewHunt(review.reviewId)
    assertEquals(storedReview, review)
  }

  @Test
  fun canRetrieveAHuntByIDWithMultipleHunts() = runTest {
    repository.addReviewHunt(review)
    val review2 = review.copy(reviewId = "review2", comment = "Not bad")
    repository.addReviewHunt(review2)
    val review3 = review.copy(reviewId = "review3", comment = "Not too bad")
    repository.addReviewHunt(review3)
    val storedHunt = repository.getReviewHunt(review3.reviewId)
    assertEquals(storedHunt, review3)
  }

  @Test(expected = IllegalArgumentException::class)
  fun retrievingNonExistentHuntThrowsException() = runTest {
    repository.addReviewHunt(review)
    repository.getReviewHunt("nonexistent_id")
  }

  @Test
  fun canEditAHuntById() = runTest {
    repository.addReviewHunt(review)
    val modifiedReview = review.copy(comment = "Modified Comment", rating = 3.0)
    repository.updateReviewHunt(review.reviewId, modifiedReview)
    assertEquals(1, repository.getHuntReviews("testHuntID").size)
    val storedReview = repository.getReviewHunt(review.reviewId)
    assertEquals(modifiedReview, storedReview)
  }

  @Test
  fun canDeleteAHuntByID() = runTest {
    repository.addReviewHunt(review)
    assertEquals(1, repository.getHuntReviews("testHuntID").size)
    repository.deleteReviewHunt(review.reviewId)
    assertEquals(0, repository.getHuntReviews("testHuntID").size)
  }
}
