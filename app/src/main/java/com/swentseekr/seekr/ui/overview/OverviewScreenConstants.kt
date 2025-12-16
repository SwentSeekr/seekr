package com.swentseekr.seekr.ui.overview

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object OverviewScreenDefaults {
  const val ALPHA_02: Float = 0.2f
  val FilterItemPadding: Dp = 4.dp
  val Border2: Dp = 2.dp
  val Border1: Dp = 1.dp

  // Spacing & Layout
  val HorizontalPadding20: Dp = 20.dp
  val VerticalPadding2: Dp = 2.dp
  val VerticalPadding12: Dp = 12.dp
  val VerticalPadding16: Dp = 16.dp
  val VerticalPadding8: Dp = 8.dp
  val ListItemSpacing: Dp = 8.dp

  // Search bar
  val SearchBarHeight: Dp = 68.dp
  val SearchBarCornerRadius: Dp = 16.dp
  val SearchBarElevation: Dp = 4.dp

  // Font sizes
  val SmallFontSize = 14.sp

  val FilterChipCornerRadius = 12.dp
  val DifficultyFilterOffset = 3

  // Refresh indicator
  val RefreshIndicatorWeight = 1f

  // BASIC NUMBER
  const val ONE = 1
}

object OverviewScreenStrings {
  const val SEARCH_PLACEHOLDER = "Search hunts..."
  const val SEARCH_ICON_DESCRIPTION = "Search Icon"
  const val CLEAR_ICON_DESCRIPTION = "Clear Icon"
  const val FILTER_BY = "Filter by"

  const val UNKNOWN_AUTHOR = "Unknown Author"
  const val TITLE = "Discover"
  const val SUB_TITLE = "Find your next adventure"
}

object OverviewScreenTestTags {
  const val HUNT_LIST = "HuntList"
  const val HUNT_CARD = "HuntCard"
  const val LAST_HUNT_CARD = "LastHuntCard"
  const val SEARCH_BAR = "SearchBar"
  const val FILTER_BAR = "FilterBar"
  const val FILTER_BUTTON = "FilterButton"
  const val OVERVIEW_SCREEN = "OverviewScreen"

  const val REFRESH_INDICATOR = "RefreshIndicator"
}
