package com.swentseekr.seekr.model.profile

import android.net.Uri
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants
import com.swentseekr.seekr.ui.profile.Profile
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the ProfileRepositoryLocal class.
 *
 * This test suite verifies the functionality of adding, updating, and retrieving user profiles and
 * their associated hunts in the local repository.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileRepositoryLocalTest {
  private lateinit var repository: ProfileRepositoryLocal
  private val sampleHunt = createHunt("hunt0", "City Exploration")
  private val done = createHunt("done1", "Completed 1")
  private val liked = createHunt("liked1", "Completed 1")
  private val profileBob =
      sampleProfileWithPseudonym(
          uid = "user2",
          pseudonym = "Bob",
      )
  private val profileAlice =
      sampleProfileWithPseudonym(
          uid = "user1",
          pseudonym = "Alice",
      )

  @Before
  fun setup() {
    repository = ProfileRepositoryLocal()
    repository.addProfile(profileAlice)
    repository.addProfile(profileBob)
  }

  @Test
  fun getProfile_returns_correct_profile() = runTest {
    val profile = repository.getProfile("user1")
    assertEquals("Alice", profile.author.pseudonym)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `getProfile throws exception for non-existent user`() = runTest {
    repository.getProfile("nonExistent")
  }

  @Test
  fun updateProfile_updates_existing_profile() = runTest {
    val updatedProfile = profileAlice.copy(author = profileAlice.author.copy(bio = "Updated bio"))

    repository.updateProfile(updatedProfile)
    val result = repository.getProfile("user1")

    assertEquals("Updated bio", result.author.bio)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateProfile_throws_for_non_existent_user() = runTest {
    val fake = profileAlice.copy(uid = "ghost")
    repository.updateProfile(fake)
  }

  @Test
  fun getMyHunts_returns_correct_list() = runTest {
    profileAlice.myHunts.add(sampleHunt)

    val hunts = repository.getMyHunts("user1")
    assertEquals(1, hunts.size)
    assertEquals("hunt0", hunts[0].uid)
  }

  @Test
  fun getDoneHunts_and_getLikedHunts_work_correctly() = runTest {
    profileAlice.doneHunts.add(done)
    profileAlice.likedHunts.add(liked)

    assertEquals(1, repository.getDoneHunts("user1").size)
    assertEquals(1, repository.getLikedHunts("user1").size)
  }

  @Test
  fun addProfile_increases_repository_size() = runTest {
    val initialSize = repository.size()

    val newProfile =
        Profile(
            uid = "user3",
            author =
                Author(
                    pseudonym = "Charlie",
                    bio = "A new challenger",
                    profilePicture = 1,
                    reviewRate = 5.0,
                    sportRate = 4.5),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())

    repository.addProfile(newProfile)

    assertEquals(initialSize + 1, repository.size())
    val retrieved = repository.getProfile("user3")
    assertEquals("Charlie", retrieved.author.pseudonym)
  }

  @Test
  fun updateProfile_replaces_existing_profile_not_duplicates() = runTest {
    val initialSize = repository.size()

    val updated = profileAlice.copy(author = profileAlice.author.copy(bio = "Replaced"))
    repository.updateProfile(updated)

    assertEquals(initialSize, repository.size())
    val result = repository.getProfile("user1")
    assertEquals("Replaced", result.author.bio)
  }

  @Test
  fun getProfile_is_case_sensitive() = runTest {
    try {
      repository.getProfile("User1") // capital U
      throw AssertionError("Expected exception for mismatched case")
    } catch (e: IllegalArgumentException) {
      assertEquals("Profile with ID User1 not found", e.message)
    }
  }

  @Test
  fun profiles_are_isolated_between_users() = runTest {
    profileAlice.myHunts.add(sampleHunt)
    assertEquals(1, repository.getMyHunts("user1").size)
    assertEquals(0, repository.getMyHunts("user2").size)
  }

  @Test fun repository_size_returns_correct_count() = runTest { assertEquals(2, repository.size()) }

  @Test
  fun addProfile_can_add_multiple_profiles_without_conflict() = runTest {
    val p4 =
        sampleProfileWithPseudonym(
            uid = "user4",
            pseudonym = "Diana",
        )
    val p5 =
        sampleProfileWithPseudonym(
            uid = "user5",
            pseudonym = "Eve",
        )
    repository.addProfile(p4)
    repository.addProfile(p5)
    assertEquals(4, repository.size())
  }

  @Test
  fun getMyHunts_returns_empty_list_for_new_profile() = runTest {
    val hunts = repository.getMyHunts("user2")
    assertTrue(hunts.isEmpty())
  }

  @Test
  fun updateProfile_preserves_hunts() = runTest {
    profileAlice.myHunts.add(sampleHunt)
    val updated = profileAlice.copy(author = profileAlice.author.copy(bio = "Preserve hunts"))
    repository.updateProfile(updated)
    val result = repository.getProfile("user1")
    assertEquals(1, result.myHunts.size)
    assertEquals("Preserve hunts", result.author.bio)
  }

  @Test
  fun getDoneHunts_returns_empty_list_for_user_with_no_done_hunts() = runTest {
    val result = repository.getDoneHunts("user2")
    assertTrue(result.isEmpty())
  }

  @Test
  fun getLikedHunts_returns_empty_list_for_user_with_no_liked_hunts() = runTest {
    val result = repository.getLikedHunts("user2")
    assertTrue(result.isEmpty())
  }

  @Test
  fun addDoneHuntAddsHuntWhenNotAlreadyInList() = runTest {
    val profile = sampleProfileWithPseudonym(uid = "user1", pseudonym = "Alice")
    val hunt = createHunt(uid = "hunt1", title = "New City Exploration")
    repository.addProfile(profile)
    repository.addDoneHunt("user1", hunt)

    val updatedProfile = repository.getProfile("user1")
    assertEquals(1, updatedProfile.doneHunts.size)
    assertEquals("hunt1", updatedProfile.doneHunts[0].uid)
  }

  @Test
  fun addDoneHuntDoesNotAddHuntIfAlreadyInList() = runTest {
    val profile = sampleProfileWithPseudonym(uid = "user1", pseudonym = "Alice")
    val hunt = createHunt(uid = "hunt1", title = "New City Exploration")
    repository.addProfile(profile)
    repository.addDoneHunt("user1", hunt)

    val updatedProfile = repository.getProfile("user1")
    assertEquals(1, updatedProfile.doneHunts.size)
    assertEquals("hunt1", updatedProfile.doneHunts[0].uid)
  }

  @Test(expected = IllegalArgumentException::class)
  fun addDoneHuntThrowsExceptionIfProfileNotFound() = runTest {
    val hunt = createHunt(uid = "hunt1", title = "New City Exploration")
    repository.addDoneHunt("nonExistentUser", hunt)
  }

  @Test
  fun createProfile_adds_new_profile_successfully() = runTest {
    val newProfile = sampleProfileWithPseudonym("user10", "Frank")
    repository.createProfile(newProfile)
    val result = repository.getProfile("user10")
    assertEquals("Frank", result.author.pseudonym)
  }

  @Test(expected = IllegalArgumentException::class)
  fun createProfile_throws_for_duplicate_uid() = runTest { repository.createProfile(profileAlice) }

  @Test
  fun uploadProfilePicture_updates_profile_and_returns_url() = runTest {
    val fakeUri = mockk<Uri>(relaxed = true)

    val url = repository.uploadProfilePicture("user1", fakeUri)
    val updatedProfile = repository.getProfile("user1")

    assertEquals("local://profile-picture/user1", url)
    assertEquals("local://profile-picture/user1", updatedProfile.author.profilePictureUrl)
  }

  @Test(expected = IllegalArgumentException::class)
  fun uploadProfilePicture_throws_for_non_existent_user() = runTest {
    val fakeUri = mockk<Uri>(relaxed = true)

    repository.uploadProfilePicture("ghostUser", fakeUri)
  }

  @Test
  fun deleteCurrentProfilePicture_resets_to_default() = runTest {
    val fakeUri = mockk<Uri>(relaxed = true)

    repository.uploadProfilePicture("user1", fakeUri)
    val profileBefore = repository.getProfile("user1")
    assertTrue(profileBefore.author.profilePictureUrl.isNotEmpty())

    repository.deleteCurrentProfilePicture("user1", "local://profile-picture/user1")
    val profileAfter = repository.getProfile("user1")

    assertEquals("", profileAfter.author.profilePictureUrl)
    assertEquals(EditProfileNumberConstants.PROFILE_PIC_DEFAULT, profileAfter.author.profilePicture)
  }

  @Test
  fun deleteCurrentProfilePicture_does_nothing_if_url_does_not_match() = runTest {
    val profileBefore = repository.getProfile("user1")
    repository.deleteCurrentProfilePicture("user1", "wrong-url")
    val profileAfter = repository.getProfile("user1")
    assertEquals(profileBefore.author.profilePictureUrl, profileAfter.author.profilePictureUrl)
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteCurrentProfilePicture_throws_for_non_existent_user() = runTest {
    repository.deleteCurrentProfilePicture("ghostUser", "url")
  }
}
