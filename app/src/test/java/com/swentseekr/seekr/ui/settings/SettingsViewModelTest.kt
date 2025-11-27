package com.swentseekr.seekr.ui.settings

import androidx.credentials.CredentialManager
import com.swentseekr.seekr.model.authentication.AuthRepository
import com.swentseekr.seekr.model.settings.SettingsRepositoryFirestore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class SettingsViewModelTest {
  private val testDispatcher = StandardTestDispatcher()

  private lateinit var authRepository: AuthRepository
  private lateinit var credentialManager: CredentialManager
  private lateinit var repository: SettingsRepositoryFirestore
  private lateinit var viewModel: SettingsViewModel

  @Before
  fun setUp() = runTest {
    Dispatchers.setMain(testDispatcher)

    repository = mockk(relaxed = true)
    authRepository = mockk(relaxed = true)
    credentialManager = mockk(relaxed = true)

    viewModel = SettingsViewModel(repository = repository, authRepository = authRepository)

    advanceUntilIdle()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  /** Test that the initial app version is set from BuildConfig */
  @Test
  fun initial_state_contains_app_version() = runTest {
    val state = viewModel.uiState.first()
    assertNotNull(state.appVersion)
    assertTrue(state.appVersion!!.isNotBlank())
  }

  /** Test that signOut updates signedOut to true when repository returns success */
  @Test
  fun signOut_success_updates_signedOut_true() = runTest {
    coEvery { authRepository.signOut() } returns Result.success(Unit)
    coEvery { credentialManager.clearCredentialState(any()) } returns Unit

    viewModel.signOut(credentialManager)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertTrue(state.signedOut)
    assertNull(state.errorMsg)
  }

  /** Test that signOut sets errorMsg when repository fails */
  @Test
  fun signOut_failure_sets_errorMsg() = runTest {
    coEvery { authRepository.signOut() } returns Result.failure(Exception("Test error"))

    viewModel.signOut(credentialManager)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse(state.signedOut)
    assertEquals("Test error", state.errorMsg)
  }

  /** Test that clearErrorMsg removes any existing error message */
  @Test
  fun clearErrorMsg_resets_errorMsg_to_null() = runTest {
    coEvery { authRepository.signOut() } returns Result.failure(Exception("Error"))

    viewModel.signOut(credentialManager)
    advanceUntilIdle()
    viewModel.clearErrorMsg()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNull(state.errorMsg)
  }

  /** Test that displayAppVersion properly updates the UI state */
  @Test
  fun displayAppVersion_updates_appVersion() = runTest {
    viewModel.setAppVersion("9.9.9")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("9.9.9", state.appVersion)
  }

  @Test
  fun updateNotifications_disabled_updates_field_only() = runTest {
    coEvery { repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, false) } returns Unit

    viewModel.updateNotifications(false, null)
    advanceUntilIdle()

    coVerify { repository.updateField(SettingsScreenStrings.NOTIFICATION_FIELD, false) }
  }

  @Test
  fun updatePictures_updates_repository_field() = runTest {
    coEvery { repository.updateField(SettingsScreenStrings.PICTURES_FIELD, true) } returns Unit

    viewModel.updatePictures(true)
    advanceUntilIdle()

    coVerify { repository.updateField(SettingsScreenStrings.PICTURES_FIELD, true) }
  }

  @Test
  fun updateLocalisation_updates_repository_field() = runTest {
    coEvery { repository.updateField(SettingsScreenStrings.LOCALISATION_FIELD, false) } returns Unit

    viewModel.updateLocalisation(false)
    advanceUntilIdle()

    coVerify { repository.updateField(SettingsScreenStrings.LOCALISATION_FIELD, false) }
  }
}
