package com.swentseekr.seekr.model.profile

import com.google.firebase.auth.FirebaseAuth
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
  private val repository = ProfileRepositoryProvider.repository
  private lateinit var auth: FirebaseAuth

  @Before
  fun setup() = runTest {
    FirebaseTestEnvironment.setup()
    if (FirebaseTestEnvironment.isEmulatorActive()) {
      clearEmulatorData()
    }

    auth = FirebaseAuth.getInstance()
    auth.signInAnonymously().await()
  }

  @Test
  fun current_user_not_null() {
    val currentUser = auth.currentUser
    assertNotNull("FirebaseAuth currentUser should not be null", currentUser)
  }

  @Test
  fun canAddAndRetrieveProfile() = runTest {
    val uid = auth.currentUser?.uid!!
    val profile =
        Profile(
            uid = uid,
            author = Author("Tester", "Bio", 0, 4.5, 4.0),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())

    repository.updateProfile(profile)
    val retrieved = repository.getProfile(uid)

    assertNotNull("Profile should be retrieved", retrieved)
    assertEquals(uid, retrieved?.uid)
    assertEquals("Tester", retrieved?.author?.pseudonym)
  }

  @Test
  fun canUpdateProfile() = runTest {
    val uid = auth.currentUser?.uid!!
    val profile =
        Profile(
            uid = uid,
            author = Author("Tester", "Bio", 0, 4.5, 4.0),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())
    repository.updateProfile(profile)

    val updatedProfile = profile.copy(author = profile.author.copy(pseudonym = "NewName"))
    repository.updateProfile(updatedProfile)

    val retrieved = repository.getProfile(uid)
    assertNotNull(retrieved)
    assertEquals("NewName", retrieved?.author?.pseudonym)
  }

  @Test
  fun getNonExistentProfileReturnsNull() = runTest {
    val profile = repository.getProfile("unknownUser")
    assertNull("Non-existent profile should return null", profile)
  }

  @Test
  fun canAddAndRetrieveProfile_withMockData() = runTest {
    val uid = auth.currentUser?.uid!!
    val profile = mockProfileData().copy(uid = uid)
    repository.updateProfile(profile)

    val retrieved = repository.getProfile(uid)

    assertNotNull(retrieved)
    assertEquals(uid, retrieved?.uid)
    assertEquals(profile.author.pseudonym, retrieved?.author?.pseudonym)
    assertEquals(1, retrieved?.myHunts?.size)
    assertEquals("hunt123", retrieved?.myHunts?.first()?.uid)
  }
}
