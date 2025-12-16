package com.swentseekr.seekr.model.hunt

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.utils.FakeHuntsImageRepository
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import com.swentseekr.seekr.utils.FirebaseTestEnvironment.clearEmulatorData
import com.swentseekr.seekr.utils.HuntTestConstants
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Firestore integration tests for the HuntsRepository.
 *
 * This test suite validates CRUD operations on hunts, including ID generation,
 * retrieval, updates, deletions, and image handling logic, using the Firebase
 * test environment and emulator-backed Firestore.
 */


class HuntsRepositoryFirestoreTest {

  private lateinit var repository: HuntsRepository
  var hunt1 =
      Hunt(
          uid = HuntTestConstants.HUNT_UID_1,
          start = Location(40.7128, -74.0060, "New York"),
          end = Location(40.730610, -73.935242, "Brooklyn"),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = HuntTestConstants.TITLE_1,
          description = HuntTestConstants.DESCRIPTION_1,
          time = 2.5,
          distance = 5.0,
          difficulty = Difficulty.EASY,
          authorId = "0",
          mainImageUrl = "",
          reviewRate = 4.5)

  @Before
  fun setUp() = runTest {
    FirebaseTestEnvironment.setup()

    if (FirebaseTestEnvironment.isEmulatorActive()) {
      clearEmulatorData()
    }

    FirebaseAuth.getInstance().signInAnonymously().await()
    hunt1 = hunt1.copy(authorId = FirebaseAuth.getInstance().currentUser?.uid ?: "0")

    val db = FirebaseFirestore.getInstance()

    db.collection("hunts").get().await().documents.forEach { it.reference.delete().await() }

    repository = HuntsRepositoryFirestore(db = db, imageRepo = FakeHuntsImageRepository())
  }

  @Test
  fun generatesNewUniqueIds() = runTest {
    val firstId = repository.getNewUid()
    val secondId = repository.getNewUid()
    assertNotEquals(firstId, secondId)
    assertTrue(firstId.isNotEmpty())
    assertTrue(secondId.isNotEmpty())
  }

  @Test
  fun getNewUidReturnsUniqueIDs() = runTest {
    val numberIDs = 100
    val uids = (0 until 100).toSet<Int>().map { repository.getNewUid() }.toSet()
    assertEquals(uids.size, numberIDs)
  }

  @Test
  fun canAddHuntsToRepository() = runTest {
    repository.addHunt(hunt1)
    val hunts = repository.getAllHunts()
    assertEquals(1, hunts.size)
    assertEquals(hunt1, hunts[0])
  }

  @Test
  fun canAddMultipleHuntsToRepository() = runTest {
    val hunt2 = hunt1.copy(uid = HuntTestConstants.HUNT_UID_2, title = HuntTestConstants.TITLE_2)
    repository.addHunt(hunt1)
    repository.addHunt(hunt2)
    val hunts = repository.getAllHunts()
    assertEquals(2, hunts.size)
    assert(hunts.contains(hunt1))
    assert(hunts.contains(hunt2))
  }

  @Test
  fun getAllHuntsReturnsEmptyListWhenNoHuntsAdded() = runTest {
    val hunts = repository.getAllHunts()
    assertTrue(hunts.isEmpty())
  }

  @Test
  fun canGetAllHuntsToRepository() = runTest {
    val hunt2 = hunt1.copy(uid = HuntTestConstants.HUNT_UID_2, title = HuntTestConstants.TITLE_2)
    val hunt3 =
        hunt1.copy(
            uid = HuntTestConstants.HUNT_UID_3, title = HuntTestConstants.TITLE_3, authorId = "2")
    repository.addHunt(hunt1)
    repository.addHunt(hunt2)
    repository.addHunt(hunt3)
    val hunts = repository.getAllHunts()
    assertEquals(3, hunts.size)
    assert(hunts.contains(hunt1))
    assert(hunts.contains(hunt2))
    assert(hunts.contains(hunt3))
  }

  @Test
  fun canGetAllMyHuntsToRepository() = runTest {
    val hunt2 = hunt1.copy(uid = HuntTestConstants.HUNT_UID_2, title = HuntTestConstants.TITLE_2)
    val hunt3 =
        hunt1.copy(
            uid = HuntTestConstants.HUNT_UID_3, title = HuntTestConstants.TITLE_3, authorId = "2")
    repository.addHunt(hunt1)
    repository.addHunt(hunt2)
    repository.addHunt(hunt3)
    advanceUntilIdle()
    val hunts = repository.getAllMyHunts("3")
    assertEquals(0, hunts.size)
  }

  @Test
  fun canRetrieveAHuntByID() = runTest {
    repository.addHunt(hunt1)
    val storedHunt = repository.getHunt(hunt1.uid)
    assertEquals(storedHunt, hunt1)
  }

  @Test
  fun canRetrieveAHuntByIDWithMultipleHunts() = runTest {
    repository.addHunt(hunt1)
    val hunt2 =
        hunt1.copy(uid = HuntTestConstants.HUNT_UID_2, title = HuntTestConstants.TITLE_SECOND)
    repository.addHunt(hunt2)
    val hunt3 = hunt1.copy(uid = HuntTestConstants.HUNT_UID_3, title = HuntTestConstants.TITLE_3)
    repository.addHunt(hunt3)
    val hunt4 = hunt1.copy(uid = HuntTestConstants.HUNT_UID_4, title = HuntTestConstants.TITLE_4)
    repository.addHunt(hunt4)
    val storedHunt = repository.getHunt(hunt3.uid)
    assertEquals(storedHunt, hunt3)
  }

