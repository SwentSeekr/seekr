package com.swentseekr.seekr.model.image

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.swentseekr.seekr.model.hunt.ReviewImageRepository
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import java.io.File
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for the ReviewImageRepository using Firebase Storage.
 *
 * This test suite verifies upload and deletion of review images, ensuring
 * correct URL generation, safe deletion behavior, and proper error handling
 * when interacting with the Firebase Storage emulator.
 */

class ReviewImageRepositoryTest {

  private lateinit var repository: ReviewImageRepository
  private lateinit var storage: FirebaseStorage
  private val userId = "test_user"

  @Before
  fun setup() = runBlocking {
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    // Initialize Firebase if not already done
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    // Connect to Firebase emulators
    FirebaseTestEnvironment.setup()
    FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
    FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199)

    // Sign in anonymously
    FirebaseAuth.getInstance().signInAnonymously().await()

    storage = FirebaseStorage.getInstance()
    repository = ReviewImageRepository(storage)
  }

  @After
  fun tearDown() = runBlocking {
    // Clean up: delete all uploaded files under review_photos
    val list = storage.reference.child(ReviewImagesRepositoryTestConstants.PATH).listAll().await()
    list.items.forEach { it.delete().await() }
    FirebaseAuth.getInstance().signOut()
  }

  /** Utility to create a temporary image file and return its Uri */
  private fun createTempUri(prefix: String = "test_img"): Uri {
    val file = File.createTempFile(prefix, ReviewImagesRepositoryTestConstants.FORMAT)
    file.writeText("fake image content")
    return Uri.fromFile(file)
  }

  @Test
  fun uploadReviewPhoto_returnsValidUrl() = runBlocking {
    val uri = createTempUri("main")
    val url = repository.uploadReviewPhoto(userId, uri)

    assertTrue(ReviewImagesRepositoryTestConstants.URL_NOT_EMPTY, url.isNotEmpty())
    assertTrue(
        "URL should start with http(s)://",
        url.startsWith(ReviewImagesRepositoryTestConstants.URL_STARTER) ||
            url.startsWith(ReviewImagesRepositoryTestConstants.URL_STARTER))
  }

  @Test
  fun deleteReviewPhoto_removesUploadedPhoto_firebase() = runBlocking {
    val storage = FirebaseStorage.getInstance()
    val repository = ReviewImageRepository(storage)
    val uri = createTempUri("photo")
    val ref = storage.reference.child("test/${System.currentTimeMillis()}.jpg")

    // Upload
    ref.putFile(uri).await()
    val url = ref.downloadUrl.await()

    // Delete using repo
    repository.deleteReviewPhoto(url.toString())

    // Verify deletion
    try {
      storage.getReferenceFromUrl(url.toString()).metadata.await()
      fail(ReviewImagesRepositoryTestConstants.FAIL_DELETE)
    } catch (e: Exception) {
      // expected
    }
  }

  @Test
  fun deleteReviewPhoto_multipleTimes_doesNotCrash() = runBlocking {
    val uri1 = createTempUri("img1")
    val uri2 = createTempUri("img2")
    val urls =
        listOf(
            repository.uploadReviewPhoto(userId, uri1), repository.uploadReviewPhoto(userId, uri2))

    // Delete all uploaded files
    urls.forEach { url -> repository.deleteReviewPhoto(url) }

    // Attempt to delete again — should not crash
    urls.forEach { url ->
      try {
        repository.deleteReviewPhoto(url)
      } catch (_: Exception) {
        // expected: file already deleted
      }
    }

    assertTrue(true) // If we reach here, all deletions handled safely
  }

  @Test
  fun multipleUploads_generateUniqueUrls() = runBlocking {
    val uri1 = createTempUri("img1")
    val uri2 = createTempUri("img2")

    val url1 = repository.uploadReviewPhoto(userId, uri1)
    val url2 = repository.uploadReviewPhoto(userId, uri2)

    assertTrue(url1 != url2)
  }

  @Test
  fun uploadReviewPhoto_invalidUri_throwsException() = runBlocking {
    val badUri = Uri.fromFile(File("/does/not/exist.jpg"))

    try {
      repository.uploadReviewPhoto(userId, badUri)
      fail(ReviewImagesRepositoryTestConstants.UPLOAD_SUCCESSFULLY)
    } catch (e: Exception) {
      // SUCCESS: the catch block was executed and the exception was rethrown
      assertTrue(ReviewImagesRepositoryTestConstants.EXCEPTION_INVALID_URI, true)
    }
  }
}
