package com.swentseekr.seekr.ui.overview

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object OverviewScreenDefaults {
  val Alpha02: Float = 0.2f
  val FilterItemPadding: Dp = 4.dp
  val Border2: Dp = 2.dp
  val Border1: Dp = 1.dp

  // Spacing & Layout
  val HorizontalPadding20: Dp = 20.dp
  val VerticalPadding12: Dp = 12.dp
  val VerticalPadding16: Dp = 16.dp
  val VerticalPadding8: Dp = 8.dp
  val ListItemSpacing: Dp = 8.dp

  // Search bar
  val SearchBarHeight: Dp = 70.dp
  val SearchBarCornerRadius: Dp = 16.dp
  val SearchBarElevation: Dp = 4.dp

  // Font sizes
  val DiscoverFontSize = 32.sp
  val NextAdventureFontSize = 16.sp
  val SmallFontSize = 14.sp

  // Placeholder / Icon gray
  val Gray666 = Color(0xFF666666)
  val Gray999 = Color(0xFF999999)

  val FilterChipCornerRadius = 12.dp
  val FilterChipLabelSize = 13.dp
  val DifficultyFilterOffset = 3

  // Status colors
  val StatusFun = Color(0xFF9C27B0)
  val StatusDiscover = Color(0xFF2196F3) // matches theme Blue but used differently → keep
  val StatusSport = Color(0xFFFF5722)

  // Difficulty colors

  val DifficultyEasy = Color(0xFF4CAF50)
  val DifficultyIntermediate = Color(0xFFFFA726)
  val DifficultyHard = Color(0xFFEF5350)
}

object OverviewScreenStrings {
  const val SearchPlaceholder = "Search hunts..."
  const val SearchIconDescription = "Search Icon"
  const val ClearIconDescription = "Clear Icon"
  const val FilterBy = "Filter by"
}

object OverviewScreenTestTags {
  const val HUNT_LIST = "HuntList"
  const val HUNT_CARD = "HuntCard"
  const val LAST_HUNT_CARD = "LastHuntCard"
  const val SEARCH_BAR = "SearchBar"
  const val FILTER_BAR = "FilterBar"
  const val FILTER_BUTTON = "FilterButton"
  const val OVERVIEW_SCREEN = "OverviewScreen"
}
