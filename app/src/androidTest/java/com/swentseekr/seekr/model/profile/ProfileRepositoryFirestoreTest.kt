package com.swentseekr.seekr.model.profile

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.ui.profile.Profile
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import com.swentseekr.seekr.utils.FirebaseTestEnvironment.clearEmulatorData
import junit.framework.TestCase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileRepositoryFirestoreTest {
  private var repository = ProfileRepositoryProvider.repository
  private lateinit var auth: FirebaseAuth

  @Before
  fun setup() = runTest {
    FirebaseTestEnvironment.setup()
    if (FirebaseTestEnvironment.isEmulatorActive()) {
      clearEmulatorData()
    }

    auth = FirebaseAuth.getInstance()
    auth.signInAnonymously().await()

    val repo = ProfileRepositoryFirestore(FirebaseFirestore.getInstance())
    ProfileRepositoryProvider.repository = repo
  }

  @Test
  fun current_user_not_null() {
    val currentUser = auth.currentUser
    assertNotNull("FirebaseAuth currentUser should not be null", currentUser)
  }

  @Test
  fun canRetrieveProfile() = runTest {
    val uid = auth.currentUser?.uid!!
    val retrieved = repository.getProfile(uid)

    assertNotNull("Profile should be retrieved", retrieved)
    assertEquals(uid, retrieved?.uid)
    assertEquals("Tester", retrieved?.author?.pseudonym)
  }

  @Test
  fun canUpdateOwnProfile() = runTest {
    val uid = auth.currentUser!!.uid
    val profile =
        Profile(
            uid = uid,
            author = Author("Tester", "Bio", 0, 4.5, 4.0),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())
    repository.updateProfile(profile)

    val updated = profile.copy(author = profile.author.copy(pseudonym = "NewName"))
    repository.updateProfile(updated)

    val retrieved = repository.getProfile(uid)
    assertEquals("NewName", retrieved!!.author.pseudonym)
  }

  @Test
  fun getNonExistentProfileReturnsNull() = runTest {
    val profile = repository.getProfile("unknown")
    assertNull("Non-existent profile should return null", profile)
  }
}
