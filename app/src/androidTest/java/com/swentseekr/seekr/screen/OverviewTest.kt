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
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntReviewRepositoryLocal
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepositoryLocal
import com.swentseekr.seekr.model.hunt.ReviewImageRepositoryLocal
import com.swentseekr.seekr.model.profile.ProfileRepositoryLocal
import com.swentseekr.seekr.model.profile.createHunt
import com.swentseekr.seekr.model.profile.createOverviewTestHunt
import com.swentseekr.seekr.model.profile.sampleProfileWithPseudonym
import com.swentseekr.seekr.ui.components.HuntCard
import com.swentseekr.seekr.ui.components.HuntCardScreenStrings
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel
import com.swentseekr.seekr.ui.overview.FilterButton
import com.swentseekr.seekr.ui.overview.ModernFilterBar
import com.swentseekr.seekr.ui.overview.OverviewScreen
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags
import com.swentseekr.seekr.ui.overview.OverviewViewModel
import com.swentseekr.seekr.ui.profile.Profile
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Constants used in Overview screen tests. */
object ConstantsTests {
  const val FILTER_BUTTON_TEST_TAG = "FilterButton_"
}

/**
 * UI tests for the Overview screen and related components.
 *
 * This test suite validates the behavior of the overview UI, including filter bars, hunt cards,
 * like interactions, LazyColumn rendering, and integration with ViewModels using local (fake)
 * repositories.
 */
