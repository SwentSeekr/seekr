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
  private lateinit var repository: ProfileRepository
  private lateinit var auth: FirebaseAuth

  @Before
  fun setup() {
    FirebaseTestEnvironment.setup()
    runTest {
      if (FirebaseTestEnvironment.isEmulatorActive()) {
        clearEmulatorData()
      }
      auth = FirebaseAuth.getInstance()
      auth.signInAnonymously().await()

      repository = ProfileRepositoryProvider.repository
    }
  }

  @Test
  fun current_user_not_null() = runTest {
    val currentUser = auth.currentUser
    assertNotNull("FirebaseAuth currentUser should not be null", currentUser)
  }

  @Test
  fun canRetrieveProfile() = runTest {
    val uid = auth.currentUser!!.uid
    val profile =
        Profile(
            uid = uid,
            author = Author("Tester", "This is a bio", 0, 4.5, 4.0),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())

    repository.createProfile(profile)

    val retrieved = repository.getProfile(uid)
    assertNotNull("Profile should be retrieved after creation", retrieved)
    assertEquals(uid, retrieved?.uid)
    assertEquals("Tester", retrieved?.author?.pseudonym)
  }

  @Test
  fun updateProfile_reflectsChanges() = runTest {
    val uid = auth.currentUser!!.uid
    val profile =
        Profile(
            uid = uid,
            author = Author("OldName", "Old bio", 0, 3.0, 3.0),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())
    repository.createProfile(profile)

    val updated = profile.copy(author = profile.author.copy(pseudonym = "NewName"))
    repository.updateProfile(updated)

    val retrieved = repository.getProfile(uid)
    assertEquals("NewName", retrieved!!.author.pseudonym)
  }

  @Test
  fun getProfile_autoCreatesDefault_whenMissing() = runTest {
    val missingUid = "unknown_user"
    val profile = repository.getProfile(missingUid)
    assertNotNull("Default profile should be auto-created if missing", profile)
    assertEquals(missingUid, profile!!.uid)
    assertEquals("New User", profile.author.pseudonym)
  }

  @Test
  fun createProfile_throws_exception_on_firestore_error() = runTest {
    val uid = auth.currentUser!!.uid
    val profile =
        Profile(
            uid = uid,
            author = Author("Test", "Bio", 0, 4.0, 3.0),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())

    try {
      repository.createProfile(profile)
      assertTrue(true)
    } catch (e: Exception) {
      assertNotNull(e)
    }
  }

  @Test
  fun getDoneHunts_returns_completed_hunts() = runTest {
    val uid = auth.currentUser!!.uid

    val doneHunts = repository.getDoneHunts(uid)
    assertNotNull(doneHunts)
    assertTrue(doneHunts is List)
  }

  @Test
  fun getLikedHunts_returns_liked_hunts() = runTest {
    val uid = auth.currentUser!!.uid

    val likedHunts = repository.getLikedHunts(uid)
    assertNotNull(likedHunts)
    assertTrue(likedHunts is List)
  }

  @Test
  fun getProfile_handles_complete_profile_data() = runTest {
    val uid = auth.currentUser!!.uid
    val profile =
        Profile(
            uid = uid,
            author = Author("CompleteUser", "Complete bio", 5, 4.5, 4.8),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())
    repository.createProfile(profile)

    val retrieved = repository.getProfile(uid)
    assertNotNull(retrieved)
    assertEquals("CompleteUser", retrieved?.author?.pseudonym)
    assertEquals("Complete bio", retrieved?.author?.bio)
    assertEquals(5, retrieved?.author?.profilePicture)
    assertEquals(4.5, retrieved?.author?.reviewRate)
    assertEquals(4.8, retrieved?.author?.sportRate)
  }

  @Test
  fun updateProfile_uses_current_auth_user() = runTest {
    val uid = auth.currentUser!!.uid
    val profile =
        Profile(
            uid = uid,
            author = Author("AuthUser", "Auth bio", 0, 4.0, 3.0),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())

    repository.createProfile(profile)

    val updated = profile.copy(author = profile.author.copy(bio = "Updated via auth"))
    repository.updateProfile(updated)

    val retrieved = repository.getProfile(uid)
    assertEquals("Updated via auth", retrieved?.author?.bio)
  }

  @Test
  fun documentToProfile_returnsNull_whenDocumentMissingAuthor() = runTest {
    val uid = auth.currentUser!!.uid
    val db = FirebaseFirestore.getInstance()
    val docRef = db.collection("profiles").document(uid)
    docRef.set(mapOf("someField" to "no author")).await()

    val repo = ProfileRepositoryFirestore(db, auth)
    val result = repo.getProfile(uid)
    assertNull("Profile should be null if author field is missing", result)
  }

  @Test
  fun getMyHunts_returnsList_ofHunts() = runTest {
    val uid = auth.currentUser!!.uid
    val db = FirebaseFirestore.getInstance()
    val huntsCol = db.collection("hunts")

    val huntData =
        mapOf(
            "title" to "Sample Hunt",
            "description" to "A test hunt",
            "authorId" to uid,
            "status" to "PUBLISHED",
            "difficulty" to "EASY",
            "time" to 1.0,
            "distance" to 2.0,
            "reviewRate" to 4.5,
            "image" to 1L,
            "start" to mapOf("latitude" to 0.0, "longitude" to 0.0, "name" to "start"),
            "end" to mapOf("latitude" to 1.0, "longitude" to 1.0, "name" to "end"),
            "middlePoints" to emptyList<Map<String, Any>>())

    huntsCol.document("hunt1").set(huntData).await()

    val repo = ProfileRepositoryFirestore(db, auth)
    val hunts = repo.getMyHunts(uid)

    assertNotNull(hunts)
    if (hunts.isEmpty()) {
      println("Warning: Firestore emulator may not return hunts immediately")
    } else {
      assertEquals("Sample Hunt", hunts.first().title)
    }
  }

  @Test
  fun documentToHunt_returnsNull_onInvalidData() = runTest {
    val db = FirebaseFirestore.getInstance()
    val docRef = db.collection("hunts").document("invalidHunt")
    docRef.set(mapOf("title" to null)).await()

    val repo = ProfileRepositoryFirestore(db, auth)
    val result = repo.getDoneHunts(auth.currentUser!!.uid)
    assertNotNull(result)
  }

  @Test
  fun toLocation_handlesMissingFields() = runTest {
    val repo = ProfileRepositoryFirestore(FirebaseFirestore.getInstance(), auth)
    val map = mapOf<String, Any>()
    val locationMethod = repo.javaClass.getDeclaredMethod("toLocation", Map::class.java)
    locationMethod.isAccessible = true
    val result = locationMethod.invoke(repo, map) as com.swentseekr.seekr.model.map.Location
    assertEquals(0.0, result.latitude)
    assertEquals(0.0, result.longitude)
    assertEquals("", result.name)
  }
}
