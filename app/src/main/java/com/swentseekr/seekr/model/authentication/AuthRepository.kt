package com.swentseekr.seekr.model.authentication

import androidx.credentials.Credential
import com.google.firebase.auth.FirebaseUser
import kotlin.Result

/**
 * Repository responsible for authentication-related operations.
 *
 * This interface abstracts the authentication mechanism used by the app, allowing different
 * implementations (e.g. Firebase, mock, or test).
 */
interface AuthRepository {

  /**
   * Signs in a user using Google authentication credentials.
   *
   * This function performs an asynchronous authentication request and returns a [Result] containing
   * the authenticated [FirebaseUser] on success, or a failure with the corresponding exception on
   * error.
   *
   * @param credential Google authentication credential obtained from the sign-in flow.
   * @return [Result] containing the authenticated [FirebaseUser] if successful, or a failure if the
   *   sign-in process fails.
   */
  suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser>

  /**
   * Signs out the currently authenticated user.
   *
   * @return [Result.success] if the sign-out operation completes successfully, or [Result.failure]
   *   if an error occurs.
   */
  fun signOut(): Result<Unit>
}
