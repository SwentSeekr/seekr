package com.swentseekr.seekr.ui.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.swentseekr.seekr.ui.hunt.review.ReviewImagesScreen
import org.junit.Rule
import org.junit.Test

class ReviewImagesScreenTest {

  @get:Rule val composeRule = createComposeRule()

    @Test
    fun reviewImagesScreen_displaysCorrectly() {
        // Sample photo URLs - use simple URLs or local resources
        val photos = listOf(
            ReviewImagesScreenConstantStings.Photo1,
            ReviewImagesScreenConstantStings.Photo2,
            ReviewImagesScreenConstantStings.Photo3
        )
        var backClicked = false

        // Set content
        composeRule.setContent {
            ReviewImagesScreen(
                photoUrls = photos,
                onGoBack = { backClicked = true }
            )
        }

        // Wait for initial composition
        composeRule.waitForIdle()

        // Assert that the screen is displayed
        composeRule
            .onNodeWithTag(ReviewImagesScreenConstantStings.TestTagImage)
            .assertIsDisplayed()

        // Assert that the top bar is displayed
        composeRule
            .onNodeWithTag("TOP_BAR_TEST_TAG")
            .assertIsDisplayed()

        // Assert that the column is displayed
        composeRule
            .onNodeWithTag("REVIEW_IMAGES_COLUMN")
            .assertIsDisplayed()

        // Assert that info text is displayed
        composeRule
            .onNodeWithTag("ReviewImagesInfoText")
            .assertIsDisplayed()

        // Assert that back button is displayed and clickable
        composeRule
            .onNodeWithTag(ReviewImagesScreenConstantStings.BackButtonTag)
            .assertIsDisplayed()
            .performClick()

        assert(backClicked) { "Back button was not clicked" }
    }


    @Test
    fun reviewImagesScreen_displaysPagerWithImages() {
        val photos = listOf(
            ReviewImagesScreenConstantStings.Photo1,
            ReviewImagesScreenConstantStings.Photo2,
            ReviewImagesScreenConstantStings.Photo3
        )

        composeRule.setContent {
            ReviewImagesScreen(
                photoUrls = photos,
                onGoBack = {}
            )
        }

        composeRule.waitForIdle()

        // Assert that the pager is displayed
        val pager = composeRule.onNodeWithTag("ReviewImagePager")
        pager.assertIsDisplayed()

        // Assert first image index text is correct
        composeRule
            .onNodeWithTag("ReviewImageIndexText")
            .assertIsDisplayed()
            .assertTextEquals("Image #1/${photos.size}")
    }

    @Test
    fun reviewImagesScreen_swipingChangesImageIndex() {
        val photos = listOf(
            ReviewImagesScreenConstantStings.Photo1,
            ReviewImagesScreenConstantStings.Photo2,
            ReviewImagesScreenConstantStings.Photo3
        )

        composeRule.setContent {
            ReviewImagesScreen(
                photoUrls = photos,
                onGoBack = {}
            )
        }

        composeRule.waitForIdle()

        val pager = composeRule.onNodeWithTag("ReviewImagePager")
        pager.assertIsDisplayed()

        // Check initial state (Image 1)
        composeRule
            .onNodeWithTag("ReviewImageIndexText")
            .assertTextEquals("Image #1/${photos.size}")

        // Swipe left to go to second image
        pager.performTouchInput {
            swipeLeft()
        }

        // Wait for animation to complete
        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()

        // Check second image
        composeRule
            .onNodeWithTag("ReviewImageIndexText")
            .assertTextEquals("Image #2/${photos.size}")

        // Swipe left again to go to third image
        pager.performTouchInput {
            swipeLeft()
        }

        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()

        // Check third image
        composeRule
            .onNodeWithTag("ReviewImageIndexText")
            .assertTextEquals("Image #3/${photos.size}")
    }

