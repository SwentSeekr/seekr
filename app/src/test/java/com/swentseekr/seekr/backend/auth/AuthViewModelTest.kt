package com.swentseekr.seekr.backend.auth

import android.content.Context
import android.os.Bundle
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.swentseekr.seekr.ui.auth.AuthViewModel
import com.swentseekr.seekr.ui.overview.OverviewViewModel
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  // Static-mocked singletons and factories
  private lateinit var firebaseAuth: FirebaseAuth

  // Common mocks
  private lateinit var context: Context
  private lateinit var credentialManager: CredentialManager

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    MockKAnnotations.init(this, relaxUnitFun = true)

    // Mock static singletons/factories before creating the ViewModel
    mockkStatic(FirebaseAuth::class)
    mockkStatic(GoogleAuthProvider::class)

    // Mock the Companion so Companion.createFrom(...) is intercepted
    mockkObject(GoogleIdTokenCredential.Companion)

    firebaseAuth = mockk(relaxed = true)
    every { FirebaseAuth.getInstance() } returns firebaseAuth

    context = mockk(relaxed = true)
    every { context.getString(any()) } returns "test-web-client-id"

    credentialManager = mockk(relaxed = true)
  }

  @After
  fun tearDown() {
    clearAllMocks()
    unmockkAll()
    Dispatchers.resetMain()
  }

  @Test
  fun initSetsUnauthenticatedWhenNoCurrentUser() =
    runTest(testDispatcher) {
      every { firebaseAuth.currentUser } returns null

      val vm = AuthViewModel()

      assertFalse(vm.uiState.value.isLoading)
      assertFalse(vm.uiState.value.user != null)
      assertFalse(vm.uiState.value.errorMsg != null)
    }

  @Test
  fun initSetsAuthenticatedWhenCurrentUserExists() =
    runTest(testDispatcher) {
      val user = mockk<FirebaseUser>()
      every { firebaseAuth.currentUser } returns user

      val vm = AuthViewModel()

      assertFalse(vm.uiState.value.isLoading)
      assertTrue(vm.uiState.value.user != null)
      assertFalse(vm.uiState.value.errorMsg != null)
    }

  @Test
  fun cleanErrorClearsErrorState() =
    runTest(testDispatcher) {
      every { firebaseAuth.currentUser } returns null
      every { firebaseAuth.signOut() } throws RuntimeException("boom")

      val vm = AuthViewModel()
      val om = OverviewViewModel()

      var onErrorCalled = false
      om.signOut(credentialManager)
      if (vm.uiState.value.errorMsg != null) {
        onErrorCalled = true
      }
      advanceUntilIdle()

      assertTrue(onErrorCalled)
      assertTrue(vm.uiState.value.errorMsg != null)
      assertFalse(vm.uiState.value.user != null)

      vm.clearErrorMsg()
      assertFalse(vm.uiState.value.errorMsg != null)
      assertEquals(null, vm.uiState.value.errorMsg)
    }

  @Test
  fun signOutSuccessUpdatesStateAndCallsOnSuccess() =
    runTest(testDispatcher) {
      every { firebaseAuth.currentUser } returns mockk()
      every { firebaseAuth.signOut() } just runs

      val vm = AuthViewModel()
      val om = OverviewViewModel()

      var successCalled = false
      om.signOut(credentialManager)
      if (vm.uiState.value.errorMsg == null) {
        successCalled = true
      }
      advanceUntilIdle()


      assertTrue(successCalled)
      assertFalse(vm.uiState.value.user != null)
      assertFalse(vm.uiState.value.errorMsg != null)
    }

  @Test
  fun signOutFailureUpdatesErrorStateAndCallsOnError() =
    runTest(testDispatcher) {
      every { firebaseAuth.currentUser } returns mockk()
      every { firebaseAuth.signOut() } throws IllegalStateException("cannot sign out")

      val vm = AuthViewModel()
      val om = OverviewViewModel()

      var errorCalled = false
      om.signOut(credentialManager)
      if (vm.uiState.value.errorMsg != null) {
        errorCalled = true
      }
      advanceUntilIdle()

      assertTrue(errorCalled)
      assertTrue(vm.uiState.value.errorMsg != null)
      assertFalse(vm.uiState.value.user != null)
      assertTrue(vm.uiState.value.errorMsg?.contains("cannot sign out") == true)
    }

  @Test
  fun signInWithGoogleSuccessSetsAuthenticatedAndCallsOnSuccess() =
    runTest(testDispatcher) {
      every { firebaseAuth.currentUser } returns null
      val vm = AuthViewModel()

      val response = mockk<GetCredentialResponse>()
      val cred = mockk<Credential>()
      every { response.credential } returns cred
      every { cred.data } returns Bundle()

      val googleToken = "fake-token"
      val googleIdTokenCredential = mockk<GoogleIdTokenCredential>()
      every { googleIdTokenCredential.idToken } returns googleToken
      every { GoogleIdTokenCredential.Companion.createFrom(any()) } returns
              googleIdTokenCredential

      val authCred = mockk<AuthCredential>()
      every { GoogleAuthProvider.getCredential(googleToken, null) } returns authCred

      val authResult = mockk<AuthResult>()
      every { authResult.user } returns mockk<FirebaseUser>()
      every { firebaseAuth.signInWithCredential(authCred) } returns Tasks.forResult(authResult)

      coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } returns
              response

      var successCalled = false
      vm.signIn(
        context = context,
        credentialManager = credentialManager)
      if (vm.uiState.value.errorMsg == null) {
        successCalled = true
      }
      advanceUntilIdle()

      assertTrue(successCalled)
      assertFalse(vm.uiState.value.isLoading)
      assertTrue(vm.uiState.value.user != null)
      assertFalse(vm.uiState.value.errorMsg != null)

      verify(exactly = 1) {
        runBlocking { credentialManager.getCredential(context, any<GetCredentialRequest>()) }
      }
      verify(exactly = 1) { firebaseAuth.signInWithCredential(authCred) }
    }

  @Test
  fun signInWithGoogleReturnsErrorWhenNoUserReturned() =
    runTest(testDispatcher) {
      every { firebaseAuth.currentUser } returns null
      val vm = AuthViewModel()

      val response = mockk<GetCredentialResponse>()
      val cred = mockk<Credential>()
      every { response.credential } returns cred
      every { cred.data } returns Bundle()

      val googleIdTokenCredential = mockk<GoogleIdTokenCredential>()
      every { googleIdTokenCredential.idToken } returns "fake-token"
      every { GoogleIdTokenCredential.Companion.createFrom(any()) } returns
              googleIdTokenCredential

      val authCred = mockk<AuthCredential>()
      every { GoogleAuthProvider.getCredential("fake-token", null) } returns authCred

      val authResult = mockk<AuthResult>()
      every { authResult.user } returns null
      every { firebaseAuth.signInWithCredential(authCred) } returns Tasks.forResult(authResult)

      coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } returns
              response

      var errorCalled = false
      vm.signIn(
        context = context,
        credentialManager = credentialManager)
      if (vm.uiState.value.errorMsg != null) {
        errorCalled = true
      }
      advanceUntilIdle()

      assertTrue(errorCalled)
      assertFalse(vm.uiState.value.user != null)
      assertFalse(vm.uiState.value.isLoading)
      assertTrue(vm.uiState.value.errorMsg?.contains("No user returned") == true)
    }

  @Test
  fun signInWithGoogleHandlesGetCredentialCancellationException() =
    runTest(testDispatcher) {
      every { firebaseAuth.currentUser } returns null
      val vm = AuthViewModel()

      coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } throws
              GetCredentialCancellationException("cancelled")

      var errorCalled = false
      vm.signIn(
        context = context,
        credentialManager = credentialManager)
      if (vm.uiState.value.errorMsg != null) {
        errorCalled = true
      }
      advanceUntilIdle()

      assertTrue(errorCalled)
      assertFalse(vm.uiState.value.user != null)
      assertFalse(vm.uiState.value.isLoading)
      assertTrue(vm.uiState.value.errorMsg?.contains("Failed to get credential") == true)
    }

  @Test
  fun signInWithGoogleHandlesUnexpectedExceptions() =
    runTest(testDispatcher) {
      every { firebaseAuth.currentUser } returns null
      val vm = AuthViewModel()

      every { GoogleIdTokenCredential.Companion.createFrom(any()) } throws
              IllegalStateException("bad bundle")

      val response = mockk<GetCredentialResponse>()
      val cred = mockk<Credential>()
      every { response.credential } returns cred
      every { cred.data } returns Bundle()
      coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } returns
              response

      var errorCalled = false
      vm.signIn(
        context = context,
        credentialManager = credentialManager)
      if (vm.uiState.value.errorMsg != null) {
        errorCalled = true
      }
      advanceUntilIdle()

      assertTrue(errorCalled)
      assertFalse(vm.uiState.value.user != null)
      assertFalse(vm.uiState.value.isLoading)
      assertTrue(vm.uiState.value.errorMsg?.contains("Authentication failed") == true)
    }
}