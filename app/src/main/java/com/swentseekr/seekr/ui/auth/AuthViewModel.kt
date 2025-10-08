package com.swentseekr.seekr.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.author.Author
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isError: Boolean = false,
    val isAuthenticated: Boolean = false,
    val author: Author? = null,
)

class AuthViewModel() : ViewModel() {
  private val auth = FirebaseAuth.getInstance()
  private val _state = MutableStateFlow(AuthState())
  val state: StateFlow<AuthState> = _state.asStateFlow()

  private fun checkAuthStatus() {
    val currentUser = auth.currentUser
    if (currentUser != null) {
      _state.value =
          AuthState(
              isLoading = false,
              isAuthenticated = true,
          )
    } else {
      _state.value = AuthState(isLoading = false, isAuthenticated = false)
    }
  }

  init {
    checkAuthStatus()
  }

  fun cleanError() {
    _state.value = _state.value.copy(isError = false, errorMessage = null)
  }

  fun signOut() {
    viewModelScope.launch {
      try {
        auth.signOut()
        _state.value = AuthState(isLoading = false, isAuthenticated = false)
      } catch (e: Exception) {
        _state.value =
            AuthState(
                isLoading = false,
                isError = true,
                errorMessage = e.message,
                isAuthenticated = false)
      }
    }
  }

  fun signInWithGoogle(
      context: Context,
      credentialManager: CredentialManager,
      onSucess: () -> Unit
  ) {
    viewModelScope.launch {
      try {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)

        // Create Google ID request option
        val googleIdOption =
            GetSignInWithGoogleOption.Builder(context.getString(R.string.default_web_client_id))
                .build()

        // Build credential request
        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        try {
          // Get credential from CredentialManager
          val response = credentialManager.getCredential(request = request, context = context)

          // Extract the Google ID token
          val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(response.credential.data)
          val idToken = googleIdTokenCredential.idToken

          // Create Firebase credential and sign in
          val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
          val authResult = auth.signInWithCredential(firebaseCredential).await()

          // Update UI state based on authentication result
          if (authResult.user != null) {
            _state.value = _state.value.copy(isLoading = false, isAuthenticated = true)
            onSucess()
          } else {
            _state.value =
                _state.value.copy(
                    isLoading = false, errorMessage = "Authentication failed: No user returned")
          }
        } catch (e: GetCredentialException) {
          Log.e("LoginViewModel", "Failed to get credential", e)
          _state.value =
              _state.value.copy(
                  isLoading = false, errorMessage = "Failed to get credential: ${e.message}")
        }
      } catch (e: Exception) {
        Log.e("LoginViewModel", "Authentication failed", e)
        _state.value =
            _state.value.copy(
                isLoading = false, errorMessage = "Authentication failed: ${e.message}")
      }
    }
  }
}
