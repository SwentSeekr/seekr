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

  companion object {
    private const val TEST_USER_ID = "user123"
    private const val OLD_PSEUDONYM = "Old Name"
    private const val OLD_BIO = "Old bio"
    private const val NEW_PSEUDONYM = "New Name"
    private const val VALID_BIO = "Valid bio under 200 chars"
    private const val PROFILE_PICTURE_OLD = 0
    private const val PROFILE_PICTURE_NEW = 123
    private const val REVIEW_RATE = 5.0
    private const val SPORT_RATE = 4.0
    private const val MAX_PSEUDONYM_LENGTH = 30
    private const val MAX_BIO_LENGTH = 200
  }

  private val test_dispatcher = StandardTestDispatcher()
  private lateinit var repository: ProfileRepository
  private lateinit var view_model: EditProfileViewModel

  private val dummy_profile =
      Profile(
          uid = TEST_USER_ID,
          author =
              Author(
                  pseudonym = OLD_PSEUDONYM, bio = OLD_BIO, profilePicture = PROFILE_PICTURE_OLD))

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
    coEvery { repository.getProfile(TEST_USER_ID) } returns dummy_profile
    view_model.loadProfile(TEST_USER_ID)
    test_dispatcher.scheduler.advanceUntilIdle()
    val state = view_model.uiState.first()
    assertEquals(OLD_PSEUDONYM, state.pseudonym)
    assertEquals(OLD_BIO, state.bio)
    assertEquals(PROFILE_PICTURE_OLD, state.profilePicture)
    assertFalse(state.isSaving)
    assertFalse(state.hasChanges)
    assertFalse(state.canSave)
    assertNull(state.errorMsg)
  }

  @Test
  fun load_profile_missing_profile_sets_error_msg() = runTest {
    coEvery { repository.getProfile(TEST_USER_ID) } returns null
    view_model.loadProfile(TEST_USER_ID)
    test_dispatcher.scheduler.advanceUntilIdle()
    val state = view_model.uiState.first()
    assertEquals("Profile not found", state.errorMsg)
  }

  @Test
  fun update_pseudonym_updates_flags() = runTest {
    view_model.loadProfile(TEST_USER_ID)
    coEvery { repository.getProfile(TEST_USER_ID) } returns dummy_profile
    test_dispatcher.scheduler.advanceUntilIdle()
    view_model.updatePseudonym(NEW_PSEUDONYM)
    val state = view_model.uiState.first()
    assertEquals(NEW_PSEUDONYM, state.pseudonym)
    assertTrue(state.hasChanges)
    assertTrue(state.canSave)
  }

  @Test
  fun update_bio_updates_flags() = runTest {
    view_model.loadProfile(TEST_USER_ID)
    coEvery { repository.getProfile(TEST_USER_ID) } returns dummy_profile
    test_dispatcher.scheduler.advanceUntilIdle()
    view_model.updateBio(VALID_BIO)
    val state = view_model.uiState.first()
    assertEquals(VALID_BIO, state.bio)
    assertTrue(state.hasChanges)
    assertTrue(state.canSave)
  }

  @Test
  fun update_profile_picture_updates_flags() = runTest {
    view_model.loadProfile(TEST_USER_ID)
    coEvery { repository.getProfile(TEST_USER_ID) } returns dummy_profile
    test_dispatcher.scheduler.advanceUntilIdle()
    view_model.updateProfilePicture(PROFILE_PICTURE_NEW)
    val state = view_model.uiState.first()
    assertEquals(PROFILE_PICTURE_NEW, state.profilePicture)
    assertTrue(state.hasChanges)
    assertTrue(state.canSave)
  }

  @Test
  fun cancel_changes_reverts_to_last_saved() = runTest {
    coEvery { repository.getProfile(TEST_USER_ID) } returns dummy_profile
    view_model.loadProfile(TEST_USER_ID)
    test_dispatcher.scheduler.advanceUntilIdle()
    view_model.updatePseudonym(NEW_PSEUDONYM)
    view_model.cancelChanges()
    val state = view_model.uiState.first()
    assertEquals(OLD_PSEUDONYM, state.pseudonym)
    assertFalse(state.hasChanges)
    assertFalse(state.canSave)
  }

  @Test
  fun save_profile_success_sets_success_flag() = runTest {
    coEvery { repository.getProfile(TEST_USER_ID) } returns dummy_profile
    coEvery { repository.updateProfile(any()) } returns Unit
    view_model.loadProfile(TEST_USER_ID)
    test_dispatcher.scheduler.advanceUntilIdle()
    view_model.updatePseudonym(NEW_PSEUDONYM)
    view_model.saveProfile()
    test_dispatcher.scheduler.advanceUntilIdle()
    val state = view_model.uiState.first()
    assertTrue(state.success)
    assertFalse(state.isSaving)
    coVerify { repository.updateProfile(match { it.author.pseudonym == NEW_PSEUDONYM }) }
  }

  @Test
  fun save_profile_missing_profile_sets_error() = runTest {
    coEvery { repository.getProfile(TEST_USER_ID) } returns null
    view_model.loadProfile(TEST_USER_ID)
    test_dispatcher.scheduler.advanceUntilIdle()
    view_model.updatePseudonym(NEW_PSEUDONYM)
    view_model.saveProfile()
    test_dispatcher.scheduler.advanceUntilIdle()
    val state = view_model.uiState.first()
    assertFalse(state.success)
    assertEquals("Profile not found", state.errorMsg)
  }

  @Test
  fun save_profile_repository_exception_sets_error() = runTest {
    coEvery { repository.getProfile(TEST_USER_ID) } returns dummy_profile
    coEvery { repository.updateProfile(any()) } throws Exception("Firestore error")
    view_model.loadProfile(TEST_USER_ID)
    test_dispatcher.scheduler.advanceUntilIdle()
    view_model.updatePseudonym(NEW_PSEUDONYM)
    view_model.saveProfile()
    test_dispatcher.scheduler.advanceUntilIdle()
    val state = view_model.uiState.first()
    assertFalse(state.success)
    assertEquals("Firestore error", state.errorMsg)
  }

  @Test
  fun save_profile_with_invalid_pseudonym_does_not_save() = runTest {
    coEvery { repository.getProfile(TEST_USER_ID) } returns dummy_profile
    view_model.loadProfile(TEST_USER_ID)
    test_dispatcher.scheduler.advanceUntilIdle()
    view_model.updatePseudonym("")
    view_model.saveProfile()
    test_dispatcher.scheduler.advanceUntilIdle()
    val state = view_model.uiState.first()
    assertFalse(state.canSave)
    coVerify(exactly = 0) { repository.updateProfile(any()) }
  }

  @Test
  fun save_profile_preserves_ratings_and_hunts() = runTest {
    val fullProfile =
        dummy_profile.copy(
            author = dummy_profile.author.copy(reviewRate = REVIEW_RATE, sportRate = SPORT_RATE),
            myHunts = mutableListOf(mockk()),
            doneHunts = mutableListOf(mockk()),
            likedHunts = mutableListOf(mockk()))

    coEvery { repository.getProfile(TEST_USER_ID) } returns fullProfile
    coEvery { repository.updateProfile(any()) } returns Unit

    view_model.loadProfile(TEST_USER_ID)
    test_dispatcher.scheduler.advanceUntilIdle()

    view_model.updatePseudonym(NEW_PSEUDONYM)
    view_model.saveProfile()
    test_dispatcher.scheduler.advanceUntilIdle()

    coVerify {
      repository.updateProfile(
          match {
            it.author.pseudonym == NEW_PSEUDONYM &&
                it.author.reviewRate == REVIEW_RATE &&
                it.author.sportRate == SPORT_RATE &&
                it.myHunts.size == 1 &&
                it.doneHunts.size == 1 &&
                it.likedHunts.size == 1
          })
    }
  }

  @Test
  fun pseudonym_blank_disables_canSave() = runTest {
    coEvery { repository.getProfile("user123") } returns dummy_profile
    view_model.loadProfile("user123")
    test_dispatcher.scheduler.advanceUntilIdle()

    view_model.updatePseudonym("")
    val state = view_model.uiState.first()
    assertFalse(state.canSave)
  }

  @Test
  fun pseudonym_too_long_disables_canSave() = runTest {
    coEvery { repository.getProfile(TEST_USER_ID) } returns dummy_profile
    view_model.loadProfile(TEST_USER_ID)
    test_dispatcher.scheduler.advanceUntilIdle()

    val longName = "a".repeat(31)
    view_model.updatePseudonym(longName)
    val state = view_model.uiState.first()
    assertFalse(state.canSave)
  }

  @Test
  fun bio_too_long_disables_canSave() = runTest {
    coEvery { repository.getProfile(TEST_USER_ID) } returns dummy_profile
    view_model.loadProfile(TEST_USER_ID)
    test_dispatcher.scheduler.advanceUntilIdle()

    val longBio = "b".repeat(201)
    view_model.updateBio(longBio)
    val state = view_model.uiState.first()
    assertFalse(state.canSave)
  }

  @Test
  fun valid_pseudonym_and_bio_enables_canSave() = runTest {
    coEvery { repository.getProfile(TEST_USER_ID) } returns dummy_profile
    view_model.loadProfile(TEST_USER_ID)
    test_dispatcher.scheduler.advanceUntilIdle()

    view_model.updatePseudonym(NEW_PSEUDONYM)
    view_model.updateBio(VALID_BIO)
    val state = view_model.uiState.first()
    assertTrue(state.canSave)
  }
}
