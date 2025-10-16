package com.swentseekr.seekr.model.profile

import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.ui.profile.Profile
import com.swentseekr.seekr.ui.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

  private lateinit var repository: ProfileRepositoryLocal
  private lateinit var viewModel: ProfileViewModel
  private val testDispatcher = StandardTestDispatcher()
  private val profileAlice =
      Profile(
          uid = "user1",
          author = Author("Alice", "Bio", 0, 4.0, 3.5),
          myHunts = mutableListOf(),
          doneHunts = mutableListOf(),
          likedHunts = mutableListOf())

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repository = ProfileRepositoryLocal()
    repository.addProfile(profileAlice)
    viewModel = ProfileViewModel(repository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun loadProfile_success_and_failure() = runTest {
    viewModel.loadProfile("user1")
    advanceUntilIdle()
    assertEquals("Alice", viewModel.uiState.value.profile?.author?.pseudonym)
    assertNull(viewModel.uiState.value.errorMsg)

    viewModel.loadProfile("ghost")
    advanceUntilIdle()

    assertEquals("Profile not found", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun loadProfile_sets_error_for_non_existent_profile() = runTest {
    viewModel.loadProfile("ghost")
    advanceUntilIdle()
    val state = viewModel.uiState.value
    assertEquals("Profile not found", state.errorMsg)
    assertNull(state.profile)
  }

  @Test
  fun updateProfile_updates_existing_profile() = runTest {
    val updated = profileAlice.copy(author = profileAlice.author.copy(bio = "Updated Bio"))
    viewModel.updateProfile(updated)
    advanceUntilIdle()
    val state = viewModel.uiState.value
    assertEquals("Updated Bio", state.profile?.author?.bio)
    assertNull(state.errorMsg)
  }

  @Test
  fun updateProfile_sets_error_for_non_existent_profile() = runTest {
    val fake = profileAlice.copy(uid = "ghost")
    viewModel.updateProfile(fake)
    advanceUntilIdle()
    val state = viewModel.uiState.value
    assertEquals("Failed to update profile", state.errorMsg)
  }

  @Test
  fun refreshUIState_reloads_existing_profile() = runTest {
    viewModel.loadProfile("user1")
    advanceUntilIdle()
    viewModel.refreshUIState()
    advanceUntilIdle()
    val state = viewModel.uiState.value
    assertEquals("Alice", state.profile?.author?.pseudonym)
  }
}
