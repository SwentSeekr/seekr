package com.swentseekr.seekr.ui.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.swentseekr.seekr.ui.hunt.review.ReviewImagesScreen
import com.swentseekr.seekr.ui.hunt.review.ReviewImagesScreenConstantsStrings
import org.junit.Rule
import org.junit.Test

class ReviewImagesScreenTest {

  val time: Long = 500

  @get:Rule val composeRule = createComposeRule()

  private fun setReviewImagesScreen(photos: List<String>) {
    composeRule.setContent { ReviewImagesScreen(photoUrls = photos, onGoBack = {}) }
    composeRule.waitForIdle()
    composeRule.mainClock.advanceTimeBy(time)
    composeRule.waitForIdle()
  }

  @Test
  fun reviewImagesScreen_displaysCorrectly() {
    val photos =
        listOf(
            ReviewImagesScreenConstantStings.Photo1,
            ReviewImagesScreenConstantStings.Photo2,
            ReviewImagesScreenConstantStings.Photo3)
    var backClicked = false

    composeRule.setContent {
      ReviewImagesScreen(photoUrls = photos, onGoBack = { backClicked = true })
    }

    composeRule.waitForIdle()

    // Screen root
    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.REVIEW_IMAGES_SCREEN_TEST_TAG)
        .assertIsDisplayed()

    // Top bar
    composeRule.onNodeWithTag(ReviewImagesScreenConstantsStrings.TOP_BAR_TEST_TAG).assertIsDisplayed()

    // Column
    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.REVIEW_IMAGES_COLUMN_TEST_TAG)
        .assertIsDisplayed()

    // Bottom info text
    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.REVIEW_IMAGE_TEXT_BOTTOM_TEST_TAG)
        .assertIsDisplayed()

    // Back button
    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.BACK_BUTTON_TAG)
        .assertIsDisplayed()
        .performClick()

    assert(backClicked) { ReviewImagesScreenConstantStings.TexteButton }
  }

  @Test
  fun reviewImagesScreen_swipingChangesImageIndex() {
    val photos =
        listOf(
            ReviewImagesScreenConstantStings.Photo1,
            ReviewImagesScreenConstantStings.Photo2,
            ReviewImagesScreenConstantStings.Photo3)

    composeRule.setContent { ReviewImagesScreen(photoUrls = photos, onGoBack = {}) }

    composeRule.waitForIdle()

    val pager =
        composeRule.onNodeWithTag(ReviewImagesScreenConstantsStrings.REVIEW_IMAGE_PAGER_TEST_TAG)
    pager.assertIsDisplayed()

    // Initial index
    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.REVIEW_IMAGE_TEXT_BOTTOM_TEST_TAG)
        .assertTextEquals("1/${photos.size}")

    // Swipe to page 2
    pager.performTouchInput { swipeLeft() }
    composeRule.mainClock.advanceTimeBy(time)
    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.REVIEW_IMAGE_TEXT_BOTTOM_TEST_TAG)
        .assertTextEquals("2/${photos.size}")

    // Swipe to page 3
    pager.performTouchInput { swipeLeft() }
    composeRule.mainClock.advanceTimeBy(time)
    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.REVIEW_IMAGE_TEXT_BOTTOM_TEST_TAG)
        .assertTextEquals("3/${photos.size}")
  }

  @Test
  fun reviewImagesScreen_displaysSingleImage() {
    val photos = listOf(ReviewImagesScreenConstantStings.Photo1)

    composeRule.setContent { ReviewImagesScreen(photoUrls = photos, onGoBack = {}) }

    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.REVIEW_IMAGE_PAGER_TEST_TAG)
        .assertIsDisplayed()

    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.REVIEW_IMAGE_TEXT_BOTTOM_TEST_TAG)
        .assertIsDisplayed()
        .assertTextEquals("1/1")
  }

  @Test
  fun reviewImagesScreen_fullscreenOpensAtCorrectIndex() {
    val photos =
        listOf(
            ReviewImagesScreenConstantStings.Photo1,
            ReviewImagesScreenConstantStings.Photo2,
            ReviewImagesScreenConstantStings.Photo3)

    setReviewImagesScreen(photos)

    // Swipe to second image in main pager
    val mainPager =
        composeRule.onNodeWithTag(ReviewImagesScreenConstantsStrings.REVIEW_IMAGE_PAGER_TEST_TAG)
    mainPager.performTouchInput { swipeLeft() }

    composeRule.mainClock.advanceTimeBy(time)
    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.REVIEW_IMAGE_TEXT_BOTTOM_TEST_TAG)
        .assertTextEquals("2/${photos.size}")

    // Open fullscreen for second image
    val baseBoxTag = ReviewImagesScreenConstantsStrings.REVIEW_IMAGE_BOX_TEST_TAG
    composeRule.onNodeWithTag("${baseBoxTag}1").performClick()
    composeRule.waitForIdle()
    composeRule.mainClock.advanceTimeBy(time)
    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(
            ReviewImagesScreenConstantsStrings.REVIEW_IMAGE_FULL_SCREEN_DIALOG_TEST_TAG,
            useUnmergedTree = true)
        .assertExists()

    composeRule
        .onNodeWithTag(
            ReviewImagesScreenConstantsStrings.REVIEW_IMAGE_FULL_SCREEN_PAGER_TEST_TAG,
            useUnmergedTree = true)
        .assertExists()

    // Close fullscreen
    composeRule
        .onNodeWithTag(
            ReviewImagesScreenConstantsStrings.REVIEW_IMAGE_FULL_SCREEN_DIALOG_TEST_TAG,
            useUnmergedTree = true)
        .performClick()

    composeRule.waitForIdle()
  }
}
