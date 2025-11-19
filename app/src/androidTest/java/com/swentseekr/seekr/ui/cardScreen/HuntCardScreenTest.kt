package com.swentseekr.seekr.ui.cardScreen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.components.HuntCardScreen
import com.swentseekr.seekr.ui.components.HuntCardScreenTestTags
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HuntCardScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun createFakeHunt() =
      Hunt(
          uid = "hunt123",
          start = Location(40.7128, -74.0060, "New York"),
          end = Location(40.730610, -73.935242, "Brooklyn"),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = "City Exploration",
          description = "Discover hidden gems in the city",
          time = 2.5,
          distance = 5.0,
          difficulty = Difficulty.DIFFICULT,
          authorId = "0",
          otherImagesUrls = emptyList(),
          mainImageUrl = "",
          reviewRate = 4.5)

  @Test
  fun testAllUIElementsAreDisplayed() {
    composeTestRule.setContent {
      HuntCardScreen(
          huntId = "hunt123",
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
    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.IMAGE_CAROUSEL_CONTAINER)
        .assertIsDisplayed()
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
          huntId = "hunt123",
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
    TestCase.assertTrue(goBackClicked)
    TestCase.assertTrue(beginClicked)
    TestCase.assertTrue(reviewClicked)
  }

  @Test
  fun huntCardScreen_showsDotsWhenMultipleImages() {
    val huntWithImages =
        createFakeHunt()
            .copy(
                mainImageUrl = "https://example.com/example1.jpg",
                otherImagesUrls =
                    listOf(
                        "https://example.com/example2.jpg",
                        "https://example.com/example3.jpg",
                    ),
            )

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = huntWithImages.uid,
          huntCardViewModel = FakeHuntCardViewModel(huntWithImages),
          onGoBack = {},
          beginHunt = {},
          addReview = {},
          testmode = true,
      )
    }

    // Carousel & pager
    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.IMAGE_CAROUSEL_CONTAINER)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.IMAGE_PAGER).assertIsDisplayed()

    // Indicator row (because > 1 image)
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.IMAGE_INDICATOR_ROW).assertIsDisplayed()

    // 3 dots (1 main + 2 others)
    (0 until 3).forEach { index ->
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
                mainImageUrl = "https://example.com/example1.jpg",
                otherImagesUrls = emptyList(),
            )

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = singleImageHunt.uid,
          huntCardViewModel = FakeHuntCardViewModel(singleImageHunt),
          onGoBack = {},
          beginHunt = {},
          addReview = {},
          testmode = true,
      )
    }

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
                mainImageUrl = "https://example.com/example1.jpg",
                otherImagesUrls = emptyList(),
            )

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = huntWithImage.uid,
          huntCardViewModel = FakeHuntCardViewModel(huntWithImage),
          onGoBack = {},
          beginHunt = {},
          addReview = {},
          testmode = true,
      )
    }

    // Click center page (index 0)
    composeTestRule
        .onNodeWithTag(HuntCardScreenTestTags.IMAGE_PAGE_PREFIX + 0)
        .assertIsDisplayed()
        .performClick()

    // Full-screen dialog appears
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.IMAGE_FULLSCREEN).assertIsDisplayed()
  }
}
