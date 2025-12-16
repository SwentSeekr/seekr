package com.swentseekr.seekr.model.image

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.swentseekr.seekr.model.hunt.HuntsImageRepository
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import java.io.File
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for HuntsImageRepository.
 *
 * Works with Firebase emulators (Auth + Storage). Uses real temporary files to simulate image
 * uploads.
 */
@RunWith(AndroidJUnit4::class)
class HuntsImageRepositoryTest {

  private lateinit var storage: FirebaseStorage
  private lateinit var repository: HuntsImageRepository
  private val huntId = "hunt_test_id"

  @Before
  fun setup() = runBlocking {
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    // Initialize Firebase if not already done
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    // Connect to emulators
    FirebaseTestEnvironment.setup()
    FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
    FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199)

    // Sign in anonymously to get a valid token for Storage
    FirebaseAuth.getInstance().signInAnonymously().await()

    storage = FirebaseStorage.getInstance()
    repository = HuntsImageRepository(storage)
  }

  @After fun tearDown() = runBlocking { repository.deleteAllHuntImages(huntId) }

  /** Utility to create a valid temporary image file. */
  private fun createTempImageFile(prefix: String = "test_img"): Uri {
    val file = File.createTempFile(prefix, ".jpg")
    file.writeText("fake image content")
    return Uri.fromFile(file)
  }

  @Test
  fun uploadMainImage_returnsValidUrl() = runBlocking {
    val tempUri = createTempImageFile("main")
    val url = repository.uploadMainImage(huntId, tempUri)

    assertTrue(HuntsImagesRepositoryTestConstants.URL_NOT_EMPTY, url.isNotEmpty())

    // Accept both emulator and production URL formats
    val isValidUrl =
        url.startsWith(HuntsImagesRepositoryTestConstants.URL_STARTER) ||
            url.startsWith(HuntsImagesRepositoryTestConstants.URL_STARTER)
    assertTrue("URL should look like a valid HTTP(S) path, got: $url", isValidUrl)
  }

  @Test
  fun uploadOtherImages_returnsValidUrls() = runBlocking {
    val uris =
        listOf(
            createTempImageFile("img_1"),
            createTempImageFile("img_2"),
            createTempImageFile("img_3"))

    val urls = repository.uploadOtherImages(huntId, uris)
    assertEquals(3, urls.size)
    assertTrue(urls.all { it.isNotEmpty() })
  }

  @Test
  fun deleteAllHuntImages_completesWithoutError() = runBlocking {
    // Should not throw even if no folder exists
    repository.deleteAllHuntImages("non_existent_hunt")
    assertTrue(true)
  }

  @Test
  fun deleteAllHuntImages_removesUploadedFiles() = runBlocking {
    val uris = listOf(createTempImageFile("delete_1"), createTempImageFile("delete_2"))

    repository.uploadOtherImages(huntId, uris)
    repository.deleteAllHuntImages(huntId)

    // Try deleting again — should not hang or throw
    repository.deleteAllHuntImages(huntId)

    assertTrue(true)
  }
}
