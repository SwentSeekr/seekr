package com.swentseekr.seekr.backend.auth

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.authentication.AuthRepository
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.ui.auth.AuthViewModel
import com.swentseekr.seekr.ui.auth.AuthViewModelMessages
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
  @MockK(relaxed = true) lateinit var auth: FirebaseAuth

  @MockK(relaxed = true) lateinit var profileRepo: ProfileRepository

  @Before
  fun setup() {
    Dispatchers.setMain(dispatcher)
    MockKAnnotations.init(this, relaxUnitFun = true)

    // Provide the web client id string the VM asks for
    every { context.getString(R.string.default_web_client_id) } returns "test-web-client-id"

    // Prevent Firebase from crashing in JVM tests
    every { auth.currentUser } returns null
    every { auth.addAuthStateListener(any()) } answers { /* no-op */}
    every { auth.removeAuthStateListener(any()) } answers { /* no-op */}
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
        val vm = AuthViewModel(repository, profileRepo, auth)

        // Cause a controlled failure from repository so we get an error in state
        val cred = mockk<Credential>()
        coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } returns
            makeResponseWith(cred)
        coEvery { repository.signInWithGoogle(cred) } returns
            Result.failure(IllegalStateException("boom"))

        vm.signIn(context, credentialManager)
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.errorMsg)

        vm.clearErrorMsg()
        assertNull(vm.uiState.value.errorMsg)
      }

  @Test
  fun signInSuccessUpdatesStateWithUserAndClearsFlags() =
      runTest(dispatcher) {
        val cred = mockk<Credential>()
        val user = mockk<FirebaseUser>()

        // IMPORTANT : mock the uid to a known value
        every { user.uid } returns "testUid"

        every { auth.currentUser } returns null
        every { auth.addAuthStateListener(any()) } answers {}

        // Onboarding mock
        coEvery { profileRepo.checkUserNeedsOnboarding("testUid") } returns false

        val vm = AuthViewModel(repository, profileRepo, auth)

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
        val vm = AuthViewModel(repository, profileRepo, auth)

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
        val vm = AuthViewModel(repository, profileRepo, auth)

        coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } throws
            GetCredentialCancellationException("cancelled by user")

        vm.signIn(context, credentialManager)
        advanceUntilIdle()

        val s = vm.uiState.value
        assertFalse(s.isLoading)
        assertNull(s.user)
        assertEquals(AuthViewModelMessages.SIGN_IN_CANCELLED, s.errorMsg)
        assertTrue(s.signedOut)
      }

  @Test
  fun signInCredentialExceptionMapsToFailedToGetCredentials() =
      runTest(dispatcher) {
        val vm = AuthViewModel(repository, profileRepo, auth)

        coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } throws
            NoCredentialException("no creds")

        vm.signIn(context, credentialManager)
        advanceUntilIdle()

        val s = vm.uiState.value
        assertFalse(s.isLoading)
        assertNull(s.user)
        assertEquals(AuthViewModelMessages.NO_GOOGLE_ACCOUNT, s.errorMsg)
        assertTrue(s.signedOut)
      }

  @Test
  fun signInUnexpectedExceptionMapsToUnexpectedError() =
      runTest(dispatcher) {
        val vm = AuthViewModel(repository, profileRepo, auth)
        coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } throws
            RuntimeException("kaboom")

        vm.signIn(context, credentialManager)
        advanceUntilIdle()

        val s = vm.uiState.value
        assertFalse(s.isLoading)
        assertNull(s.user)
        assertTrue(s.errorMsg?.startsWith(AuthViewModelMessages.UNEXPECTED_ERROR_PREFIX) == true)
        assertTrue(s.signedOut)
      }

  @Test
  fun signInIsIdempotentWhileLoading() =
      runTest(dispatcher) {
        val cred = mockk<Credential>()
        val user = mockk<FirebaseUser>()

        every { user.uid } returns "testUid"

        every { auth.currentUser } returns null
        every { auth.addAuthStateListener(any()) } answers {}

        coEvery { profileRepo.checkUserNeedsOnboarding("testUid") } returns false

        val vm = AuthViewModel(repository, profileRepo, auth)

        coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } returns
            makeResponseWith(cred)

        coEvery { repository.signInWithGoogle(cred) } returns Result.success(user)

        vm.signIn(context, credentialManager)
        vm.signIn(context, credentialManager)

        advanceUntilIdle()

        val s = vm.uiState.value
        assertFalse(s.isLoading)
        assertSame(user, s.user)
      }

  @Test
  fun signInSuccess_setsNeedsOnboardingTrue_forNewUser() =
      runTest(dispatcher) {
        val cred = mockk<Credential>()
        val user = mockk<FirebaseUser>()

        every { user.uid } returns "newUser123"

        every { auth.currentUser } returns null
        every { auth.addAuthStateListener(any()) } answers {}

        // NEW USER → onboarding required
        coEvery { profileRepo.checkUserNeedsOnboarding("newUser123") } returns true

        val vm = AuthViewModel(repository, profileRepo, auth)

        coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } returns
            makeResponseWith(cred)

        coEvery { repository.signInWithGoogle(cred) } returns Result.success(user)

        vm.signIn(context, credentialManager)

        advanceUntilIdle() // finish sign-in coroutine
        advanceUntilIdle() // finish onboarding-check coroutine

        val s = vm.uiState.value

        assertSame(user, s.user)
        assertTrue(AuthViewModelMessages.ONBOARDING, s.needsOnboarding)
        assertNull(s.errorMsg)
        assertFalse(s.signedOut)
      }
}
