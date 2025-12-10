package com.swentseekr.seekr.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.ui.components.HuntCard
import com.swentseekr.seekr.ui.huntcardview.HuntCardViewModel
import com.swentseekr.seekr.ui.overview.OverviewScreenDefaults.Alpha02
import com.swentseekr.seekr.ui.overview.OverviewScreenDefaults.Border1
import com.swentseekr.seekr.ui.overview.OverviewScreenDefaults.Border2
import com.swentseekr.seekr.ui.overview.OverviewScreenDefaults.FilterItemPadding
import com.swentseekr.seekr.ui.overview.OverviewScreenDefaults.Gray666
import com.swentseekr.seekr.ui.overview.OverviewScreenDefaults.HorizontalPadding20
import com.swentseekr.seekr.ui.overview.OverviewScreenDefaults.SearchBarCornerRadius
import com.swentseekr.seekr.ui.overview.OverviewScreenDefaults.SearchBarElevation
import com.swentseekr.seekr.ui.overview.OverviewScreenDefaults.SearchBarHeight
import com.swentseekr.seekr.ui.overview.OverviewScreenDefaults.SmallFontSize
import com.swentseekr.seekr.ui.overview.OverviewScreenDefaults.VerticalPadding12
import com.swentseekr.seekr.ui.overview.OverviewScreenDefaults.VerticalPadding16
import com.swentseekr.seekr.ui.overview.OverviewScreenDefaults.VerticalPadding8
import com.swentseekr.seekr.ui.overview.OverviewScreenStrings.FilterBy

/**
 * Overview screen displaying the main hunt discovery experience.
 *
 * This screen includes:
 * - A header section with title and subtitle
 * - A search bar with expand/collapse behavior
 * - Status and difficulty filter controls
 * - A scrollable list of hunts with pull-to-refresh support
 *
 * @param modifier Modifier used to adjust layout or styling.
 * @param overviewViewModel Provides hunt data, search state, and filtering logic.
 * @param huntCardViewModel Provides like/unlike state for individual hunt cards.
 * @param onActiveBar Called when the search bar expands or collapses.
 * @param onHuntClick Invoked when a hunt card is tapped, returning the hunt ID.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun OverviewScreen(
    modifier: Modifier = Modifier,
    overviewViewModel: OverviewViewModel = viewModel(),
    huntCardViewModel: HuntCardViewModel = viewModel(),
    onActiveBar: (Boolean) -> Unit = {},
    onHuntClick: (String) -> Unit = {},
) {

  val uiState by overviewViewModel.uiState.collectAsState()
  val query = overviewViewModel.searchQuery
  val hunts = uiState.hunts

  LaunchedEffect(Unit) {
    overviewViewModel.refreshUIState()
    huntCardViewModel.loadCurrentUserID()
  }

  val uiStateHuntCard by huntCardViewModel.uiState.collectAsState()

  val pullRefreshState =
      rememberPullRefreshState(
          refreshing = uiState.isRefreshing, onRefresh = { overviewViewModel.refreshUIState() })

  Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.onPrimary)) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag(OverviewScreenTestTags.OVERVIEW_SCREEN),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

      // MODERN HEADER WITH TITLE
      ModernHeader()

      val likedHuntsCache by huntCardViewModel.likedHuntsCache.collectAsState()

      Box(
          modifier =
              Modifier.fillMaxWidth()
                  .weight(OverviewScreenDefaults.RefreshIndicatorWeight)
                  .pullRefresh(pullRefreshState)) {
            LazyColumn(
                modifier = modifier.testTag(OverviewScreenTestTags.HUNT_LIST).fillMaxSize(),
                contentPadding = PaddingValues(bottom = OverviewScreenDefaults.VerticalPadding16),
                horizontalAlignment = Alignment.CenterHorizontally) {
                  item {
                    // MODERN SEARCH BAR
                    ModernSearchBar(
                        query = query,
                        onQueryChange = { overviewViewModel.onSearchChange(it) },
                        onSearch = { overviewViewModel.onSearchChange(it) },
                        onActiveChange = onActiveBar,
                        onClear = { overviewViewModel.onClearSearch() },
                        modifier = modifier)
                  }
                  item {
                    // MODERN FILTER BAR
                    ModernFilterBar(
                        selectedStatus = uiState.selectedStatus,
                        selectedDifficulty = uiState.selectedDifficulty,
                        onStatusSelected = { overviewViewModel.onStatusFilterSelect(it) },
                        onDifficultySelected = { overviewViewModel.onDifficultyFilterSelect(it) })
                  }

                  // HUNTS LIST with pull-to-refresh ONLY on the list
                  items(hunts, key = { it.hunt.uid }) { hunt ->
                    val authorId = hunt.hunt.authorId

                    LaunchedEffect(authorId) {
                      if (authorId.isNotBlank()) {
                        huntCardViewModel.loadMultipleAuthorProfiles(authorId)
                      }
                    }

                    // Retrieve correct profile for THIS hunt
                    val authorProfile = uiStateHuntCard.authorProfiles[authorId]
                    val author = authorProfile?.author?.pseudonym ?: "Unknown Author"

                    HuntCard(
                        hunt.hunt,
                        authorName = author,
                        modifier =
                            modifier
                                .testTag(
                                    if (hunts.lastIndex == hunts.size - OverviewScreenDefaults.ONE)
                                        OverviewScreenTestTags.LAST_HUNT_CARD
                                    else OverviewScreenTestTags.HUNT_CARD)
                                .clickable { onHuntClick(hunt.hunt.uid) },
                        isLiked = likedHuntsCache.contains(hunt.hunt.uid),
                        onLikeClick = { huntId -> huntCardViewModel.onLikeClick(huntId) },
                    )

                    Spacer(modifier = Modifier.height(OverviewScreenDefaults.ListItemSpacing))
                  }
                }

            // Indicator now tied just to the list area
            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier =
                    Modifier.align(Alignment.TopCenter)
                        .testTag(OverviewScreenTestTags.REFRESH_INDICATOR))
          }
    }
  }
}

/**
 * Displays the header of the screen with the main section title ("Discover") and a subtitle
 * encouraging users to explore new hunts.
 *
 * Uses theme colors for text and spacing from OverviewScreenDefaults.
 */
