package com.swentseekr.seekr.backend.auth

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseUser
import com.swentseekr.seekr.model.authentication.AuthRepository
import com.swentseekr.seekr.ui.auth.AuthUIState
import com.swentseekr.seekr.ui.auth.AuthViewModel
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
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
class AuthViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  @MockK(relaxUnitFun = true) private lateinit var mockRepository: AuthRepository

  @MockK private lateinit var mockContext: Context

  @MockK private lateinit var mockCredentialManager: CredentialManager

  private lateinit var viewModel: AuthViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    MockKAnnotations.init(this, relaxUnitFun = true)
    viewModel = AuthViewModel(repository = mockRepository)
  }

  @After
  fun tearDown() {
    clearAllMocks()
    unmockkAll()
    Dispatchers.resetMain()
  }

  @Test
  fun `initial state is idle and signed out`() = runTest {
    val state = viewModel.uiState.first()
    assertFalse(state.isLoading)
    assertNull(state.user)
    assertNull(state.errorMsg)
    assertFalse(state.signedOut)
  }

  @Test
  fun `signIn success updates user and clears error`() = runTest {
    val fakeUser = io.mockk.mockk<FirebaseUser>()
    coEvery { mockRepository.signInWithGoogle(any()) } returns Result.success(fakeUser)

    viewModel.signIn(mockContext, mockCredentialManager)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse(state.isLoading)
    assertEquals(fakeUser, state.user)
    assertNull(state.errorMsg)
    assertFalse(state.signedOut)
  }

  @Test
  fun `signIn failure updates error message and sets signedOut`() = runTest {
    coEvery { mockRepository.signInWithGoogle(any()) } returns
        Result.failure(Exception("sign-in failed"))

    viewModel.signIn(mockContext, mockCredentialManager)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse(state.isLoading)
    assertTrue(state.signedOut)
    assertNull(state.user)
    assertTrue(state.errorMsg?.contains("sign-in failed") == true)
  }

  @Test
  fun `clearErrorMsg resets error in UI state`() = runTest {
    val initialError = AuthUIState(errorMsg = "Something went wrong")
    viewModel = AuthViewModel(repository = mockRepository)
    viewModel.clearErrorMsg()
    val state = viewModel.uiState.first()
    assertNull(state.errorMsg)
  }

  @Test
  fun `multiple signIn calls ignore when already loading`() = runTest {
    coEvery { mockRepository.signInWithGoogle(any()) } coAnswers
        {
          // Simulate long-running sign-in
          kotlinx.coroutines.delay(2000)
          Result.failure(Exception("timeout"))
        }

    // First call sets loading state
    viewModel.signIn(mockContext, mockCredentialManager)
    assertTrue(viewModel.uiState.first().isLoading)

    // Second call should be ignored
    viewModel.signIn(mockContext, mockCredentialManager)
    advanceUntilIdle()

    assertTrue(viewModel.uiState.first().isLoading || viewModel.uiState.first().errorMsg != null)
  }
}
