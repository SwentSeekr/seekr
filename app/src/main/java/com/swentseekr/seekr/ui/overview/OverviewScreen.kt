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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.ui.components.HuntCard

object OverviewScreenTestTags {
  const val HUNT_LIST = "HuntList"
  const val HUNT_CARD = "HuntCard"
  const val LAST_HUNT_CARD = "LastHuntCard"
  const val SEARCH_BAR = "SearchBar"
  const val FILTER_BAR = "FilterBar"
  const val FILTER_BUTTON = "FilterButton"
  const val OVERVIEW_SCREEN = "OverviewScreen"
}

const val FILTERS_SECOND = 3

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
        placeholder = { Text("Search hunts...") },
        leadingIcon = {
          if (query.isEmpty()) {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
          } else null
        },
        trailingIcon = {
          if (query.isNotEmpty()) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear Icon",
                modifier = Modifier.clickable { overviewViewModel.onClearSearch() })
          } else null
        },
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 3.dp)
                .height(72.dp)
                .clip(RoundedCornerShape(70))
                .testTag(OverviewScreenTestTags.SEARCH_BAR),
        shape = RoundedCornerShape(70),
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
        Spacer(modifier = Modifier.height(8.dp))
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
      modifier = Modifier.fillMaxWidth().padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    LazyRow(
        modifier = modifier.fillMaxWidth().testTag(OverviewScreenTestTags.FILTER_BAR),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)) {
          val huntStatuses = HuntStatus.values()
          val difficulties = Difficulty.values()
          items(huntStatuses.size) { status ->
            FilterButton(
                text = huntStatuses[status].name,
                isSelected = (selectedStatus == huntStatuses[status]),
                modifier = Modifier.testTag("FilterButton_${status}")) {
                  onStatusSelected(huntStatuses[status])
                }
          }

          items(difficulties.size) { difficulty ->
            FilterButton(
                text = difficulties[difficulty].name,
                isSelected = (selectedDifficulty == difficulties[difficulty]),
                modifier = modifier.testTag("FilterButton_${difficulty + FILTERS_SECOND}")) {
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
      modifier = modifier.padding(4.dp)) {
        Text(text)
      }
}
