package com.swentseekr.seekr.model.authentication

import androidx.credentials.Credential
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk

class FakeAuthRepository(private val shouldFail: Boolean = false) : AuthRepository {

  private var signedInUser: FirebaseUser? = null

  override suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser> {
    return if (shouldFail) {
      Result.failure(Exception("Fake sign-in failure"))
    } else {
      val fakeUser = mockk<FirebaseUser>(relaxed = true)
      every { fakeUser.uid } returns "fakeUid123"
      every { fakeUser.email } returns "fakeuser@example.com"

      signedInUser = fakeUser
      Result.success(fakeUser)
    }
  }

  override fun signOut(): Result<Unit> {
    return if (shouldFail) {
      Result.failure(Exception("Fake sign-out failure"))
    } else {
      signedInUser = null
      Result.success(Unit)
    }
  }
}
