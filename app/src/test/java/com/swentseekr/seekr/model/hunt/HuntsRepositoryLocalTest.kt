package com.swentseekr.seekr.model.hunt

import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.map.Location
import java.lang.IllegalArgumentException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class HuntsRepositoryLocalTest {
  private lateinit var huntsRepositoryLocal: HuntsRepositoryLocal

  private val sampleHunt =
      Hunt(
          uid = "0",
          start = Location(40.7128, -74.0060, "New York"),
          end = Location(40.730610, -73.935242, "Brooklyn"),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = "City Exploration",
          description = "Discover hidden gems in the city",
          time = 2.5,
          distance = 5.0,
          difficulty = Difficulty.EASY,
          author = Author("spike man", "", 1, 2.5, 3.0),
          image = 2,
          reviewRate = 4.5)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    huntsRepositoryLocal = HuntsRepositoryLocal()
  }
  /**
   * This test verifies that getNewUid generates unique, non-empty identifiers on consecutive calls.
   */
  @Test
  fun generatesNewUniqueIds() {
    val firstId = huntsRepositoryLocal.getNewUid()
    val secondId = huntsRepositoryLocal.getNewUid()
    assertNotEquals(firstId, secondId)
    assertTrue(firstId.isNotEmpty())
    assertTrue(secondId.isNotEmpty())
  }
  /** This test verifies that getNewUid increments the ID correctly on each call. */
  @Test
  fun getNewUid_incrementsCorrectly() {
    val id1 = huntsRepositoryLocal.getNewUid()
    val id2 = huntsRepositoryLocal.getNewUid()
    val id3 = huntsRepositoryLocal.getNewUid()

    assertEquals("0", id1)
    assertEquals("1", id2)
    assertEquals("2", id3)
  }

  /**
   * This test verifies that addToDo successfully adds a ToDo item to the local repository It also
   * tests that getAllTodos and getTodo successfully retrieve the todos.
   */
  @Test
  fun addHunt_succeeds() = runTest {
    huntsRepositoryLocal.addHunt(sampleHunt)

    val hunts = huntsRepositoryLocal.getAllHunts()
    assertTrue(hunts.contains(sampleHunt))
    assertEquals(1, hunts.size)

    val retrievedTodo = huntsRepositoryLocal.getHunt(sampleHunt.uid)
    assertEquals(sampleHunt, retrievedTodo)
  }

  /**
   * This test verifies that a Hunt can be added to the repository and then retrieved successfully
   * by its ID and from the list of all hunts.
   */
  @Test
  fun addAndRetrieveHuntSuccessfully() = runBlocking {
    huntsRepositoryLocal.addHunt(sampleHunt)
    val hunts = huntsRepositoryLocal.getAllHunts()
    assertTrue(hunts.contains(sampleHunt))
    assertEquals(1, hunts.size)
    val retrieved = huntsRepositoryLocal.getHunt(sampleHunt.uid)
    assertEquals(sampleHunt, retrieved)
  }

  /**
   * This test verifies that multiple Hunts can be added to the repository and retrieved
   * successfully. It checks that all added Hunts are present in the retrieved list.
   */
  @Test
  fun addMultipleHunts_retrievesAll() = runBlocking {
    val hunt1 = sampleHunt.copy(uid = "1", title = "Hunt 1")
    val hunt2 = sampleHunt.copy(uid = "2", title = "Hunt 2")

    huntsRepositoryLocal.addHunt(hunt1)
    huntsRepositoryLocal.addHunt(hunt2)

    val hunts = huntsRepositoryLocal.getAllHunts()

    assertEquals(2, hunts.size)
    assertTrue(hunts.contains(hunt1))
    assertTrue(hunts.contains(hunt2))
  }

  /**
   * This test verifies that a Hunt can be edited in the repository. It checks that the updated Hunt
   * is present and the old version is not.
   */
  @Test
  fun editHuntSuccessfully() = runBlocking {
    huntsRepositoryLocal.addHunt(sampleHunt)
    val updatedHunt = sampleHunt.copy(title = "New York City Exploration")

    huntsRepositoryLocal.editHunt(sampleHunt.uid, updatedHunt)

    val hunts = huntsRepositoryLocal.getAllHunts()
    assertTrue(hunts.contains(updatedHunt))
    assertFalse(hunts.contains(sampleHunt))
    assertEquals(1, hunts.size)
  }

  @Test(expected = IllegalArgumentException::class)
  fun editHuntThrowsWhenNotFound() = runBlocking {
    huntsRepositoryLocal.editHunt("nonexistent-id", sampleHunt)
  }
  /**
   * This test verifies that a Hunt can be deleted from the repository. It checks that the Hunt is
   * no longer present after deletion.
   */
  @Test
  fun deleteHunt_removesSuccessfully() = runBlocking {
    huntsRepositoryLocal.addHunt(sampleHunt)
    huntsRepositoryLocal.deleteHunt(sampleHunt.uid)
    val hunts = huntsRepositoryLocal.getAllHunts()
    assertFalse(hunts.contains(sampleHunt))
  }

  /** This test verifies that attempting to delete a non-existent Hunt throws an exception */
  @Test(expected = IllegalArgumentException::class)
  fun deleteHunt_notFound_throws() = runBlocking {
    huntsRepositoryLocal.deleteHunt("non-existent-id")
  }

  /** This test verifies that attempting to edit a non-existent Hunt throws an exception */
  @Test(expected = IllegalArgumentException::class)
  fun editHunt_notFound_throws() = runBlocking {
    huntsRepositoryLocal.editHunt("non-existent-id", sampleHunt)
  }
}
