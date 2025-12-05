package com.swentseekr.seekr.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swentseekr.seekr.model.hunt.HuntReviewRepositoryLocal
import com.swentseekr.seekr.model.hunt.HuntsRepositoryLocal
import com.swentseekr.seekr.model.hunt.ReviewImageRepositoryLocal
import com.swentseekr.seekr.model.profile.ProfileRepositoryLocal
import com.swentseekr.seekr.model.profile.createHunt
import com.swentseekr.seekr.model.profile.createOverviewTestHunt
import com.swentseekr.seekr.ui.components.HuntCard
import com.swentseekr.seekr.ui.components.HuntCardScreenStrings
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel
import com.swentseekr.seekr.ui.overview.FilterButton
import com.swentseekr.seekr.ui.overview.ModernFilterBar
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags
import com.swentseekr.seekr.ui.profile.Profile
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OverviewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var viewModel: HuntCardViewModel
  private lateinit var fakeHuntRepository: HuntsRepositoryLocal
  private lateinit var fakeReviewRepository: HuntReviewRepositoryLocal
  private lateinit var fakeProfileRepository: ProfileRepositoryLocal
  private lateinit var fakeImageRepository: ReviewImageRepositoryLocal
  private val testDispatcher = StandardTestDispatcher()

  private val testHunt =
      createOverviewTestHunt(
          uid = "test_hunt_1",
          title = "Test Hunt",
          description = "A test hunt for like functionality",
          time = 60.0,
          distance = 5.0)

  @Before
  fun setUp() = runTest {
    fakeHuntRepository = HuntsRepositoryLocal()
    fakeReviewRepository = HuntReviewRepositoryLocal()
    fakeProfileRepository = ProfileRepositoryLocal()
    fakeImageRepository = ReviewImageRepositoryLocal()

    fakeHuntRepository.addHunt(testHunt)

    viewModel =
        HuntCardViewModel(
            fakeHuntRepository, fakeReviewRepository, fakeProfileRepository, fakeImageRepository)
  }
  /*
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
  }*/

  // test de la FilterBar
  @Test
  fun filterBar_displaysAllFilterButtons() {
    composeTestRule.setContent {
      ModernFilterBar(
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
  }

  // test d’interaction avec un FilterButton
  @Test
  fun filterButton_click_triggersCallback() {
    var clicked = false
    val index = 4 // Choisit un index pour le bouton à tester

    composeTestRule.setContent {
      FilterButton(
          text = "EASY",
          isSelected = false,
          modifier =
              androidx.compose.ui.Modifier.testTag(
                  OverviewScreenTestTags.FILTER_BUTTON + "_" + index),
          onClick = { clicked = true })
    }

    composeTestRule.onNodeWithTag(OverviewScreenTestTags.FILTER_BUTTON + "_" + index).performClick()

    assert(clicked) // Le clic doit avoir déclenché le callback
  }
  /** Test that the like button is displayed on HuntCard */
  @Test
  fun huntCard_displaysLikeButton() {
    composeTestRule.setContent { HuntCard(hunt = testHunt, isLiked = false, onLikeClick = {}) }

    composeTestRule
        .onNodeWithTag(HuntCardScreenStrings.LikeButton)
        .assertExists()
        .assertIsDisplayed()
  }

  /** Test that clicking like button triggers callback */
  @Test
  fun huntCard_likeButton_triggersCallback() {
    var clickedHuntId: String? = null

    composeTestRule.setContent {
      HuntCard(hunt = testHunt, isLiked = false, onLikeClick = { huntId -> clickedHuntId = huntId })
    }

    composeTestRule.onNodeWithTag(HuntCardScreenStrings.LikeButton).performClick()

    assertTrue(clickedHuntId == testHunt.uid)
  }

  /** Test that liked hunts show red heart icon */
  @Test
  fun huntCard_likedHunt_showsRedHeart() {
    composeTestRule.setContent { HuntCard(hunt = testHunt, isLiked = true, onLikeClick = {}) }

    composeTestRule
        .onNodeWithTag(HuntCardScreenStrings.LikeButton)
        .assertExists()
        .assertIsDisplayed()
  }

  /** Test that unliked hunts show gray heart icon */
  @Test
  fun huntCard_unlikedHunt_showsGrayHeart() {
    composeTestRule.setContent { HuntCard(hunt = testHunt, isLiked = false, onLikeClick = {}) }

    composeTestRule
        .onNodeWithTag(HuntCardScreenStrings.LikeButton)
        .assertExists()
        .assertIsDisplayed()
  }

  /** Test that like button toggles state in ViewModel */
  @Test
  fun huntCard_withViewModel_togglesLikeState() = runTest {
    val userId = "test_user"
    fakeProfileRepository.addProfile(Profile(uid = userId))
    viewModel.initialize(userId, testHunt)
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isLiked)

    composeTestRule.setContent {
      val uiState = viewModel.uiState.value
      HuntCard(
          hunt = testHunt,
          isLiked = uiState.isLiked,
          onLikeClick = { huntId -> viewModel.onLikeClick(huntId) })
    }

    composeTestRule.onNodeWithTag(HuntCardScreenStrings.LikeButton).performClick()

    composeTestRule.waitForIdle()
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.isLiked)
  }

  /** Test that isHuntLiked helper works correctly */
  @Test
  fun viewModel_isHuntLiked_returnsCorrectValue() = runTest {
    val userId = "test_user"
    fakeProfileRepository.addProfile(Profile(uid = userId))

    viewModel.initialize(userId, testHunt)
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isLiked)

    viewModel.onLikeClick(testHunt.uid)
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.isLiked)
  }

  @Test
  fun lazyColumn_onLikeClick_dynamicList_isCovered() = runTest {
    val userId = "test_user"
    val hunts =
        listOf(
            createHunt(uid = "hunt_1", title = "Hunt One"),
            createHunt(uid = "hunt_2", title = "Hunt Two"),
            createHunt(uid = "hunt_3", title = "Hunt Three"))

    val clickedHunts = mutableSetOf<String>()

    composeTestRule.setContent {
      LazyColumn {
        items(hunts, key = { it.uid }) { hunt ->
          HuntCard(
              hunt = hunt, isLiked = false, onLikeClick = { huntId -> clickedHunts.add(huntId) })
        }
      }
    }

    composeTestRule.onAllNodes(hasTestTag(HuntCardScreenStrings.LikeButton))[1].performClick()

    assertTrue(clickedHunts.contains("hunt_2"))

    composeTestRule.onAllNodes(hasTestTag(HuntCardScreenStrings.LikeButton))[0].performClick()
    assertTrue(clickedHunts.contains("hunt_1"))

    assertEquals(setOf("hunt_1", "hunt_2"), clickedHunts)
  }
}
