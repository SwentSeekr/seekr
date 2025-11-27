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
    val photos = listOf("photo1", "photo2", "photo3")
    var backClicked = false

    // Set content
    composeRule.setContent {
      ReviewImagesScreen(photoUrls = photos, onGoBack = { backClicked = true })
    }

    // Assert that the top bar is displayed
    composeRule.onNodeWithTag("REVIEW_IMAGES_SCREEN").assertIsDisplayed()

    // Assert that back button is displayed and clickable
    composeRule.onNodeWithTag("back_button").assertIsDisplayed().performClick()
    assert(backClicked) { "Back button should have triggered onGoBack" }

    // Assert that all images are displayed
    composeRule.onAllNodesWithText("2").apply {
      assert(this.fetchSemanticsNodes().size == photos.size) {
        "Expected ${photos.size} images, found ${this.fetchSemanticsNodes().size}"
      }
    }
  }
}