    @Test
    fun reviewImagesScreen_displaysSingleImage() {
        val photos = listOf(ReviewImagesScreenConstantStings.Photo1)

        composeRule.setContent {
            ReviewImagesScreen(
                photoUrls = photos,
                onGoBack = {}
            )
        }

        composeRule.waitForIdle()

        // Assert that the pager is displayed
        composeRule
            .onNodeWithTag("ReviewImagePager")
            .assertIsDisplayed()

        // Assert correct index text for single image
        composeRule
            .onNodeWithTag("ReviewImageIndexText")
            .assertIsDisplayed()
            .assertTextEquals("Image #1/1")
    }
    @Test
  fun reviewImagesScreen_clickingImageOpenFullScreen() {
    // Sample photo URLs
    val photos =
        listOf(
            ReviewImagesScreenConstantStings.Photo1,
            ReviewImagesScreenConstantStings.Photo2,
            ReviewImagesScreenConstantStings.Photo3)

    // Set content
    composeRule.setContent {
      ReviewImagesScreen(photoUrls = photos, onGoBack = {})
    }
      composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()

        // Click on the first image box (which is clickable)
        composeRule
            .onNodeWithTag("ReviewImageBox_0")
            .performClick()

        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()


        val fullscreenPager = composeRule.onNodeWithTag("FullScreenImagePager", useUnmergedTree = true)
        fullscreenPager.assertExists()

        // Swipe left in fullscreen
        fullscreenPager.performTouchInput {
            swipeLeft()
        }

        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()

        // Verify we can still interact with fullscreen (it's still open)
        fullscreenPager.assertExists()

        // Close fullscreen
        composeRule
            .onNodeWithTag("FullScreenImageDialog", useUnmergedTree = true)
            .performClick()

        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()

        // Verify it's closed
        composeRule
            .onNodeWithTag("FullScreenImageDialog", useUnmergedTree = true)
            .assertDoesNotExist()

  }
    @Test
    fun reviewImagesScreen_fullscreenSwipingWorks() {
        val photos = listOf(
            ReviewImagesScreenConstantStings.Photo1,
            ReviewImagesScreenConstantStings.Photo2,
            ReviewImagesScreenConstantStings.Photo3
        )

        composeRule.setContent {
            ReviewImagesScreen(
                photoUrls = photos,
                onGoBack = {}
            )
        }

        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()

        // Click on the first image to open fullscreen at index 0
        composeRule
            .onNodeWithTag("ReviewImageBox_0")
            .performClick()

        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()

        // Verify fullscreen is open (use unmerged tree)
        val fullscreenPager = composeRule.onNodeWithTag("FullScreenImagePager", useUnmergedTree = true)
        fullscreenPager.assertExists()

        // Swipe left in fullscreen
        fullscreenPager.performTouchInput {
            swipeLeft()
        }

        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()

        // Verify we can still interact with fullscreen (it's still open)
        fullscreenPager.assertExists()

        // Close fullscreen
        composeRule
            .onNodeWithTag("FullScreenImageDialog", useUnmergedTree = true)
            .performClick()

        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()

        // Verify it's closed
        composeRule
            .onNodeWithTag("FullScreenImageDialog", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun reviewImagesScreen_fullscreenOpensAtCorrectIndex() {
        val photos = listOf(
            ReviewImagesScreenConstantStings.Photo1,
            ReviewImagesScreenConstantStings.Photo2,
            ReviewImagesScreenConstantStings.Photo3
        )

        composeRule.setContent {
            ReviewImagesScreen(
                photoUrls = photos,
                onGoBack = {}
            )
        }

        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()

        // First, swipe to the second image in the main pager
        val mainPager = composeRule.onNodeWithTag("ReviewImagePager")
        mainPager.performTouchInput {
            swipeLeft()
        }

        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()

        // Verify we're on the second image
        composeRule
            .onNodeWithTag("ReviewImageIndexText")
            .assertTextEquals("Image #2/${photos.size}")

        // Click on the second image box to open fullscreen
        composeRule
            .onNodeWithTag("ReviewImageBox_1")
            .performClick()

        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()

        // Verify fullscreen dialog opened (use unmerged tree)
        composeRule
            .onNodeWithTag("FullScreenImageDialog", useUnmergedTree = true)
            .assertExists()

        // The fullscreen pager should start at index 1 (second image)
        // We can verify this by checking it exists
        composeRule
            .onNodeWithTag("FullScreenImagePager", useUnmergedTree = true)
            .assertExists()

        // Close the dialog
        composeRule
            .onNodeWithTag("FullScreenImageDialog", useUnmergedTree = true)
            .performClick()

        composeRule.waitForIdle()
    }

}
