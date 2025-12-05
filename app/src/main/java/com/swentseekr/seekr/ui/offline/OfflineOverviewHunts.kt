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
import androidx.compose.ui.tooling.preview.Preview
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

  Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.onPrimary)) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag(OverviewScreenTestTags.OVERVIEW_SCREEN),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // HEADER
      ModernHeader()

      // SEARCH BAR
      ModernSearchBar(
          query = query,
          onQueryChange = { offlineViewModel.onSearchChange(it) },
          onSearch = { offlineViewModel.onSearchChange(it) },
          onActiveChange = {}, // always active offline as well
          onClear = { offlineViewModel.onClearSearch() },
          modifier = modifier)

      // FILTER BAR
      ModernFilterBar(
          selectedStatus = uiState.selectedStatus,
          selectedDifficulty = uiState.selectedDifficulty,
          onStatusSelected = { status: HuntStatus? ->
            offlineViewModel.onStatusFilterSelect(status)
          },
          onDifficultySelected = { difficulty: Difficulty? ->
            offlineViewModel.onDifficultyFilterSelect(difficulty)
          })

      // HUNTS LIST
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
                              if (index == huntUiStates.size - 1)
                                  OverviewScreenTestTags.LAST_HUNT_CARD
                              else OverviewScreenTestTags.HUNT_CARD)
                          .clickable { onHuntClick(hunt.uid) })

              Spacer(modifier = Modifier.height(OverviewScreenDefaults.ListItemSpacing))
            }
          }
    }
  }
}

// Optional very simple preview with empty list
@Preview
@Composable
fun OfflineOverviewHuntsScreenPreview() {
  OfflineOverviewHuntsScreen(hunts = emptyList())
}
