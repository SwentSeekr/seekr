package com.swentseekr.seekr.ui.huntcardview

import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntReviewRepositoryLocal
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
  private lateinit var fakeRevRepository: HuntReviewRepositoryLocal
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
    fakeRevRepository = HuntReviewRepositoryLocal() // Not used in these tests

    viewModel = HuntCardViewModel(fakeRepository, fakeRevRepository)

    advanceUntilIdle()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  /** Test that the hunt is correctly loaded */
  @Test
  fun loadHunt_correctly() = runTest {
    viewModel.loadHunt(testHunt.uid)
    advanceUntilIdle()

    val state = viewModel.uiState.value

    assertNotNull(state.hunt)
    assertEquals(testHunt.uid, state.hunt?.uid)
    assertFalse(state.isLiked)
    assertFalse(state.isAchieved)
  }

  /** Test in case of non existing hunt laoded */
  @Test
  fun loadHunt_withInvalidId_logsError() = runTest {
    viewModel.loadHunt("invalid_id")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNull(state.hunt)
    assertFalse(state.isLiked)
    assertFalse(state.isAchieved)
  }

  /** Test that the default state has a hunt not null, no like and not achieved */
  @Test
  fun initialUiState_isDefault() {
    val state = viewModel.uiState.value
    assertNull(state.hunt)
    assertFalse(state.isLiked)
    assertFalse(state.isAchieved)
  }

  /** Test that check that the load Author does not crash not really implemented yet */
  @Test
  fun loadHuntAuthor_withValidId_doesNotCrash() = runTest {
    viewModel.loadHuntAuthor(testHunt.uid)
    advanceUntilIdle()

    assertTrue(true)
  }

  /** est that check that the load Author does not crash not really implemented yet */
  @Test
  fun loadHuntAuthor_withInvalidId_logsError() = runTest {
    viewModel.loadHuntAuthor("invalid_id")
    advanceUntilIdle()

    assertTrue(true)
  }

  /** Test that check the change of the hunt state from dislike to like and from like to dislike */
  @Test
  fun onLikeClick_isLiked() = runTest {
    assertFalse(viewModel.uiState.value.isLiked)

    viewModel.onLikeClick("hunt123")
    assertEquals(true, viewModel.uiState.value.isLiked)

    viewModel.onLikeClick("hunt123")
    assertEquals(false, viewModel.uiState.value.isLiked)
  }

  /** Test that check the change of the hunt state form not achieved to achieved */
  @Test
  fun onDoneClick_sets_isAchieved_to_true() = runTest {
    assertFalse(viewModel.uiState.value.isAchieved)
    viewModel.onDoneClick()
    assertEquals(true, viewModel.uiState.value.isAchieved)
  }

  /** Test that check that the hunt is deleted */
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

  /** Test that check no delete if not valid hunt id */
  @Test
  fun deleteHunt_withInvalidId_logsError() = runTest {
    viewModel.deleteHunt("nonexistent_id")
    advanceUntilIdle()

    // Nothing to assert — just ensures no crash/log handled
  }

  /** Test that check the edit change correctly the hunt */
  @Test
  fun onEditClick() = runTest {
    val newHuntValue = testHunt.copy(title = "Edited Title")
    viewModel.editHunt(testHunt.uid, newHuntValue)
    advanceUntilIdle()

    val updatedHunt = fakeRepository.getHunt(testHunt.uid)
    assertEquals("Edited Title", updatedHunt.title)
    assertEquals(newHuntValue, updatedHunt)
  }

  /** Test that check that there is an error with not a valid hunt */
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
