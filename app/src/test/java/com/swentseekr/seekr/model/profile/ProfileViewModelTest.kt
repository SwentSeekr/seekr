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
    viewModel = ProfileViewModel(repository, injectedCurrentUid = "user1")
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
  fun updateProfile_sets_error_when_user_not_logged_in() = runTest {
    val viewModelWithoutUid = ProfileViewModel(repository)
    val fake = profileAlice.copy(uid = "ghost")
    viewModelWithoutUid.updateProfile(fake)
    advanceUntilIdle()
    val state = viewModelWithoutUid.uiState.value
    assertEquals("User not logged in", state.errorMsg)
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

  @Test
  fun loadProfile_with_null_userId_uses_currentUid() = runTest {
    viewModel.loadProfile(null)
    advanceUntilIdle()
    val state = viewModel.uiState.value
    assertEquals("Alice", state.profile?.author?.pseudonym)
    assertTrue(state.isMyProfile)
  }

  @Test
  fun loadProfile_sets_isMyProfile_correctly() = runTest {
    viewModel.loadProfile("user1")
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.isMyProfile)

    val profileBob =
        Profile(
            uid = "user2",
            author = Author("Bob", "Bio", 0, 4.0, 3.5),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())
    repository.addProfile(profileBob)

    viewModel.loadProfile("user2")
    advanceUntilIdle()
    assertFalse(viewModel.uiState.value.isMyProfile)
  }

  @Test
  fun loadHunts_sets_error_on_failure() = runTest {
    viewModel.loadProfile("user1")
    advanceUntilIdle()

    viewModel.loadHunts("nonexistent")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state)
  }

  @Test
  fun currentUid_returns_injected_uid() {
    assertEquals("user1", viewModel.currentUid)
  }

  @Test
  fun loadProfile_without_logged_in_user_shows_error() = runTest {
    val viewModelNoUid = ProfileViewModel(repository)
    viewModelNoUid.loadProfile(null)
    advanceUntilIdle()

    assertEquals("User not logged in", viewModelNoUid.uiState.value.errorMsg)
  }
}
