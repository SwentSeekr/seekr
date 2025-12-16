package com.swentseekr.seekr.model.hunt

import com.swentseekr.seekr.ui.hunt.edit.EditHuntViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertFailsWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Shared constants for the `EditHuntViewModelTest`. */
object EditHuntViewModelTestContantsMessage {
  const val FAIL_DELETE = "Failed to delete hunt: Network error"
  const val NO_HUNT_LOADED = "No hunt loaded to delete."
}

/**
 * Unit tests for EditHuntViewModel delete logic.
 *
 * Verifies successful deletion, error handling, and behavior when no hunt is loaded.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EditHuntViewModelTest {

  private val repository: HuntsRepository = mockk(relaxed = true)
  private val viewModel = EditHuntViewModel(repository)

  /** Helper to set the private `huntId` in the ViewModel so we don't need to call `load()`. */
  private fun setHuntId(id: String?) {
    val field = EditHuntViewModel::class.java.getDeclaredField("huntId")
    field.isAccessible = true
    field.set(viewModel, id)
  }

  @Test
  fun deleteCurrentHunt_withLoadedHunt_callsRepositoryAndNoError() = runTest {
    // Given a loaded hunt
    val huntId = "hunt123"
    setHuntId(huntId)

    // delete succeeds
    coEvery { repository.deleteHunt(huntId) } returns Unit

    // When
    viewModel.deleteCurrentHunt()

    // Then
    coVerify(exactly = 1) { repository.deleteHunt(huntId) }
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun deleteCurrentHunt_repositoryThrows_setsErrorMsg() = runTest {
    val huntId = "hunt123"
    setHuntId(huntId)

    coEvery { repository.deleteHunt(huntId) } throws Exception("Network error")

    viewModel.deleteCurrentHunt()

    val state = viewModel.uiState.value
    assertEquals(EditHuntViewModelTestContantsMessage.FAIL_DELETE, state.errorMsg)
  }

  @Test
  fun deleteCurrentHunt_withoutLoadedHunt_throws() = runTest {
    // No hunt loaded → huntId = null
    setHuntId(null)

    val ex = assertFailsWith<IllegalArgumentException> { viewModel.deleteCurrentHunt() }
    assertEquals(EditHuntViewModelTestContantsMessage.NO_HUNT_LOADED, ex.message)
  }
}
