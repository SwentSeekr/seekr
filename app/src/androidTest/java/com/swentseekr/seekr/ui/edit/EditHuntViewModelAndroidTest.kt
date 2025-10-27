package com.swentseekr.seekr.ui.edit

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepository
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.testing.MainDispatcherRule
import com.swentseekr.seekr.ui.hunt.add.AddHuntViewModel
import com.swentseekr.seekr.ui.hunt.edit.EditHuntViewModel
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import com.swentseekr.seekr.utils.FirebaseTestEnvironment.clearEmulatorData
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class EditHuntViewModelAndroidTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var repository: HuntsRepository
  private lateinit var addVM: AddHuntViewModel
  private lateinit var editVM: EditHuntViewModel

  @Before
  fun setUp() = runTest {
    FirebaseTestEnvironment.setup()
    if (FirebaseTestEnvironment.isEmulatorActive()) {
      clearEmulatorData()
    }
    FirebaseAuth.getInstance().signInAnonymously().await()
    repository = HuntRepositoryProvider.repository
    addVM = AddHuntViewModel(repository)
    editVM = EditHuntViewModel(repository)
  }

  @After
  fun tearDown() = runTest {
    if (FirebaseTestEnvironment.isEmulatorActive()) {
      clearEmulatorData()
    }
    FirebaseAuth.getInstance().signOut()
  }

  @Test
  fun load_populates_state_and_buildHunt_uses_loaded_id() = runTest {
    createHunt()
    advanceUntilIdle()
    val all = repository.getAllHunts()
    assertEquals(1, all.size)
    val created = all.first()
    val id = created.uid

    editVM.load(id)
    advanceUntilIdle()

    val s = editVM.uiState.value
    assertEquals(created.title, s.title)
    assertEquals(created.description, s.description)
    assertEquals(created.time.toString(), s.time)
    assertEquals(created.distance.toString(), s.distance)
    assertEquals(created.difficulty, s.difficulty)
    assertEquals(created.status, s.status)
    assertEquals(created.image, s.image)
    assertEquals(listOf(created.start) + created.middlePoints + listOf(created.end), s.points)
    assertNull(s.errorMsg)

    // buildHunt should succeed and preserve uid when state is valid (proves huntId is set)
    val built = editVM.buildHunt(s)
    assertEquals(id, built.uid)
  }

  @Test
  fun buildHunt_throws_if_no_hunt_loaded() {
    val ex =
        assertThrows(IllegalArgumentException::class.java) {
          // Empty state is fine; error should be from missing huntId
          editVM.buildHunt(editVM.uiState.value)
        }
    assertEquals("No hunt loaded to edit.", ex.message)
  }

  @Test
  fun submit_returnsFalse_andSetsError_whenStateInvalid() = runTest {
    createHunt()
    advanceUntilIdle()
    val all = repository.getAllHunts()
    assertEquals(1, all.size)
    val created = all.first()
    val id = created.uid
    editVM.load(id)
    advanceUntilIdle()

    // Invalidate by clearing title
    editVM.setTitle("")
    val result = editVM.submit()
    assertFalse(result)
    assertEquals(
        "Please fill all required fields before saving the hunt.", editVM.uiState.value.errorMsg)
  }

  @Test
  fun submit_returnsFalse_andSetsError_whenNotLoggedIn() = runTest {
    createHunt()
    advanceUntilIdle()
    val all = repository.getAllHunts()
    assertEquals(1, all.size)
    val created = all.first()

    val id = created.uid
    editVM.load(id)
    advanceUntilIdle()

    // Ensure valid state
    editVM.setTitle("Edited Title")
    FirebaseAuth.getInstance().signOut()

    val result = editVM.submit()
    assertFalse(result)
    assertEquals("You must be logged in to perform this action.", editVM.uiState.value.errorMsg)
  }

  @Test
  fun submit_returnsTrue_updatesRepository_andClearsError_onSuccess() = runTest {
    createHunt()
    advanceUntilIdle()
    val all = repository.getAllHunts()
    assertEquals(1, all.size)
    val created = all.first()
    val id = created.uid

    editVM.load(id)
    advanceUntilIdle()

    val a = Location(10.0, 10.0, "New Start")
    val m1 = Location(10.5, 10.5, "New Mid")
    val b = Location(11.0, 11.0, "New End")

    editVM.setTitle("New Title")
    editVM.setDescription("New Desc")
    editVM.setTime("2.5")
    editVM.setDistance("4.2")
    editVM.setDifficulty(Difficulty.DIFFICULT)
    editVM.setStatus(HuntStatus.FUN)
    editVM.setImage(9)
    editVM.setPoints(listOf(a, m1, b))

    val result = editVM.submit()
    if (!result) {
      println("EditHuntViewModelAndroidTest: submit failed: ${editVM.uiState.value.errorMsg}")
    }
    assertTrue(result)
    advanceUntilIdle()

    val updated = repository.getHunt(id)
    assertEquals("New Title", updated.title)
    assertEquals("New Desc", updated.description)
    assertEquals(2.5, updated.time, 0.0)
    assertEquals(4.2, updated.distance, 0.0)
    assertEquals(Difficulty.DIFFICULT, updated.difficulty)
    assertEquals(HuntStatus.FUN, updated.status)
    assertEquals(9, updated.image)
    assertEquals(a, updated.start)
    assertEquals(b, updated.end)
    assertEquals(listOf(m1), updated.middlePoints)
    assertEquals(FirebaseAuth.getInstance().currentUser?.uid, updated.authorId)
    assertNull(editVM.uiState.value.errorMsg)
  }

  @Test
  fun load_invalidId_setsErrorMsg() = runTest {
    editVM.load("non-existent-id-123")
    advanceUntilIdle()
    val err = editVM.uiState.value.errorMsg
    assertNotNull(err)
    assertTrue(err!!.startsWith("Failed to load hunt:"))
  }

  @Test
  fun clearErrorMsg_setsNull() = runTest {
    editVM.load("non-existent")
    advanceUntilIdle()
    assertNotNull(editVM.uiState.value.errorMsg)
    editVM.clearErrorMsg()
    assertNull(editVM.uiState.value.errorMsg)
  }

  // Helpers

  private fun createHunt() {
    val a = Location(0.0, 0.0, "Start")
    val m = Location(0.5, 0.5, "Mid")
    val b = Location(1.0, 1.0, "End")

    addVM.setTitle("T")
    addVM.setDescription("D")
    addVM.setTime("1.5")
    addVM.setDistance("2.0")
    addVM.setDifficulty(Difficulty.EASY)
    addVM.setStatus(HuntStatus.FUN)
    addVM.setImage(7)
    addVM.setPoints(listOf(a, m, b))

    val ok = addVM.submit()
    assertTrue(ok)
  }
}