@Composable
fun ModernHeader() {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .background(MaterialTheme.colorScheme.onPrimary)
              .padding(horizontal = HorizontalPadding20, vertical = VerticalPadding16)) {
        Text(
            text = "Discover",
            fontSize = OverviewScreenDefaults.DiscoverFontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface)
        Text(
            text = "Find your next adventure",
            fontSize = OverviewScreenDefaults.NextAdventureFontSize,
            color = Gray666,
            modifier = Modifier.padding(top = FilterItemPadding))
      }
}

/**
 * Search bar allowing the user to filter hunts by text.
 *
 * Features:
 * - Leading search icon when query is empty
 * - Trailing clear icon when query is non-empty
 * - Uses container and text colors from theme/defaults
 *
 * @param query The current search query text.
 * @param onQueryChange Triggered on text updates.
 * @param onSearch Triggered when submitting the search.
 * @param onActiveChange Triggered when the bar becomes active/inactive.
 * @param onClear Clears the current query.
 * @param modifier Optional layout modifier.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
  SearchBar(
      query = query,
      onQueryChange = onQueryChange,
      onSearch = onSearch,
      active = true,
      onActiveChange = onActiveChange,
      placeholder = {
        Text(OverviewScreenStrings.SearchPlaceholder, color = OverviewScreenDefaults.Gray999)
      },
      leadingIcon = {
        if (query.isEmpty()) {
          Icon(
              imageVector = Icons.Default.Search,
              contentDescription = OverviewScreenStrings.SearchIconDescription,
              tint = Gray666)
        } else null
      },
      trailingIcon = {
        if (query.isNotEmpty()) {
          Icon(
              imageVector = Icons.Default.Clear,
              contentDescription = OverviewScreenStrings.ClearIconDescription,
              tint = Gray666,
              modifier = Modifier.clickable { onClear() })
        } else null
      },
      modifier =
          modifier
              .fillMaxWidth()
              .padding(horizontal = HorizontalPadding20, vertical = VerticalPadding12)
              .height(SearchBarHeight)
              .testTag(OverviewScreenTestTags.SEARCH_BAR),
      shape = RoundedCornerShape(SearchBarCornerRadius),
      colors =
          SearchBarDefaults.colors(
              containerColor = MaterialTheme.colorScheme.onPrimary,
              dividerColor = Color.Transparent),
      shadowElevation = SearchBarElevation,
      content = {})
}

/**
 * Displays filter chips for both HuntStatus and Difficulty.
 *
 * Includes section title ("Filter by") and two horizontal chip groups:
 * - Status chips (FUN, DISCOVER, SPORT)
 * - Difficulty chips (EASY, INTERMEDIATE, DIFFICULT)
 *
 * @param selectedStatus Currently selected hunt status.
 * @param selectedDifficulty Currently selected difficulty.
 * @param onStatusSelected Callback when a status filter is pressed.
 * @param onDifficultySelected Callback when a difficulty filter is pressed.
 * @param modifier Optional layout modifier.
 */
