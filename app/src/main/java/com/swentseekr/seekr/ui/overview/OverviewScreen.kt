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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.components.HuntCard

object OverviewScreenTestTags {
  const val HUNT_LIST = "HuntList"
  const val HUNT_CARD = "HuntCard"
  const val LAST_HUNT_CARD = "LastHuntCard"
  const val SEARCH_BAR = "SearchBar"
  const val FILTER_BAR = "FilterBar"
  const val FILTER_BUTTON = "FilterButton"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    modifier: Modifier = Modifier,
    overviewViewModel: OverviewViewModel = viewModel(),
    onActiveBar: (Boolean) -> Unit = {},
) {

  val uiState by overviewViewModel.uiState.collectAsState()
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
          image = R.drawable.ic_launcher_foreground,
          reviewRate = 4.5)
  val hunts =
      listOf(
          HuntUiState(huntsample, isLiked = true, isAchived = false),
          HuntUiState(huntsample, isLiked = true, isAchived = false),
          HuntUiState(huntsample, isLiked = true, isAchived = false),
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
        {overviewViewModel.onSearchChange(it)},
        {overviewViewModel.onSearchChange(it)},
        true,
        onActiveChange = onActiveBar,
        modifier =
        modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 3.dp)
          .height(64.dp)
          .testTag(OverviewScreenTestTags.SEARCH_BAR),
        shape = RoundedCornerShape(50),
        content = {}
    )

    FilterBar(
        uiState.selectedStatus,
        uiState.selectedDifficulty,
        { overviewViewModel.onStatusFilterSelect(it) },
        { overviewViewModel.onDifficultyFilterSelect(it)}
    )

    LazyColumn (
      modifier = modifier.testTag(OverviewScreenTestTags.HUNT_LIST)
    ) {
      items(hunts.size) { index ->
        val hunt = hunts[index]
        HuntCard (
          hunt.hunt,
          modifier = modifier
            .testTag
            (
            if (index == (hunts.size - 1)) OverviewScreenTestTags.LAST_HUNT_CARD
            else OverviewScreenTestTags.HUNT_CARD)
            .clickable { overviewViewModel.onHuntClick(hunt.hunt.uid) },
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
    LazyRow(modifier = modifier
      .testTag(OverviewScreenTestTags.FILTER_BAR)
    ) {
      val huntStatuses = HuntStatus.values()
      val difficulties = Difficulty.values()
      // FilterButton("All", selectedStatus == null) { onStatusSelected(null) }
      items(huntStatuses.size) { status ->
        FilterButton(
            text = huntStatuses[status].name,
            isSelected = (selectedStatus == huntStatuses[status]),
          modifier = Modifier.testTag("FilterButton_${status}")
        ) {
          onStatusSelected(huntStatuses[status])
        }
      }

      items(difficulties.size) { difficulty ->
        FilterButton(
            text = difficulties[difficulty].name,
            isSelected = (selectedDifficulty == difficulties[difficulty]),
            modifier = modifier.testTag("FilterButton_${difficulty + 3}")
        ) {
          onDifficultySelected(difficulties[difficulty])
        }
      }
    }
    /*Row {
      // FilterButton("All", selectedDifficulty == null) { onDifficultySelected(null) }
      Difficulty.values().forEach { difficulty ->
        FilterButton(difficulty.name, selectedDifficulty == difficulty) {
          onDifficultySelected(difficulty)
        }
      }
    }*/
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
              containerColor = if (isSelected) Color.Green else Color.LightGray),
      modifier = modifier
        .padding(4.dp)
  ) {
        Text(text)
      }
}
