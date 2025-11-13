package com.swentseekr.seekr.ui.overview

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object OverviewScreenDefaults {
  val SearchBarHorizontalPadding: Dp = 8.dp
  val SearchBarVerticalPadding: Dp = 3.dp
  val SearchBarHeight: Dp = 72.dp
  val SearchBarCornerRadius: Dp = 70.dp
  val FilterBarPadding: Dp = 8.dp
  val FilterItemSpacing: Dp = 8.dp
  val FilterItemPadding: Dp = 4.dp
  val ListItemSpacing: Dp = 8.dp
  const val DifficultyFilterOffset = 3
}

object OverviewScreenStrings {
  const val SearchPlaceholder = "Search hunts..."
  const val SearchIconDescription = "Search Icon"
  const val ClearIconDescription = "Clear Icon"
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
