package com.swentseekr.seekr.ui.review

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.ui.huntcardview.AddReviewScreen
import com.swentseekr.seekr.ui.huntcardview.AddReviewScreenTestTags
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
    composeRule.onNodeWithTag(AddReviewScreenTestTags.INFO_COLLUMN).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.RATE_TEXTFIELD).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.COMMENT_TEXTFIELD).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.BUTTONS_ROW).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.CANCEL_BUTTON).assertExists()
    composeRule.onNodeWithTag(AddReviewScreenTestTags.DONE_BUTTON).assertExists()
  }

  @Test
  fun typing_in_fields_updates_text() {
    composeRule.setContent { MaterialTheme { AddReviewScreen(huntId = "hunt123") } }

    composeRule.onNodeWithTag(AddReviewScreenTestTags.RATE_TEXTFIELD).performTextInput("4.5")
    composeRule
        .onNodeWithTag(AddReviewScreenTestTags.COMMENT_TEXTFIELD)
        .performTextInput("Great hunt!")
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

    composeRule.onNodeWithTag(AddReviewScreenTestTags.RATE_TEXTFIELD).performTextInput("4")

    composeRule.onNodeWithTag(AddReviewScreenTestTags.DONE_BUTTON).performClick()

    assertTrue(backCalled)
    assertTrue(cancelCalled)
    assertTrue(doneCalled)
  }
}
