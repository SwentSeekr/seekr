package com.swentseekr.seekr

import kotlin.math.sqrt

/**
 * Represents a 2D point with coordinates [x] and [y].
 *
 * @property x The x-coordinate of the point.
 * @property y The y-coordinate of the point.
 */
data class Point(val x: Double, val y: Double) {

  /**
   * Calculates the Euclidean distance from this point to another point [p].
   *
   * Uses the formula: √((x2 - x1)² + (y2 - y1)²)
   *
   * @param p The other [Point] to which the distance is calculated.
   * @return The Euclidean distance as a [Double].
   */
  fun distanceTo(p: Point): Double {
    val dx = x - p.x
    val dy = y - p.y
    return sqrt(dx * dx + dy * dy)
  }
}
