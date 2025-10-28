package com.swentseekr.seekr.model.authentication

import androidx.credentials.Credential
import com.google.firebase.auth.FirebaseUser
import kotlin.Result

interface AuthRepository {
  suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser>

  fun signOut(): Result<Unit>
}
