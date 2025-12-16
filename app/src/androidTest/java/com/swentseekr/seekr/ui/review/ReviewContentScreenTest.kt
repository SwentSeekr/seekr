package com.swentseekr.seekr.ui.review

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.swentseekr.seekr.FakeReviewHuntViewModel
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.review.ReviewScreenContent
import com.swentseekr.seekr.ui.huntCardScreen.FakeHuntCardViewModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the ReviewScreenContent composable.
 *
 * This test suite verifies that when a non-empty reviewId is provided, the loadReview() method of
 * the ReviewHuntViewModel is called.
 */
class ReviewContentScreenTest {
  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun loadReview_called_when_reviewId_notEmpty() {
    val fakeHunt =
        Hunt(
            uid = "hunt1",
            start = Location(0.0, 0.0, ""),
            end = Location(0.0, 0.0, ""),
            middlePoints = emptyList(),
            status = HuntStatus.FUN,
            title = "Test Hunt",
            description = "Desc",
            time = 1.0,
            distance = 5.0,
            difficulty = Difficulty.EASY,
            authorId = "author1",
            mainImageUrl = "",
            reviewRate = 4.0)
    val reviewVm = FakeReviewHuntViewModel()
    val huntVm = FakeHuntCardViewModel(fakeHunt)

    composeRule.setContent {
      ReviewScreenContent(
          title = "hey",
          huntId = "hunt1",
          reviewId = "123",
          onGoBack = {},
          onDone = {},
          onCancel = {},
          reviewViewModel = reviewVm,
          huntCardViewModel = huntVm)
    }

    // Give time for LaunchedEffect to run
    composeRule.waitForIdle()

    // ASSERT that loadReview() was called
    assertTrue(reviewVm.loadReviewCalled)
  }
}
