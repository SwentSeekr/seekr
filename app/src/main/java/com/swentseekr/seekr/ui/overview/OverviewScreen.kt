package com.swentseekr.seekr.ui.overview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.components.HuntCard

object OverviewScreenTestTags {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    modifier: Modifier = Modifier,
    // overviewViewModel: OverviewViewModel = viewModel(),
    searchbarClick: (String) -> Unit = {},
    // onHuntClick: () -> Unit = {},
    onSearchBar: (String) -> Unit = {},
    onActiveBar: (Boolean) -> Unit = {},
) {

  // val uiState by overviewViewModel.uiState.collectAsState()
  // val hunts = uiState.hunts
  val huntsample =
      Hunt(
          uid = "1",
          start = Location(40.7128, -74.0060, "New York"),
          end = Location(40.730610, -73.935242, "Brooklyn"),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = "City Exploration",
          description = "Discover hidden gems in the city",
          time = 2.5,
          distance = 5.0,
          difficulty = Difficulty.EASY,
          author = Author("spike man", "", 1, 2.5, 3.0),
          image = 0,
          reviewRate = 4.5)
  val hunts =
      listOf(
          HuntUiState(huntsample, isLiked = true, isAchived = false),
          HuntUiState(huntsample, isLiked = true, isAchived = false),
          HuntUiState(huntsample, isLiked = true, isAchived = false),
      )

  Column(
      modifier = modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    SearchBar(
        query = "Search",
        searchbarClick,
        onSearchBar,
        true,
        onActiveBar,
        modifier = modifier.fillMaxWidth().padding(8.dp),
        content = {})

    Spacer(modifier = Modifier.height(10.dp))

    /*FilterBar(
        uiState.selectedStatus,
        uiState.selectedDifficulty,
        { overviewViewModel.onStatusFilterSelect(it) },
        { overviewViewModel.onDifficultyFilterSelect(it) }
    )*/

    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn {
      items(hunts.size) { index ->
        val hunt = hunts[index]
        HuntCard(hunt.hunt)
      }
    }
  }
}
/*
@Composable
fun FilterBar(
    selectedStatus: HuntStatus?,
    selectedDifficulty: Difficulty?,
    onStatusSelected: (HuntStatus?) -> Unit,
    onDifficultySelected: (Difficulty?) -> Unit
) {
  Column(
      modifier = Modifier.fillMaxWidth().padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Row {
      // FilterButton("All", selectedStatus == null) { onStatusSelected(null) }
      HuntStatus.values().forEach { status ->
        FilterButton(status.name, selectedStatus == status) { onStatusSelected(status) }
      }
    }
    Row {
      // FilterButton("All", selectedDifficulty == null) { onDifficultySelected(null) }
      Difficulty.values().forEach { difficulty ->
        FilterButton(difficulty.name, selectedDifficulty == difficulty) {
          onDifficultySelected(difficulty)
        }
      }
    }
  }
}

@Composable
fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
  Button(
      onClick = onClick,
      colors =
          ButtonDefaults.buttonColors(
              containerColor = if (isSelected) Color.Green else Color.LightGray),
      modifier = Modifier.padding(4.dp)) {
        Text(text)
      }
}*/

@Preview
@Composable
fun OverviewScreenPreview() {
  OverviewScreen()
}
