package com.swentseekr.seekr.ui.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.ui.components.HuntCard
import com.swentseekr.seekr.ui.overview.ModernFilterBar
import com.swentseekr.seekr.ui.overview.ModernHeader
import com.swentseekr.seekr.ui.overview.ModernSearchBar
import com.swentseekr.seekr.ui.overview.OverviewScreenDefaults
import com.swentseekr.seekr.ui.overview.OverviewScreenDefaults.VerticalPadding16
import com.swentseekr.seekr.ui.overview.OverviewScreenTestTags

/**
 * Offline variant of the overview hunts screen.
 *
 * This composable mirrors the online overview experience but operates purely on a provided list of
 * [Hunt]s that are available offline. It reuses the same UI building blocks as the online screen:
 * - [ModernHeader] for the top title/header section.
 * - [ModernSearchBar] for client-side search.
 * - [ModernFilterBar] for filtering by [HuntStatus] and [Difficulty].
 * - [HuntCard] for rendering each hunt in the list.
 *
 * Behavior:
 * - The provided [hunts] are passed into [OfflineOverviewViewModel], which:
 *     - Maintains the search query and filter state.
 *     - Exposes a derived list of hunts via its UI state.
 * - Searching and filtering are performed locally in memory, without network calls.
 * - Tapping a hunt triggers [onHuntClick] with the hunt's unique ID.
 *
 * The root container uses the `onPrimary` color from the theme, keeping visual parity with the rest
 * of the offline UI.
 *
 * @param hunts List of hunts available offline. This is the source data for the offline overview;
 *   no remote fetching is performed.
 * @param modifier Optional [Modifier] applied to the outermost container.
 * @param onHuntClick Callback invoked when a hunt card is tapped. The parameter is the unique hunt
 *   ID, which the caller can use to navigate to an offline hunt-details screen.
 */
@Composable
fun OfflineOverviewHuntsScreen(
    hunts: List<Hunt>,
    modifier: Modifier = Modifier,
    onHuntClick: (String) -> Unit = {},
) {
  val offlineViewModel = remember(hunts) { OfflineOverviewViewModel(hunts) }
  val uiState by offlineViewModel.uiState.collectAsState()
  val query = offlineViewModel.searchQuery
  val huntUiStates = uiState.hunts

  Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag(OverviewScreenTestTags.OVERVIEW_SCREEN),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      ModernHeader()

      // SEARCH BAR – local, offline search.
      ModernSearchBar(
          query = query,
          onQueryChange = { offlineViewModel.onSearchChange(it) },
          onSearch = { offlineViewModel.onSearchChange(it) },
          onActiveChange = {}, // always active offline as well
          onClear = { offlineViewModel.onClearSearch() },
          modifier = modifier)

      // FILTER BAR – offline filters for status and difficulty.
      ModernFilterBar(
          selectedStatus = uiState.selectedStatus,
          selectedDifficulty = uiState.selectedDifficulty,
          onStatusSelected = { status: HuntStatus? ->
            offlineViewModel.onStatusFilterSelect(status)
          },
          onDifficultySelected = { difficulty: Difficulty? ->
            offlineViewModel.onDifficultyFilterSelect(difficulty)
          })

      // HUNTS LIST – filtered, searched offline list of HuntCard items.
      LazyColumn(
          modifier = modifier.testTag(OverviewScreenTestTags.HUNT_LIST).fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          contentPadding = PaddingValues(bottom = VerticalPadding16)) {
            items(huntUiStates.size) { index ->
              val item = huntUiStates[index]
              val hunt = item.hunt

              HuntCard(
                  hunt = hunt,
                  modifier =
                      modifier
                          .testTag(
                              if (index == huntUiStates.size - OfflineConstants.ONE)
                                  OverviewScreenTestTags.LAST_HUNT_CARD
                              else OverviewScreenTestTags.HUNT_CARD)
                          .clickable { onHuntClick(hunt.uid) })

              Spacer(modifier = Modifier.height(OverviewScreenDefaults.ListItemSpacing))
            }
          }
    }
  }
}
