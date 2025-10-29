package com.swentseekr.seekr.model.hunt

import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import com.swentseekr.seekr.utils.FirebaseTestEnvironment.clearEmulatorData
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HuntsRepositoryFirestoreTest {

  private var repository: HuntsRepository = HuntRepositoryProvider.repository
  var hunt1 =
      Hunt(
          uid = "hunt1",
          start = Location(40.7128, -74.0060, "New York"),
          end = Location(40.730610, -73.935242, "Brooklyn"),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = "City Exploration",
          description = "Discover hidden gems in the city",
          time = 2.5,
          distance = 5.0,
          difficulty = Difficulty.EASY,
          authorId = "0",
          image = 0,
          reviewRate = 4.5)

  @Before
  fun setUp() {

    FirebaseTestEnvironment.setup()
    runTest {
      if (FirebaseTestEnvironment.isEmulatorActive()) {
        clearEmulatorData()
      }
      FirebaseAuth.getInstance().signInAnonymously().await()
      hunt1 = hunt1.copy(authorId = FirebaseAuth.getInstance().currentUser?.uid ?: "0")
    }
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
    val hunt2 = hunt1.copy(uid = "hunt2", title = "Another Hunt")
    repository.addHunt(hunt1)
    repository.addHunt(hunt2)
    val hunts = repository.getAllHunts()
    assertEquals(2, hunts.size)
    assert(hunts.contains(hunt1))
    assert(hunts.contains(hunt2))
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
    val hunt2 = hunt1.copy(uid = "hunt2", title = "Second Hunt")
    repository.addHunt(hunt2)
    val hunt3 = hunt1.copy(uid = "hunt3", title = "Third Hunt")
    repository.addHunt(hunt3)
    val hunt4 = hunt1.copy(uid = "hunt4", title = "Fourth Hunt")
    repository.addHunt(hunt4)
    val storedHunt = repository.getHunt(hunt3.uid)
    assertEquals(storedHunt, hunt3)
  }

  @Test(expected = IllegalArgumentException::class)
  fun retrievingNonExistentHuntThrowsException() = runTest {
    repository.addHunt(hunt1)
    repository.getHunt("nonexistent_id")
  }

  @Test
  fun canEditAHuntByID() = runTest {
    repository.addHunt(hunt1)
    val modifiedHunt = hunt1.copy(title = "Modified Hunt", status = HuntStatus.SPORT)
    repository.editHunt(hunt1.uid, modifiedHunt)
    assertEquals(1, repository.getAllHunts().size)
    val storedHunt = repository.getHunt(hunt1.uid)
    assertEquals(modifiedHunt, storedHunt)
  }

  @Test
  fun canEditAHuntByIDWithMultipleHunts() = runTest {
    repository.addHunt(hunt1)
    val hunt2 = hunt1.copy(uid = "hunt2", title = "Second Hunt")
    repository.addHunt(hunt2)
    val hunt3 = hunt1.copy(uid = "hunt3", title = "Third Hunt")
    repository.addHunt(hunt3)
    val modifiedHunt = hunt1.copy(title = "Modified Hunt", status = HuntStatus.SPORT)
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

  @Test
  fun canDeleteAHuntByIDWithMultipleHunts() = runTest {
    repository.addHunt(hunt1)
    val hunt2 = hunt1.copy(uid = "hunt2", title = "Second Hunt")
    repository.addHunt(hunt2)
    val hunt3 = hunt1.copy(uid = "hunt3", title = "Third Hunt")
    repository.addHunt(hunt3)
    assertEquals(3, repository.getAllHunts().size)
    repository.deleteHunt(hunt2.uid)
    assertEquals(2, repository.getAllHunts().size)
    val expectedHunts = setOf(hunt1, hunt3)
    assertEquals(expectedHunts, repository.getAllHunts().toSet())
  }
}
