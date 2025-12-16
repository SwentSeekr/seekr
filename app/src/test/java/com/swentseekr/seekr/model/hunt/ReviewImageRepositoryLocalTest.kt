package com.swentseekr.seekr.model.hunt

import androidx.core.net.toUri
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the ReviewImageRepositoryLocal.
 *
 * This test suite verifies that the uploadReviewPhoto method
 * returns the expected local URL format and that the deleteReviewPhoto
 * method executes without throwing exceptions.
 */
class ReviewImageRepositoryLocalTest {
  private lateinit var repository: ReviewImageRepositoryLocal

  @Before
  fun setUp() {
    repository = ReviewImageRepositoryLocal()
  }

  @Test
  fun uploadReviewPhoto_returns_expected_local_URL() = runTest {
    val userId = "user123"
    val fakeUri = ("content://fake/path").toUri() // non-null fake Uri

    // Call uploadReviewPhoto twice to test the id increment
    val url1 = repository.uploadReviewPhoto(userId, fakeUri)
    val url2 = repository.uploadReviewPhoto(userId, fakeUri)

    // Assert that the URLs are as expected
    assertEquals("local://review_image/${userId}_0", url1)
    assertEquals("local://review_image/${userId}_1", url2)
  }

  @Test
  fun deleteReviewPhoto_does_not_throw() = runTest {
    val fakeUrl = "local://review_image/user123_0"
    try {
      repository.deleteReviewPhoto(fakeUrl)
    } catch (e: Exception) {
      fail("deleteReviewPhoto should not throw, but got: ${e.message}")
    }
  }
}
