package com.swentseekr.seekr.ui.overview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.ui.components.HuntCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    modifier: Modifier = Modifier,
    overviewViewModel: OverviewViewModel = viewModel(),
    navHostController: NavHostController = rememberNavController(),
    onActiveBar: (Boolean) -> Unit = {},
    onHuntClick: (String) -> Unit = {},
) {

  val uiState by overviewViewModel.uiState.collectAsState()
  val query = overviewViewModel.searchQuery
  val hunts = uiState.hunts

  LaunchedEffect(Unit) { overviewViewModel.refreshUIState() }

  Column(
      modifier = modifier.fillMaxWidth().testTag(OverviewScreenTestTags.OVERVIEW_SCREEN),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    SearchBar(
        query = query,
        { overviewViewModel.onSearchChange(it) },
        { overviewViewModel.onSearchChange(it) },
        true,
        onActiveChange = onActiveBar,
        placeholder = { Text(OverviewScreenStrings.SearchPlaceholder) },
        leadingIcon = {
          if (query.isEmpty()) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = OverviewScreenStrings.SearchIconDescription)
          } else null
        },
        trailingIcon = {
          if (query.isNotEmpty()) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = OverviewScreenStrings.ClearIconDescription,
                modifier = Modifier.clickable { overviewViewModel.onClearSearch() })
          } else null
        },
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = OverviewScreenDefaults.SearchBarHorizontalPadding,
                    vertical = OverviewScreenDefaults.SearchBarVerticalPadding)
                .height(OverviewScreenDefaults.SearchBarHeight)
                .clip(RoundedCornerShape(OverviewScreenDefaults.SearchBarCornerRadius))
                .testTag(OverviewScreenTestTags.SEARCH_BAR),
        shape = RoundedCornerShape(OverviewScreenDefaults.SearchBarCornerRadius),
        content = {})

    FilterBar(
        uiState.selectedStatus,
        uiState.selectedDifficulty,
        { overviewViewModel.onStatusFilterSelect(it) },
        { overviewViewModel.onDifficultyFilterSelect(it) })

    LazyColumn(modifier = modifier.testTag(OverviewScreenTestTags.HUNT_LIST)) {
      items(hunts.size) { index ->
        val hunt = hunts[index]
        HuntCard(
            hunt.hunt,
            modifier =
                modifier
                    .testTag(
                        if (index == (hunts.size - 1)) OverviewScreenTestTags.LAST_HUNT_CARD
                        else OverviewScreenTestTags.HUNT_CARD)
                    .clickable { onHuntClick(hunt.hunt.uid) },
        )
        Spacer(modifier = Modifier.height(OverviewScreenDefaults.ListItemSpacing))
      }
    }
  }
}

@Preview
@Composable
fun OverviewScreenPreview() {
  OverviewScreen()
}

@Composable
fun FilterBar(
    selectedStatus: HuntStatus?,
    selectedDifficulty: Difficulty?,
    onStatusSelected: (HuntStatus?) -> Unit,
    onDifficultySelected: (Difficulty?) -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(
      modifier = Modifier.fillMaxWidth().padding(OverviewScreenDefaults.FilterBarPadding),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    val huntStatuses = remember { HuntStatus.values() }
    val difficulties = remember { Difficulty.values() }
    LazyRow(
        modifier = modifier.fillMaxWidth().testTag(OverviewScreenTestTags.FILTER_BAR),
        horizontalArrangement =
            androidx.compose.foundation.layout.Arrangement.spacedBy(
                OverviewScreenDefaults.FilterItemSpacing),
        contentPadding =
            androidx.compose.foundation.layout.PaddingValues(
                horizontal = OverviewScreenDefaults.FilterBarPadding)) {
          items(huntStatuses.size) { status ->
            FilterButton(
                text = huntStatuses[status].name,
                isSelected = (selectedStatus == huntStatuses[status]),
                modifier = Modifier.testTag("${OverviewScreenTestTags.FILTER_BUTTON}_${status}")) {
                  onStatusSelected(huntStatuses[status])
                }
          }
          items(difficulties.size) { difficulty ->
            FilterButton(
                text = difficulties[difficulty].name,
                isSelected = (selectedDifficulty == difficulties[difficulty]),
                modifier =
                    modifier.testTag(
                        "${OverviewScreenTestTags.FILTER_BUTTON}_${difficulty + OverviewScreenDefaults.DifficultyFilterOffset}")) {
                  onDifficultySelected(difficulties[difficulty])
                }
          }
        }
  }
}

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
