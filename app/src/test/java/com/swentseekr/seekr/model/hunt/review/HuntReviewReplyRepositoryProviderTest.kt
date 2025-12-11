package com.swentseekr.seekr.model.hunt.review

import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class HuntReviewReplyRepositoryProviderTest {

  @After
  fun tearDown() {
    HuntReviewReplyRepositoryProvider.clearTestRepository()
  }

  @Test
  fun repository_isInitialized_withTestOverride() {
    val fakeRepo = HuntReviewReplyRepositoryLocal()

    HuntReviewReplyRepositoryProvider.setTestRepository(fakeRepo)

    val repo = HuntReviewReplyRepositoryProvider.repository

    assertNotNull(repo)
    assertSame(fakeRepo, repo)
  }
}
