package com.swentseekr.seekr.ui.huntCardScreen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.FakeReviewHuntViewModel
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

  private fun setHuntContent(
      hunt: Hunt,
      onGoBack: () -> Unit = {},
      beginHunt: () -> Unit = {},
      addReview: () -> Unit = {},
  ) {
    composeTestRule.setContent {
      HuntCardScreen(
          huntId = hunt.uid,
          huntCardViewModel = FakeHuntCardViewModel(hunt),
          onGoBack = onGoBack,
          beginHunt = beginHunt,
          addReview = addReview,
      )
    }
  }

  @Test
  fun testAllUIElementsAreDisplayed() {
    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TestHunt,
          huntCardViewModel = FakeHuntCardViewModel(createFakeHunt()),
          onGoBack = {},
          beginHunt = {},
          addReview = {})
    }

    // Verifies the presence of principal UI elements
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.GO_BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.TITLE_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.AUTHOR_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.IMAGE_PAGER).assertIsDisplayed()
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
          addReview = { reviewClicked = true })
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
      HuntCardScreen(huntId = HuntCardScreenConstantStrings.TestHunt, huntCardViewModel = fakeVm)
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
  fun testReviewsAreDisplayed() {
    val fakeVm = FakeHuntCardViewModel(createFakeHunt())
    val fakeReviewVm = FakeReviewHuntViewModel()

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TestHunt,
          huntCardViewModel = fakeVm,
          reviewViewModel = fakeReviewVm)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onAllNodesWithTag(HuntCardScreenTestTags.REVIEW_CARD).onFirst().assertExists()
  }

  @Test
  fun testAddReviewButtonShownForOtherUsers() {
    val fakeVm =
        FakeHuntCardViewModel(
            hunt = createFakeHunt().copy(authorId = HuntCardScreenConstantStrings.AuthorId))

    composeTestRule.setContent {
      HuntCardScreen(huntId = HuntCardScreenConstantStrings.TestHunt, huntCardViewModel = fakeVm)
    }

    composeTestRule.onNodeWithText(HuntCardScreenConstantStrings.AddReview).assertIsDisplayed()
  }

  @Test
  fun testMapIsVisible() {
    composeTestRule.setContent {
      HuntCardScreen(
          huntId = HuntCardScreenConstantStrings.TestHunt,
          huntCardViewModel = FakeHuntCardViewModel(createFakeHunt()))
    }

    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.MAP_CONTAINER).assertIsDisplayed()
  }

  @Test
  fun huntCardScreen_showsDotsWhenMultipleImages() {
    val huntWithImages =
        createFakeHunt()
            .copy(
                mainImageUrl = HuntCardScreenConstantStrings.MainImageUrlWithDots,
                otherImagesUrls =
                    listOf(
                        HuntCardScreenConstantStrings.OtherImageUrl2WithDots,
                        HuntCardScreenConstantStrings.OtherImageUrl3WithDots,
                    ),
            )

    setHuntContent(hunt = huntWithImages)

    // Carousel & pager
    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.IMAGE_CAROUSEL_CONTAINER)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.IMAGE_PAGER).assertIsDisplayed()

    // Indicator row (because > 1 image)
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.IMAGE_INDICATOR_ROW).assertIsDisplayed()

    // 3 dots (1 main + 2 others)
    (0 until HuntCardScreenConstantNumbers.ImageCount).forEach { index ->
      composeTestRule
          .onNodeWithTag(HuntCardScreenTestTags.IMAGE_INDICATOR_DOT_PREFIX + index)
          .assertIsDisplayed()
    }
  }

  @Test
  fun huntCardScreen_noDotsWhenSingleImage() {
    val singleImageHunt =
        createFakeHunt()
            .copy(
                mainImageUrl = HuntCardScreenConstantStrings.SingleImageUrl,
                otherImagesUrls = emptyList(),
            )

    setHuntContent(hunt = singleImageHunt)

    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.IMAGE_CAROUSEL_CONTAINER)
        .assertIsDisplayed()

    // Indicator row should not exist
    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.IMAGE_INDICATOR_ROW, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun huntCardScreen_tapCenterImageOpensFullscreen() {
    val huntWithImage =
        createFakeHunt()
            .copy(
                mainImageUrl = HuntCardScreenConstantStrings.FullscreenImageUrl,
                otherImagesUrls = emptyList(),
            )

    setHuntContent(hunt = huntWithImage)

    // Click center page (index 0)
    composeTestRule
        .onNodeWithTag(
            HuntCardScreenTestTags.IMAGE_PAGE_PREFIX +
                HuntCardScreenConstantNumbers.FirstImageIndex)
        .assertIsDisplayed()
        .performClick()

    // Full-screen dialog appears
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.IMAGE_FULLSCREEN).assertIsDisplayed()
  }
}
