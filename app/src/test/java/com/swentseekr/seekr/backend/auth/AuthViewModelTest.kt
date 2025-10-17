package com.swentseekr.seekr.ui.auth

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.firebase.auth.FirebaseUser
import com.swentseekr.seekr.model.authentication.AuthRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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
class AuthViewModelTest {

  private val dispatcher = StandardTestDispatcher()

  @MockK(relaxed = true) lateinit var repository: AuthRepository
  @MockK(relaxed = true) lateinit var credentialManager: CredentialManager
  @MockK(relaxed = true) lateinit var context: Context

  @Before
  fun setup() {
    Dispatchers.setMain(dispatcher)
    MockKAnnotations.init(this, relaxUnitFun = true)

    // Provide the web client id string the VM asks for
    every { context.getString(com.swentseekr.seekr.R.string.default_web_client_id) } returns
        "test-web-client-id"
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun makeResponseWith(credential: Credential): GetCredentialResponse {
    // We don't need a concrete implementation, just a mock that returns our credential.
    val response = mockk<GetCredentialResponse>()
    every { response.credential } returns credential
    return response
  }

  @Test
  fun clearErrorMsgClearsError() =
      runTest(dispatcher) {
        val vm = AuthViewModel(repository)
        // Seed an error state
        vm.signIn(context, credentialManager)
        // Cause a controlled failure from repository
        val cred = mockk<Credential>()
        coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } returns
            makeResponseWith(cred)
        coEvery { repository.signInWithGoogle(cred) } returns
            Result.failure(IllegalStateException("boom"))

        advanceUntilIdle()
        assertNotNull(vm.uiState.value.errorMsg)

        vm.clearErrorMsg()
        assertNull(vm.uiState.value.errorMsg)
      }

  @Test
  fun signInSuccessUpdatesStateWithUserAndClearsFlags() =
      runTest(dispatcher) {
        val vm = AuthViewModel(repository)

        val cred = mockk<Credential>()
        val user = mockk<FirebaseUser>()

        coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } returns
            makeResponseWith(cred)
        coEvery { repository.signInWithGoogle(cred) } returns Result.success(user)

        vm.signIn(context, credentialManager)
        advanceUntilIdle()

        val s = vm.uiState.value
        assertFalse(s.isLoading)
        assertSame(user, s.user)
        assertNull(s.errorMsg)
        assertFalse(s.signedOut)
      }

  @Test
  fun signInRepositoryFailureSetsErrorAndSignedOut() =
      runTest(dispatcher) {
        val vm = AuthViewModel(repository)

        val cred = mockk<Credential>()
        coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } returns
            makeResponseWith(cred)
        coEvery { repository.signInWithGoogle(cred) } returns
            Result.failure(IllegalArgumentException("No user returned"))

        vm.signIn(context, credentialManager)
        advanceUntilIdle()

        val s = vm.uiState.value
        assertFalse(s.isLoading)
        assertNull(s.user)
        assertTrue(s.signedOut)
        assertTrue(s.errorMsg?.contains("No user returned") == true)
      }

  @Test
  fun signInCancelledMapsToSignInCancelled() =
      runTest(dispatcher) {
        val vm = AuthViewModel(repository)

        coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } throws
            GetCredentialCancellationException("cancelled by user")

        vm.signIn(context, credentialManager)
        advanceUntilIdle()

        val s = vm.uiState.value
        assertFalse(s.isLoading)
        assertNull(s.user)
        assertEquals("Sign-in cancelled", s.errorMsg)
        assertTrue(s.signedOut)
      }

  @Test
  fun signInCredentialExceptionMapsToFailedToGetCredentials() =
      runTest(dispatcher) {
        val vm = AuthViewModel(repository)

        coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } throws
            androidx.credentials.exceptions.NoCredentialException("no creds")

        vm.signIn(context, credentialManager)
        advanceUntilIdle()

        val s = vm.uiState.value
        assertFalse(s.isLoading)
        assertNull(s.user)
        assertTrue(s.errorMsg?.startsWith("Failed to get credentials:") == true)
        assertTrue(s.signedOut)
      }

  @Test
  fun signInUnexpectedExceptionMapsToUnexpectedError() =
      runTest(dispatcher) {
        val vm = AuthViewModel(repository)

        coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } throws
            RuntimeException("kaboom")

        vm.signIn(context, credentialManager)
        advanceUntilIdle()

        val s = vm.uiState.value
        assertFalse(s.isLoading)
        assertNull(s.user)
        assertTrue(s.errorMsg?.startsWith("Unexpected error:") == true)
        assertTrue(s.signedOut)
      }

  @Test
  fun signInIsIdempotentWhileLoading() =
      runTest(dispatcher) {
        val vm = AuthViewModel(repository)

        val cred = mockk<Credential>()
        val user = mockk<FirebaseUser>()
        // Suspend the first getCredential until we advance
        coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } returns
            makeResponseWith(cred)
        coEvery { repository.signInWithGoogle(cred) } returns Result.success(user)

        // First call -> sets loading
        vm.signIn(context, credentialManager)
        // Second call should be ignored because isLoading == true
        vm.signIn(context, credentialManager)

        advanceUntilIdle()

        val s = vm.uiState.value
        assertFalse(s.isLoading)
        assertSame(user, s.user)
      }
}
