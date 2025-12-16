package com.swentseekr.seekr.ui.navigation

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp

/**
 * Centralized constants for the Bottom Navigation UI.
 *
 * Provides sizes, colors, shapes, and alpha values used in the bottom navigation bar.
 * Extracted from original hardcoded values to avoid magic numbers and maintain consistency.
 */
object BottomNavUIConstants {

  // Sizes
  val IconContainerSize = 48.dp
  val IconHaloSize = 40.dp
  val IconSizeSelected = 24.dp
  val IconSizeUnselected = 24.dp

  // Colors / Alpha values
  const val HALO_ALPHA = 0.12f

  // Shapes
  val HaloShape = CircleShape
}
