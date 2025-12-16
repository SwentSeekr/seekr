package com.swentseekr.seekr.offline.cache

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.profile.Profile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for the ProfileCache class.
 *
 * This test suite verifies the functionality of saving,
 * observing, and clearing cached user profiles.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ProfileCacheTest {
  companion object {
    private const val NO_PROFILE_IF_CACHE = "Expected null when no profile is cached"
    private const val PROFILE_CACHE_NOT_NULL = "Cached profile should not be null"
    private const val PROFILE_EQUAL_SAVE = "Cached profile should equal the saved profile"
    private const val PROFILE_NULL_AFTER_CLEAR = "Profile should be null after clear()"
    private const val INVALID_JSON = "Invalid JSON should result in a null Profile"
  }

  private lateinit var context: Context

  @Before
  fun setup() = runTest {
    context = ApplicationProvider.getApplicationContext()
    ProfileCache.clear(context)
  }

  @After fun tearDown() = runTest { ProfileCache.clear(context) }

  @Test
  fun observeProfile_whenEmpty_returnsNull() = runTest {
    val result = ProfileCache.observeProfile(context).first()

    assertNull(NO_PROFILE_IF_CACHE, result)
  }

  @Test
  fun saveProfile_thenObserve_returnsSameProfile() = runTest {
    val profile = sampleProfile()

    ProfileCache.saveProfile(context, profile)

    val cached = ProfileCache.observeProfile(context).first()

    assertNotNull(PROFILE_CACHE_NOT_NULL, cached)
    assertEquals(PROFILE_EQUAL_SAVE, profile, cached)
  }

  @Test
  fun clear_removesSavedProfile() = runTest {
    val profile = sampleProfile()
    ProfileCache.saveProfile(context, profile)

    val cachedBeforeClear = ProfileCache.observeProfile(context).first()
    assertNotNull(cachedBeforeClear)

    ProfileCache.clear(context)

    val cachedAfterClear = ProfileCache.observeProfile(context).first()
    assertNull(PROFILE_NULL_AFTER_CLEAR, cachedAfterClear)
  }

  @Test
  fun observeProfile_withInvalidJson_returnsNull() = runTest {
    context.profileDataStore.edit { prefs -> prefs[ProfileCache.PROFILE_JSON] = "not a valid json" }

    val result = ProfileCache.observeProfile(context).first()

    assertNull(INVALID_JSON, result)
  }

  private fun sampleProfile(): Profile {
    val author =
        Author(
            pseudonym = "OfflineUser",
            bio = "Cached bio",
            profilePicture = 0,
            reviewRate = 4.5,
            sportRate = 3.5)

    val start = Location(latitude = 46.5191, longitude = 6.5668, name = "Lausanne")
    val end = Location(latitude = 46.2044, longitude = 6.1432, name = "Geneva")

    val hunt =
        Hunt(
            uid = "hunt1",
            start = start,
            end = end,
            middlePoints = emptyList(),
            status = HuntStatus.DISCOVER,
            title = "Sample Hunt",
            description = "A cached hunt",
            time = 120.0,
            distance = 10.0,
            difficulty = Difficulty.EASY,
            authorId = "user1",
            otherImagesUrls = emptyList(),
            mainImageUrl = "https://example.com/image.jpg",
            reviewRate = 4.0)

    return Profile(
        uid = "user1",
        author = author,
        myHunts = mutableListOf(hunt),
        doneHunts = mutableListOf(),
        likedHunts = mutableListOf())
  }
}
