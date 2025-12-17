package com.swentseekr.seekr.ui.review

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.ui.hunt.review.AddReviewScreenTestTags
import com.swentseekr.seekr.ui.hunt.review.BaseReviewScreen
import com.swentseekr.seekr.ui.review.AddReviewTestConstantStrings.PHOTOS_LAZY_ROW_TEST_TAG
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the BaseReviewScreen composable.
 *
 * This test suite verifies that the base review screen components are displayed correctly and that
 * user interactions, such as selecting a star rating, entering review text, adding photos, and
 * clicking buttons, function as expected.
 */
@RunWith(AndroidJUnit4::class)
class BaseReviewScreenTest {
  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private val fakePhotos = listOf("uri1", "uri2")

  @Test
  fun screen_displays_all_elements() {
    composeRule.setContent {
      MaterialTheme {
        BaseReviewScreen(
            title = "Add Review",
            huntTitle = "Some Hunt",
            authorName = "John",
            reviewText = "",
            onRatingChanged = {},
            onReviewTextChanged = {},
            onAddPhotos = {},
            onRemovePhoto = {},
            onGoBack = {},
            onCancel = {},
            onDone = {})
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
  fun star_rating_can_be_selected() {
    var newRating = 0

    composeRule.setContent {
      MaterialTheme {
        BaseReviewScreen(
            title = "Test",
            huntTitle = "Hunt",
            authorName = "Author",
            reviewText = "",
            onRatingChanged = { newRating = it },
            onReviewTextChanged = {},
            onAddPhotos = {},
            onRemovePhoto = {},
            onGoBack = {},
            onCancel = {},
            onDone = {})
      }
    }
    val starTag = AddReviewScreenTestTags.starTag(3)
    composeRule.onNodeWithTag(starTag).performClick()
    assertTrue(newRating == 3)
    composeRule.onNodeWithTag(starTag).assertExists()
  }

  @Test
  fun clicking_buttons_triggers_callbacks() {
    var backCalled = false
    var doneCalled = false
    var cancelCalled = false

    composeRule.setContent {
      MaterialTheme {
        BaseReviewScreen(
            title = "Test",
            huntTitle = "Hunt",
            authorName = "Author",
            reviewText = "",
            isDoneEnabled = true,
            onRatingChanged = {},
            onReviewTextChanged = {},
            onAddPhotos = {},
            onRemovePhoto = {},
            onGoBack = { backCalled = true },
            onCancel = { cancelCalled = true },
            onDone = { doneCalled = true })
      }
    }

    composeRule.onNodeWithTag(AddReviewScreenTestTags.GO_BACK_BUTTON).performClick()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.CANCEL_BUTTON).performClick()
    val starTag = AddReviewScreenTestTags.starTag(4)
    composeRule.onNodeWithTag(starTag).performClick()
    composeRule
        .onNodeWithTag(AddReviewScreenTestTags.COMMENT_TEXT_FIELD)
        .performTextInput(AddReviewTestConstantStrings.TEXT_INPUT)
    composeRule.onNodeWithTag(AddReviewScreenTestTags.DONE_BUTTON).performClick()

    assertTrue(backCalled)
    assertTrue(cancelCalled)
    assertTrue(doneCalled)
  }

  @Test
  fun clicking_add_photos_calls_callback() {
    var addPhotoCalled = false

    composeRule.setContent {
      MaterialTheme {
        BaseReviewScreen(
            title = "Test",
            huntTitle = "Hunt",
            authorName = "Author",
            reviewText = "",
            onRatingChanged = {},
            onReviewTextChanged = {},
            onAddPhotos = { addPhotoCalled = true },
            onRemovePhoto = {},
            onGoBack = {},
            onCancel = {},
            onDone = {})
      }
    }

    composeRule.onNodeWithTag(AddReviewScreenTestTags.ADD_PHOTO_BUTTON_TEST_TAG).performClick()

    assertTrue(addPhotoCalled)
  }

  @Test
  fun lazyRow_isVisible_when_photos_exist() {
    composeRule.setContent {
      MaterialTheme {
        BaseReviewScreen(
            title = "Test",
            huntTitle = "Hunt",
            authorName = "Author",
            reviewText = "",
            photos = fakePhotos,
            onRatingChanged = {},
            onReviewTextChanged = {},
            onAddPhotos = {},
            onRemovePhoto = {},
            onGoBack = {},
            onCancel = {},
            onDone = {})
      }
    }

    composeRule.onNodeWithTag(PHOTOS_LAZY_ROW_TEST_TAG).assertExists()
  }
}
