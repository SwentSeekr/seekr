package com.swentseekr.seekr.ui.map

import com.google.android.gms.maps.model.LatLng
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.map.Location
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MapViewModelHelpers functions.
 *
 * This test suite verifies the correctness of mapping functions, distance calculations, and point
 * ordering related to hunts and locations.
 */
class MapViewModelHelpersTest {

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxUnitFun = true)
  }

  @Test
  fun toLatLngMapsLocationFieldsCorrectly() {
    val loc = Location(latitude = 46.5, longitude = 6.6, name = "Lausanne")

    val latLng = loc.toLatLng()

    assertEquals(loc.latitude, latLng.latitude, 0.0)
    assertEquals(loc.longitude, latLng.longitude, 0.0)
  }

  @Test
  fun orderedPointsForReturnsStartAllMiddlePointsThenEnd() {
    val start = Location(1.0, 1.0, "start")
    val middle1 = Location(2.0, 2.0, "m1")
    val middle2 = Location(3.0, 3.0, "m2")
    val end = Location(4.0, 4.0, "end")

    val hunt = mockk<Hunt>()
    every { hunt.start } returns start
    every { hunt.middlePoints } returns listOf(middle1, middle2)
    every { hunt.end } returns end

    val points = orderedPointsFor(hunt)

    assertEquals(4, points.size)
    assertEquals(LatLng(1.0, 1.0), points[0])
    assertEquals(LatLng(2.0, 2.0), points[1])
    assertEquals(LatLng(3.0, 3.0), points[2])
    assertEquals(LatLng(4.0, 4.0), points[3])
  }

  @Test
  fun nextPointForReturnsCorrectNextPointAndHandlesNullHunt() {
    val start = Location(1.0, 1.0, "start")
    val m1 = Location(2.0, 2.0, "m1")
    val end = Location(3.0, 3.0, "end")

    val hunt = mockk<Hunt>()
    every { hunt.start } returns start
    every { hunt.middlePoints } returns listOf(m1)
    every { hunt.end } returns end

    assertEquals(LatLng(1.0, 1.0), nextPointFor(hunt, 0))

    assertEquals(LatLng(2.0, 2.0), nextPointFor(hunt, 1))

    assertEquals(LatLng(3.0, 3.0), nextPointFor(hunt, 2))

    assertNull(nextPointFor(hunt, 3))

    assertNull(nextPointFor(null, 0))
  }

  @Test
  fun distanceMetersReturnsPositiveDistanceForValidPoints() {
    val a = LatLng(46.5191, 6.6338)
    val b = LatLng(46.5200, 6.6320)

    val distance = distanceMeters(a, b)

    assertNotNull(distance)
    assertTrue(distance!! > 0)
  }

  @Test
  fun computeDistanceMetersRawAndDistanceMetersAreConsistentWithinRounding() {
    val a = LatLng(46.5191, 6.6338)
    val b = LatLng(46.5200, 6.6320)

    val raw = computeDistanceMetersRaw(a, b)
    val rounded = distanceMeters(a, b)

    assertNotNull(rounded)
    assertEquals(raw.toInt(), rounded!!)
  }

  @Test
  fun computeDistanceToNextPointReturnsNullWhenThereIsNoNextPoint() {
    val start = Location(1.0, 1.0, "start")
    val end = Location(2.0, 2.0, "end")

    val hunt = mockk<Hunt>()
    every { hunt.start } returns start
    every { hunt.middlePoints } returns emptyList()
    every { hunt.end } returns end

    val current = LatLng(0.0, 0.0)

    val distance = computeDistanceToNextPoint(hunt, validatedCount = 2, currentLocation = current)

    assertNull(distance)
  }

  @Test
  fun computeDistanceToNextPointReturnsDistanceToNextCheckpoint() {
    val start = Location(0.0, 0.001, "start")
    val end = Location(0.0, 0.002, "end")

    val hunt = mockk<Hunt>()
    every { hunt.start } returns start
    every { hunt.middlePoints } returns emptyList()
    every { hunt.end } returns end

    val current = LatLng(0.0, 0.0)

    val distance = computeDistanceToNextPoint(hunt, validatedCount = 0, currentLocation = current)

    assertNotNull(distance)
    assertTrue(distance!! > 0)
  }

  @Test
  fun isHuntFullyValidatedReturnsTrueOnlyWhenAllPointsValidatedOrMore() {
    val hunt = mockk<Hunt>()
    every { hunt.middlePoints } returns listOf(Location(0.0, 0.0, "m1"), Location(0.0, 0.0, "m2"))

    assertFalse(isHuntFullyValidated(hunt, 0))
    assertFalse(isHuntFullyValidated(hunt, 3))
    assertTrue(isHuntFullyValidated(hunt, 4))
    assertTrue(isHuntFullyValidated(hunt, 5))
  }

  @Suppress("UNCHECKED_CAST")
  private fun parseDirectionsResponseReflect(json: JSONObject): List<LatLng> {
    val clazz = Class.forName("com.swentseekr.seekr.ui.map.MapViewModelHelpersKt")
    val method = clazz.getDeclaredMethod("parseDirectionsResponse", JSONObject::class.java)
    method.isAccessible = true
    return method.invoke(null, json) as List<LatLng>
  }
}
