package com.swentseekr.seekr.model.hunt

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreConstantsStrings.FIELD_HUNT_ID
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreConstantsStrings.HUNTS_COLLECTION_PATH
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreConstantsStrings.HUNT_REVIEW_REPLY_COLLECTION_PATH
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.ADD_FAIL_CLEANUP_HUNT_ID
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.ADD_WITH_OTHERS_HUNT_ID
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.BAD_DOC_ID
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.EDIT_TEST_HUNT_ID
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.ERROR_TEST_HUNT_ID
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.FILE_MAIN
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.FILE_OTHER_1
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.FILE_OTHER_2
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.HUNT_CASCADE_ID
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.IMAGE_URI
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.PHOTO_1
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.PHOTO_2
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.REPLY_ID_1
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.REPLY_ID_2
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.REVIEW_ID_1
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.SECOND_HUNT_TITLE
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreTestConstants.SHOULD_FAIL_TITLE
import com.swentseekr.seekr.model.hunt.review.HuntReviewReplyFirestoreConstants.FIELD_REVIEW_ID
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
    val uids = (0 until numberIDs).map { repository.getNewUid() }.toSet()
    assertEquals(numberIDs, uids.size)
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
    val hunt2 = hunt1.copy(uid = HuntTestConstants.HUNT_UID_2, title = SECOND_HUNT_TITLE)
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
    val hunt2 = hunt1.copy(uid = HuntTestConstants.HUNT_UID_2, title = SECOND_HUNT_TITLE)
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
    val fakeImageRepo = FakeHuntsImageRepository()
    val db = FirebaseFirestore.getInstance()
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
        huntID = EDIT_TEST_HUNT_ID,
        newValue = updatedHunt,
        mainImageUri = newMainUri,
        addedOtherImages = newOtherUris,
        removedOtherImages = removed)

    advanceUntilIdle()

    val stored = repo.getHunt(EDIT_TEST_HUNT_ID)

    assertTrue(
        HuntTestConstants.ASSERT_MAIN_IMAGE_MESSAGE,
        stored.mainImageUrl.startsWith("fake://main_image_url_for_$EDIT_TEST_HUNT_ID"))

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

    val hunt = hunt1.copy(uid = ERROR_TEST_HUNT_ID)
    repo.addHunt(hunt)

    val updated = hunt.copy(title = SHOULD_FAIL_TITLE)

    repo.editHunt(
        huntID = ERROR_TEST_HUNT_ID, newValue = updated, mainImageUri = Uri.parse(IMAGE_URI))
  }

  @Test
  fun addHunt_uploadsOtherImages_whenProvided_andStoresUrls() = runTest {
    val fakeImageRepo = FakeHuntsImageRepository()
    val db = FirebaseFirestore.getInstance()
    val repo = HuntsRepositoryFirestore(db = db, imageRepo = fakeImageRepo)

    val hunt =
        hunt1.copy(uid = ADD_WITH_OTHERS_HUNT_ID, mainImageUrl = "", otherImagesUrls = emptyList())

    val otherUris =
        listOf(
            Uri.parse(FILE_OTHER_1),
            Uri.parse(FILE_OTHER_2),
        )

    repo.addHunt(hunt = hunt, mainImageUri = null, otherImageUris = otherUris)

    val stored = repo.getHunt(ADD_WITH_OTHERS_HUNT_ID)
    assertEquals(ADD_WITH_OTHERS_HUNT_ID, stored.uid)
    assertTrue("Expected other image urls to be stored", stored.otherImagesUrls.isNotEmpty())
    assertEquals(2, stored.otherImagesUrls.size)
  }

  @Test
  fun addHunt_whenOtherUploadFails_callsCleanup_andRethrows_andDoesNotWriteDoc() = runTest {
    val db = FirebaseFirestore.getInstance()
    val imageRepo = FailingOnOtherUploadsImageRepo()
    val repo = HuntsRepositoryFirestore(db = db, imageRepo = imageRepo)

    val huntId = ADD_FAIL_CLEANUP_HUNT_ID
    val hunt = hunt1.copy(uid = huntId)

    val mainUri = Uri.parse(FILE_MAIN)
    val otherUris = listOf(Uri.parse(FILE_OTHER_1))

    try {
      repo.addHunt(hunt = hunt, mainImageUri = mainUri, otherImageUris = otherUris)
      fail("Expected exception to be rethrown")
    } catch (_: Exception) {
      // expected
    }

    assertTrue("Expected cleanup to be called", imageRepo.cleanupCalled)

    // Ensure no Firestore document was written
    val doc = db.collection(HUNTS_COLLECTION_PATH).document(huntId).get().await()
    assertFalse("Hunt doc should not exist if uploads failed", doc.exists())
  }

  @Test
  fun deleteHunt_deletesReviewsRepliesAndReviewPhotos() = runTest {
    val db = FirebaseFirestore.getInstance()
    val fakeImageRepo = FakeHuntsImageRepository()
    val fakeReviewImageRepo = FakeReviewImageRepository()
    val repo =
        HuntsRepositoryFirestore(
            db = db, imageRepo = fakeImageRepo, reviewImageRepo = fakeReviewImageRepo)

    val huntId = HUNT_CASCADE_ID
    repo.addHunt(hunt1.copy(uid = huntId))

    val reviewId = REVIEW_ID_1
    val photo1 = PHOTO_1
    val photo2 = PHOTO_2

    val reviewData: Map<String, Any> =
        mapOf(FIELD_HUNT_ID to huntId, "photos" to listOf(photo1, photo2))
    db.collection(HUNT_REVIEW_COLLECTION_PATH).document(reviewId).set(reviewData).await()

    val replyData: Map<String, Any> = mapOf(FIELD_REVIEW_ID to reviewId)
    db.collection(HUNT_REVIEW_REPLY_COLLECTION_PATH).document(REPLY_ID_1).set(replyData).await()
    db.collection(HUNT_REVIEW_REPLY_COLLECTION_PATH).document(REPLY_ID_2).set(replyData).await()

    repo.deleteHunt(huntId)

    assertEquals(listOf(photo1, photo2), fakeReviewImageRepo.deleted)

    val reviewDoc = db.collection(HUNT_REVIEW_COLLECTION_PATH).document(reviewId).get().await()
    assertFalse(reviewDoc.exists())

    val repliesRemaining =
        db.collection(HUNT_REVIEW_REPLY_COLLECTION_PATH)
            .whereEqualTo(FIELD_REVIEW_ID, reviewId)
            .get()
            .await()
    assertTrue(repliesRemaining.isEmpty)
  }

  @Test
  fun getAllHunts_skipsInvalidDocuments_andCoversDocumentToHuntCatch() = runTest {
    val db = FirebaseFirestore.getInstance()
    val repo = HuntsRepositoryFirestore(db = db, imageRepo = FakeHuntsImageRepository())

    // Insert a malformed hunt doc (invalid status)
    db.collection(HUNTS_COLLECTION_PATH)
        .document(BAD_DOC_ID)
        .set(
            mapOf(
                HuntsRepositoryFirestoreConstantsStrings.STATUS to "NOT_A_REAL_STATUS",
                HuntsRepositoryFirestoreConstantsStrings.TITLE to "x",
                HuntsRepositoryFirestoreConstantsStrings.DESCRIPTION to "x",
                HuntsRepositoryFirestoreConstantsStrings.TIME to 1.0,
                HuntsRepositoryFirestoreConstantsStrings.DISTANCE to 1.0,
                HuntsRepositoryFirestoreConstantsStrings.DIFFICULTY to "EASY",
                HuntsRepositoryFirestoreConstantsStrings.AUTHOR_ID to "a",
                HuntsRepositoryFirestoreConstantsStrings.RATING_REVIEW to 1.0))
        .await()

    val hunts = repo.getAllHunts()
    assertTrue(
        "Malformed docs should be skipped (documentToHunt returns null)",
        hunts.none { it.uid == BAD_DOC_ID })
  }
}

private class FakeReviewImageRepository : IReviewImageRepository {
  val deleted = mutableListOf<String>()
  val uploaded = mutableListOf<String>()

  override suspend fun uploadReviewPhoto(userId: String, uri: Uri): String {
    val url = "fake://review_photo/$userId/${uri.lastPathSegment ?: "photo"}"
    uploaded += url
    return url
  }

  override suspend fun deleteReviewPhoto(url: String) {
    deleted += url
  }
}

/**
 * Image repo that:
 * - succeeds main upload
 * - fails other-images upload
 * - records whether cleanup deleteAllHuntImages was called
 */
private class FailingOnOtherUploadsImageRepo : IHuntsImageRepository {
  var cleanupCalled = false

  override suspend fun uploadMainImage(huntId: String, uri: Uri): String {
    return "fake://main/$huntId"
  }

  override suspend fun uploadOtherImages(huntId: String, uris: List<Uri>): List<String> {
    throw RuntimeException("boom on other uploads")
  }

  override suspend fun deleteAllHuntImages(huntId: String) {
    cleanupCalled = true
  }

  override suspend fun deleteImageByUrl(url: String) {
    // no-op
  }
}
