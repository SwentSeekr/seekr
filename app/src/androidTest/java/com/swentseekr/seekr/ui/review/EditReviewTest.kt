package com.swentseekr.seekr.ui.review

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.ui.hunt.review.AddReviewScreenTestTags
import com.swentseekr.seekr.ui.hunt.review.EditReviewScreen
import com.swentseekr.seekr.ui.hunt.review.ReviewHuntUIState
import com.swentseekr.seekr.ui.hunt.review.ReviewHuntViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
      MaterialTheme { EditReviewScreen(huntId = AddReviewTestConstantStings.TestHuntId) }
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
    // Create a fake ViewModel
    val fakeViewModel =
        object : ReviewHuntViewModel() {
          override val uiState =
              MutableStateFlow(
                  ReviewHuntUIState(
                      photos =
                          listOf(
                              AddReviewTestConstantStings.Photo1,
                              AddReviewTestConstantStings.Photo2),
                      rating = 0.0,
                  ))
        }

    composeRule.setContent {
      MaterialTheme {
        EditReviewScreen(
            huntId = AddReviewTestConstantStings.TestHuntId, reviewViewModel = fakeViewModel)
      }
    }

    // Assert that the LazyRow is displayed
    composeRule.onNodeWithTag(AddReviewTestConstantStings.TestTagLazyRow).assertExists()
  }
}
