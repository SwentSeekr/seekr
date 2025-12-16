package com.swentseekr.seekr.model.profile

import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
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

/**
 * Unit tests for the ProfileViewModel.
 *
 * This test suite verifies loading, updating, and refreshing profiles,
 * as well as error handling and computed profile statistics.
 */

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
  companion object {
    private const val PROFILE_NOT_FOUND = "Profile not found"
    private const val USER_NOT_LOGIN = "User not logged in"
  }

  private lateinit var repository: ProfileRepositoryLocal
  private lateinit var viewModel: ProfileViewModel
  private val testDispatcher = StandardTestDispatcher()
  private val profileAlice =
      Profile(
          uid = "user1",
          author =
              Author(
                  hasCompletedOnboarding = true,
                  hasAcceptedTerms = true,
                  pseudonym = "Alice",
                  bio = "Bio",
                  profilePicture = 0,
                  reviewRate = 4.0,
                  sportRate = 3.5),
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

    assertEquals(PROFILE_NOT_FOUND, viewModel.uiState.value.errorMsg)
  }

  @Test
  fun loadProfile_sets_error_for_non_existent_profile() = runTest {
    viewModel.loadProfile("ghost")
    advanceUntilIdle()
    val state = viewModel.uiState.value
    assertEquals(PROFILE_NOT_FOUND, state.errorMsg)
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
    assertEquals(USER_NOT_LOGIN, state.errorMsg)
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
            author =
                Author(
                    hasCompletedOnboarding = true,
                    hasAcceptedTerms = true,
                    "Bob",
                    "Bio",
                    0,
                    4.0,
                    3.5),
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

    assertEquals(USER_NOT_LOGIN, viewModelNoUid.uiState.value.errorMsg)
  }

  @Test
  fun buildComputedProfile_calculatesCorrectReviewAndSportRates() = runTest {
    val myHunts =
        listOf(
            createHuntWithRateAndDifficulty("hunt1", "Hunt 1", reviewRate = 3.0),
            createHuntWithRateAndDifficulty("hunt2", "Hunt 2", reviewRate = 5.0))
    val doneHunts =
        listOf(
            createHuntWithRateAndDifficulty("done1", "Done 1", difficulty = Difficulty.EASY),
            createHuntWithRateAndDifficulty("done2", "Done 2", difficulty = Difficulty.DIFFICULT))

    val baseProfile = sampleProfile(myHunts = myHunts, doneHunts = doneHunts)
    val computedProfile = viewModel.buildComputedProfile(baseProfile)

    assertEquals(4.0, computedProfile.author.reviewRate, 0.01)
    assertEquals(3.0, computedProfile.author.sportRate, 0.01)
    assertEquals(2, computedProfile.doneHunts.size)
    assertEquals(2, computedProfile.myHunts.size)
  }

  @Test
  fun loadEmptyHunts_computesZeroRates() = runTest {
    val profile = sampleProfile(myHunts = emptyList(), doneHunts = emptyList(), uid = "user1")
    repository.addProfile(profile)

    viewModel.loadProfile("user1")
    advanceUntilIdle()

    val loadedProfile = viewModel.uiState.value.profile!!
    assertEquals(0.0, loadedProfile.author.reviewRate, 0.01)
    assertEquals(0.0, loadedProfile.author.sportRate, 0.01)
    assertEquals(0, viewModel.totalReviews.value)
  }

  @Test
  fun repositoryFailure_doesNotCrashViewModel() = runTest {
    viewModel.loadProfile("nonexistent_user")
    advanceUntilIdle()

    assertNull(viewModel.uiState.value.profile)
  }

  @Test
  fun toggleLikedHunt_addsHuntToLikedHunts() = runTest {
    viewModel.loadProfile("user1")
    advanceUntilIdle()

    val hunt =
        createHuntWithRateAndDifficulty(
            uid = "hunt1", title = "Test Hunt", reviewRate = 4.0, difficulty = Difficulty.EASY)

    viewModel.toggleLikedHunt(hunt)
    advanceUntilIdle()

    val likedHunts = viewModel.uiState.value.profile!!.likedHunts

    assertEquals(1, likedHunts.size)
    assertEquals("hunt1", likedHunts.first().uid)
  }

  @Test
  fun toggleLikedHunt_removesHuntFromLikedHunts() = runTest {
    val hunt =
        createHuntWithRateAndDifficulty(
            uid = "hunt1", title = "Test Hunt", reviewRate = 4.0, difficulty = Difficulty.EASY)

    val profileWithLike = profileAlice.copy(likedHunts = mutableListOf(hunt))

    repository.updateProfile(profileWithLike)

    viewModel.loadProfile("user1")
    advanceUntilIdle()

    assertEquals(1, viewModel.uiState.value.profile!!.likedHunts.size)

    viewModel.toggleLikedHunt(hunt)
    advanceUntilIdle()

    val likedHunts = viewModel.uiState.value.profile!!.likedHunts
    assertTrue(likedHunts.isEmpty())
  }
}
