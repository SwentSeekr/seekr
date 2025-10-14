package com.swentseekr.seekr.screen

import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.ui.overview.FilterBar
import com.swentseekr.seekr.ui.overview.FilterButton
import com.swentseekr.seekr.ui.overview.OverviewScreen
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags
import java.lang.reflect.Modifier
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OverviewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun overviewScreen_displaysSearchBarAndHuntCards() {
    composeTestRule.setContent { OverviewScreen() }

    // Vérifie que la SearchBar est affichée
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.SEARCH_BAR).assertIsDisplayed()

    // Vérifie que les HuntCard sont affichées
    composeTestRule
        .onAllNodes(hasTestTag(OverviewScreenTestTags.HUNT_CARD))
        .assertCountEquals(5) // il y en a 6 au total (la dernière a un tag différent)
  }

  @Test
  fun searchBar_click_changesActiveState() {
    var activeState = false
    composeTestRule.setContent { OverviewScreen(onActiveBar = { activeState = it }) }

    val searchBarNode = composeTestRule.onNodeWithTag(OverviewScreenTestTags.SEARCH_BAR)
    searchBarNode.performClick()

    assert(activeState) // Devrait être true après le clic
  }

  @Test
  fun huntCardScroll_works() {
    composeTestRule.setContent { OverviewScreen() }

    // Scroll jusqu'à la dernière HuntCard
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.HUNT_LIST)
        .performScrollToNode(hasTestTag(OverviewScreenTestTags.LAST_HUNT_CARD))
        .assertIsDisplayed()
  }

  // test de la FilterBar
  @Test
  fun filterBar_displaysAllFilterButtons() {
    composeTestRule.setContent {
      FilterBar(
          selectedStatus = null,
          selectedDifficulty = null,
          onStatusSelected = {},
          onDifficultySelected = {})
    }

    val statuses = com.swentseekr.seekr.model.hunt.HuntStatus.values()
    val difficulties = com.swentseekr.seekr.model.hunt.Difficulty.values()
    val allLabels = statuses.map { it.name } + difficulties.map { it.name }

    // Vérifie chaque bouton individuellement, en scrollant si nécessaire
    allLabels.forEachIndexed { index, label ->
      composeTestRule
          .onNodeWithTag(OverviewScreenTestTags.FILTER_BAR)
          .performScrollToNode(hasTestTag("FilterButton_$index"))

      composeTestRule.onNodeWithTag("FilterButton_$index").assertIsDisplayed()
    }

    // Vérifie le nombre total de boutons
    val totalExpectedButtons = statuses.size + difficulties.size
    /*
    composeTestRule
      .onAllNodes(hasTestTag(OverviewScreenTestTags.FILTER_BUTTON))
      .assertCountEquals(totalExpectedButtons)*/
  }

  // test d’interaction avec un FilterButton
  @Test
  fun filterButton_click_triggersCallback() {
    var clicked = false

    composeTestRule.setContent {
      FilterButton(
          text = "EASY",
          isSelected = false,
          modifier =
              androidx.compose.ui.Modifier.testTag(OverviewScreenTestTags.FILTER_BUTTON + "_4"),
          onClick = { clicked = true })
    }

    composeTestRule.onNodeWithTag(OverviewScreenTestTags.FILTER_BUTTON + "_4").performClick()

    assert(clicked) // Le clic doit avoir déclenché le callback
  }
}