@Composable
fun ModernFilterBar(
    selectedStatus: HuntStatus?,
    selectedDifficulty: Difficulty?,
    onStatusSelected: (HuntStatus?) -> Unit,
    onDifficultySelected: (Difficulty?) -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .background(MaterialTheme.colorScheme.onPrimary)
              .padding(vertical = VerticalPadding12),
      horizontalAlignment = Alignment.Start,
  ) {
    // SECTION TITLE
    Text(
        text = FilterBy,
        fontSize = SmallFontSize,
        fontWeight = FontWeight.SemiBold,
        color = Gray666,
        modifier = Modifier.padding(horizontal = HorizontalPadding20, vertical = VerticalPadding8))

    val huntStatuses = remember { HuntStatus.values() }
    val difficulties = remember { Difficulty.values() }

    LazyRow(
        modifier = modifier.fillMaxWidth().testTag(OverviewScreenTestTags.FILTER_BAR),
        horizontalArrangement = Arrangement.spacedBy(VerticalPadding8),
        contentPadding = PaddingValues(horizontal = HorizontalPadding20)) {
          // STATUS FILTERS
          items(huntStatuses.size) { index ->
            val status = huntStatuses[index]
            ModernFilterChip(
                text = status.name,
                isSelected = (selectedStatus == status),
                color = getStatusColor(status),
                modifier = Modifier.testTag("${OverviewScreenTestTags.FILTER_BUTTON}_$index")) {
                  onStatusSelected(status)
                }
          }

          // DIFFICULTY FILTERS
          items(difficulties.size) { index ->
            val difficulty = difficulties[index]
            ModernFilterChip(
                text = difficulty.name,
                isSelected = (selectedDifficulty == difficulty),
                color = getDifficultyColor(difficulty),
                modifier =
                    Modifier.testTag(
                        "${OverviewScreenTestTags.FILTER_BUTTON}_${index + OverviewScreenDefaults.DifficultyFilterOffset}")) {
                  onDifficultySelected(difficulty)
                }
          }
        }

    Spacer(modifier = Modifier.height(VerticalPadding8))
  }
}

/**
 * Single filter chip used for both status and difficulty filters.
 *
 * Behavior:
 * - Highlights with custom colors when selected
 * - Displays bold text when active
 * - Uses rounded shape and border logic based on selection state
 *
 * @param text Chip label.
 * @param isSelected Whether the chip is currently selected.
 * @param color Base color used for borders and selected backgrounds.
 * @param modifier Optional layout modifier.
 * @param onClick Called when the chip is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernFilterChip(
    text: String,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
  FilterChip(
      selected = isSelected,
      onClick = onClick,
      label = {
        Text(
            text = text,
            fontSize = SmallFontSize,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
      },
      modifier = modifier,
      colors =
          FilterChipDefaults.filterChipColors(
              containerColor = MaterialTheme.colorScheme.onPrimary,
              selectedContainerColor = color.copy(alpha = Alpha02),
              labelColor = Gray666,
              selectedLabelColor = color),
      border =
          FilterChipDefaults.filterChipBorder(
              borderColor = if (isSelected) color else MaterialTheme.colorScheme.onPrimary,
              selectedBorderColor = color,
              borderWidth = if (isSelected) Border2 else Border1,
              enabled = true,
              selected = true),
      shape = RoundedCornerShape(OverviewScreenDefaults.FilterChipCornerRadius),
  )
}

/**
 * Helper function for status colors Returns the color associated with a HuntStatus.
 *
 * Colors are defined in OverviewScreenDefaults.
 *
 * @param status HuntStatus value.
 */
fun getStatusColor(status: HuntStatus): Color {
  return when (status) {
    HuntStatus.FUN -> OverviewScreenDefaults.StatusFun // Purple
    HuntStatus.DISCOVER -> OverviewScreenDefaults.StatusDiscover // Blue
    HuntStatus.SPORT -> OverviewScreenDefaults.StatusSport // Orange-red
  }
}

/**
 * Helper function for difficulty colors Returns the color associated with a Difficulty level.
 *
 * Difficulty colors also come from OverviewScreenDefaults.
 *
 * @param difficulty The selected difficulty.
 */
fun getDifficultyColor(difficulty: Difficulty): Color {
  return when (difficulty) {
    Difficulty.EASY -> OverviewScreenDefaults.DifficultyEasy // Green
    Difficulty.INTERMEDIATE -> OverviewScreenDefaults.DifficultyIntermediate // Orange
    Difficulty.DIFFICULT -> OverviewScreenDefaults.DifficultyHard // Red
  }
}

/** Preview for the OverviewScreen in an isolated Compose environment. */
@Preview
@Composable
fun OverviewScreenPreview() {
  OverviewScreen()
}

/**
 * Legacy filter button kept for compatibility. Used only if older UI code relies on the previous
 * filter layout.
 *
 * @param text Button label.
 * @param isSelected Whether the button is selected.
 * @param modifier Optional layout modifier.
 * @param onClick Triggered when the button is pressed.
 */
@Composable
fun FilterButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
  Button(
      onClick = onClick,
      colors =
          ButtonDefaults.buttonColors(
              containerColor =
                  if (isSelected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.onSecondary),
      modifier = modifier.padding(OverviewScreenDefaults.FilterItemPadding)) {
        Text(text)
      }
}
