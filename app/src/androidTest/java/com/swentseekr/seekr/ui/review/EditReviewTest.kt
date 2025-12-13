package com.swentseekr.seekr.ui.review

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.ui.hunt.review.AddReviewScreenTestTags
import com.swentseekr.seekr.ui.hunt.review.BaseReviewScreen
import com.swentseekr.seekr.ui.hunt.review.EditReviewScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditReviewScreenTest {
  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()
  // Test
  @Test
  fun screen_displays_all_elements() {
    composeRule.setContent {
      MaterialTheme {
        EditReviewScreen(huntId = AddReviewTestConstantStings.TestHuntId, reviewId = "")
      }
    }

    composeRule.onNodeWithTag(AddReviewScreenTestTags.GO_BACK_BUTTON).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.INFO_COLUMN).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.RATING_BAR).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.COMMENT_TEXT_FIELD).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.BUTTONS_ROW).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.CANCEL_BUTTON).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.DONE_BUTTON).assertExists()
  }

  @Test
  fun clicking_buttons_triggers_callbacks() {
    var backCalled = false
    var doneCalled = false
    var cancelCalled = false

    composeRule.setContent {
      MaterialTheme {
        EditReviewScreen(
            huntId = AddReviewTestConstantStings.TestHuntId,
            reviewId = "",
            onGoBack = { backCalled = true },
            onDone = { doneCalled = true },
            onCancel = { cancelCalled = true })
      }
    }

    composeRule.onNodeWithTag(AddReviewScreenTestTags.GO_BACK_BUTTON).performClick()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.CANCEL_BUTTON).performClick()
    val starTag = AddReviewScreenTestTags.starTag(4)
    composeRule.onNodeWithTag(starTag).performClick()
    composeRule
        .onNodeWithTag(AddReviewScreenTestTags.COMMENT_TEXT_FIELD)
        .performTextInput(AddReviewTestConstantStings.TextInput)
    composeRule.onNodeWithTag(AddReviewScreenTestTags.DONE_BUTTON).performClick()

    assertTrue(backCalled)
    assertTrue(cancelCalled)
    assertTrue(doneCalled)
  }

  @Test
  fun photos_lazyRow_isDisplayed_whenPhotosExist() {
    composeRule.setContent {
      MaterialTheme {
        // Use BaseReviewScreen directly to control the state
        BaseReviewScreen(
            title = "Edit Review",
            huntTitle = "Test Hunt",
            authorName = "Test Author",
            rating = 3.0,
            reviewText = "Great hunt!",
            photos = listOf(AddReviewTestConstantStings.Photo1, AddReviewTestConstantStings.Photo2),
            isReviewTextError = false,
            isDoneEnabled = true,
            reviewTextErrorMessage = null,
            onRatingChanged = {},
            onReviewTextChanged = {},
            onAddPhotos = {},
            onRemovePhoto = {},
            onGoBack = {},
            onCancel = {},
            onDone = {})
      }
    }

    // Assert that the LazyRow is displayed
    composeRule.onNodeWithTag(AddReviewScreenTestTags.PHOTOS_LAZY_ROW_TAG).assertExists()
  }

  @Test
  fun addPhotoButton_isDisplayed_whenPhotosEmpty() {
    composeRule.setContent {
      MaterialTheme {
        BaseReviewScreen(
            title = "Edit Review",
            huntTitle = "Test Hunt",
            authorName = "Test Author",
            rating = 0.0,
            reviewText = "",
            photos = emptyList(), // No photos
            isReviewTextError = false,
            isDoneEnabled = false,
            reviewTextErrorMessage = null,
            onRatingChanged = {},
            onReviewTextChanged = {},
            onAddPhotos = {},
            onRemovePhoto = {},
            onGoBack = {},
            onCancel = {},
            onDone = {})
      }
    }

    // Assert that the Add Photos button is displayed instead of LazyRow
    composeRule.onNodeWithTag(AddReviewScreenTestTags.ADD_PHOTO_BUTTON_TEST_TAG).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.PHOTOS_LAZY_ROW_TAG).assertDoesNotExist()
  }
}
