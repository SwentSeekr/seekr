package com.swentseekr.seekr

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.ui.components.HuntCardScreen
import com.swentseekr.seekr.ui.components.HuntCardScreenTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HuntCardScreenTest {

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun displaysAllHuntCardScreenElements() {
    composeRule.setContent { HuntCardScreen(onGoBack = {}) }

    // Vérifie la présence de tous les éléments
    composeRule.onNodeWithTag(HuntCardScreenTestTags.GO_BACK_BUTTON).assertIsDisplayed()
    composeRule.onNodeWithTag(HuntCardScreenTestTags.TITLE_TEXT).assertIsDisplayed()
    composeRule.onNodeWithTag(HuntCardScreenTestTags.AUTHOR_TEXT).assertIsDisplayed()
    composeRule.onNodeWithTag(HuntCardScreenTestTags.IMAGE).assertIsDisplayed()
    composeRule.onNodeWithTag(HuntCardScreenTestTags.DIFFICULTY_BOX).assertIsDisplayed()
    composeRule.onNodeWithTag(HuntCardScreenTestTags.DISTANCE_BOX).assertIsDisplayed()
    composeRule.onNodeWithTag(HuntCardScreenTestTags.TIME_BOX).assertIsDisplayed()
    composeRule.onNodeWithTag(HuntCardScreenTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
    composeRule.onNodeWithTag(HuntCardScreenTestTags.MAP_CONTAINER).assertIsDisplayed()
    composeRule.onNodeWithTag(HuntCardScreenTestTags.BEGIN_BUTTON).assertIsDisplayed()
  }

  @Test
  fun backButton_isDisplayedAndClickable() {
    var clicked = false
    composeRule.setContent { HuntCardScreen(onGoBack = { clicked = true }) }

    val backButton = composeRule.onNodeWithTag(HuntCardScreenTestTags.GO_BACK_BUTTON)
    backButton.assertIsDisplayed()
    backButton.performClick()

    assert(clicked) // Vérifie que le callback a été appelé
  }

  @Test
  fun beginHuntButton_isDisplayedAndClickable() {
    composeRule.setContent { HuntCardScreen() }

    val beginButton = composeRule.onNodeWithTag(HuntCardScreenTestTags.BEGIN_BUTTON)
    beginButton.assertIsDisplayed()
    beginButton.performClick() // Pas de callback dans ton code, juste vérifier que ça clique
  }

  @Test
  fun mapContainer_isDisplayed() {
    composeRule.setContent { HuntCardScreen() }

    composeRule
        .onNodeWithTag(HuntCardScreenTestTags.MAP_CONTAINER)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun titleAndAuthorAndDescription_areDisplayed() {
    composeRule.setContent { HuntCardScreen() }

    composeRule.onNodeWithTag(HuntCardScreenTestTags.TITLE_TEXT).assertIsDisplayed()
    composeRule.onNodeWithTag(HuntCardScreenTestTags.AUTHOR_TEXT).assertIsDisplayed()
    composeRule.onNodeWithTag(HuntCardScreenTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
  }

  @Test
  fun difficultyDistanceTimeBoxes_areDisplayed() {
    composeRule.setContent { HuntCardScreen() }

    composeRule.onNodeWithTag(HuntCardScreenTestTags.DIFFICULTY_BOX).assertIsDisplayed()
    composeRule.onNodeWithTag(HuntCardScreenTestTags.DISTANCE_BOX).assertIsDisplayed()
    composeRule.onNodeWithTag(HuntCardScreenTestTags.TIME_BOX).assertIsDisplayed()
  }
}
