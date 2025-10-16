package com.swentseekr.seekr.ui.addhunt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntStatus

// ----------------------
// Constants
// ----------------------
private const val TITLE_TEXT = "Add your Hunt"
private const val BACK_CONTENT_DESC = "Back"

private const val LABEL_TITLE = "Title"
private const val PLACEHOLDER_TITLE = "Enter hunt name"

private const val LABEL_DESCRIPTION = "Description"
private const val PLACEHOLDER_DESCRIPTION = "Describe your hunt"

private const val LABEL_STATUS = "Status"
private const val EXPAND_STATUS_DESC = "Expand Status"

private const val LABEL_DIFFICULTY = "Difficulty"
private const val EXPAND_DIFFICULTY_DESC = "Expand Difficulty"

private const val LABEL_TIME = "Estimated Time (hours)"
private const val PLACEHOLDER_TIME = "e.g., 1.5"

private const val LABEL_DISTANCE = "Distance (km)"
private const val PLACEHOLDER_DISTANCE = "e.g., 2.3"

private const val BUTTON_SELECT_LOCATIONS = "Select Locations"
private const val BUTTON_SAVE_HUNT = "Save Hunt"

private const val UNIT_POINT = "point"
private const val UNIT_POINTS = "points"

private const val FIELD_CORNER_RADIUS = 12
private const val DESCRIPTION_HEIGHT = 150
private const val SAVE_BUTTON_HEIGHT = 64
private const val SAVE_BUTTON_RADIUS = 16
private const val SCREEN_PADDING = 16
private const val SPACER_HEIGHT = 24

