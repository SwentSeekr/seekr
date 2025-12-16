package com.swentseekr.seekr.model.hunt

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the HuntReviewRepositoryLocal class.
 *
 * This test suite verifies the functionality of adding, retrieving, updating, and deleting hunt
 * reviews in the local repository.
 */
class HuntReviewRepositoryLocalTest {
  private lateinit var repository: HuntReviewRepositoryLocal
  private lateinit var review1: HuntReview
  private lateinit var review2: HuntReview

  @Before
  fun setUp() {
    repository = HuntReviewRepositoryLocal()
    review1 =
        HuntReview(
            reviewId = "1",
            authorId = "userA",
            huntId = "hunt123",
            rating = 4.5,
            comment = "Awesome hunt!",
            photos = emptyList())
    review2 =
        HuntReview(
            reviewId = "2",
            authorId = "userB",
            huntId = "hunt123",
            rating = 3.0,
            comment = "Not bad",
            photos = emptyList())
  }

  @Test
  fun getNewUid_generatesUniqueIds() {
    val id1 = repository.getNewUid()
    val id2 = repository.getNewUid()
    assertNotEquals(id1, id2)
  }

  @Test
  fun addReviewHunt_addsSuccessfully() = runTest {
    repository.addReviewHunt(review1)
    val reviews = repository.getHuntReviews("hunt123")
    assertEquals(1, reviews.size)
    assertEquals(review1, reviews[0])
  }

  @Test
  fun getReviewHunt_returnsCorrectReview() = runTest {
    repository.addReviewHunt(review1)
    repository.addReviewHunt(review2)
    val result = repository.getReviewHunt("1")
    assertEquals(review1, result)
  }

  @Test(expected = IllegalArgumentException::class)
  fun getReviewHunt_invalidId_throwsException() = runTest {
    repository.getReviewHunt("does_not_exist")
  }

  @Test
  fun updateReviewHunt_updatesSuccessfully() = runTest {
    repository.addReviewHunt(review1)
    val updatedReview = review1.copy(comment = "Updated Comment", rating = 5.0)
    repository.updateReviewHunt("1", updatedReview)

    val stored = repository.getReviewHunt("1")
    assertEquals("Updated Comment", stored.comment)
    assertEquals(5.0, stored.rating, 0.001)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateReviewHunt_invalidId_throwsException() = runTest {
    repository.updateReviewHunt("not_found", review1)
  }

  @Test
  fun deleteReviewHunt_removesSuccessfully() = runTest {
    repository.addReviewHunt(review1)
    assertEquals(1, repository.getHuntReviews("hunt123").size)

    repository.deleteReviewHunt("1")

    val reviews = repository.getHuntReviews("hunt123")
    assertTrue(reviews.isEmpty())
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteReviewHunt_invalidId_throwsException() = runTest {
    repository.deleteReviewHunt("nonexistent")
  }

  @Test
  fun getHuntReviews_returnsCorrectList() = runTest {
    val reviewOtherHunt = review1.copy(reviewId = "3", huntId = "hunt999")
    repository.addReviewHunt(review1)
    repository.addReviewHunt(review2)
    repository.addReviewHunt(reviewOtherHunt)

    val reviews = repository.getHuntReviews("hunt123")

    assertEquals(2, reviews.size)
    assertTrue(reviews.contains(review1))
    assertTrue(reviews.contains(review2))
    assertFalse(reviews.contains(reviewOtherHunt))
  }
}
