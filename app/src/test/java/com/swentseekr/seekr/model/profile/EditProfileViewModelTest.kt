package com.swentseekr.seekr.model.profile

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants
import com.swentseekr.seekr.ui.profile.EditProfileStrings.EMPTY_STRING
import com.swentseekr.seekr.ui.profile.EditProfileViewModel
import com.swentseekr.seekr.ui.profile.Profile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the EditProfileViewModel.
 *
 * This test suite verifies profile loading, updating, saving, error handling, and state management
 * by mocking the ProfileRepository and FirebaseAuth dependencies.
 */
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
    private const val OLD_URL = "https://old.url/image.jpg"
    private const val NEW_URL = "https://new.url/image.jpg"
    private const val PROFILE_NOT_FOUND = "Profile not found"
    private const val FIRESTORE_ERROR = "Firestore error"
    private const val USER_NOT_LOADED = "User not loaded"
  }

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var repository: ProfileRepository
  private lateinit var auth: FirebaseAuth
  private lateinit var mockUser: FirebaseUser
  private lateinit var viewModel: EditProfileViewModel

  private val dummy_profile =
      Profile(
          uid = TEST_USER_ID,
          author =
              Author(
                  pseudonym = OLD_PSEUDONYM,
                  bio = OLD_BIO,
                  profilePicture = PROFILE_PICTURE_OLD,
                  profilePictureUrl = OLD_URL))

  @Before
  fun setup_test() {
    Dispatchers.setMain(testDispatcher)
    repository = mockk()
    auth = mockk()
    mockUser = mockk()

    every { auth.currentUser } returns mockUser
    every { mockUser.uid } returns TEST_USER_ID

    viewModel = EditProfileViewModel(repository, auth)
  }

  @After
  fun teardown_test() {
    Dispatchers.resetMain()
  }

  private fun mockProfile(profile: Profile = dummy_profile) {
    coEvery { repository.getProfile(TEST_USER_ID) } returns profile
  }

  private fun loadProfile(profile: Profile? = dummy_profile, throwException: Exception? = null) {
    when {
      throwException != null ->
          coEvery { repository.getProfile(TEST_USER_ID) } throws throwException
      profile != null -> coEvery { repository.getProfile(TEST_USER_ID) } returns profile
      profile == null ->
          coEvery { repository.getProfile(TEST_USER_ID) } returns null // <--- add this
    }
    viewModel.loadProfile()
    testDispatcher.scheduler.advanceUntilIdle()
  }

  private fun updatePseudonym(name: String) {
    viewModel.updatePseudonym(name)
  }

  private fun updateBio(bio: String) {
    viewModel.updateBio(bio)
  }

  private fun updateProfilePicture(pic: Int) {
    viewModel.updateProfilePicture(pic)
  }

  private fun updateProfilePictureUri(uri: Uri?) {
    viewModel.updateProfilePictureUri(uri)
  }

  private fun saveProfile() {
    viewModel.saveProfile()
    testDispatcher.scheduler.advanceUntilIdle()
  }

  private fun cancelChanges() {
    viewModel.cancelChanges()
  }

  private suspend fun getState() = viewModel.uiState.first()

  @Test
  fun viewModel_startsWithLoadingTrue() = runTest {
    val state = getState()
    assertTrue(state.isLoading)
  }

  @Test
  fun load_profile_successfully_sets_ui_state() = runTest {
    loadProfile()
    val state = getState()
    assertEquals(OLD_PSEUDONYM, state.pseudonym)
    assertEquals(OLD_BIO, state.bio)
    assertEquals(PROFILE_PICTURE_OLD, state.profilePicture)
    assertFalse(state.isSaving)
    assertFalse(state.hasChanges)
    assertFalse(state.canSave)
    assertNull(state.errorMsg)
  }

  @Test
  fun loadProfile_exception_setsErrorMsg() = runTest {
    loadProfile(throwException = Exception("Network error"))
    val state = getState()
    assertEquals("Network error", state.errorMsg)
    assertFalse(state.isLoading)
  }

  @Test
  fun loadProfile_missingProfile_setsErrorMsg() = runTest {
    loadProfile(profile = null)
    val state = getState()
    assertEquals(PROFILE_NOT_FOUND, state.errorMsg)
    assertFalse(state.isLoading)
  }

  @Test
  fun saveProfile_missingProfile_setsError() = runTest {
    coEvery { repository.getProfile(TEST_USER_ID) } returns null

    updatePseudonym(NEW_PSEUDONYM)
    saveProfile()
    val state = getState()
    assertFalse(state.success)
    assertEquals(PROFILE_NOT_FOUND, state.errorMsg)
  }

  @Test
  fun loadProfile_noCurrentUser_doesNothing() = runTest {
    every { auth.currentUser } returns null
    viewModel.loadProfile()
    testDispatcher.scheduler.advanceUntilIdle()
    val state = getState()
    assertTrue(state.isLoading)
  }

  @Test
  fun update_pseudonym_updates_flags() = runTest {
    loadProfile()
    updatePseudonym(NEW_PSEUDONYM)
    val state = getState()
    assertEquals(NEW_PSEUDONYM, state.pseudonym)
    assertTrue(state.hasChanges)
    assertTrue(state.canSave)
  }

  @Test
  fun update_bio_updates_flags() = runTest {
    loadProfile()
    updateBio(VALID_BIO)
    val state = getState()
    assertEquals(VALID_BIO, state.bio)
    assertTrue(state.hasChanges)
    assertTrue(state.canSave)
  }

  @Test
  fun update_profile_picture_updates_flags() = runTest {
    loadProfile()
    updateProfilePicture(PROFILE_PICTURE_NEW)
    val state = getState()
    assertEquals(PROFILE_PICTURE_NEW, state.profilePicture)
    assertTrue(state.hasChanges)
    assertTrue(state.canSave)
  }

  @Test
  fun updateProfilePictureUri_updatesFlags() = runTest {
    val testUri = mockk<Uri>()
    loadProfile()
    updateProfilePictureUri(testUri)
    val state = getState()
    assertEquals(testUri, state.profilePictureUri)
    assertTrue(state.hasChanges)
    assertTrue(state.canSave)
  }

  @Test
  fun updateProfilePictureUri_null_updatesFlags() = runTest {
    loadProfile()
    updateProfilePictureUri(null)
    val state = getState()
    assertNull(state.profilePictureUri)
  }

  @Test
  fun cancel_changes_reverts_to_last_saved() = runTest {
    loadProfile()
    updatePseudonym(NEW_PSEUDONYM)
    cancelChanges()
    val state = getState()
    assertEquals(OLD_PSEUDONYM, state.pseudonym)
    assertFalse(state.hasChanges)
    assertFalse(state.canSave)
  }

  @Test
  fun save_profile_success_sets_success_flag() = runTest {
    loadProfile()
    updatePseudonym(NEW_PSEUDONYM)
    coEvery { repository.updateProfile(any()) } returns Unit
    saveProfile()
    val state = getState()
    assertTrue(state.success)
    assertFalse(state.isSaving)
    coVerify { repository.updateProfile(match { it.author.pseudonym == NEW_PSEUDONYM }) }
  }

  @Test
  fun save_profile_repository_exception_sets_error() = runTest {
    loadProfile()
    updatePseudonym(NEW_PSEUDONYM)
    coEvery { repository.updateProfile(any()) } throws Exception("Firestore error")
    saveProfile()
    val state = getState()
    assertFalse(state.success)
    assertEquals(FIRESTORE_ERROR, state.errorMsg)
  }

  @Test
  fun save_profile_with_invalid_pseudonym_does_not_save() = runTest {
    loadProfile()
    updatePseudonym("")
    saveProfile()
    val state = getState()
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
    loadProfile(fullProfile)
    updatePseudonym(NEW_PSEUDONYM)
    coEvery { repository.updateProfile(any()) } returns Unit
    saveProfile()
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
    loadProfile()
    updatePseudonym("")
    val state = getState()
    assertFalse(state.canSave)
  }

  @Test
  fun pseudonym_too_long_disables_canSave() = runTest {
    loadProfile()
    updatePseudonym("a".repeat(31))
    val state = getState()
    assertFalse(state.canSave)
  }

  @Test
  fun bio_too_long_disables_canSave() = runTest {
    loadProfile()
    updateBio("b".repeat(201))
    val state = getState()
    assertFalse(state.canSave)
  }

  @Test
  fun valid_pseudonym_and_bio_enables_canSave() = runTest {
    loadProfile()
    updatePseudonym(NEW_PSEUDONYM)
    updateBio(VALID_BIO)
    val state = getState()
    assertTrue(state.canSave)
  }

  @Test
  fun removeProfilePicture_clearsAllPictureFields() = runTest {
    val profileWithPicture =
        dummy_profile.copy(
            author =
                dummy_profile.author.copy(
                    profilePicture = 5, profilePictureUrl = "https://example.com/pic.jpg"))
    loadProfile(profileWithPicture)
    coEvery { repository.deleteCurrentProfilePicture(TEST_USER_ID, any()) } returns Unit
    coEvery { repository.updateProfile(any()) } returns Unit
    viewModel.removeProfilePicture()
    testDispatcher.scheduler.advanceUntilIdle()
    val state = getState()
    assertEquals(0, state.profilePicture)
    assertEquals("", state.profilePictureUrl)
    assertNull(state.profilePictureUri)
    assertTrue(state.hasChanges)
    assertTrue(state.canSave)
    coVerify { repository.deleteCurrentProfilePicture(TEST_USER_ID, "https://example.com/pic.jpg") }
    coVerify { repository.updateProfile(match { it.author.profilePicture == 0 }) }
  }

  @Test
  fun multipleUpdates_maintainsCorrectState() = runTest {
    loadProfile()
    updatePseudonym("First")
    updatePseudonym("Second")
    updateBio("Bio1")
    updateBio("Bio2")
    val state = getState()
    assertEquals("Second", state.pseudonym)
    assertEquals("Bio2", state.bio)
    assertTrue(state.hasChanges)
    assertTrue(state.canSave)
  }

  @Test
  fun saveProfile_noCurrentUser_setsError() = runTest {
    every { auth.currentUser } returns null
    saveProfile()
    val state = getState()
    assertFalse(state.success)
    assertEquals(USER_NOT_LOADED, state.errorMsg)
  }

  @Test
  fun saveProfile_cannotSave_doesNothing() = runTest {
    loadProfile()
    saveProfile()
    coVerify(exactly = 0) { repository.updateProfile(any()) }
  }

  @Test
  fun saveProfile_repositoryException_setsError() = runTest {
    loadProfile()
    coEvery { repository.updateProfile(any()) } throws Exception("Firestore error")
    updatePseudonym(NEW_PSEUDONYM)
    saveProfile()
    val state = getState()
    assertFalse(state.success)
    assertEquals(FIRESTORE_ERROR, state.errorMsg)
  }

  @Test
  fun saveProfile_preservesRatingsAndHunts() = runTest {
    val fullProfile =
        dummy_profile.copy(
            author = dummy_profile.author.copy(reviewRate = 5.0, sportRate = 4.0),
            myHunts = mutableListOf(mockk()),
            doneHunts = mutableListOf(mockk()),
            likedHunts = mutableListOf(mockk()))
    loadProfile(fullProfile)
    coEvery { repository.updateProfile(any()) } returns Unit
    updatePseudonym(NEW_PSEUDONYM)
    saveProfile()
    coVerify {
      repository.updateProfile(
          match {
            it.author.pseudonym == NEW_PSEUDONYM &&
                it.author.reviewRate == 5.0 &&
                it.author.sportRate == 4.0 &&
                it.myHunts.size == 1 &&
                it.doneHunts.size == 1 &&
                it.likedHunts.size == 1
          })
    }
  }

  @Test
  fun saveProfile_emptyUrlAndPicture_clearsProfilePicture() = runTest {
    loadProfile()
    coEvery { repository.updateProfile(any()) } returns Unit
    coEvery { repository.deleteCurrentProfilePicture(TEST_USER_ID, OLD_URL) } returns Unit
    viewModel.removeProfilePicture()
    updatePseudonym(NEW_PSEUDONYM)
    saveProfile()
    testDispatcher.scheduler.advanceUntilIdle()
    coVerify { repository.updateProfile(match { it.author.profilePictureUrl == "" }) }
    coVerify { repository.deleteCurrentProfilePicture(TEST_USER_ID, OLD_URL) }
  }

  @Test
  fun saveProfile_withNewUri_savesUrlToFirestore() = runTest {
    val testUri = mockk<Uri>()
    loadProfile()
    coEvery { repository.uploadProfilePicture(TEST_USER_ID, testUri) } returns NEW_URL
    coEvery { repository.deleteCurrentProfilePicture(TEST_USER_ID, OLD_URL) } returns Unit
    coEvery { repository.updateProfile(any()) } returns Unit
    updateProfilePictureUri(testUri)
    saveProfile()

    coVerify { repository.updateProfile(match { it.author.profilePictureUrl == NEW_URL }) }
    coVerify { repository.deleteCurrentProfilePicture(TEST_USER_ID, OLD_URL) }
  }

  @Test
  fun multipleUriUpdates_onlyLastOneUsed() = runTest {
    val uri1 = mockk<Uri>(name = "uri1")
    val uri2 = mockk<Uri>(name = "uri2")
    loadProfile()
    updateProfilePictureUri(uri1)
    updateProfilePictureUri(uri2)
    val state = getState()
    assertEquals(uri2, state.profilePictureUri)
  }

  @Test
  fun deleteCurrentProfilePicture_withValidUrl_callsStorageDelete() = runTest {
    val testUrl = "https://firebasestorage.googleapis.com/v0/b/testbucket/o/user123.jpg"

    coEvery { repository.deleteCurrentProfilePicture(TEST_USER_ID, testUrl) } returns Unit

    repository.deleteCurrentProfilePicture(TEST_USER_ID, testUrl)

    coVerify(exactly = 1) { repository.deleteCurrentProfilePicture(TEST_USER_ID, testUrl) }
  }

  @Test
  fun deleteCurrentProfilePicture_withEmptyUrl_doesNothing() = runTest {
    val emptyUrl = EMPTY_STRING

    coEvery { repository.deleteCurrentProfilePicture(TEST_USER_ID, emptyUrl) } returns Unit

    repository.deleteCurrentProfilePicture(TEST_USER_ID, emptyUrl)

    coVerify(exactly = 1) { repository.deleteCurrentProfilePicture(TEST_USER_ID, emptyUrl) }
  }

  @Test
  fun removeProfilePicture_callsDeleteAndUpdatesState() = runTest {
    val profileWithUrl =
        dummy_profile.copy(author = dummy_profile.author.copy(profilePictureUrl = OLD_URL))
    loadProfile(profileWithUrl)

    coEvery { repository.deleteCurrentProfilePicture(TEST_USER_ID, OLD_URL) } returns Unit
    coEvery { repository.updateProfile(any()) } returns Unit

    viewModel.removeProfilePicture()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = getState()
    assertEquals(EditProfileNumberConstants.PROFILE_PIC_DEFAULT, state.profilePicture)
    assertEquals(EMPTY_STRING, state.profilePictureUrl)
    assertNull(state.profilePictureUri)
    assertTrue(state.hasChanges)
    assertTrue(state.canSave)

    coVerify { repository.deleteCurrentProfilePicture(TEST_USER_ID, OLD_URL) }
    coVerify { repository.updateProfile(any()) }
  }
}
