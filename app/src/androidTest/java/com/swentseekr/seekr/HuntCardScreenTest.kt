package com.swentseekr.seekr

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.components.HuntCardScreen
import com.swentseekr.seekr.ui.components.HuntCardScreenTestTags
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HuntCardScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun createFakeHunt() =
      Hunt(
          uid = HuntCardScreenConstantStrings.TestHunt,
          start =
              Location(
                  HuntCardScreenConstantNumbers.Location1,
                  HuntCardScreenConstantNumbers.Location2,
                  HuntCardScreenConstantStrings.Name1),
          end =
              Location(
                  HuntCardScreenConstantNumbers.Location3,
                  HuntCardScreenConstantNumbers.Location4,
                  HuntCardScreenConstantStrings.Name2),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = HuntCardScreenConstantStrings.Title,
          description = HuntCardScreenConstantStrings.Description,
          time = HuntCardScreenConstantNumbers.Time,
          distance = HuntCardScreenConstantNumbers.Distance,
          difficulty = Difficulty.DIFFICULT,
          authorId = HuntCardScreenConstantStrings.AuthorId,
          otherImagesUrls = emptyList(),
          mainImageUrl = "",
          reviewRate = HuntCardScreenConstantNumbers.ReviewRate)

  @Test
  fun testAllUIElementsAreDisplayed() {
    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TestHunt,
          huntCardViewModel = FakeHuntCardViewModel(createFakeHunt()),
          onGoBack = {},
          beginHunt = {},
          addReview = {},
          testmode = true)
    }

    // Verifies the presence of principal UI elements
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.GO_BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.TITLE_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.AUTHOR_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.IMAGE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.MAP_CONTAINER).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.BEGIN_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.REVIEW_BUTTON).assertIsDisplayed()
  }

  @Test
  fun testButtonsTriggerCallbacks() {
    var goBackClicked = false
    var beginClicked = false
    var reviewClicked = false

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TestHunt,
          huntCardViewModel = FakeHuntCardViewModel(createFakeHunt()),
          onGoBack = { goBackClicked = true },
          beginHunt = { beginClicked = true },
          addReview = { reviewClicked = true },
          testmode = true)
    }

    // Click on boutons
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.GO_BACK_BUTTON).performClick()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.BEGIN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.REVIEW_BUTTON).performClick()

    // Verifies callbacks
    assertTrue(goBackClicked)
    assertTrue(beginClicked)
    assertTrue(reviewClicked)
  }

  @Test
  fun testLikeButtonTogglesState() {
    val fakeVm = FakeHuntCardViewModel(createFakeHunt())

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TestHunt,
          huntCardViewModel = fakeVm,
          testmode = true)
    }

    // Initially: not liked
    assertTrue(!fakeVm.uiState.value.isLiked)

    // Click like
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.LIKE_BUTTON).performClick()

    // ViewModel should now show liked = true
    assertTrue(fakeVm.uiState.value.isLiked)

    // Click again → unlike
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.LIKE_BUTTON).performClick()

    assertTrue(!fakeVm.uiState.value.isLiked)
  }

  @Test
  fun testAddReviewButtonShownForOtherUsers() {
    val fakeVm =
        FakeHuntCardViewModel(
            hunt = createFakeHunt().copy(authorId = HuntCardScreenConstantStrings.AuthorId))

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TestHunt,
          huntCardViewModel = fakeVm,
          testmode = true)
    }

    composeTestRule.onNodeWithText(HuntCardScreenConstantStrings.AddReview).assertIsDisplayed()
  }

  @Test
  fun testMapIsVisible() {
    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TestHunt,
          huntCardViewModel = FakeHuntCardViewModel(createFakeHunt()),
          testmode = true)
    }

    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.MAP_CONTAINER).assertIsDisplayed()
  }

  @Test
  fun testReviewsAreDisplayed() {
    val fakeVm = FakeHuntCardViewModel(createFakeHunt())
    val fakeReviewVm = FakeReviewHuntViewModel() // you need to create this

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TestHunt,
          huntCardViewModel = fakeVm,
          reviewViewModel = fakeReviewVm,
          testmode = true)
    }

    composeTestRule.waitForIdle()

    // composeTestRule.onNodeWithTag(HuntCardScreenTestTags.REVIEW_CARD).assertExists()
    composeTestRule.onAllNodesWithTag(HuntCardScreenTestTags.REVIEW_CARD).onFirst().assertExists()
  }
}