// ----------------------
// Test Tags
// ----------------------
object AddHuntScreenTestTags {
  const val INPUT_HUNT_TITLE = "inputHuntTitle"
  const val INPUT_HUNT_DESCRIPTION = "inputHuntDescription"
  const val INPUT_HUNT_TIME = "inputHuntTime"
  const val INPUT_HUNT_DISTANCE = "inputHuntDistance"
  const val DROPDOWN_STATUS = "dropdownStatus"
  const val DROPDOWN_DIFFICULTY = "dropdownDifficulty"
  const val BUTTON_SELECT_LOCATION = "buttonSelectLocation"
  const val HUNT_SAVE = "huntSave"
  const val ERROR_MESSAGE = "errorMessage"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHuntFieldsScreen(
    uiState: AddHuntUIState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onDifficultySelect: (Difficulty) -> Unit,
    onStatusSelect: (HuntStatus) -> Unit,
    onSelectLocations: () -> Unit,
    onSave: () -> Unit,
    onGoBack: () -> Unit,
) {
  var showStatusDropdown by remember { mutableStateOf(false) }
  var showDifficultyDropdown by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(TITLE_TEXT) },
            navigationIcon = {
              IconButton(onClick = onGoBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = BACK_CONTENT_DESC)
              }
            })
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(SCREEN_PADDING.dp)
                    .padding(paddingValues)
                    .verticalScroll(scrollState),
        ) {
          val fieldShape = RoundedCornerShape(FIELD_CORNER_RADIUS.dp)

          // Title
          OutlinedTextField(
              value = uiState.title,
              onValueChange = onTitleChange,
              label = { Text(LABEL_TITLE) },
              placeholder = { Text(PLACEHOLDER_TITLE) },
              isError = uiState.invalidTitleMsg != null,
              supportingText = {
                uiState.invalidTitleMsg?.let {
                  Text(it, modifier = Modifier.testTag(AddHuntScreenTestTags.ERROR_MESSAGE))
                }
              },
              modifier = Modifier.fillMaxWidth().testTag(AddHuntScreenTestTags.INPUT_HUNT_TITLE),
              shape = fieldShape,
              colors =
                  OutlinedTextFieldDefaults.colors(
                      unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                      focusedBorderColor = MaterialTheme.colorScheme.primary),
          )

          // Description
          OutlinedTextField(
              value = uiState.description,
              onValueChange = onDescriptionChange,
              label = { Text(LABEL_DESCRIPTION) },
              placeholder = { Text(PLACEHOLDER_DESCRIPTION) },
              isError = uiState.invalidDescriptionMsg != null,
              supportingText = {
                uiState.invalidDescriptionMsg?.let {
                  Text(it, modifier = Modifier.testTag(AddHuntScreenTestTags.ERROR_MESSAGE))
                }
              },
              modifier =
                  Modifier.fillMaxWidth()
                      .height(DESCRIPTION_HEIGHT.dp)
                      .testTag(AddHuntScreenTestTags.INPUT_HUNT_DESCRIPTION),
              shape = fieldShape,
              colors =
                  OutlinedTextFieldDefaults.colors(
                      unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                      focusedBorderColor = MaterialTheme.colorScheme.primary))

          // Status Dropdown
          ExposedDropdownMenuBox(
              expanded = showStatusDropdown, onExpandedChange = { showStatusDropdown = it }) {
                OutlinedTextField(
                    value = uiState.status?.name ?: "",
                    onValueChange = {},
                    label = { Text(LABEL_STATUS) },
                    readOnly = true,
                    trailingIcon = {
                      Icon(
                          imageVector =
                              if (showStatusDropdown) Icons.Outlined.KeyboardArrowUp
                              else Icons.Outlined.KeyboardArrowDown,
                          contentDescription = EXPAND_STATUS_DESC)
                    },
                    modifier =
                        Modifier.menuAnchor()
                            .fillMaxWidth()
                            .testTag(AddHuntScreenTestTags.DROPDOWN_STATUS),
                    shape = fieldShape,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary))
                ExposedDropdownMenu(
                    expanded = showStatusDropdown,
                    onDismissRequest = { showStatusDropdown = false }) {
                      HuntStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name) },
                            onClick = {
                              onStatusSelect(status)
                              showStatusDropdown = false
                            })
                      }
                    }
              }

          // Difficulty Dropdown
          ExposedDropdownMenuBox(
              expanded = showDifficultyDropdown,
              onExpandedChange = { showDifficultyDropdown = it }) {
                OutlinedTextField(
                    value = uiState.difficulty?.name ?: "",
                    onValueChange = {},
                    label = { Text(LABEL_DIFFICULTY) },
                    readOnly = true,
                    trailingIcon = {
                      Icon(
                          imageVector =
                              if (showDifficultyDropdown) Icons.Outlined.KeyboardArrowUp
                              else Icons.Outlined.KeyboardArrowDown,
                          contentDescription = EXPAND_DIFFICULTY_DESC)
                    },
                    modifier =
                        Modifier.menuAnchor()
                            .fillMaxWidth()
                            .testTag(AddHuntScreenTestTags.DROPDOWN_DIFFICULTY),
                    shape = fieldShape,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary))
                ExposedDropdownMenu(
                    expanded = showDifficultyDropdown,
                    onDismissRequest = { showDifficultyDropdown = false }) {
                      Difficulty.values().forEach { diff ->
                        DropdownMenuItem(
                            text = { Text(diff.name) },
                            onClick = {
                              onDifficultySelect(diff)
                              showDifficultyDropdown = false
                            })
                      }
                    }
              }

          // Time
          OutlinedTextField(
              value = uiState.time,
              onValueChange = onTimeChange,
              label = { Text(LABEL_TIME) },
              placeholder = { Text(PLACEHOLDER_TIME) },
              modifier = Modifier.fillMaxWidth().testTag(AddHuntScreenTestTags.INPUT_HUNT_TIME),
              isError = uiState.invalidTimeMsg != null,
              supportingText = {
                uiState.invalidTimeMsg?.let {
                  Text(it, modifier = Modifier.testTag(AddHuntScreenTestTags.ERROR_MESSAGE))
                }
              },
              shape = fieldShape,
              colors =
                  OutlinedTextFieldDefaults.colors(
                      unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                      focusedBorderColor = MaterialTheme.colorScheme.primary))

          // Distance
          OutlinedTextField(
              value = uiState.distance,
              onValueChange = onDistanceChange,
              label = { Text(LABEL_DISTANCE) },
              placeholder = { Text(PLACEHOLDER_DISTANCE) },
              modifier = Modifier.fillMaxWidth().testTag(AddHuntScreenTestTags.INPUT_HUNT_DISTANCE),
              isError = uiState.invalidDistanceMsg != null,
              supportingText = {
                uiState.invalidDistanceMsg?.let {
                  Text(it, modifier = Modifier.testTag(AddHuntScreenTestTags.ERROR_MESSAGE))
                }
              },
              shape = fieldShape,
              colors =
                  OutlinedTextFieldDefaults.colors(
                      unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                      focusedBorderColor = MaterialTheme.colorScheme.primary))

          // Map Location Button with count
          Button(
              onClick = onSelectLocations,
              modifier =
                  Modifier.fillMaxWidth().testTag(AddHuntScreenTestTags.BUTTON_SELECT_LOCATION),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.secondary,
                      contentColor = MaterialTheme.colorScheme.onSecondary)) {
                val pointCount = uiState.points.size
                val label =
                    if (pointCount > 0) {
                      "$BUTTON_SELECT_LOCATIONS ($pointCount ${if (pointCount == 1) UNIT_POINT else UNIT_POINTS})"
                    } else {
                      BUTTON_SELECT_LOCATIONS
                    }
                Text(label)
              }

          Spacer(modifier = Modifier.height(SPACER_HEIGHT.dp))

          // Save Button
          Button(
              onClick = onSave,
              modifier =
                  Modifier.fillMaxWidth()
                      .height(SAVE_BUTTON_HEIGHT.dp)
                      .clip(RoundedCornerShape(SAVE_BUTTON_RADIUS.dp))
                      .testTag(AddHuntScreenTestTags.HUNT_SAVE),
              enabled = uiState.isValid,
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.primary,
                      contentColor = MaterialTheme.colorScheme.onPrimary)) {
                Text(BUTTON_SAVE_HUNT, style = MaterialTheme.typography.titleMedium)
              }
        }
      })
}
