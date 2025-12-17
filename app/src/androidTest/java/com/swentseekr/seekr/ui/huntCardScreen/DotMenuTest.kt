package com.swentseekr.seekr.ui.huntCardScreen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.swentseekr.seekr.FakeReviewHuntViewModel
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.ui.components.DotMenu
import com.swentseekr.seekr.ui.components.HuntCardScreenTestTags
import com.swentseekr.seekr.ui.components.ModernReviewCard
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DotMenuTest {

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun dotMenuOpensAndTriggersEdit() {
    var editClicked = false

    composeRule.setContent { DotMenu(onEdit = { editClicked = true }, onDelete = {}) }

    // Open menu
    composeRule.onNodeWithTag(HuntCardScreenTestTags.DOTBUTOON).performClick()

    // Click Edit
    composeRule.onNodeWithTag(HuntCardScreenTestTags.EDIT_BUTTON).assertIsDisplayed().performClick()

    assertTrue(editClicked)
  }

  @Test
  fun dotMenuOpensAndTriggersDelete() {
    var deleteClicked = false

    composeRule.setContent { DotMenu(onEdit = {}, onDelete = { deleteClicked = true }) }

    // Open menu
    composeRule.onNodeWithTag(HuntCardScreenTestTags.DOTBUTOON).performClick()

    // Click Delete
    composeRule
        .onNodeWithTag(HuntCardScreenTestTags.DELETE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    assertTrue(deleteClicked)
  }

  @Test
  fun dotMenuOpenAndTriggersEdit() {
    var editClicked = false
    composeRule.setContent { DotMenu(onEdit = { editClicked = true }, onDelete = {}) }

    // Open menu
    composeRule.onNodeWithTag(HuntCardScreenTestTags.DOTBUTOON).performClick()

    // Click Edit button
    composeRule.onNodeWithTag(HuntCardScreenTestTags.EDIT_BUTTON).assertIsDisplayed().performClick()

    assertTrue(editClicked)
  }

  @Test
  fun dotMenuInReviewCardHeaderForCurrentUserTriggersEditAndDelete() {
    var editClicked = false
    var deleteClicked = false

    val review =
        HuntReview(
            reviewId = "review1",
            huntId = "hunt1",
            authorId = "user1",
            comment = "Great hunt!",
            rating = 5.0,
            photos = emptyList())

    composeRule.setContent {
      ModernReviewCard(
          review = review,
          reviewHuntViewModel = FakeReviewHuntViewModel(),
          currentUserId = review.authorId,
          navController = rememberNavController(),
          onDeleteReview = { deleteClicked = true },
          onEdit = { editClicked = true },
          authorProfile = null)
    }

    // Open menu
    composeRule.onNodeWithTag(HuntCardScreenTestTags.DOTBUTOON).performClick()

    // Click Edit
    composeRule.onNodeWithTag(HuntCardScreenTestTags.EDIT_BUTTON).performClick()
    assertTrue(editClicked)

    // Open menu again
    composeRule.onNodeWithTag(HuntCardScreenTestTags.DOTBUTOON).performClick()

    // Click Delete
    composeRule.onNodeWithTag(HuntCardScreenTestTags.DELETE_BUTTON).performClick()
    assertTrue(deleteClicked)
  }
}
