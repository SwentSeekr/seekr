package com.swentseekr.seekr

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the Point data class.
 *
 * This test suite verifies the distance calculation
 * between two Point instances.
 */
class PointTest {

  @Test
  fun checkSimpleDistance() {
    val p1 = Point(2.5, 4.0)
    val p2 = Point(5.5, 8.0)
    assertEquals(5.0, p1.distanceTo(p2), 0.01)
  }
}
