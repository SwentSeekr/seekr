package com.swentseekr.seekr.model.profile

import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.ui.profile.Profile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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
  fun `getProfile returns correct profile`() = runTest {
    val profile = repository.getProfile("user1")
    assertEquals("Alice", profile.author.pseudonym)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `getProfile throws exception for non-existent user`() = runTest {
    repository.getProfile("nonExistent")
  }

  @Test
  fun `updateProfile updates existing profile`() = runTest {
    val updatedProfile = profileAlice.copy(author = profileAlice.author.copy(bio = "Updated bio"))

    repository.updateProfile(updatedProfile)
    val result = repository.getProfile("user1")

    assertEquals("Updated bio", result.author.bio)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `updateProfile throws for non-existent user`() = runTest {
    val fake = profileAlice.copy(uid = "ghost")
    repository.updateProfile(fake)
  }

  @Test
  fun `getMyHunts returns correct list`() = runTest {
    profileAlice.myHunts.add(sampleHunt)

    val hunts = repository.getMyHunts("user1")
    assertEquals(1, hunts.size)
    assertEquals("hunt0", hunts[0].uid)
  }

  @Test
  fun `getDoneHunts and getLikedHunts work correctly`() = runTest {
    profileAlice.doneHunts.add(done)
    profileAlice.likedHunts.add(liked)

    assertEquals(1, repository.getDoneHunts("user1").size)
    assertEquals(1, repository.getLikedHunts("user1").size)
  }

  @Test
  fun `addProfile increases repository size`() = runTest {
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
  fun `updateProfile replaces existing profile not duplicates`() = runTest {
    val initialSize = repository.size()

    val updated = profileAlice.copy(author = profileAlice.author.copy(bio = "Replaced"))
    repository.updateProfile(updated)

    assertEquals(initialSize, repository.size())
    val result = repository.getProfile("user1")
    assertEquals("Replaced", result.author.bio)
  }

  @Test
  fun `getProfile is case sensitive`() = runTest {
    try {
      repository.getProfile("User1") // capital U
      throw AssertionError("Expected exception for mismatched case")
    } catch (e: IllegalArgumentException) {
      assertEquals("Hunt with ID User1 not found", e.message)
    }
  }

  @Test
  fun `profiles are isolated between users`() = runTest {
    profileAlice.myHunts.add(sampleHunt)
    assertEquals(1, repository.getMyHunts("user1").size)
    assertEquals(0, repository.getMyHunts("user2").size)
  }

  @Test
  fun `repository size returns correct count`() = runTest { assertEquals(2, repository.size()) }

  @Test
  fun `addProfile can add multiple profiles without conflict`() = runTest {
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
  fun `getMyHunts returns empty list for new profile`() = runTest {
    val hunts = repository.getMyHunts("user2")
    assertTrue(hunts.isEmpty())
  }

  @Test
  fun `updateProfile preserves hunts`() = runTest {
    profileAlice.myHunts.add(sampleHunt)
    val updated = profileAlice.copy(author = profileAlice.author.copy(bio = "Preserve hunts"))
    repository.updateProfile(updated)
    val result = repository.getProfile("user1")
    assertEquals(1, result.myHunts.size)
    assertEquals("Preserve hunts", result.author.bio)
  }

  @Test
  fun `getDoneHunts returns empty list for user with no done hunts`() = runTest {
    val result = repository.getDoneHunts("user2")
    assertTrue(result.isEmpty())
  }

  @Test
  fun `getLikedHunts returns empty list for user with no liked hunts`() = runTest {
    val result = repository.getLikedHunts("user2")
    assertTrue(result.isEmpty())
  }
}
