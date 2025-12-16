package com.swentseekr.seekr.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.swentseekr.seekr.ui.components.Rating
import com.swentseekr.seekr.ui.components.RatingTestTags
import com.swentseekr.seekr.ui.components.RatingType
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the Rating composable.
 *
 * This test suite verifies that the Rating component displays
 * the correct number of full, half, and empty icons based on
 * the provided rating value and type.
 */
class RatingTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun rating_starDisplaysCorrectly() {
    composeTestRule.setContent { Rating(3.5, RatingType.STAR) }
    repeat(3) { index ->
      composeTestRule.onNodeWithTag(RatingTestTags.full(index, RatingType.STAR)).assertIsDisplayed()
    }
    composeTestRule.onNodeWithTag(RatingTestTags.half(RatingType.STAR)).assertIsDisplayed()
    composeTestRule.onNodeWithTag(RatingTestTags.empty(0, RatingType.STAR)).assertIsDisplayed()
  }

  @Test
  fun rating_sportDisplaysCorrectly() {
    composeTestRule.setContent { Rating(2.0, RatingType.SPORT) }

    repeat(2) { index ->
      composeTestRule
          .onNodeWithTag(RatingTestTags.full(index, RatingType.SPORT))
          .assertIsDisplayed()
    }

    repeat(3) { index ->
      composeTestRule
          .onNodeWithTag(RatingTestTags.empty(index, RatingType.SPORT))
          .assertIsDisplayed()
    }
  }

  @Test
  fun rating_throwsException_onInvalidRating() {
    assertThrows(IllegalStateException::class.java) {
      composeTestRule.setContent { Rating(6.0, RatingType.STAR) }
    }

    assertThrows(IllegalStateException::class.java) {
      composeTestRule.setContent { Rating(-1.0, RatingType.STAR) }
    }
  }
}
