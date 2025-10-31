package com.swentseekr.seekr.ui.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.utils.FakeRepoEmpty
import com.swentseekr.seekr.utils.FakeRepoSuccess
import com.swentseekr.seekr.utils.FakeRepoThrows
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.net.URLStreamHandlerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

  private fun sample(uid: String = "1", lat: Double = 10.0, lng: Double = 20.0) =
      Hunt(
          uid = uid,
          start = Location(lat, lng, "Start"),
          end = Location(lat, lng, "End"),
          middlePoints = listOf(Location(lat, lng, "M1"), Location(lat, lng, "M2")),
          status = HuntStatus.FUN,
          title = "Hunt $uid",
          description = "desc",
          time = 1.0,
          distance = 2.0,
          difficulty = Difficulty.EASY,
          authorId = "A",
          image = 0,
          reviewRate = 4.2)

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun initialStateLoadsHuntsAndTargetsFirstStart() =
      runTest(mainDispatcherRule.dispatcher) {
        val hunts = listOf(sample(uid = "1", lat = 46.5, lng = 6.6), sample(uid = "2"))
        val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(hunts.size, state.hunts.size)
        assertEquals(46.5, state.target.latitude, 1e-6)
        assertEquals(6.6, state.target.longitude, 1e-6)
        assertNull(state.errorMsg)
      }

  @Test
  fun initialStateWithEmptyRepoUsesLausanneFallback() = runTest {
    val vm = MapViewModel(repository = FakeRepoEmpty())
    val state = vm.uiState.value
    // Lausanne fallback from MapViewModel default path
    assertEquals(46.519962, state.target.latitude, 0.0001)
    assertEquals(6.633597, state.target.longitude, 0.0001)
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
        assertTrue(state.errorMsg!!.contains("Failed to load hunts"))
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

  @Test
  fun onMarkerClickSelectsHuntAndIsNotFocused() = runTest {
    val hunts = listOf(sample(uid = "1"), sample(uid = "2"))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))
    vm.onMarkerClick(hunts[1])

    val state = vm.uiState.value
    assertEquals("2", state.selectedHunt?.uid)
    assertFalse(state.isFocused)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun onViewHuntClickSetsFocusedTrue() = runTest {
    val hunts = listOf(sample(uid = "1"))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

    advanceUntilIdle()

    vm.onMarkerClick(hunts[0])
    vm.onViewHuntClick()

    val state = vm.uiState.value
    assertTrue(state.isFocused)
    assertEquals("1", state.selectedHunt?.uid)
  }

  @Test
  fun onBackToAllHuntsClearsSelectionAndFocus() = runTest {
    val hunts = listOf(sample(uid = "1"))
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
    val hunts = listOf(sample(uid = "1"))
    val repo = FakeRepoSuccess(hunts)
    val vm = MapViewModel(repository = repo)

    val before = vm.uiState.value.hunts.size
    vm.refreshUIState()
    val after = vm.uiState.value.hunts.size
    assertEquals(before, after)
  }

  companion object {
    object FakeDirections {
      @Volatile
      var nextBody: String =
          """{"status":"OK","routes":[{"legs":[{"steps":[]}],"overview_polyline":{"points":""}}]}"""
    }

    private object HttpsFactory : URLStreamHandlerFactory {
      override fun createURLStreamHandler(protocol: String?): URLStreamHandler? {
        return if (protocol == "https") {
          object : URLStreamHandler() {
            override fun openConnection(u: URL): URLConnection {
              return object : HttpURLConnection(u) {
                override fun getInputStream(): InputStream =
                    ByteArrayInputStream(FakeDirections.nextBody.toByteArray(Charsets.UTF_8))

                override fun getOutputStream(): OutputStream = ByteArrayOutputStream()

                override fun usingProxy(): Boolean = false

                override fun disconnect() {}

                override fun connect() {}
              }
            }
          }
        } else null
      }
    }

    init {
      try {
        URL.setURLStreamHandlerFactory(HttpsFactory)
      } catch (_: Error) {}
    }
  }

  private fun MapViewModel.callRequestDirectionsPolylineSync(): List<LatLng> {
    val m =
        MapViewModel::class
            .java
            .getDeclaredMethod(
                "requestDirectionsPolyline",
                Double::class.javaPrimitiveType,
                Double::class.javaPrimitiveType,
                Double::class.javaPrimitiveType,
                Double::class.javaPrimitiveType,
                List::class.java,
                String::class.java)
    m.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    return m.invoke(this, 46.52, 6.63, 46.53, 6.64, emptyList<Pair<Double, Double>>(), "walking")
        as List<LatLng>
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun onViewHuntClickWithNoSelectedHuntKeepsRouteEmpty() = runTest {
    val hunts = listOf(sample(uid = "1"))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

    advanceUntilIdle()

    vm.onViewHuntClick()

    val state = vm.uiState.value
    assertTrue(state.isFocused)
    assertTrue(state.route.isEmpty())
    assertFalse(state.isRouteLoading)
  }

  @Test
  fun requestDirectionsPolylineParsesStepPolylinesAndDeduplicates() {
    val vm = MapViewModel(repository = FakeRepoEmpty())

    val step1 = listOf(LatLng(0.0, 0.0), LatLng(0.0, 1.0))
    val step2 = listOf(LatLng(0.0, 1.0), LatLng(1.0, 1.0))
    val step1Enc = PolyUtil.encode(step1)
    val step2Enc = PolyUtil.encode(step2)

    val overviewEnc = PolyUtil.encode(listOf(step1[0], step1[1], step2[1]))

    FakeDirections.nextBody =
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

    val points = vm.callRequestDirectionsPolylineSync()

    assertEquals(3, points.size)
    assertEquals(0.0, points[0].latitude, 1e-6)
    assertEquals(0.0, points[0].longitude, 1e-6)
    assertEquals(0.0, points[1].latitude, 1e-6)
    assertEquals(1.0, points[1].longitude, 1e-6)
    assertEquals(1.0, points[2].latitude, 1e-6)
    assertEquals(1.0, points[2].longitude, 1e-6)
  }

  @Test
  fun requestDirectionsPolylineFallsBackToOverviewWhenStepsEmpty() {
    val vm = MapViewModel(repository = FakeRepoEmpty())

    val overviewEnc = PolyUtil.encode(listOf(LatLng(2.0, 2.0), LatLng(3.0, 3.0)))

    FakeDirections.nextBody =
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

    val points = vm.callRequestDirectionsPolylineSync()

    assertEquals(2, points.size)
    assertEquals(2.0, points[0].latitude, 1e-6)
    assertEquals(2.0, points[0].longitude, 1e-6)
    assertEquals(3.0, points[1].latitude, 1e-6)
    assertEquals(3.0, points[1].longitude, 1e-6)
  }

  @Test
  fun requestDirectionsPolylineErrorResponseIsWrappedAndHasCorrectMessage() {
    val vm = MapViewModel(repository = FakeRepoEmpty())

    FakeDirections.nextBody =
        """
        {
          "status": "REQUEST_DENIED",
          "error_message": "Key is invalid"
        }
        """
            .trimIndent()

    try {
      vm.callRequestDirectionsPolylineSync()
      fail("Expected the reflected call to fail with an InvocationTargetException")
    } catch (e: InvocationTargetException) {
      val cause = e.cause
      assertNotNull(cause)
      assertTrue(cause is IllegalStateException)
      if (cause != null) {
        assertTrue(
            cause.message?.contains("Directions API error") == true ||
                cause.message?.contains("REQUEST_DENIED") == true)
      }
    }
  }

  @Test
  fun onBackToAllHuntsClearsRoute() = runTest {
    val hunts = listOf(sample(uid = "1"))
    val vm = MapViewModel(repository = FakeRepoSuccess(hunts))

    val s =
        vm.uiState.value.copy(route = listOf(LatLng(1.0, 1.0), LatLng(2.0, 2.0)), isFocused = true)
    val field = MapViewModel::class.java.getDeclaredField("_uiState")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val mutable = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<*>
    @Suppress("UNCHECKED_CAST")
    (mutable as kotlinx.coroutines.flow.MutableStateFlow<com.swentseekr.seekr.ui.map.MapUIState>)
        .value = s

    vm.onBackToAllHunts()

    val after = vm.uiState.value
    assertFalse(after.isFocused)
    assertTrue(after.route.isEmpty())
    assertNull(after.selectedHunt)
  }
}
