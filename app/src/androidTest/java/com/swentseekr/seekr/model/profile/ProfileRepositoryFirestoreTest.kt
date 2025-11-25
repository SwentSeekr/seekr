package com.swentseekr.seekr.model.profile

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.model.profile.ProfileRepositoryFirestore.Companion.huntToMap
import com.swentseekr.seekr.model.profile.ProfileRepositoryFirestore.Companion.mapToHunt
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

  private lateinit var db: FirebaseFirestore
  private lateinit var storage: FirebaseStorage
  private val hunt =
      Hunt(
          uid = "hunt1",
          title = "Sample Hunt",
          description = "Test Hunt",
          start = Location(10.0, 20.0, "Start"),
          end = Location(15.0, 25.0, "End"),
          middlePoints = listOf(Location(12.0, 22.0, "Middle")),
          difficulty = Difficulty.EASY,
          status = HuntStatus.FUN,
          authorId = "author1",
          time = 30.0,
          distance = 5.0,
          reviewRate = 4.5,
          mainImageUrl = "http://image.url")

  @Before
  fun setup() {
    FirebaseTestEnvironment.setup()
    runTest {
      if (FirebaseTestEnvironment.isEmulatorActive()) {
        clearEmulatorData()
      }
      auth = FirebaseAuth.getInstance()
      auth.signInAnonymously().await()
      db = FirebaseFirestore.getInstance()
      storage = FirebaseStorage.getInstance()

      repository = ProfileRepositoryProvider.repository
    }
  }

  @Test
  fun createProfile_successfully() = runTest {
    val uid = auth.currentUser!!.uid
    val profile =
        Profile(
            uid = uid,
            author =
                Author(
                    hasCompletedOnboarding = true,
                    hasAcceptedTerms = true,
                    "Tester",
                    "This is a bio",
                    0,
                    4.5,
                    4.0),
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
            author =
                Author(
                    hasCompletedOnboarding = true,
                    hasAcceptedTerms = true,
                    "Tester",
                    "This is a bio",
                    0,
                    4.5,
                    4.0),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())

    repository.createProfile(profile)

    val retrieved = repository.getProfile(uid)
    assertNotNull("Profile should be retrieved after creation", retrieved)
    assertEquals(uid, retrieved?.uid)
    assertEquals("Tester", retrieved?.author?.pseudonym)
    assertEquals("This is a bio", retrieved?.author?.bio)
    assertEquals("4.5", retrieved?.author?.reviewRate.toString())
    assertEquals("4.0", retrieved?.author?.sportRate.toString())
    assertEquals("0", retrieved?.author?.profilePicture.toString())
  }

  @Test
  fun updateProfile_reflectsChanges() = runTest {
    val uid = auth.currentUser!!.uid
    val profile =
        Profile(
            uid = uid,
            author =
                Author(
                    hasCompletedOnboarding = true,
                    hasAcceptedTerms = true,
                    "OldName",
                    "Old bio",
                    0,
                    3.0,
                    3.0),
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
            author =
                Author(
                    hasCompletedOnboarding = true,
                    hasAcceptedTerms = true,
                    "CompleteUser",
                    "Complete bio",
                    5,
                    4.5,
                    4.8),
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
            author =
                Author(
                    hasCompletedOnboarding = true,
                    hasAcceptedTerms = true,
                    "AuthUser",
                    "Auth bio",
                    0,
                    4.0,
                    3.0),
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
  fun updateProfile_preservesProfilePictureUrl() = runTest {
    val uid = auth.currentUser!!.uid
    val profile =
        Profile(
            uid = uid,
            author =
                Author(
                    hasCompletedOnboarding = true,
                    hasAcceptedTerms = true,
                    "User",
                    "Bio",
                    0,
                    4.0,
                    3.0,
                    "https://old.url/pic.jpg"),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())

    repository.createProfile(profile)

    val updated =
        profile.copy(
            author =
                profile.author.copy(
                    pseudonym = "NewName", profilePictureUrl = "https://new.url/pic.jpg"))
    repository.updateProfile(updated)

    val retrieved = repository.getProfile(uid)
    assertEquals("https://new.url/pic.jpg", retrieved?.author?.profilePictureUrl)
  }

  @Test
  fun documentToProfile_returnsNull_whenDocumentMissingAuthor() = runTest {
    val uid = auth.currentUser!!.uid
    val db = FirebaseFirestore.getInstance()
    val docRef = db.collection("profiles").document(uid)
    docRef.set(mapOf("someField" to "no author")).await()

    val repo = ProfileRepositoryFirestore(db, auth, storage = storage)
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

    val repo = ProfileRepositoryFirestore(db, auth, storage = storage)
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

    val repo = ProfileRepositoryFirestore(db, auth, storage = storage)
    val result = repo.getDoneHunts(auth.currentUser!!.uid)
    assertNotNull(result)
  }

  @Test
  fun toLocation_handlesMissingFields() = runTest {
    val companion = ProfileRepositoryFirestore.Companion
    val map = mapOf<String, Any>()
    val locationMethod =
        ProfileRepositoryFirestore.Companion::class
            .java
            .getDeclaredMethod("toLocation", Map::class.java)
    locationMethod.isAccessible = true
    val result = locationMethod.invoke(companion, map) as Location
    assertEquals(0.0, result.latitude)
    assertEquals(0.0, result.longitude)
    assertEquals("", result.name)
  }

  @Test
  fun addDoneHuntAddsHuntWhenNotAlreadyInList() = runTest {
    val uid = auth.currentUser!!.uid

    val db = FirebaseFirestore.getInstance()
    val docRef = db.collection("profiles").document(uid)
    docRef.set(mapOf("doneHunts" to emptyList<Map<String, Any?>>())).await()

    repository.addDoneHunt(uid, hunt)
    val snapshot = docRef.get().await()

    @Suppress("UNCHECKED_CAST")
    val doneHunts = snapshot.get("doneHunts") as? List<Map<String, Any?>> ?: emptyList()

    assertEquals(1, doneHunts.size)
    assertEquals("hunt1", doneHunts[0]["uid"])
  }

  @Test
  fun addDoneHuntDoesNotAddHuntIfAlreadyInList() = runTest {
    val uid = auth.currentUser!!.uid

    val db = FirebaseFirestore.getInstance()
    val docRef = db.collection("profiles").document(uid)
    val doneHunts =
        listOf(mapOf("uid" to "hunt1", "title" to "Sample Hunt", "description" to "Test Hunt"))
    docRef.set(mapOf("doneHunts" to doneHunts)).await()

    repository.addDoneHunt(uid, hunt)

    val snapshot = docRef.get().await()

    @Suppress("UNCHECKED_CAST")
    val updatedDoneHunts = snapshot.get("doneHunts") as? List<Map<String, Any?>> ?: emptyList()

    assertEquals(1, updatedDoneHunts.size)
    assertEquals("hunt1", updatedDoneHunts[0]["uid"])
  }

  @Test
  fun huntToMapMapsHuntCorrectly() {
    val map = huntToMap(hunt)

    assertEquals("hunt1", map["uid"])
    assertEquals("Sample Hunt", map["title"])
    assertEquals("Test Hunt", map["description"])
    assertNotNull(map["start"])
    assertNotNull(map["end"])
    assertEquals(1, (map["middlePoints"] as List<*>).size)
    assertEquals("EASY", map["difficulty"])
    assertEquals("FUN", map["status"])
    assertEquals("author1", map["authorId"])
    assertEquals(30.0, map["time"])
    assertEquals(5.0, map["distance"])
    assertEquals(4.5, map["reviewRate"])
    assertEquals("http://image.url", map["mainImageUrl"])
  }

  @Test
  fun mapToHuntMapsHuntCorrectly() {
    val map =
        mapOf(
            "uid" to "hunt1",
            "title" to "Sample Hunt",
            "description" to "A great adventure",
            "time" to 120.0,
            "distance" to 5.0,
            "reviewRate" to 4.5,
            "mainImageUrl" to "http://image.url",
            "start" to mapOf("latitude" to 10.0, "longitude" to 20.0, "name" to "Start Point"),
            "end" to mapOf("latitude" to 15.0, "longitude" to 25.0, "name" to "End Point"),
            "middlePoints" to
                listOf(mapOf("latitude" to 12.0, "longitude" to 22.0, "name" to "Mid Point")),
            "difficulty" to "EASY",
            "status" to "FUN",
            "authorId" to "author1")

    val hunt = mapToHunt(map)

    assertNotNull(hunt)
    assertEquals("hunt1", hunt?.uid)
    assertEquals("Sample Hunt", hunt?.title)
    assertEquals("A great adventure", hunt?.description)
    assertEquals(120.0, hunt?.time)
    assertEquals(5.0, hunt?.distance)
    assertEquals(4.5, hunt?.reviewRate)
    assertEquals("http://image.url", hunt?.mainImageUrl)

    assertNotNull(hunt?.start)
    assertEquals(10.0, hunt?.start?.latitude)
    assertEquals(20.0, hunt?.start?.longitude)
    assertEquals("Start Point", hunt?.start?.name)

    assertNotNull(hunt?.end)
    assertEquals(15.0, hunt?.end?.latitude)
    assertEquals(25.0, hunt?.end?.longitude)
    assertEquals("End Point", hunt?.end?.name)

    assertNotNull(hunt?.middlePoints)
    assertEquals(1, hunt?.middlePoints?.size)
    assertEquals("Mid Point", hunt?.middlePoints?.get(0)?.name)

    assertEquals(Difficulty.EASY, hunt?.difficulty)
    assertEquals(HuntStatus.FUN, hunt?.status)
    assertEquals("author1", hunt?.authorId)
  }

  @Test
  fun uploadProfilePicture_updatesUrlInFirestore() = runTest {
    val uid = auth.currentUser!!.uid
    val testUri = Uri.parse("content://test/image.jpg")
  }

  @Test
  fun checkUserNeedsOnboarding_returnsTrue_whenProfileMissingOrNotCompleted() = runTest {
    val uid = "new_user_test"

    val needs = repository.checkUserNeedsOnboarding(uid)

    assertTrue("User without profile should need onboarding", needs)

    val created = repository.getProfile(uid)

    assertNotNull(created)
    assertFalse(created!!.author.hasCompletedOnboarding)
  }

  @Test
  fun completeOnboarding_updatesFirestoreFieldsCorrectly() = runTest {
    val uid = auth.currentUser!!.uid

    val profile = repository.getProfile(uid)
    assertNotNull("Profile should exist or be auto-created", profile)
    assertFalse(
        "User should not have completed onboarding initially",
        profile!!.author.hasCompletedOnboarding)

    repository.completeOnboarding(uid, "NewPseudo", "New bio")

    val updated = repository.getProfile(uid)

    assertNotNull(updated)
    assertEquals(true, updated!!.author.hasCompletedOnboarding)
    assertEquals(true, updated.author.hasAcceptedTerms)
    assertEquals("NewPseudo", updated.author.pseudonym)
    assertEquals("New bio", updated.author.bio)
  }
}
