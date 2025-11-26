package com.swentseekr.seekr.ui.review

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.ui.hunt.review.AddReviewScreen
import com.swentseekr.seekr.ui.hunt.review.AddReviewScreenTestTags
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HuntCardReviewScreenTest {
  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun screen_displays_all_elements() {
    composeRule.setContent { MaterialTheme { AddReviewScreen(huntId = "hunt123") } }

    composeRule.onNodeWithTag(AddReviewScreenTestTags.GO_BACK_BUTTON).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.INFO_COLUMN).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.RATING_BAR).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.COMMENT_TEXT_FIELD).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.BUTTONS_ROW).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.CANCEL_BUTTON).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.DONE_BUTTON).assertExists()
  }

  @Test
  fun typing_in_fields_updates_text() {
    composeRule.setContent { MaterialTheme { AddReviewScreen(huntId = "hunt123") } }
    composeRule
        .onNodeWithTag(AddReviewScreenTestTags.COMMENT_TEXT_FIELD)
        .performTextInput("Great hunt!")
  }

  @Test
  fun star_rating_can_be_selected() {
    composeRule.setContent { MaterialTheme { AddReviewScreen(huntId = "hunt123") } }
    val starTag = AddReviewScreenTestTags.starTag(3)
    composeRule.onNodeWithTag(starTag).performClick()
    composeRule.onNodeWithTag(starTag).assertExists()
  }

  @Test
  fun clicking_buttons_triggers_callbacks() {
    var backCalled = false
    var doneCalled = false
    var cancelCalled = false

    composeRule.setContent {
      MaterialTheme {
        AddReviewScreen(
            huntId = "hunt123",
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
        .performTextInput("Hello world")
    composeRule.onNodeWithTag(AddReviewScreenTestTags.DONE_BUTTON).performClick()

    assertTrue(backCalled)
    assertTrue(cancelCalled)
    assertTrue(doneCalled)
  }

  @Test
  fun topAppBar_displays_back_button() {
    composeRule.setContent { MaterialTheme { AddReviewScreen(huntId = "hunt123") } }

    composeRule.onNodeWithTag(AddReviewScreenTestTags.GO_BACK_BUTTON).assertExists()
  }

  @Test
  fun info_column_scrolls_to_bottom() {
    composeRule.setContent { MaterialTheme { AddReviewScreen(huntId = "hunt123") } }

    composeRule.onNodeWithTag(AddReviewScreenTestTags.INFO_COLUMN).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.BUTTONS_ROW).performScrollTo()

    composeRule.onNodeWithTag(AddReviewScreenTestTags.CANCEL_BUTTON).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.DONE_BUTTON).assertExists()

    composeRule.onNodeWithTag(AddReviewScreenTestTags.DONE_BUTTON).performClick()
  }

  /*
  @Test
  fun lazyRow_photosAreDisplayed_andRemoveClickCallsViewModel() {
    // Fake ViewModel with observable list
    val viewModel = ReviewHuntViewModel()

    // Prepopulate photos
    val photoUrls = listOf("photo1", "photo2")
    viewModel.loadReviewImages(photoUrls)

    // Set content
    composeRule.setContent {
      AddReviewScreen(
        huntId = "hunt123",
        reviewViewModel = viewModel,
        onGoBack = {},
        onDone = {},
        onCancel = {}
      )
    }

    // Assert that each photo is displayed
    photoUrls.forEachIndexed { index, _ ->
      composeRule
        .onNodeWithContentDescription(
          "${AddReviewScreenStrings.SelectedImageContentDescriptionPrefix}$index"
        )
        .assertIsDisplayed()
    }

    // Click on the "close" icon of the first photo
    composeRule
      .onNodeWithContentDescription(AddReviewScreenStrings.RemovePhotoContentDescription)
      .performClick()
    composeRule
      .onNodeWithTag("RemovePhoto0")
      .performScrollTo()
      .performClick()

    composeRule.waitForIdle()
    // Assert the photo was removed from ViewModel
    assert(viewModel.uiState.value.photos.size == photoUrls.size - 1)
    assert(!viewModel.uiState.value.photos.contains("photo1"))
  }

   */
}
