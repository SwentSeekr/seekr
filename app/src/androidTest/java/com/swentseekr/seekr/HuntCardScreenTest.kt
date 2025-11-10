package com.swentseekr.seekr

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.ui.components.HuntCardScreen
import com.swentseekr.seekr.ui.components.HuntCardScreenTestTags
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HuntCardScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun reviewCards_areDisplayed() {

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = "hunt123",
          modifier = Modifier,
          huntCardViewModel = HuntCardViewModel(),
          onGoBack = {})
    }
    // Données factices pour les reviews
    val fakeReviews =
        List(10) { index ->
          HuntReview(
              reviewId = "review$index",
              authorId = "author$index",
              huntId = "hunt123",
              rating = 4.0 + (index % 2),
              comment = "This is review number $index",
              photos = emptyList())
        }

    // Vérifie que les 10 ReviewCards sont bien présentes
    composeTestRule.onAllNodesWithTag(HuntCardScreenTestTags.REVIEW_CARD).assertCountEquals(10)
  }

  @Test
  fun allButtonsWork() {
    var goBackClicked = false
    var beginHuntClicked = false
    var addReviewClicked = false

    composeTestRule.setContent {
      HuntCardScreen(
          huntId = "hunt123",
          modifier = Modifier,
          huntCardViewModel = HuntCardViewModel(),
          onGoBack = { goBackClicked = true },
          beginHunt = { beginHuntClicked = true },
          addReview = { addReviewClicked = true })
    }

    // Vérifie que le bouton de retour fonctionne
    val goBackButton = composeTestRule.onNodeWithTag(HuntCardScreenTestTags.GO_BACK_BUTTON)
    goBackButton.assertIsDisplayed()
    goBackButton.performClick()
    assert(goBackClicked)
    // Vérifie que le bouton "Begin Hunt" fonctionne
    val beginHuntButton = composeTestRule.onNodeWithTag(HuntCardScreenTestTags.BEGIN_BUTTON)
    beginHuntButton.assertIsDisplayed()
    beginHuntButton.performClick()
    assert(beginHuntClicked)
    // Vérifie que le bouton "Add Review" fonctionne
    val addReviewButton = composeTestRule.onNodeWithTag(HuntCardScreenTestTags.REVIEW_BUTTON)
    addReviewButton.assertIsDisplayed()
    addReviewButton.performClick()
    assert(addReviewClicked)
  }
}

/*
@Test fun displaysAllHuntCardScreenElements() {
  composeRule.setContent { HuntCardScreen(onGoBack = {}) }
}*/
 /**
  * // Vérifie la présence de tous les éléments
  * composeRule.onNodeWithTag(HuntCardScreenTestTags.GO_BACK_BUTTON).assertIsDisplayed()
  * composeRule.onNodeWithTag(HuntCardScreenTestTags.TITLE_TEXT).assertIsDisplayed()
  * composeRule.onNodeWithTag(HuntCardScreenTestTags.AUTHOR_TEXT).assertIsDisplayed()
  * composeRule.onNodeWithTag(HuntCardScreenTestTags.IMAGE).assertIsDisplayed()
  * composeRule.onNodeWithTag(HuntCardScreenTestTags.DIFFICULTY_BOX).assertIsDisplayed()
  * composeRule.onNodeWithTag(HuntCardScreenTestTags.DISTANCE_BOX).assertIsDisplayed()
  * composeRule.onNodeWithTag(HuntCardScreenTestTags.TIME_BOX).assertIsDisplayed()
  * composeRule.onNodeWithTag(HuntCardScreenTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
  * composeRule.onNodeWithTag(HuntCardScreenTestTags.MAP_CONTAINER).assertIsDisplayed()
  * composeRule.onNodeWithTag(HuntCardScreenTestTags.BEGIN_BUTTON).assertIsDisplayed() }
  *
  * @Test fun backButton_isDisplayedAndClickable() { var clicked = false composeRule.setContent {
  *   HuntCardScreen(onGoBack = { clicked = true }) }
  *
  * val backButton = composeRule.onNodeWithTag(HuntCardScreenTestTags.GO_BACK_BUTTON)
  * backButton.assertIsDisplayed() backButton.performClick()
  *
  * assert(clicked) // Vérifie que le callback a été appelé }
  *
  * @Test fun beginHuntButton_isDisplayedAndClickable() { composeRule.setContent { HuntCardScreen()
  *   }
  *
  * val beginButton = composeRule.onNodeWithTag(HuntCardScreenTestTags.BEGIN_BUTTON)
  * beginButton.assertIsDisplayed() beginButton.performClick() // Pas de callback dans ton code,
  * juste vérifier que ça clique }
  *
  * @Test fun mapContainer_isDisplayed() { composeRule.setContent { HuntCardScreen() }
  *
  * composeRule .onNodeWithTag(HuntCardScreenTestTags.MAP_CONTAINER) .assertExists()
  * .assertIsDisplayed() }
  *
  * @Test fun titleAndAuthorAndDescription_areDisplayed() { composeRule.setContent {
  *   HuntCardScreen() }
  *
  * composeRule.onNodeWithTag(HuntCardScreenTestTags.TITLE_TEXT).assertIsDisplayed()
  * composeRule.onNodeWithTag(HuntCardScreenTestTags.AUTHOR_TEXT).assertIsDisplayed()
  * composeRule.onNodeWithTag(HuntCardScreenTestTags.DESCRIPTION_TEXT).assertIsDisplayed() }
  *
  * @Test fun difficultyDistanceTimeBoxes_areDisplayed() { composeRule.setContent { HuntCardScreen()
  *   }
  *
  * composeRule.onNodeWithTag(HuntCardScreenTestTags.DIFFICULTY_BOX).assertIsDisplayed()
  * composeRule.onNodeWithTag(HuntCardScreenTestTags.DISTANCE_BOX).assertIsDisplayed()
  * composeRule.onNodeWithTag(HuntCardScreenTestTags.TIME_BOX).assertIsDisplayed() } }
  */
