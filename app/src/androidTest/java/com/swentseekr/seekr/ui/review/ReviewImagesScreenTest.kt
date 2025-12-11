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
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.ReviewImagesScreenTestTag)
        .assertIsDisplayed()

    // Top bar
    composeRule.onNodeWithTag(ReviewImagesScreenConstantsStrings.TopBarTestTag).assertIsDisplayed()

    // Column
    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.ReviewImagesColumnTestTag)
        .assertIsDisplayed()

    // Bottom info text
    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.ReviewImageTextBottomTestTag)
        .assertIsDisplayed()

    // Back button
    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.BackButtonTag)
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
        composeRule.onNodeWithTag(ReviewImagesScreenConstantsStrings.ReviewImagePagerTestTag)
    pager.assertIsDisplayed()

    // Initial index
    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.ReviewImageTextBottomTestTag)
        .assertTextEquals("1/${photos.size}")

    // Swipe to page 2
    pager.performTouchInput { swipeLeft() }
    composeRule.mainClock.advanceTimeBy(time)
    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.ReviewImageTextBottomTestTag)
        .assertTextEquals("2/${photos.size}")

    // Swipe to page 3
    pager.performTouchInput { swipeLeft() }
    composeRule.mainClock.advanceTimeBy(time)
    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.ReviewImageTextBottomTestTag)
        .assertTextEquals("3/${photos.size}")
  }

  @Test
  fun reviewImagesScreen_displaysSingleImage() {
    val photos = listOf(ReviewImagesScreenConstantStings.Photo1)

    composeRule.setContent { ReviewImagesScreen(photoUrls = photos, onGoBack = {}) }

    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.ReviewImagePagerTestTag)
        .assertIsDisplayed()

    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.ReviewImageTextBottomTestTag)
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
        composeRule.onNodeWithTag(ReviewImagesScreenConstantsStrings.ReviewImagePagerTestTag)
    mainPager.performTouchInput { swipeLeft() }

    composeRule.mainClock.advanceTimeBy(time)
    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantsStrings.ReviewImageTextBottomTestTag)
        .assertTextEquals("2/${photos.size}")

    // Open fullscreen for second image
    val baseBoxTag = ReviewImagesScreenConstantsStrings.ReviewImageBoxTestTag
    composeRule.onNodeWithTag("${baseBoxTag}1").performClick()
    composeRule.waitForIdle()
    composeRule.mainClock.advanceTimeBy(time)
    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(
            ReviewImagesScreenConstantsStrings.ReviewImageFullScreenDialogTestTag,
            useUnmergedTree = true)
        .assertExists()

    composeRule
        .onNodeWithTag(
            ReviewImagesScreenConstantsStrings.ReviewImageFullScreenPagerTestTag,
            useUnmergedTree = true)
        .assertExists()

    // Close fullscreen
    composeRule
        .onNodeWithTag(
            ReviewImagesScreenConstantsStrings.ReviewImageFullScreenDialogTestTag,
            useUnmergedTree = true)
        .performClick()

    composeRule.waitForIdle()
  }
}
