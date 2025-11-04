package com.swentseekr.seekr

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.ui.components.HuntCardScreenTestTags
import com.swentseekr.seekr.ui.components.ReviewCard
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HuntCardScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun reviewCards_areDisplayed() {
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

    // On affiche directement les ReviewCard dans le composable HuntCardScreen
    composeTestRule.setContent {
      // On peut simuler un état où les reviews sont déjà chargées
      fakeReviews.forEach { review -> ReviewCard(review) }
    }

    // Vérifie que les 10 ReviewCards sont bien présentes
    composeTestRule.onAllNodesWithTag(HuntCardScreenTestTags.REVIEW_CARD).assertCountEquals(10)
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
