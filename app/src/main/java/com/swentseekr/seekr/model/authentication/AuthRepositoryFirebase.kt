package com.swentseekr.seekr.model.authentication

import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
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

  fun getGoogleSignInOption(serverClientId: String) =
      GetSignInWithGoogleOption.Builder(serverClientId = serverClientId).build()

  override suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser> {
    return try {
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val idToken = helper.extractIdTokenCredential(credential.data).idToken
        val firebaseCred = helper.toFirebaseCredential(idToken)

        // Sign in with Firebase
        val user =
            auth.signInWithCredential(firebaseCred).await().user
                ?: return Result.failure(
                    IllegalStateException(
                        AuthRepositoryFirebaseConstantsString.ERROR_INFO_NOT_FOUND))

        return Result.success(user)
      } else {
        return Result.failure(
            IllegalStateException(AuthRepositoryFirebaseConstantsString.ERROR_CREDENTIAL))
      }
    } catch (e: Exception) {
      Result.failure(
          IllegalStateException(
              "${AuthRepositoryFirebaseConstantsString.ERROR_FAIL_LOGIN} ${e.localizedMessage ?: AuthRepositoryFirebaseConstantsString.UNEXPECTED_ERROR}"))
    }
  }

  override fun signOut(): Result<Unit> {
    return try {
      // Firebase sign out
      auth.signOut()

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(
          IllegalStateException(
              "${AuthRepositoryFirebaseConstantsString.ERROR_FAIL_LOGIN} ${e.localizedMessage ?: AuthRepositoryFirebaseConstantsString.UNEXPECTED_ERROR}"))
    }
  }
}
