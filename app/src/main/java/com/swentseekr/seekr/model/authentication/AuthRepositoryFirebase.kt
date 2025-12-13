package com.swentseekr.seekr.model.authentication

import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

/**
 * A Firebase implementation of [AuthRepository].
 *
 * Retrieves a Google ID token via Credential Manager and authenticates the user with Firebase. Also
 * handles sign-out and credential state clearing.
 *
 * @param context Used to launch the Credential Manager UI and load string resources.
 * @param credentialManager The [CredentialManager] used to retrieve credentials.
 * @param auth The [FirebaseAuth] instance for Firebase authentication.
 * @param helper A [GoogleSignInHelper] to extract Google ID token credentials and convert them to
 *   Firebase credentials.
 */
class AuthRepositoryFirebase(
    private val auth: FirebaseAuth = Firebase.auth,
    private val helper: GoogleSignInHelper = DefaultGoogleSignInHelper()
) : AuthRepository {

  /**
   * Signs in a user to Firebase using a Google credential.
   *
   * This method expects a [CustomCredential] containing a Google ID token. If the credential is
   * valid, it authenticates the user with Firebase and returns the corresponding [FirebaseUser].
   *
   * @param credential The credential returned by the Credential Manager.
   * @return [Result.success] containing the authenticated [FirebaseUser] on success, or
   *   [Result.failure] if authentication fails or the credential type is invalid.
   * @throws IllegalStateException wrapped in [Result.failure] if authentication fails due to
   *   invalid credentials or unexpected Firebase errors.
   */
  override suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser> {
    return try {
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val idToken = helper.extractIdTokenCredential(credential.data).idToken
        val firebaseCred = helper.toFirebaseCredential(idToken)

        // Sign in with Firebase
        val user =
            auth.signInWithCredential(firebaseCred).await().user
                ?: return Result.failure(
                    IllegalStateException("Login failed : Could not retrieve user information"))

        return Result.success(user)
      } else {
        return Result.failure(
            IllegalStateException("Login failed: Credential is not of type Google ID"))
      }
    } catch (e: Exception) {
      Result.failure(
          IllegalStateException("Login failed: ${e.localizedMessage ?: "Unexpected error."}"))
    }
  }

  /**
   * Signs out the currently authenticated user from Firebase.
   *
   * @return [Result.success] if the sign-out operation completes successfully, or [Result.failure]
   *   if an error occurs during sign-out.
   * @throws IllegalStateException wrapped in [Result.failure] if Firebase sign-out fails.
   */
  override fun signOut(): Result<Unit> {
    return try {
      // Firebase sign out
      auth.signOut()

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(
          IllegalStateException("Logout failed: ${e.localizedMessage ?: "Unexpected error."}"))
    }
  }
}
