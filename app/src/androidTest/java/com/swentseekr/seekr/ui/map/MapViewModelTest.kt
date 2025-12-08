package com.swentseekr.seekr.ui.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.utils.FakeRepoEmpty
import com.swentseekr.seekr.utils.FakeRepoSuccess
import com.swentseekr.seekr.utils.FakeRepoThrows
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.IllegalStateException
import java.lang.reflect.Field
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(val dispatcher: TestDispatcher = StandardTestDispatcher()) :
    TestWatcher() {
  override fun starting(description: Description) {
    Dispatchers.setMain(dispatcher)
  }

  override fun finished(description: Description) {
    Dispatchers.resetMain()
  }
}

class MapViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private fun sample(
      uid: String = Constants.HUNT_UID_1,
      lat: Double = Constants.VALID_LAT,
      lng: Double = Constants.VALID_LNG
  ) =
      Hunt(
          uid = uid,
          start = Location(lat, lng, "Start"),
          end = Location(lat, lng, "End"),
          middlePoints = listOf(Location(lat, lng, "M1"), Location(lat, lng, "M2")),
          status = HuntStatus.FUN,
          title = "${Constants.HUNT_TITLE} $uid",
          description = Constants.HUNT_DESCRIPTION,
          time = Constants.HUNT_TIME,
          distance = Constants.HUNT_DISTANCE,
          difficulty = Difficulty.EASY,
          authorId = Constants.AUTHOR_ID,
          mainImageUrl = "",
          reviewRate = Constants.REVIEW_RATE)

  // --------------------
  // Basic initial state
  // --------------------

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun initialStateLoadsHuntsAndTargetsFirstStart() =
      runTest(mainDispatcherRule.dispatcher) {
        val hunts =
            listOf(
                sample(uid = Constants.HUNT_UID_1, lat = 46.5, lng = 6.6),
                sample(uid = Constants.HUNT_UID_2))
        val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

        // Let fetchHunts coroutine complete
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(hunts.size, state.hunts.size)
        assertEquals(46.5, state.target.latitude, 1e-6)
        assertEquals(6.6, state.target.longitude, 1e-6)
        assertNull(state.errorMsg)
      }

  @Test
  fun initialStateWithEmptyRepoUsesDefaultFallback() = runTest {
    val vm = MapViewModel(repository = FakeRepoEmpty())

    // Without advancing, uiState still has default target, which matches fallback constants.
    val state = vm.uiState.value
    assertEquals(MapConfig.DefaultLat, state.target.latitude, 1e-6)
    assertEquals(MapConfig.DefaultLng, state.target.longitude, 1e-6)
    assertTrue(state.hunts.isEmpty())
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun initialStateFailureSetsErrorMessage() =
      runTest(mainDispatcherRule.dispatcher) {
        val vm = MapViewModel(repository = FakeRepoThrows("boom"))

        advanceUntilIdle()

        val state = vm.uiState.value
        assertNotNull(state.errorMsg)
        assertTrue(state.errorMsg!!.contains(MapScreenStrings.ErrorLoadHuntsPrefix))
        assertTrue(state.hunts.isEmpty())
      }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun clearErrorMsgRemovesError() =
      runTest(mainDispatcherRule.dispatcher) {
        val vm = MapViewModel(repository = FakeRepoThrows("boom"))

        advanceUntilIdle()
        assertNotNull(vm.uiState.value.errorMsg)

        vm.clearErrorMsg()
        advanceUntilIdle()

        assertNull(vm.uiState.value.errorMsg)
      }

  // --------------------
  // Selection / focus
  // --------------------

  @Test
  fun onMarkerClickSelectsHuntAndIsNotFocused() = runTest {
    val hunts = listOf(sample(uid = Constants.HUNT_UID_1), sample(uid = Constants.HUNT_UID_2))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))
    vm.onMarkerClick(hunts[1])

    val state = vm.uiState.value
    assertEquals(Constants.HUNT_UID_2, state.selectedHunt?.uid)
    assertFalse(state.isFocused)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun onViewHuntClickSetsFocusedTrue() = runTest {
    val hunts = listOf(sample(uid = Constants.HUNT_UID_1))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

    advanceUntilIdle()

    vm.onMarkerClick(hunts[0])
    vm.onViewHuntClick()

    val state = vm.uiState.value
    assertTrue(state.isFocused)
    assertEquals(Constants.HUNT_UID_1, state.selectedHunt?.uid)
  }

  @Test
  fun onBackToAllHuntsClearsSelectionAndFocus() = runTest {
    val hunts = listOf(sample(uid = Constants.HUNT_UID_1))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

    vm.onMarkerClick(hunts[0])
    vm.onViewHuntClick()
    vm.onBackToAllHunts()

    val state = vm.uiState.value
    assertFalse(state.isFocused)
    assertNull(state.selectedHunt)
  }

  @Test
  fun refreshUIStateReloadsFromRepository() = runTest {
    val hunts = listOf(sample(uid = Constants.HUNT_UID_1))
    val repo = FakeRepoSuccess(hunts)
    val vm = MapViewModel(repository = repo)

    val before = vm.uiState.value.hunts.size
    vm.refreshUIState()
    val after = vm.uiState.value.hunts.size
    assertEquals(before, after)
  }

  // --------------------
  // Route computation & helpers
  // --------------------

  /**
   * Small fake HttpURLConnection that just returns [body] as its InputStream. Used via
   * [directionsConnectionFactory] to test requestDirectionsPolyline.
   */
  private class FakeHttpURLConnection(private val body: String) :
      HttpURLConnection(URL("https://example.com")) {

    override fun getInputStream(): InputStream =
        ByteArrayInputStream(body.toByteArray(Charsets.UTF_8))

    override fun getOutputStream(): OutputStream = ByteArrayOutputStream()

    override fun usingProxy(): Boolean = false

    override fun disconnect() {}

    override fun connect() {}
  }

  private fun withFakeDirectionsBody(body: String, block: () -> Unit) {
    val original = directionsConnectionFactory
    directionsConnectionFactory = { FakeHttpURLConnection(body) }
    try {
      block()
    } finally {
      directionsConnectionFactory = original
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun onViewHuntClickWithNoSelectedHuntKeepsRouteEmpty() = runTest {
    val hunts = listOf(sample(uid = Constants.HUNT_UID_1))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

    advanceUntilIdle()

    // No selected hunt
    vm.onViewHuntClick()

    val state = vm.uiState.value
    assertTrue(state.isFocused)
    assertTrue(state.route.isEmpty())
    assertFalse(state.isRouteLoading)
  }

  @Test
  fun requestDirectionsPolylineParsesStepPolylinesAndDeduplicates() {
    val step1 = listOf(LatLng(0.0, 0.0), LatLng(0.0, 1.0))
    val step2 = listOf(LatLng(0.0, 1.0), LatLng(1.0, 1.0))

    val step1Enc = PolyUtil.encode(step1)
    val step2Enc = PolyUtil.encode(step2)

    val overviewEnc = PolyUtil.encode(listOf(step1[0], step1[1], step2[1]))

    val body =
        """
        {
          "status": "OK",
          "routes": [
            {
              "legs": [
                {
                  "steps": [
                    { "polyline": { "points": "$step1Enc" } },
                    { "polyline": { "points": "$step2Enc" } }
                  ]
                }
              ],
              "overview_polyline": { "points": "$overviewEnc" }
            }
          ]
        }
        """
            .trimIndent()

    withFakeDirectionsBody(body) {
      val points =
          requestDirectionsPolyline(
              originLat = MapConfig.DefaultLat,
              originLng = MapConfig.DefaultLng,
              destLat = MapConfig.DefaultLat + 0.01,
              destLng = MapConfig.DefaultLng + 0.01,
              waypoints = emptyList(),
              travelMode = MapConfig.TravelModeWalking,
              apiKey = "test-key")

      assertEquals(3, points.size)
      assertEquals(0.0, points[0].latitude, 1e-6)
      assertEquals(0.0, points[0].longitude, 1e-6)
      assertEquals(0.0, points[1].latitude, 1e-6)
      assertEquals(1.0, points[1].longitude, 1e-6)
      assertEquals(1.0, points[2].latitude, 1e-6)
      assertEquals(1.0, points[2].longitude, 1e-6)
    }
  }

  @Test
  fun requestDirectionsPolylineFallsBackToOverviewWhenStepsEmpty() {
    val overviewEnc = PolyUtil.encode(listOf(LatLng(2.0, 2.0), LatLng(3.0, 3.0)))

    val body =
        """
        {
          "status": "OK",
          "routes": [
            {
              "legs": [ { "steps": [] } ],
              "overview_polyline": { "points": "$overviewEnc" }
            }
          ]
        }
        """
            .trimIndent()

    withFakeDirectionsBody(body) {
      val points =
          requestDirectionsPolyline(
              originLat = MapConfig.DefaultLat,
              originLng = MapConfig.DefaultLng,
              destLat = MapConfig.DefaultLat + 0.01,
              destLng = MapConfig.DefaultLng + 0.01,
              waypoints = emptyList(),
              travelMode = MapConfig.TravelModeWalking,
              apiKey = "test-key")

      assertEquals(2, points.size)
      assertEquals(2.0, points[0].latitude, 1e-6)
      assertEquals(2.0, points[0].longitude, 1e-6)
      assertEquals(3.0, points[1].latitude, 1e-6)
      assertEquals(3.0, points[1].longitude, 1e-6)
    }
  }

  @Test
  fun requestDirectionsPolylineErrorResponseIsWrappedAndHasCorrectMessage() {
    val body =
        """
        {
          "status": "REQUEST_DENIED",
          "error_message": "Key is invalid"
        }
        """
            .trimIndent()

    withFakeDirectionsBody(body) {
      try {
        requestDirectionsPolyline(
            originLat = MapConfig.DefaultLat,
            originLng = MapConfig.DefaultLng,
            destLat = MapConfig.DefaultLat + 0.01,
            destLng = MapConfig.DefaultLng + 0.01,
            waypoints = emptyList(),
            travelMode = MapConfig.TravelModeWalking,
            apiKey = "test-key")
        fail("Expected IllegalStateException for error status")
      } catch (e: IllegalStateException) {
        assertTrue(
            e.message?.contains("Directions API error") == true ||
                e.message?.contains("REQUEST_DENIED") == true)
      }
    }
  }

  // --------------------
  // Back to all hunts should clear route too
  // --------------------

  @Test
  fun onBackToAllHuntsClearsRoute() = runTest {
    val hunts = listOf(sample(uid = Constants.HUNT_UID_1))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

    val initial =
        vm.uiState.value.copy(
            route = listOf(LatLng(1.0, 1.0), LatLng(2.0, 2.0)),
            isFocused = true,
            selectedHunt = hunts[0])

    // Use reflection to mutate private _uiState directly (no MockK)
    val field: Field = MapViewModel::class.java.getDeclaredField("_uiState")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST") val mutable = field.get(vm) as MutableStateFlow<MapUIState>
    mutable.value = initial

    vm.onBackToAllHunts()

    val after = vm.uiState.value
    assertFalse(after.isFocused)
    assertTrue(after.route.isEmpty())
    assertNull(after.selectedHunt)
  }

  // --------------------
  // Hunt lifecycle (start / validate / finish)
  // --------------------

  @Test
  fun startHuntUpdatesUIStateCorrectly() = runTest {
    val hunts = listOf(sample(uid = Constants.HUNT_UID_1))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

    vm.onMarkerClick(hunts[0])
    vm.startHunt()

    val state = vm.uiState.value
    assertTrue(state.isHuntStarted)
    assertTrue(state.isFocused)
    assertEquals(0, state.validatedCount)
    assertTrue(state.route.isEmpty()) // route is cleared when starting
  }

  @Test
  fun validateCurrentPointWithValidLocationUpdatesState() = runTest {
    val hunts = listOf(sample(uid = Constants.HUNT_UID_1))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

    vm.onMarkerClick(hunts[0])
    vm.startHunt()

    val validLocation = LatLng(Constants.VALID_LAT, Constants.VALID_LNG)
    vm.validateCurrentPoint(validLocation)

    val state = vm.uiState.value
    assertEquals(1, state.validatedCount)
  }

  @Test
  fun validateCurrentPointWithInvalidLocationDoesNotUpdateState() = runTest {
    val hunts = listOf(sample(uid = Constants.HUNT_UID_1))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

    vm.onMarkerClick(hunts[0])
    vm.startHunt()

    val validLocation = LatLng(Constants.VALID_LAT, Constants.VALID_LNG)
    vm.validateCurrentPoint(validLocation)

    val stateBeforeInvalid = vm.uiState.value.validatedCount

    val invalidLocation = LatLng(Constants.INVALID_LAT, Constants.INVALID_LNG)
    vm.validateCurrentPoint(invalidLocation)

    val state = vm.uiState.value
    assertEquals(stateBeforeInvalid, state.validatedCount)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun finishHuntIsEnabledWhenAllCheckpointsValidated() = runTest {
    val hunts = listOf(sample(uid = Constants.HUNT_UID_1))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

    vm.onMarkerClick(hunts[0])
    vm.startHunt()

    val validLocation = LatLng(Constants.VALID_LAT, Constants.VALID_LNG)
    // total checkpoints = 2 (start/end) + middlePoints.size (2) = 4
    repeat(4) { vm.validateCurrentPoint(validLocation) }

    assertTrue(vm.uiState.value.validatedCount >= (hunts[0].middlePoints.size + 2))

    var persistedHunt: Hunt? = null
    vm.finishHunt { finishedHunt -> persistedHunt = finishedHunt }

    advanceUntilIdle()

    val state = vm.uiState.value
    assertFalse(state.isHuntStarted)
    assertNull(state.selectedHunt)
    assertEquals(0, state.validatedCount)
    assertNotNull(persistedHunt)
  }

  @Test
  fun finishHuntIsNotAllowedWhenNotAllCheckpointsValidated() = runTest {
    val hunts = listOf(sample(uid = Constants.HUNT_UID_1))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

    vm.onMarkerClick(hunts[0])
    vm.startHunt()

    val validLocation = LatLng(Constants.VALID_LAT, Constants.VALID_LNG)
    vm.validateCurrentPoint(validLocation)

    vm.finishHunt {}

    val state = vm.uiState.value
    assertTrue(state.isHuntStarted)
    assertNotNull(state.selectedHunt)
    assertEquals(1, state.validatedCount)

    val errorMsg = state.errorMsg
    assertNotNull(errorMsg)
    assertTrue(errorMsg!!.contains(MapScreenStrings.ErrorIncompleteHunt))
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun selectHuntByIdSelectsHuntAndResetsFlags() =
      runTest(mainDispatcherRule.dispatcher) {
        val hunts =
            listOf(
                sample(uid = Constants.HUNT_UID_1),
                sample(uid = Constants.HUNT_UID_2),
            )
        val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

        // Let fetchHunts complete so uiState.hunts is populated
        advanceUntilIdle()

        // Put the ViewModel in a "dirty" state first
        val initialState =
            vm.uiState.value.copy(
                selectedHunt = hunts[0],
                isFocused = true,
                isHuntStarted = true,
                validatedCount = 3,
                currentDistanceToNextMeters = 42,
                route = listOf(LatLng(1.0, 1.0)),
                errorMsg = "Some error",
            )

        // Use reflection to set the internal state (similar to other tests)
        val field: Field = MapViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST") val mutable = field.get(vm) as MutableStateFlow<MapUIState>
        mutable.value = initialState

        // Act: select second hunt by id
        vm.selectHuntById(Constants.HUNT_UID_2)

        val state = vm.uiState.value
        assertEquals(Constants.HUNT_UID_2, state.selectedHunt?.uid)
        assertFalse(state.isFocused)
        assertFalse(state.isHuntStarted)
        assertTrue(state.route.isEmpty())
        assertEquals(MapConfig.DefaultValidatedCount, state.validatedCount)
        assertNull(state.currentDistanceToNextMeters)
        assertNull(state.errorMsg)
      }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun selectHuntByIdWithUnknownIdDoesNotChangeState() =
      runTest(mainDispatcherRule.dispatcher) {
        val hunts =
            listOf(
                sample(uid = Constants.HUNT_UID_1),
                sample(uid = Constants.HUNT_UID_2),
            )
        val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

        // Ensure hunts are loaded into state
        advanceUntilIdle()

        val before = vm.uiState.value
        vm.selectHuntById("non-existent-id")
        val after = vm.uiState.value

        // Data class equality is fine here
        assertEquals(before, after)
      }
}