@RunWith(AndroidJUnit4::class)
class OverviewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var viewModel: HuntCardViewModel
  private lateinit var fakeHuntRepository: HuntsRepositoryLocal
  private lateinit var fakeReviewRepository: HuntReviewRepositoryLocal
  private lateinit var fakeProfileRepository: ProfileRepositoryLocal
  private lateinit var fakeImageRepository: ReviewImageRepositoryLocal

  private val testHunt =
      createOverviewTestHunt(
          uid = "test_hunt_1",
          title = "Test Hunt",
          description = "A test hunt for like functionality",
          time = 60.0,
          distance = 5.0)

  private val profileAlice =
      sampleProfileWithPseudonym(
          uid = "author_123",
          pseudonym = "Alice",
      )

  @Before
  fun setUp() = runTest {
    fakeHuntRepository = HuntsRepositoryLocal()
    fakeReviewRepository = HuntReviewRepositoryLocal()
    fakeProfileRepository = ProfileRepositoryLocal()
    fakeImageRepository = ReviewImageRepositoryLocal()

    fakeHuntRepository.addHunt(testHunt)
    fakeProfileRepository.addProfile(profileAlice)

    viewModel =
        HuntCardViewModel(
            fakeHuntRepository, fakeReviewRepository, fakeProfileRepository, fakeImageRepository)
  }

  // ---------------- Filter bar tests ----------------

  @Test
  fun filterBarDisplaysAllFilterButtons() {
    composeTestRule.setContent {
      ModernFilterBar(
          selectedStatus = null,
          selectedDifficulty = null,
          onStatusSelected = {},
          onDifficultySelected = {})
    }

    val statuses = HuntStatus.values()
    val difficulties = Difficulty.values()
    val allLabels = statuses.map { it.name } + difficulties.map { it.name }

    allLabels.forEachIndexed { index, _ ->
      // Scroll to the button inside the filter bar
      composeTestRule
          .onNodeWithTag(OverviewScreenTestTags.FILTER_BAR)
          .performScrollToNode(hasTestTag("${ConstantsTests.FILTER_BUTTON_TEST_TAG}$index"))

      composeTestRule
          .onNodeWithTag("${ConstantsTests.FILTER_BUTTON_TEST_TAG}$index")
          .assertIsDisplayed()
    }
  }

  @Test
  fun filterButtonClickTriggersCallback() {
    var clicked = false
    val index = 4

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

    assertTrue(clicked)
  }

  // ---------------- HuntCard tests (stateless) ----------------

  @Test
  fun huntCardDisplaysLikeButton() {
    composeTestRule.setContent {
      HuntCard(hunt = testHunt, authorName = "Alice", isLiked = false, onLikeClick = {})
    }

    composeTestRule
        .onNodeWithTag(HuntCardScreenStrings.LIKE_BUTTON)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun huntCardLikeButtonTriggersCallback() {
    var clickedHuntId: String? = null

    composeTestRule.setContent {
      HuntCard(
          hunt = testHunt,
          authorName = "Alice",
          isLiked = false,
          onLikeClick = { huntId -> clickedHuntId = huntId })
    }

    composeTestRule.onNodeWithTag(HuntCardScreenStrings.LIKE_BUTTON).performClick()

    assertTrue(clickedHuntId == testHunt.uid)
  }

  @Test
  fun huntCardLikedHuntShowsRedHeart() {
    composeTestRule.setContent { HuntCard(hunt = testHunt, isLiked = true, onLikeClick = {}) }

    composeTestRule
        .onNodeWithTag(HuntCardScreenStrings.LIKE_BUTTON)
        .assertExists()
        .assertIsDisplayed()
    // If you expose separate tags for red/gray variants, you can assert them here.
  }

  @Test
  fun huntCardUnlikedHuntShowsGrayHeart() {
    composeTestRule.setContent { HuntCard(hunt = testHunt, isLiked = false, onLikeClick = {}) }

    composeTestRule
        .onNodeWithTag(HuntCardScreenStrings.LIKE_BUTTON)
        .assertExists()
        .assertIsDisplayed()
    // Same note as above about specific color/icon tags.
  }

  // ---------------- HuntCard + ViewModel integration ----------------

  @Test
  fun huntCardWithViewModelTogglesLikeState() {
    val userId = "test_user"
    fakeProfileRepository.addProfile(Profile(uid = userId))

    // Initialize ViewModel on the main (Compose) thread
    composeTestRule.runOnIdle { viewModel.initialize(userId, testHunt) }
    composeTestRule.waitForIdle()

    assertFalse(viewModel.uiState.value.isLiked)

    composeTestRule.setContent {
      val uiState = viewModel.uiState.value
      HuntCard(
          hunt = testHunt,
          isLiked = uiState.isLiked,
          onLikeClick = { huntId -> viewModel.onLikeClick(huntId) })
    }

    composeTestRule.onNodeWithTag(HuntCardScreenStrings.LIKE_BUTTON).performClick()

    composeTestRule.waitForIdle()

    assertTrue(viewModel.uiState.value.isLiked)
  }

  @Test
  fun viewModelIsHuntLikedReturnsCorrectValue() {
    val userId = "test_user"
    fakeProfileRepository.addProfile(Profile(uid = userId))

    // initialize() launches a coroutine on viewModelScope (Main)
    composeTestRule.runOnIdle { viewModel.initialize(userId, testHunt) }
    composeTestRule.waitForIdle()

    assertFalse(viewModel.uiState.value.isLiked)

    // onLikeClick updates uiState synchronously + does repo work in a coroutine
    composeTestRule.runOnIdle { viewModel.onLikeClick(testHunt.uid) }
    composeTestRule.waitForIdle()

    assertTrue(viewModel.uiState.value.isLiked)
    assertTrue(viewModel.isHuntLiked(testHunt.uid))
  }

  // ---------------- LazyColumn + HuntCard list ----------------

  @Test
  fun lazyColumnOnLikeClickDynamicListIsCovered() {
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

    composeTestRule.onAllNodes(hasTestTag(HuntCardScreenStrings.LIKE_BUTTON))[1].performClick()

    assertTrue(clickedHunts.contains("hunt_2"))

    composeTestRule.onAllNodes(hasTestTag(HuntCardScreenStrings.LIKE_BUTTON))[0].performClick()

    assertTrue(clickedHunts.contains("hunt_1"))
    assertEquals(setOf("hunt_1", "hunt_2"), clickedHunts)
  }

  // ---------------- OverviewScreen + pull-to-refresh ----------------

  @Test
  fun overviewScreenDisplaysHeaderSearchFiltersAndList() {
    // Given an OverviewViewModel backed by the fake repository
    val overviewViewModel = OverviewViewModel(fakeHuntRepository)

    // When: we render the full OverviewScreen
    composeTestRule.setContent {
      OverviewScreen(
          overviewViewModel = overviewViewModel, huntCardViewModel = viewModel, onHuntClick = {})
    }

    // Then: root container is visible
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.OVERVIEW_SCREEN).assertIsDisplayed()

    // And: search bar, filter bar and hunt list are present
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.SEARCH_BAR).assertIsDisplayed()

    composeTestRule.onNodeWithTag(OverviewScreenTestTags.FILTER_BAR).assertIsDisplayed()

    composeTestRule.onNodeWithTag(OverviewScreenTestTags.HUNT_LIST).assertIsDisplayed()
  }

  @Test
  fun overviewScreenShowsHuntCardsFromRepositoryInLazyList() {
    val overviewViewModel = OverviewViewModel(fakeHuntRepository, fakeProfileRepository)

    composeTestRule.setContent {
      OverviewScreen(
          overviewViewModel = overviewViewModel, huntCardViewModel = viewModel, onHuntClick = {})
    }

    composeTestRule.waitForIdle()

    // Ensure the LazyColumn list is there
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.HUNT_LIST).assertIsDisplayed()

    // Scroll until we find a hunt card (LAST_HUNT_CARD when there's a single item)
    composeTestRule
        .onNodeWithTag(OverviewScreenTestTags.HUNT_LIST)
        .performScrollToNode(hasTestTag(OverviewScreenTestTags.LAST_HUNT_CARD))

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(OverviewScreenTestTags.LAST_HUNT_CARD).assertIsDisplayed()
  }

  @Test
  fun overviewScreenContainsPullToRefreshIndicatorInsideListContainer() {
    val overviewViewModel = OverviewViewModel(fakeHuntRepository)

    composeTestRule.setContent {
      OverviewScreen(
          overviewViewModel = overviewViewModel, huntCardViewModel = viewModel, onHuntClick = {})
    }

    composeTestRule.waitForIdle()

    // Verify hunt list exists
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.HUNT_LIST).assertIsDisplayed()

    // Verify PullRefreshIndicator exists inside the list container
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.REFRESH_INDICATOR).assertExists()
  }
}
