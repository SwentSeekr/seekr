package com.swent.seekr

import org.junit.Assert
import org.junit.Test

class PointTest {

  @Test
  fun checkSimpleDistance() {
    val p1 = Point(2.5, 4.0)
    val p2 = Point(5.5, 8.0)
    Assert.assertEquals(5.0, p1.distanceTo(p2), 0.01)
  }
}
