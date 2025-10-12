package com.swentseekr.seekr.ui

import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.overview.OverviewViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OverviewViewModelTest {

  private lateinit var viewModel: OverviewViewModel
  private lateinit var fakeRepository: HuntsRepositoryLocal
  private val testDispatcher = StandardTestDispatcher()

  private val hunt1 =
      Hunt(
          uid = "1",
          start = Location(1.0, 2.0, "Start1"),
          end = Location(3.0, 4.0, "End1"),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = "Fun Hunt",
          description = "Explore the fun areas",
          time = 1.0,
          distance = 2.0,
          difficulty = Difficulty.EASY,
          author = Author("Alice", "", 1, 1.0, 1.0),
          image = 1,
          reviewRate = 4.0)

  private val hunt2 =
      hunt1.copy(
          uid = "2",
          title = "Sport Challenge",
          description = "A challenging sport hunt",
          status = HuntStatus.SPORT,
          difficulty = Difficulty.DIFFICULT)

  @Before
  fun setUp() = runTest {
    Dispatchers.setMain(testDispatcher)
    fakeRepository = HuntsRepositoryLocal()
    fakeRepository.addHunt(hunt1)
    fakeRepository.addHunt(hunt2)

    viewModel = OverviewViewModel(fakeRepository)

    // Wait briefly to ensure loadHunts completes
    advanceUntilIdle()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
  }

  /**
   * Test that the initial state of the ViewModel loads all hunts from the repository. The fake
   * repository is pre-populated with two hunts in the setUp method.
   */
  @Test
  fun initial_state_loads_hunts_from_repository() = runTest {
    advanceUntilIdle()
    val state = viewModel.uiState.value
    assertEquals(2, state.hunts.size)
    assertTrue(state.hunts.any { it.hunt.uid == "1" })
    assertTrue(state.hunts.any { it.hunt.uid == "2" })
  }

  /**
   * Test that searching for a word that matches one hunt filters the list correctly. Searching for
   * "fun" should match "Fun Hunt".
   */
  @Test
  fun onSearchChange_filters_by_search() = runTest {
    viewModel = OverviewViewModel(fakeRepository)

    advanceUntilIdle()
    viewModel.onSearchChange("fun")
    val state = viewModel.uiState.value
    assertEquals(1, state.hunts.size)
    assertEquals("Fun Hunt", state.hunts[0].hunt.title)
  }

  /** Test that searching for a term that matches no hunts results in an empty hunt list. */
  @Test
  fun onSearchChange_NothingFound() = runTest {
    viewModel = OverviewViewModel(fakeRepository)

    advanceUntilIdle()
    viewModel.onSearchChange("discovery")
    val state = viewModel.uiState.value
    assertEquals(0, state.hunts.size)
  }

  /** Test that clearing the search term resets the hunt list to show all hunts. */
  @Test
  fun onSearchChange_withEmptyString_resetsHuntsList() = runTest {
    viewModel.onSearchChange("")
    val state = viewModel.uiState.value
    assertEquals("", state.searchWord)
    assertEquals(2, state.hunts.size)
  }

  /** Test that clicking the like button toggles the liked status of a hunt. */
  @Test
  fun onLikeClick_toggles_liked_status() = runTest {
    val huntId = "1"
    viewModel.onLikeClick(huntId)
    var state = viewModel.uiState.value
    assertTrue(state.hunts.first { it.hunt.uid == huntId }.isLiked)

    viewModel.onLikeClick(huntId)
    state = viewModel.uiState.value
    assertFalse(state.hunts.first { it.hunt.uid == huntId }.isLiked)
  }

  /**
   * Test that clicking "Achieved" filters the hunt list to only show achieved hunts. This test
   * first marks one hunt as achieved, then simulates the click.
   */
  @Test
  fun onAchivedClick_filters_achieved_hunts() = runTest {
    val updatedList =
        viewModel.uiState.value.hunts.map {
          if (it.hunt.uid == "2") it.copy(isAchived = true) else it
        }

    val internalStateField = OverviewViewModel::class.java.getDeclaredField("huntItems")
    internalStateField.isAccessible = true
    internalStateField.set(viewModel, updatedList.toMutableList())

    viewModel.onAchivedClick()
    val state = viewModel.uiState.value
    assertEquals(1, state.hunts.size)
    assertEquals("2", state.hunts[0].hunt.uid)
  }

  /**
   * Test that clicking "Achieved" when no hunts are achieved results in an empty hunt list. At the
   * start, no hunts are marked as achieved.
   */
  @Test
  fun onAchivedClick_withNoAchievedHunts_returnsEmptyList() = runTest {
    viewModel.onAchivedClick()
    val state = viewModel.uiState.value
    assertTrue(state.hunts.isEmpty())
  }

  /** Test that clearing the search word resets the hunt list to show all hunts after a search. */
  @Test
  fun onClearSearch_resets_filtered_list() = runTest {
    viewModel.onSearchChange("sport")
    val filteredState = viewModel.uiState.value
    assertEquals(1, filteredState.hunts.size)

    viewModel.onClearSearch()
    val resetState = viewModel.uiState.value
    assertEquals(2, resetState.hunts.size)
  }

  /** Test that filtering by status correctly filters the hunt list. */
  @Test
  fun onStatusFilterSelect_filters_by_status() = runTest {
    viewModel.onStatusFilterSelect(HuntStatus.SPORT)
    val state = viewModel.uiState.value
    assertEquals(1, state.hunts.size)
    assertEquals(HuntStatus.SPORT, state.hunts[0].hunt.status)
  }

  /** Test that filtering by difficulty correctly filters the hunt list. */
  @Test
  fun onDifficultyFilterSelect_filters_by_difficulty() = runTest {
    viewModel.onDifficultyFilterSelect(Difficulty.DIFFICULT)
    val state = viewModel.uiState.value
    assertEquals(1, state.hunts.size)
    assertEquals(Difficulty.DIFFICULT, state.hunts[0].hunt.difficulty)
  }
  /**
   * Test that clicking on a hunt does not throw an exception. Navigation is not implemented, so we
   * just ensure no errors occur.
   */
  @Test
  fun onHuntClick_doesNotThrow() {
    viewModel.onHuntClick("some-id")
    // Just ensure no exceptions are thrown
  }

  /**
   * Test that clicking on the icon marker does not throw an exception. Actual navigation is not
   * implemented, so we just ensure no errors occur.
   */
  @Test
  fun onIconMarkerClick_doesNotThrow() {
    viewModel.onIconMarkerClick()
    // Just ensure no exceptions are thrown
  }
}
