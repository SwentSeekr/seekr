package com.swentseekr.seekr.ui

import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.overview.OverviewViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.coroutines.test.runTest
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
          authorId = "0",
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

    advanceUntilIdle()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  /** * Test that the initial state of the ViewModel loads hunts from the repository correctly. */
  @Test
  fun initial_state_loads_hunts_from_repository() = runTest {
    advanceUntilIdle()
    val state = viewModel.uiState.value
    assertEquals(2, state.hunts.size)
    assertTrue(state.hunts.any { it.hunt.uid == "1" })
    assertTrue(state.hunts.any { it.hunt.uid == "2" })
  }

  /** Test that changing the search term filters the hunts correctly based on the search input. */
  @Test
  fun onSearchChange_filters_by_search() = runTest {
    viewModel = OverviewViewModel(fakeRepository)

    advanceUntilIdle()
    viewModel.onSearchChange("fun")
    val state = viewModel.uiState.value
    assertEquals(1, state.hunts.size)
    assertEquals("Fun Hunt", state.hunts[0].hunt.title)
  }

  /** Test that clicking the like button toggles the liked status of a hunt item. */
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

  /** Test that clearing the search resets the filtered list to show all hunts. */
  @Test
  fun onClearSearch_resets_filtered_list() = runTest {
    viewModel.onSearchChange("sport")
    val filteredState = viewModel.uiState.value
    assertEquals(1, filteredState.hunts.size)

    viewModel.onClearSearch()
    val resetState = viewModel.uiState.value
    assertEquals(2, resetState.hunts.size)
  }

  /** Test that selecting a status filter correctly filters the hunts by the selected status. */
  @Test
  fun onStatusFilterSelect_filters_by_status() = runTest {
    viewModel.onStatusFilterSelect(HuntStatus.SPORT)
    val state = viewModel.uiState.value
    assertEquals(1, state.hunts.size)
    assertEquals(HuntStatus.SPORT, state.hunts[0].hunt.status)
  }

  /**
   * Test that selecting a difficulty filter correctly filters the hunts by the selected difficulty.
   */
  @Test
  fun onDifficultyFilterSelect_filters_by_difficulty() = runTest {
    viewModel.onDifficultyFilterSelect(Difficulty.DIFFICULT)
    val state = viewModel.uiState.value
    assertEquals(1, state.hunts.size)
    assertEquals(Difficulty.DIFFICULT, state.hunts[0].hunt.difficulty)
  }
}
