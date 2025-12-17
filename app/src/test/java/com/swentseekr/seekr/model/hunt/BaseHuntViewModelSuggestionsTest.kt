@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.swentseekr.seekr.model.hunt

import com.google.android.gms.maps.model.LatLng
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.BaseHuntViewModel
import com.swentseekr.seekr.ui.hunt.HuntUIState
import com.swentseekr.seekr.ui.map.computeDistanceMetersRaw
import com.swentseekr.seekr.ui.map.requestDirectionsPolyline
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BaseHuntViewModelSuggestionsTest {

  private val testDispatcher = StandardTestDispatcher()

  private class TestVm(ioDispatcher: kotlinx.coroutines.CoroutineDispatcher) :
      BaseHuntViewModel(repository = mockk(relaxed = true), ioDispatcher = ioDispatcher) {
    override fun buildHunt(state: HuntUIState): Hunt = mockk(relaxed = true)

    override suspend fun persist(hunt: Hunt) {}
  }

  private lateinit var vm: TestVm

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    mockkStatic(::requestDirectionsPolyline)
    mockkStatic(::computeDistanceMetersRaw)

    vm = TestVm(ioDispatcher = testDispatcher)
  }

  @After
  fun tearDown() {
    unmockkStatic(::requestDirectionsPolyline)
    unmockkStatic(::computeDistanceMetersRaw)
    Dispatchers.resetMain()
  }

  @Test
  fun autoSuggestionsUsesDirectionsAndCachesDistanceWhenStatusChanges() = runTest {
    val points =
        listOf(
            Location(latitude = 46.0, longitude = 6.0, ""),
            Location(latitude = 46.001, longitude = 6.001, ""),
            Location(latitude = 46.002, longitude = 6.002, ""),
        )

    every {
      requestDirectionsPolyline(
          originLat = any(),
          originLng = any(),
          destLat = any(),
          destLng = any(),
          waypoints = any(),
          travelMode = any(),
          apiKey = any())
    } returns listOf(LatLng(0.0, 0.0), LatLng(0.0, 0.01))

    every { computeDistanceMetersRaw(any(), any()) } returns 1000.0

    assertTrue(vm.setPoints(points))
    advanceUntilIdle()

    val firstTime = vm.uiState.value.time
    val firstDistance = vm.uiState.value.distance
    assertTrue("distance should be suggested", firstDistance.isNotBlank())
    assertTrue("time should be suggested", firstTime.isNotBlank())
    assertNull(vm.uiState.value.invalidDistanceMsg)
    assertNull(vm.uiState.value.invalidTimeMsg)

    vm.setStatus(HuntStatus.FUN)
    advanceUntilIdle()

    verify(exactly = 1) {
      requestDirectionsPolyline(any(), any(), any(), any(), any(), any(), any())
    }

    val funTime = vm.uiState.value.time
    assertNotEquals("time should be recomputed for new status", firstTime, funTime)

    vm.setStatus(HuntStatus.SPORT)
    advanceUntilIdle()

    val sportTime = vm.uiState.value.time
    assertNotEquals("time should change again for another status", funTime, sportTime)
  }

  @Test
  fun autoSuggestionsNotAppliedWhenPolylineIsTooShort() = runTest {
    val points =
        listOf(
            Location(latitude = 46.0, longitude = 6.0, ""),
            Location(latitude = 46.001, longitude = 6.001, ""),
            Location(latitude = 46.002, longitude = 6.002, ""),
        )

    every { requestDirectionsPolyline(any(), any(), any(), any(), any(), any(), any()) } returns
        emptyList()
    every { computeDistanceMetersRaw(any(), any()) } returns 1000.0

    assertTrue(vm.setPoints(points))
    advanceUntilIdle()

    assertEquals("", vm.uiState.value.distance)
    assertEquals("", vm.uiState.value.time)

    verify(exactly = 0) { computeDistanceMetersRaw(any(), any()) }
  }

  @Test
  fun autoSuggestionsNotAppliedWhenComputedDistanceIsZero() = runTest {
    val points =
        listOf(
            Location(latitude = 46.0, longitude = 6.0, ""),
            Location(latitude = 46.001, longitude = 6.001, ""),
            Location(latitude = 46.002, longitude = 6.002, ""),
        )

    every { requestDirectionsPolyline(any(), any(), any(), any(), any(), any(), any()) } returns
        listOf(LatLng(0.0, 0.0), LatLng(0.0, 0.01))

    every { computeDistanceMetersRaw(any(), any()) } returns 0.0

    assertTrue(vm.setPoints(points))
    advanceUntilIdle()

    assertEquals("", vm.uiState.value.distance)
    assertEquals("", vm.uiState.value.time)
  }

  @Test
  fun autoSuggestionsDoesNotOverrideUserEnteredTimeOrDistance() = runTest {
    val points =
        listOf(
            Location(latitude = 46.0, longitude = 6.0, ""),
            Location(latitude = 46.001, longitude = 6.001, ""),
            Location(latitude = 46.002, longitude = 6.002, ""),
        )

    every { requestDirectionsPolyline(any(), any(), any(), any(), any(), any(), any()) } returns
        listOf(LatLng(0.0, 0.0), LatLng(0.0, 0.01))

    every { computeDistanceMetersRaw(any(), any()) } returns 1500.0

    vm.setTime("9")
    vm.setDistance("9")

    assertTrue(vm.setPoints(points))
    advanceUntilIdle()

    assertEquals("9", vm.uiState.value.time)
    assertEquals("9", vm.uiState.value.distance)
  }
}
