package com.swentseekr.seekr

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.components.HuntCardScreen
import com.swentseekr.seekr.ui.components.HuntCardScreenTestTags
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
          image = R.drawable.ic_launcher_foreground,
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

    // Vérifie la présence des éléments principaux
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.GO_BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.TITLE_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.AUTHOR_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.IMAGE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.DIFFICULTY_BOX).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.DISTANCE_BOX).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.TIME_BOX).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.MAP_CONTAINER).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.BEGIN_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.REVIEW_BUTTON).assertIsDisplayed()
    composeTestRule.onAllNodesWithTag(HuntCardScreenTestTags.REVIEW_CARD).assertCountEquals(10)
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

    // Clique sur les boutons
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.GO_BACK_BUTTON).performClick()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.BEGIN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(HuntCardScreenTestTags.REVIEW_BUTTON).performClick()

    // Vérifie les callbacks
    assert(goBackClicked)
    assert(beginClicked)
    assert(reviewClicked)
  }
}
