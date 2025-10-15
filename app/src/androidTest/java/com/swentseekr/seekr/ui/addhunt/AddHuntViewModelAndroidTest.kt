package com.swentseekr.seekr.ui.addhunt

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepository
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.testing.MainDispatcherRule
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
class AddHuntViewModelAndroidTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()
  private lateinit var repository: HuntsRepository
  private lateinit var viewModel: AddHuntViewModel

  @Before
  fun setUp() {
    FirebaseTestEnvironment.setup()
    runTest {
      if (FirebaseTestEnvironment.isEmulatorActive()) {
        clearEmulatorData()
      }
      FirebaseAuth.getInstance().signInAnonymously().await()
    }
    repository = HuntRepositoryProvider.repository
    viewModel = AddHuntViewModel(repository)
  }

  @After
  fun tearDown() = runTest {
    if (FirebaseTestEnvironment.isEmulatorActive()) {
      clearEmulatorData()
    }
    FirebaseAuth.getInstance().signOut()
  }

  @Test
  fun initialState_isEmptyAndInvalid() {
    val s = viewModel.uiState.value
    assertEquals("", s.title)
    assertEquals("", s.description)
    assertEquals("", s.time)
    assertEquals("", s.distance)
    assertTrue(s.points.isEmpty())
    assertFalse(s.isValid)
    assertNull(s.errorMsg)
  }

  @Test
  fun setters_updateState_andValidation() {
    viewModel.setTitle("")
    assertEquals("Title cannot be empty", viewModel.uiState.value.invalidTitleMsg)
    viewModel.setTitle("T")
    assertNull(viewModel.uiState.value.invalidTitleMsg)
    assertEquals("T", viewModel.uiState.value.title)

    viewModel.setDescription("")
    assertEquals("Description cannot be empty", viewModel.uiState.value.invalidDescriptionMsg)
    viewModel.setDescription("D")
    assertNull(viewModel.uiState.value.invalidDescriptionMsg)
    assertEquals("D", viewModel.uiState.value.description)

    viewModel.setTime("x")
    assertEquals("Invalid time format", viewModel.uiState.value.invalidTimeMsg)
    viewModel.setTime("1.5")
    assertNull(viewModel.uiState.value.invalidTimeMsg)
    assertEquals("1.5", viewModel.uiState.value.time)

    viewModel.setDistance("y")
    assertEquals("Invalid distance format", viewModel.uiState.value.invalidDistanceMsg)
    viewModel.setDistance("2.0")
    assertNull(viewModel.uiState.value.invalidDistanceMsg)
    assertEquals("2.0", viewModel.uiState.value.distance)

    val a = Location(0.0, 0.0, "A")
    val b = Location(1.0, 1.0, "B")
    val c = Location(2.0, 2.0, "C")
    viewModel.setDifficulty(Difficulty.EASY)
    viewModel.setStatus(HuntStatus.FUN)
    viewModel.setImage(7)
    viewModel.setPoints(listOf(a, b, c))

    val s = viewModel.uiState.value
    assertEquals(Difficulty.EASY, s.difficulty)
    assertEquals(HuntStatus.FUN, s.status)
    assertEquals(7, s.image)
    assertEquals(listOf(a, b, c), s.points)
  }

  @Test
  fun isValid_true_whenAllFieldsSet() {
    setValidState(points = listOf(Location(0.0, 0.0, "A"), Location(1.0, 1.0, "B")))
    assertTrue(viewModel.uiState.value.isValid)
  }

  @Test
  fun addHunt_returnsFalse_andSetsError_whenStateInvalid() = runTest {
    val result = viewModel.addHunt()
    assertFalse(result)
    assertEquals(
        "Please fill all required fields before creating a hunt.", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addHunt_returnsFalse_andSetsError_whenNotLoggedIn() = runTest {
    setValidState(points = listOf(Location(0.0, 0.0, "A"), Location(1.0, 1.0, "B")))
    FirebaseAuth.getInstance().signOut()

    val result = viewModel.addHunt()
    assertFalse(result)
    assertEquals("You must be logged in to create a hunt.", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addHunt_returnsTrue_addsToRepository_andClearsError_onSuccess() = runTest {
    val a = Location(0.0, 0.0, "Start")
    val m = Location(0.5, 0.5, "Mid")
    val b = Location(1.0, 1.0, "End")
    setValidState(points = listOf(a, m, b))

    val result = viewModel.addHunt()
    assertTrue(result)

    advanceUntilIdle()

    val hunts = repository.getAllHunts()
    assertEquals(1, hunts.size)
    val h = hunts.first()
    assertTrue(h.uid.isNotBlank())
    assertEquals(a, h.start)
    assertEquals(b, h.end)
    assertEquals(listOf(m), h.middlePoints)
    assertEquals(HuntStatus.FUN, h.status)
    assertEquals("T", h.title)
    assertEquals("D", h.description)
    assertEquals(1.5, h.time, 0.0)
    assertEquals(2.0, h.distance, 0.0)
    assertEquals(Difficulty.EASY, h.difficulty)
    assertEquals(FirebaseAuth.getInstance().currentUser?.uid, h.authorId)
    assertEquals(7, h.image)
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun clearErrorMsg_setsNull() {
    viewModel.addHunt()
    assertNotNull(viewModel.uiState.value.errorMsg)
    viewModel.clearErrorMsg()
    assertNull(viewModel.uiState.value.errorMsg)
  }

  private fun setValidState(points: List<Location>) {
    viewModel.setTitle("T")
    viewModel.setDescription("D")
    viewModel.setTime("1.5")
    viewModel.setDistance("2.0")
    viewModel.setDifficulty(Difficulty.EASY)
    viewModel.setStatus(HuntStatus.FUN)
    viewModel.setImage(7)
    viewModel.setPoints(points)
  }
}
