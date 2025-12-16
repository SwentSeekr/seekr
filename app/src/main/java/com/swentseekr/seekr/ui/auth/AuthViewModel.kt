package com.swentseekr.seekr.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.authentication.AuthRepository
import com.swentseekr.seekr.model.authentication.AuthRepositoryFirebase
import com.swentseekr.seekr.model.authentication.OnboardingHandler
import com.swentseekr.seekr.model.profile.ProfileRepository
import com.swentseekr.seekr.model.profile.ProfileRepositoryFirestore
import com.swentseekr.seekr.ui.auth.AuthViewModelMessages.DEFAULT_SIGN_IN_FAILURE
import com.swentseekr.seekr.ui.auth.AuthViewModelMessages.NO_GOOGLE_ACCOUNT
import com.swentseekr.seekr.ui.auth.AuthViewModelMessages.NO_PROVIDER_AVAILABLE
import com.swentseekr.seekr.ui.auth.AuthViewModelMessages.SIGN_IN_CANCELLED
import com.swentseekr.seekr.ui.auth.AuthViewModelMessages.credentialFailure
import com.swentseekr.seekr.ui.auth.AuthViewModelMessages.unexpectedFailure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the UI state for authentication operations.
 *
 * @property isLoading Indicates if an authentication operation is in progress.
 * @property user The currently signed-in Firebase user, if any.
 * @property errorMsg Any error message to display in the UI.
 * @property signedOut True if the user is signed out.
 * @property needsOnboarding True if the signed-in user still needs to complete onboarding.
 */
data class AuthUIState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val errorMsg: String? = null,
    val signedOut: Boolean = false,
    val needsOnboarding: Boolean = false
)

/**
 * ViewModel responsible for handling user authentication and onboarding state.
 *
 * @param repository Repository used for authentication operations.
 * @param profileRepository Repository used for profile and onboarding operations.
 * @param auth FirebaseAuth instance for managing authentication state.
 */
class AuthViewModel(
    private val repository: AuthRepository = AuthRepositoryFirebase(),
    private val profileRepository: ProfileRepository =
        ProfileRepositoryFirestore(
            db = FirebaseFirestore.getInstance(),
            auth = FirebaseAuth.getInstance(),
            storage = FirebaseStorage.getInstance()),
    private val auth: FirebaseAuth = Firebase.auth
) : ViewModel(), OnboardingHandler {

  private val _uiState = MutableStateFlow(AuthUIState())
  val uiState: StateFlow<AuthUIState> = _uiState

  private val authListener =
      FirebaseAuth.AuthStateListener { firebaseAuth ->
        val current = firebaseAuth.currentUser
        _uiState.update { it.copy(user = current, signedOut = (current == null)) }
      }

  init {
    _uiState.update { it.copy(user = auth.currentUser, signedOut = (auth.currentUser == null)) }
    auth.addAuthStateListener(authListener)
  }

  override fun onCleared() {
    auth.removeAuthStateListener(authListener)
    super.onCleared()
  }

    /** Clears the current error message in the UI state. */
    fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
  }

  private fun getSignInOptions(context: Context) =
      GetSignInWithGoogleOption.Builder(
              serverClientId = context.getString(R.string.default_web_client_id))
          .build()

  private fun signInRequest(signInOptions: GetSignInWithGoogleOption) =
      GetCredentialRequest.Builder().addCredentialOption(signInOptions).build()

    /**
     * Retrieves a credential using the provided [CredentialManager] and [GetCredentialRequest].
     *
     * @param context Android context.
     * @param request Credential request configuration.
     * @param credentialManager CredentialManager instance.
     * @return The retrieved credential.
     * @throws GetCredentialCancellationException If the user cancels the sign-in flow.
     * @throws NoCredentialException If no credential is available.
     * @throws GetCredentialException For other credential retrieval failures.
     */
  private suspend fun getCredential(
      context: Context,
      request: GetCredentialRequest,
      credentialManager: CredentialManager
  ) = credentialManager.getCredential(context, request).credential

    /**
     * Initiates Google sign-in for the user.
     *
     * Updates [uiState] to reflect loading, success, or failure states.
     *
     * @param context Android context.
     * @param credentialManager CredentialManager instance for retrieving credentials.
     * @throws GetCredentialCancellationException If the user cancels the sign-in flow.
     * @throws NoCredentialException If no credential is available.
     * @throws GetCredentialException For other credential retrieval failures.
     */
  fun signIn(context: Context, credentialManager: CredentialManager) {
    if (_uiState.value.isLoading) return

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, errorMsg = null) }

      val signInOptions = getSignInOptions(context)
      val request = signInRequest(signInOptions)

      try {
        val credential = getCredential(context, request, credentialManager)

        repository.signInWithGoogle(credential).fold({ user ->
          _uiState.update {
            it.copy(isLoading = false, user = user, errorMsg = null, signedOut = false)
          }

          user.uid.let { uid ->
            viewModelScope.launch {
              val needs = profileRepository.checkUserNeedsOnboarding(uid)
              _uiState.update { it.copy(needsOnboarding = needs) }
            }
          }
        }) { failure ->
          val message = failure.localizedMessage ?: DEFAULT_SIGN_IN_FAILURE
          _uiState.update {
            it.copy(isLoading = false, errorMsg = message, signedOut = true, user = null)
          }
        }
      } catch (e: GetCredentialCancellationException) {
        _uiState.update {
          it.copy(isLoading = false, errorMsg = SIGN_IN_CANCELLED, signedOut = true, user = null)
        }
      } catch (e: NoCredentialException) {
        _uiState.update {
          it.copy(isLoading = false, errorMsg = NO_GOOGLE_ACCOUNT, signedOut = true, user = null)
        }
      } catch (e: GetCredentialException) {
        val message =
            when {
              e.localizedMessage?.contains(AuthViewModelMessages.NO_PROVIDER, ignoreCase = true) ==
                  true -> NO_PROVIDER_AVAILABLE
              else -> credentialFailure(e.localizedMessage)
            }
        _uiState.update {
          it.copy(isLoading = false, errorMsg = message, signedOut = true, user = null)
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
              isLoading = false,
              errorMsg = unexpectedFailure(e.localizedMessage),
              signedOut = true,
              user = null)
        }
      }
    }
  }

    /**
     * Completes the onboarding process for a user.
     *
     * @param userId The UID of the user completing onboarding.
     * @param pseudonym The pseudonym to set for the user.
     * @param bio The bio to set for the user.
     */
  override fun completeOnboarding(userId: String, pseudonym: String, bio: String) {
    viewModelScope.launch {
      profileRepository.completeOnboarding(userId, pseudonym, bio)
      _uiState.update { it.copy(needsOnboarding = false) }
    }
  }
}