  @Test(expected = IllegalArgumentException::class)
  fun retrievingNonExistentHuntThrowsException() = runTest {
    repository.addHunt(hunt1)
    repository.getHunt(HuntTestConstants.HUNT_UID_NON_EXISTENT)
  }

  @Test
  fun canEditAHuntByID() = runTest {
    repository.addHunt(hunt1)
    val modifiedHunt =
        hunt1.copy(title = HuntTestConstants.TITLE_MODIFIED, status = HuntStatus.SPORT)
    repository.editHunt(hunt1.uid, modifiedHunt)
    assertEquals(1, repository.getAllHunts().size)
    val storedHunt = repository.getHunt(hunt1.uid)
    assertEquals(modifiedHunt, storedHunt)
  }

  @Test
  fun canEditAHuntByIDWithMultipleHunts() = runTest {
    repository.addHunt(hunt1)
    val hunt2 = hunt1.copy(uid = HuntTestConstants.HUNT_UID_2, title = "Second Hunt")
    repository.addHunt(hunt2)
    val hunt3 = hunt1.copy(uid = HuntTestConstants.HUNT_UID_3, title = HuntTestConstants.TITLE_3)
    repository.addHunt(hunt3)
    val modifiedHunt =
        hunt1.copy(title = HuntTestConstants.TITLE_MODIFIED, status = HuntStatus.SPORT)
    repository.editHunt(hunt1.uid, modifiedHunt)
    assertEquals(3, repository.getAllHunts().size)
    val storedHunt = repository.getHunt(hunt1.uid)
    assertEquals(modifiedHunt, storedHunt)
  }

  @Test
  fun canDeleteAHuntByID() = runTest {
    repository.addHunt(hunt1)
    assertEquals(1, repository.getAllHunts().size)
    repository.deleteHunt(hunt1.uid)
    assertEquals(0, repository.getAllHunts().size)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun canDeleteAHuntByIDWithMultipleHunts() = runTest {
    repository.addHunt(hunt1)
    val hunt2 = hunt1.copy(uid = HuntTestConstants.HUNT_UID_2, title = "Second Hunt")
    repository.addHunt(hunt2)
    val hunt3 = hunt1.copy(uid = HuntTestConstants.HUNT_UID_3, title = HuntTestConstants.TITLE_3)
    repository.addHunt(hunt3)
    assertEquals(3, repository.getAllHunts().size)
    repository.deleteHunt(hunt2.uid)
    advanceUntilIdle()
    val hunts = repository.getAllHunts()
    assertEquals(2, hunts.size)
    val expectedHunts = setOf(hunt1, hunt3)
    assertEquals(expectedHunts, repository.getAllHunts().toSet())
  }

  @Test
  fun editHunt_updatesImagesCorrectly_withDeletion_andAddition() = runTest {
    val fakeImageRepo = com.swentseekr.seekr.utils.FakeHuntsImageRepository()
    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    val repo = HuntsRepositoryFirestore(db, fakeImageRepo)

    val original =
        hunt1.copy(
            uid = HuntTestConstants.HUNT_UID_EDIT,
            mainImageUrl = HuntTestConstants.OLD_MAIN_URL,
            otherImagesUrls = listOf(HuntTestConstants.OLD_URL_1, HuntTestConstants.OLD_URL_2))

    repo.addHunt(original)
    advanceUntilIdle()

    val updatedHunt =
        original.copy(
            title = HuntTestConstants.TITLE_UPDATED,
            description = HuntTestConstants.DESCRIPTION_UPDATED)

    val newMainUri = Uri.parse(HuntTestConstants.NEW_MAIN_URI)
    val newOtherUris =
        listOf(Uri.parse(HuntTestConstants.NEW_OTHER_1), Uri.parse(HuntTestConstants.NEW_OTHER_2))

    val removed = listOf(HuntTestConstants.OLD_URL_1)

    repo.editHunt(
        huntID = "editTest",
        newValue = updatedHunt,
        mainImageUri = newMainUri,
        addedOtherImages = newOtherUris,
        removedOtherImages = removed)

    advanceUntilIdle()

    val stored = repo.getHunt("editTest")

    assertTrue(
        HuntTestConstants.ASSERT_MAIN_IMAGE_MESSAGE,
        stored.mainImageUrl.startsWith("fake://main_image_url_for_editTest"))

    assertFalse(stored.otherImagesUrls.contains(HuntTestConstants.OLD_URL_1))
    assertTrue(stored.otherImagesUrls.contains(HuntTestConstants.OLD_URL_2))

    assertTrue(stored.otherImagesUrls.contains(HuntTestConstants.FAKE_OTHER_0))
    assertTrue(stored.otherImagesUrls.contains(HuntTestConstants.FAKE_OTHER_1))

    assertEquals(3, stored.otherImagesUrls.size)

    assertEquals(HuntTestConstants.TITLE_UPDATED, stored.title)
    assertEquals(HuntTestConstants.DESCRIPTION_UPDATED, stored.description)
  }

  @Test(expected = Exception::class)
  fun editHunt_fails_when_image_repo_fails() = runTest {
    val imageRepo = FakeHuntsImageRepository(shouldFail = true)
    val db = FirebaseFirestore.getInstance()
    val repo = HuntsRepositoryFirestore(db, imageRepo)

    val hunt = hunt1.copy(uid = "errorTest")
    repo.addHunt(hunt)

    val updated = hunt.copy(title = "Should fail")

    repo.editHunt(
        huntID = "errorTest", newValue = updated, mainImageUri = Uri.parse("file://image"))
  }
}
