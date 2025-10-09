package com.swentseekr.seekr.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.swentseekr.seekr.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data class representing the authentication state
data class AuthState(
    val isLoading: Boolean = true, // Indicates if an auth operation is in progress
    val errorMessage: String? = null, // Holds error messages if any
    val isError: Boolean = false, // Indicates if there is an error
    val isAuthenticated: Boolean = false, // Indicates if the user is authenticated
)

// ViewModel for authentication logic
class AuthViewModel() : ViewModel() {
  private val auth = FirebaseAuth.getInstance() // Firebase authentication instance
  private val _state = MutableStateFlow(AuthState()) // Internal state flow
  val state: StateFlow<AuthState> = _state.asStateFlow() // Public state flow

  // Checks current authentication status and updates state
  private fun checkAuthStatus() {
    val currentUser = auth.currentUser
    if (currentUser != null) {
      _state.update {
        it.copy(
            isLoading = false,
            isAuthenticated = true,
        )
      }
    } else {
      _state.update { it.copy(isLoading = false, isAuthenticated = false) }
    }
  }

  // Initialize ViewModel by checking auth status
  init {
    checkAuthStatus()
  }

  // Clears error state and message
  fun cleanError() {
    _state.update { it.copy(isError = false, errorMessage = null) }
  }

  // Signs out the user and updates state
  fun signOut(onSucess: () -> Unit = {}, onError: (String) -> Unit = {}) {
    viewModelScope.launch {
      try {
        auth.signOut()
        _state.update { it.copy(isLoading = false, isAuthenticated = false) }
        onSucess()
      } catch (e: Exception) {
        _state.update {
          it.copy(
              isLoading = false, isError = true, errorMessage = e.message, isAuthenticated = false)
        }
        onError(e.message ?: "Unknown error")
      }
    }
  }

  // Signs in the user with Google credentials
  fun signInWithGoogle(
      context: Context,
      credentialManager: CredentialManager,
      onSuccess: () -> Unit = {},
      onError: (String) -> Unit = {},
  ) {
    if (_state.value.isLoading) return
    viewModelScope.launch {
      try {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        // Create Google ID request option
        val googleIdOption = getSignInOptions(context)

        // Build credential request
        val request = signInRequest(googleIdOption)

        try {
          // Get credential from CredentialManager
          val response = getCredential(context, credentialManager, request)

          // Extract the Google ID token
          val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(response.credential.data)
          val idToken = googleIdTokenCredential.idToken

          // Create Firebase credential and sign in
          val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
          val authResult = auth.signInWithCredential(firebaseCredential).await()

          // Update UI state based on authentication result
          if (authResult.user != null) {
            _state.update { it.copy(isLoading = false, isAuthenticated = true) }
            onSuccess()
          } else {
            _state.update {
              it.copy(isLoading = false, errorMessage = "Authentication failed: No user returned")
            }
            onError("Authentication failed: No user returned")
          }
        } catch (e: GetCredentialCancellationException) {
          _state.update {
            it.copy(isLoading = false, errorMessage = "Failed to get credential: ${e.message}")
          }
          onError("Failed to get credential: ${e.message}")
        }
      } catch (e: Exception) {
        _state.update {
          it.copy(isLoading = false, errorMessage = "Authentication failed: ${e.message}")
        }
        onError("Authentication failed: ${e.message}")
      }
    }
  }

  // Helper: builds the Google Sign-In option
  private fun getSignInOptions(context: Context): GetSignInWithGoogleOption {
    // Create Google ID request option
    return GetSignInWithGoogleOption.Builder(context.getString(R.string.default_web_client_id))
        .build()
  }

  // Helper: builds the CredentialManager request
  private fun signInRequest(googleIdOption: GetSignInWithGoogleOption): GetCredentialRequest {
    // Build credential request
    return GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
  }

  // Helper: invokes CredentialManager to fetch a credential
  private suspend fun getCredential(
      context: Context,
      credentialManager: CredentialManager,
      request: GetCredentialRequest
  ): GetCredentialResponse {
    // Get credential from CredentialManager
    return credentialManager.getCredential(request = request, context = context)
  }
}
