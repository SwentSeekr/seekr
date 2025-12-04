package com.swentseekr.seekr.ui.navigation

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Constants used for the bottom navigation bar UI. All values come from the original hardcoded
 * values (no visual change).
 */
object BottomNavUIConstants {

  // Sizes
  val IconContainerSize = 48.dp
  val IconHaloSize = 40.dp
  val IconSizeSelected = 24.dp
  val IconSizeUnselected = 24.dp

  // Colors / Alpha values
  const val HaloAlpha = 0.12f

  // Shapes
  val HaloShape = CircleShape

  // Indicator
  val IndicatorColorTransparent = Color.Transparent
}
