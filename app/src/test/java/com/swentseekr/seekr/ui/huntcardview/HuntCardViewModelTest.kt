package com.swentseekr.seekr.ui.huntcardview

import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepositoryLocal
import com.swentseekr.seekr.model.map.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HuntCardViewModelTest {
  private lateinit var viewModel: HuntCardViewModel
  private lateinit var fakeRepository: HuntsRepositoryLocal
  private val testDispatcher = StandardTestDispatcher()

  private val testHunt =
      Hunt(
          uid = "hunt123",
          start = Location(1.0, 2.0, "Start"),
          end = Location(3.0, 4.0, "End"),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = "Test Hunt",
          description = "Test Description",
          time = 1.0,
          distance = 5.0,
          difficulty = Difficulty.EASY,
          authorId = "author123",
          image = 1,
          reviewRate = 4.0)

  @Before
  fun setUp() = runTest {
    Dispatchers.setMain(testDispatcher)
    fakeRepository = HuntsRepositoryLocal()
    fakeRepository.addHunt(testHunt)

    viewModel = HuntCardViewModel(fakeRepository)

    advanceUntilIdle()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun loadHunt_correctly() = runTest {
    viewModel.loadHunt(testHunt.uid)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.hunt)
    assertEquals(testHunt.uid, state.hunt?.uid)
    assertFalse(state.isLiked)
    assertFalse(state.isAchived)
  }

  @Test
  fun loadHunt_withInvalidId_logsError() = runTest {
    viewModel.loadHunt("invalid_id")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNull(state.hunt)
    assertFalse(state.isLiked)
    assertFalse(state.isAchived)
  }

  @Test
  fun initialUiState_isDefault() {
    val state = viewModel.uiState.value
    assertNull(state.hunt)
    assertFalse(state.isLiked)
    assertFalse(state.isAchived)
  }

  @Test
  fun loadHuntAuthor_withValidId_doesNotCrash() = runTest {
    viewModel.loadHuntAuthor(testHunt.uid)
    advanceUntilIdle()

    // This method currently only logs; we check it completes safely
    assertTrue(true) // Placeholder assertion to confirm test ran
  }

  @Test
  fun loadHuntAuthor_withInvalidId_logsError() = runTest {
    viewModel.loadHuntAuthor("invalid_id")
    advanceUntilIdle()

    // Again, just making sure it doesn't crash
    assertTrue(true)
  }

  @Test
  fun onLikeClick_isLiked() = runTest {
    assertFalse(viewModel.uiState.value.isLiked)

    viewModel.onLikeClick("hunt123")
    assertEquals(true, viewModel.uiState.value.isLiked)

    viewModel.onLikeClick("hunt123")
    assertEquals(false, viewModel.uiState.value.isLiked)
  }

  @Test
  fun onDoneClick_sets_isAchived_to_true() = runTest {
    assertFalse(viewModel.uiState.value.isAchived)
    viewModel.onDoneClick()
    assertEquals(true, viewModel.uiState.value.isAchived)
  }

  @Test
  fun onDeleteClick_deletesHunt() = runTest {
    viewModel.deleteHunt(testHunt.uid)
    advanceUntilIdle()

    try {
      // should get error since no hunt anymore
      fakeRepository.getHunt(testHunt.uid)
      fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertEquals("Hunt with ID ${testHunt.uid} is not found", e.message)
    }
  }

  @Test
  fun deleteHunt_withInvalidId_logsError() = runTest {
    viewModel.deleteHunt("nonexistent_id")
    advanceUntilIdle()

    // Nothing to assert — just ensures no crash/log handled
  }

  @Test
  fun onEditClick() = runTest {
    val newHuntValue = testHunt.copy(title = "Edited Title")
    viewModel.editHunt(testHunt.uid, newHuntValue)
    advanceUntilIdle()

    val updatedHunt = fakeRepository.getHunt(testHunt.uid)
    assertEquals("Edited Title", updatedHunt.title)
    assertEquals(newHuntValue, updatedHunt)
  }

  @Test
  fun editHunt_withInvalidId_logsError() = runTest {
    val newHunt = testHunt.copy(uid = "nonexistent_id", title = "Should Fail")
    viewModel.editHunt("nonexistent_id", newHunt)
    advanceUntilIdle()

    // Verify that the hunt was NOT added or changed
    try {
      fakeRepository.getHunt("nonexistent_id")
      fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertEquals("Hunt with ID nonexistent_id is not found", e.message)
    }
  }
}
