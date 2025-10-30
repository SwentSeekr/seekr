package com.swentseekr.seekr.ui.profile

import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.profile.ProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

  private val test_dispatcher = StandardTestDispatcher()
  private lateinit var repository: ProfileRepository
  private lateinit var view_model: EditProfileViewModel

  private val dummy_profile =
      Profile(
          uid = "user123",
          author = Author(pseudonym = "Old Name", bio = "Old bio", profilePicture = 0))

  @Before
  fun setup_test() {
    Dispatchers.setMain(test_dispatcher)
    repository = mockk()
    view_model = EditProfileViewModel(repository)
  }

  @After
  fun teardown_test() {
    Dispatchers.resetMain()
  }

  @Test
  fun load_profile_successfully_sets_ui_state() = runTest {
    coEvery { repository.getProfile("user123") } returns dummy_profile
    view_model.loadProfile("user123")
    test_dispatcher.scheduler.advanceUntilIdle()
    val state = view_model.uiState.first()
    assertEquals("Old Name", state.pseudonym)
    assertEquals("Old bio", state.bio)
    assertEquals(0, state.profilePicture)
    assertFalse(state.isSaving)
    assertFalse(state.hasChanges)
    assertFalse(state.canSave)
    assertNull(state.errorMsg)
  }

  @Test
  fun load_profile_missing_profile_sets_error_msg() = runTest {
    coEvery { repository.getProfile("user123") } returns null
    view_model.loadProfile("user123")
    test_dispatcher.scheduler.advanceUntilIdle()
    val state = view_model.uiState.first()
    assertEquals("Profile not found", state.errorMsg)
  }

  @Test
  fun update_pseudonym_updates_flags() = runTest {
    view_model.loadProfile("user123")
    coEvery { repository.getProfile("user123") } returns dummy_profile
    test_dispatcher.scheduler.advanceUntilIdle()
    view_model.updatePseudonym("New Name")
    val state = view_model.uiState.first()
    assertEquals("New Name", state.pseudonym)
    assertTrue(state.hasChanges)
    assertTrue(state.canSave)
  }

  @Test
  fun update_bio_updates_flags() = runTest {
    view_model.loadProfile("user123")
    coEvery { repository.getProfile("user123") } returns dummy_profile
    test_dispatcher.scheduler.advanceUntilIdle()
    view_model.updateBio("New bio")
    val state = view_model.uiState.first()
    assertEquals("New bio", state.bio)
    assertTrue(state.hasChanges)
    assertTrue(state.canSave)
  }

  @Test
  fun update_profile_picture_updates_flags() = runTest {
    view_model.loadProfile("user123")
    coEvery { repository.getProfile("user123") } returns dummy_profile
    test_dispatcher.scheduler.advanceUntilIdle()
    view_model.updateProfilePicture(123)
    val state = view_model.uiState.first()
    assertEquals(123, state.profilePicture)
    assertTrue(state.hasChanges)
    assertTrue(state.canSave)
  }

  @Test
  fun cancel_changes_reverts_to_last_saved() = runTest {
    coEvery { repository.getProfile("user123") } returns dummy_profile
    view_model.loadProfile("user123")
    test_dispatcher.scheduler.advanceUntilIdle()
    view_model.updatePseudonym("New Name")
    view_model.cancelChanges()
    val state = view_model.uiState.first()
    assertEquals("Old Name", state.pseudonym)
    assertFalse(state.hasChanges)
    assertFalse(state.canSave)
  }

  @Test
  fun save_profile_success_sets_success_flag() = runTest {
    coEvery { repository.getProfile("user123") } returns dummy_profile
    coEvery { repository.updateProfile(any()) } returns Unit
    view_model.loadProfile("user123")
    test_dispatcher.scheduler.advanceUntilIdle()
    view_model.updatePseudonym("New Name")
    view_model.saveProfile()
    test_dispatcher.scheduler.advanceUntilIdle()
    val state = view_model.uiState.first()
    assertTrue(state.success)
    assertFalse(state.isSaving)
    coVerify { repository.updateProfile(match { it.author.pseudonym == "New Name" }) }
  }

  @Test
  fun save_profile_missing_profile_sets_error() = runTest {
    coEvery { repository.getProfile("user123") } returns null
    view_model.loadProfile("user123")
    test_dispatcher.scheduler.advanceUntilIdle()
    view_model.updatePseudonym("New Name")
    view_model.saveProfile()
    test_dispatcher.scheduler.advanceUntilIdle()
    val state = view_model.uiState.first()
    assertFalse(state.success)
    assertEquals("Profile not found", state.errorMsg)
  }

  @Test
  fun save_profile_repository_exception_sets_error() = runTest {
    coEvery { repository.getProfile("user123") } returns dummy_profile
    coEvery { repository.updateProfile(any()) } throws Exception("Firestore error")
    view_model.loadProfile("user123")
    test_dispatcher.scheduler.advanceUntilIdle()
    view_model.updatePseudonym("New Name")
    view_model.saveProfile()
    test_dispatcher.scheduler.advanceUntilIdle()
    val state = view_model.uiState.first()
    assertFalse(state.success)
    assertEquals("Firestore error", state.errorMsg)
  }

  @Test
  fun save_profile_with_invalid_pseudonym_does_not_save() = runTest {
    coEvery { repository.getProfile("user123") } returns dummy_profile
    view_model.loadProfile("user123")
    test_dispatcher.scheduler.advanceUntilIdle()
    view_model.updatePseudonym("")
    view_model.saveProfile()
    test_dispatcher.scheduler.advanceUntilIdle()
    val state = view_model.uiState.first()
    assertFalse(state.canSave)
    coVerify(exactly = 0) { repository.updateProfile(any()) }
  }
}
