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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

  Box(modifier = modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag(OverviewScreenTestTags.OVERVIEW_SCREEN),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // MODERN HEADER WITH TITLE
      ModernHeader()

      // MODERN SEARCH BAR
      ModernSearchBar(
          query = query,
          onQueryChange = { overviewViewModel.onSearchChange(it) },
          onSearch = { overviewViewModel.onSearchChange(it) },
          onActiveChange = onActiveBar,
          onClear = { overviewViewModel.onClearSearch() },
          modifier = modifier)

      // MODERN FILTER BAR
      ModernFilterBar(
          selectedStatus = uiState.selectedStatus,
          selectedDifficulty = uiState.selectedDifficulty,
          onStatusSelected = { overviewViewModel.onStatusFilterSelect(it) },
          onDifficultySelected = { overviewViewModel.onDifficultyFilterSelect(it) })

      // HUNTS LIST
      LazyColumn(
          modifier = modifier.testTag(OverviewScreenTestTags.HUNT_LIST).fillMaxWidth(),
          contentPadding = PaddingValues(bottom = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally) {
            items(hunts.size) { index ->
              val hunt = hunts[index]

              HuntCard(
                  hunt.hunt,
                  modifier =
                      modifier
                          .testTag(
                              if (index == hunts.size - 1) OverviewScreenTestTags.LAST_HUNT_CARD
                              else OverviewScreenTestTags.HUNT_CARD)
                          .clickable { onHuntClick(hunt.hunt.uid) })

              Spacer(modifier = Modifier.height(OverviewScreenDefaults.ListItemSpacing))
            }
          }
    }
  }
}

@Composable
fun ModernHeader() {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .background(Color.White)
              .padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(
            text = "Discover",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A))
        Text(
            text = "Find your next adventure",
            fontSize = 16.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 4.dp))
      }
}

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
      placeholder = { Text(OverviewScreenStrings.SearchPlaceholder, color = Color(0xFF999999)) },
      leadingIcon = {
        if (query.isEmpty()) {
          Icon(
              imageVector = Icons.Default.Search,
              contentDescription = OverviewScreenStrings.SearchIconDescription,
              tint = Color(0xFF666666))
        } else null
      },
      trailingIcon = {
        if (query.isNotEmpty()) {
          Icon(
              imageVector = Icons.Default.Clear,
              contentDescription = OverviewScreenStrings.ClearIconDescription,
              tint = Color(0xFF666666),
              modifier = Modifier.clickable { onClear() })
        } else null
      },
      modifier =
          modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 12.dp)
              .height(70.dp)
              .testTag(OverviewScreenTestTags.SEARCH_BAR),
      shape = RoundedCornerShape(16.dp),
      colors =
          SearchBarDefaults.colors(containerColor = Color.White, dividerColor = Color.Transparent),
      shadowElevation = 4.dp,
      content = {})
}

@Composable
fun ModernFilterBar(
    selectedStatus: HuntStatus?,
    selectedDifficulty: Difficulty?,
    onStatusSelected: (HuntStatus?) -> Unit,
    onDifficultySelected: (Difficulty?) -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(
      modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 12.dp),
      horizontalAlignment = Alignment.Start,
  ) {
    // SECTION TITLE
    Text(
        text = "Filter by",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF666666),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

    val huntStatuses = remember { HuntStatus.values() }
    val difficulties = remember { Difficulty.values() }

    LazyRow(
        modifier = modifier.fillMaxWidth().testTag(OverviewScreenTestTags.FILTER_BAR),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)) {
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

    Spacer(modifier = Modifier.height(8.dp))
  }
}

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
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
      },
      modifier = modifier,
      colors =
          FilterChipDefaults.filterChipColors(
              containerColor = Color.White,
              selectedContainerColor = color.copy(alpha = 0.2f),
              labelColor = Color(0xFF666666),
              selectedLabelColor = color),
      border =
          FilterChipDefaults.filterChipBorder(
              borderColor = if (isSelected) color else Color(0xFFE0E0E0),
              selectedBorderColor = color,
              borderWidth = if (isSelected) 2.dp else 1.dp,
              enabled = true,
              selected = true),
      shape = RoundedCornerShape(12.dp),
  )
}

// Helper function for status colors
fun getStatusColor(status: HuntStatus): Color {
  return when (status) {
    HuntStatus.FUN -> Color(0xFF9C27B0) // Purple
    HuntStatus.DISCOVER -> Color(0xFF2196F3) // Blue
    HuntStatus.SPORT -> Color(0xFFFF5722) // Orange-red
  }
}

// Helper function for difficulty colors
fun getDifficultyColor(difficulty: Difficulty): Color {
  return when (difficulty) {
    Difficulty.EASY -> Color(0xFF4CAF50) // Green
    Difficulty.INTERMEDIATE -> Color(0xFFFFA726) // Orange
    Difficulty.DIFFICULT -> Color(0xFFEF5350) // Red
  }
}

@Preview
@Composable
fun OverviewScreenPreview() {
  OverviewScreen()
}

// Keep the old FilterButton for compatibility if needed
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
