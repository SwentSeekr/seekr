package com.swentseekr.seekr.model.hunt.review

import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class HuntReviewReplyRepositoryProviderTest {

  @After
  fun tearDown() {
    // Make sure we don't leak overrides into other tests
    HuntReviewReplyRepositoryProvider.clearTestRepository()
  }

  @Test
  fun repository_isInitialized_withTestOverride() {
    val fakeRepo = HuntReviewReplyRepositoryLocal() // or FakeHuntReviewReplyRepository()

    // Override the repository for this test
    HuntReviewReplyRepositoryProvider.setTestRepository(fakeRepo)

    val repo = HuntReviewReplyRepositoryProvider.repository

    assertNotNull(repo)
    assertSame(fakeRepo, repo)
  }
}
