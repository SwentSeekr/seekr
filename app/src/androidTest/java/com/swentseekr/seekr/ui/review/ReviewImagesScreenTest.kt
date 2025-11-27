package com.swentseekr.seekr.ui.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.swentseekr.seekr.ui.hunt.review.ReviewImagesScreen
import org.junit.Rule
import org.junit.Test

class ReviewImagesScreenTest {

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun reviewImagesScreen_displaysAllImages_andBackButtonWorks() {
    // Sample photo URLs
    val photos =
        listOf(
            ReviewImagesScreenConstantStings.Photo1,
            ReviewImagesScreenConstantStings.Photo2,
            ReviewImagesScreenConstantStings.Photo3)
    var backClicked = false

    // Set content
    composeRule.setContent {
      ReviewImagesScreen(photoUrls = photos, onGoBack = { backClicked = true })
    }

    // Assert that the top bar is displayed
    composeRule.onNodeWithTag(ReviewImagesScreenConstantStings.TestTagImage).assertIsDisplayed()

    // Assert that back button is displayed and clickable
    composeRule
        .onNodeWithTag(ReviewImagesScreenConstantStings.BackButtonTag)
        .assertIsDisplayed()
        .performClick()
    assert(backClicked) { ReviewImagesScreenConstantStings.TexteButton }

    // Assert that all images are displayed
    composeRule.onAllNodesWithText(ReviewImagesScreenConstantStings.TestNumber).apply {
      assert(this.fetchSemanticsNodes().size == photos.size) {
        "${ReviewImagesScreenConstantStings.Expected} ${photos.size} ${ReviewImagesScreenConstantStings.TextMiddle} ${this.fetchSemanticsNodes().size}"
      }
    }
  }
}
