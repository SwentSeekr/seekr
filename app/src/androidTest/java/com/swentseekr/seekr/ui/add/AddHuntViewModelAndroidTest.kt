package com.swentseekr.seekr.ui.add

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntRepositoryProvider
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepository
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.testing.MainDispatcherRule
import com.swentseekr.seekr.ui.hunt.BaseHuntViewModelMessages
import com.swentseekr.seekr.ui.hunt.add.AddHuntViewModel
import com.swentseekr.seekr.utils.FirebaseTestEnvironment
import com.swentseekr.seekr.utils.FirebaseTestEnvironment.clearEmulatorData
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Android integration tests for the AddHuntViewModel.
 *
 * This test suite verifies state management, validation logic, image handling, checkpoint image
 * attachment, and submission behavior when interacting with the HuntsRepository and Firebase
 * authentication layer.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class AddHuntViewModelAndroidTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()
  private lateinit var repository: HuntsRepository
  private lateinit var viewModel: AddHuntViewModel

  @Before
  fun setUp() = runTest {
    FirebaseTestEnvironment.setup()
    if (FirebaseTestEnvironment.isEmulatorActive()) {
      clearEmulatorData()
    }
    FirebaseAuth.getInstance().signInAnonymously().await()
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
  fun initialStateIsEmptyAndInvalid() {
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
  fun settersUpdateStateAndValidation() {
    viewModel.setTitle("")
    assertEquals(BaseHuntViewModelMessages.TITLE_EMPTY, viewModel.uiState.value.invalidTitleMsg)
    viewModel.setTitle("T")
    assertNull(viewModel.uiState.value.invalidTitleMsg)
    assertEquals("T", viewModel.uiState.value.title)

    viewModel.setDescription("")
    assertEquals(
        BaseHuntViewModelMessages.DESCRIPTION_EMPTY, viewModel.uiState.value.invalidDescriptionMsg)
    viewModel.setDescription("D")
    assertNull(viewModel.uiState.value.invalidDescriptionMsg)
    assertEquals("D", viewModel.uiState.value.description)

    viewModel.setTime("x")
    assertEquals(BaseHuntViewModelMessages.INVALID_TIME, viewModel.uiState.value.invalidTimeMsg)
    viewModel.setTime("1.5")
    assertNull(viewModel.uiState.value.invalidTimeMsg)
    assertEquals("1.5", viewModel.uiState.value.time)

    viewModel.setDistance("y")
    assertEquals(
        BaseHuntViewModelMessages.INVALID_DISTANCE, viewModel.uiState.value.invalidDistanceMsg)
    viewModel.setDistance("2.0")
    assertNull(viewModel.uiState.value.invalidDistanceMsg)
    assertEquals("2.0", viewModel.uiState.value.distance)

    val a = Location(0.0, 0.0, "A")
    val b = Location(1.0, 1.0, "B")
    val c = Location(2.0, 2.0, "C")
    viewModel.setDifficulty(Difficulty.EASY)
    viewModel.setStatus(HuntStatus.FUN)

    viewModel.setPoints(listOf(a, b, c))

    val s = viewModel.uiState.value
    assertEquals(Difficulty.EASY, s.difficulty)
    assertEquals(HuntStatus.FUN, s.status)
    assertEquals(listOf(a, b, c), s.points)
  }

  @Test
  fun isValidTrueWhenAllFieldsSet() {
    setValidState(points = listOf(Location(0.0, 0.0, "A"), Location(1.0, 1.0, "B")))
    assertTrue(viewModel.uiState.value.isValid)
  }

  @Test
  fun addHuntReturnsFalseAndSetsErrorWhenStateInvalid() = runTest {
    val result = viewModel.submit()
    assertFalse(result)
    assertEquals(BaseHuntViewModelMessages.NOT_ALL_FIELD_FILL, viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addHuntReturnsFalseAndSetsErrorWhenNotLoggedIn() = runTest {
    setValidState(points = listOf(Location(0.0, 0.0, "A"), Location(1.0, 1.0, "B")))
    FirebaseAuth.getInstance().signOut()

    val result = viewModel.submit()
    assertFalse(result)
    assertEquals(BaseHuntViewModelMessages.MUST_LOGIN, viewModel.uiState.value.errorMsg)
  }

  @Test
  fun clearErrorMsgSetsNull() {
    viewModel.submit()
    assertNotNull(viewModel.uiState.value.errorMsg)
    viewModel.clearErrorMsg()
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun imageHandlersUpdateStateCorrectly() = runTest {
    val main = Uri.parse("file://main.png")
    viewModel.updateMainImageUri(main)

    val s1 = viewModel.uiState.value
    assertEquals(main.toString(), s1.mainImageUrl)

    val uri1 = Uri.parse("file://img1.png")
    val uri2 = Uri.parse("file://img2.png")

    viewModel.updateOtherImagesUris(listOf(uri1))
    viewModel.updateOtherImagesUris(listOf(uri2))

    val s2 = viewModel.uiState.value

    assertEquals(listOf(uri1, uri2), s2.otherImagesUris)

    viewModel.removeOtherImage(uri1)

    val s3 = viewModel.uiState.value

    assertEquals(listOf(uri2), s3.otherImagesUris)
    assertFalse(s3.otherImagesUris.contains(uri1))
  }

  @Test
  fun checkpointImagesAreAttachedAndStateUpdated() {
    val existing = Uri.parse("file://existing.png")
    viewModel.updateOtherImagesUris(listOf(existing))

    val a = Location(0.0, 0.0, "A")
    val b = Location(1.0, 1.0, "B")
    val c = Location(2.0, 2.0, "C")

    val uriA = Uri.parse("file://checkpoint_a.png")
    val uriC = Uri.parse("file://checkpoint_c.png")

    viewModel.registerCheckpointImage(a, uriA)
    viewModel.registerCheckpointImage(c, uriC)

    val pointsWithImages = viewModel.attachCheckpointImages(listOf(a, b, c))

    val state = viewModel.uiState.value
    assertEquals(listOf(existing, uriA, uriC), state.otherImagesUris)

    assertEquals(1, pointsWithImages[0].imageIndex)
    assertNull(pointsWithImages[1].imageIndex)
    assertEquals(2, pointsWithImages[2].imageIndex)
  }

  @Test
  fun checkpointImagesIgnoreNullUriAndBufferIsClearedAfterAttach() {
    val a = Location(0.0, 0.0, "A")
    val uriA = Uri.parse("file://checkpoint_a.png")

    viewModel.registerCheckpointImage(a, null)
    var pointsWithImages = viewModel.attachCheckpointImages(listOf(a))

    var state = viewModel.uiState.value
    assertTrue(state.otherImagesUris.isEmpty())
    assertNull(pointsWithImages[0].imageIndex)

    viewModel.registerCheckpointImage(a, uriA)
    pointsWithImages = viewModel.attachCheckpointImages(listOf(a))

    state = viewModel.uiState.value
    assertEquals(listOf(uriA), state.otherImagesUris)
    assertEquals(0, pointsWithImages[0].imageIndex)

    val pointsSecondAttach = viewModel.attachCheckpointImages(listOf(pointsWithImages[0]))

    state = viewModel.uiState.value
    assertEquals(listOf(uriA), state.otherImagesUris)
    assertEquals(0, pointsSecondAttach[0].imageIndex)
  }

  @Test
  fun removeMainImageClearsMainImageUrlAndMainImageUri() = runTest {
    // Arrange: set a main image
    val uri = Uri.parse("file://main-to-remove.png")
    viewModel.updateMainImageUri(uri)

    // Ensure state was updated
    val before = viewModel.uiState.value.mainImageUrl
    assertEquals(uri.toString(), before)
    assertEquals(uri, viewModel.mainImageUri)

    // Act: call function to cover BaseHuntViewModel.removeMainImage()
    viewModel.removeMainImage()

    // Assert: UI state cleared
    val after = viewModel.uiState.value.mainImageUrl
    assertEquals("", after)

    // Assert: AddHuntViewModel.mainImageUri cleared
    assertNull(viewModel.mainImageUri)
  }

  private fun setValidState(points: List<Location>) {
    viewModel.setTitle("T")
    viewModel.setDescription("D")
    viewModel.setTime("1.5")
    viewModel.setDistance("2.0")
    viewModel.setDifficulty(Difficulty.EASY)
    viewModel.setStatus(HuntStatus.FUN)

    viewModel.setPoints(points)
  }
}
