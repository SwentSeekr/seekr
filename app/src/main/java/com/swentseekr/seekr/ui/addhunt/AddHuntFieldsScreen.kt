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
                title = { Text("Add your Hunt") },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
                    .padding(paddingValues)
                    .verticalScroll(scrollState),

            ) {
                val fieldShape = RoundedCornerShape(12.dp)

                // Title
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = onTitleChange,
                    label = { Text("Title") },
                    placeholder = { Text("Enter hunt name") },
                    isError = uiState.invalidTitleMsg != null,
                    supportingText = {
                        uiState.invalidTitleMsg?.let {
                            Text(it, modifier = Modifier.testTag(AddHuntScreenTestTags.ERROR_MESSAGE))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AddHuntScreenTestTags.INPUT_HUNT_TITLE),
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),

                )

                // Description
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    placeholder = { Text("Describe your hunt") },
                    isError = uiState.invalidDescriptionMsg != null,
                    supportingText = {
                        uiState.invalidDescriptionMsg?.let {
                            Text(it, modifier = Modifier.testTag(AddHuntScreenTestTags.ERROR_MESSAGE))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .testTag(AddHuntScreenTestTags.INPUT_HUNT_DESCRIPTION),
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = showStatusDropdown,
                    onExpandedChange = { showStatusDropdown = it }
                ) {
                    OutlinedTextField(
                        value = uiState.status?.name ?: "",
                        onValueChange = {},
                        label = { Text("Status") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = if (showStatusDropdown)
                                    Icons.Outlined.KeyboardArrowUp
                                else
                                    Icons.Outlined.KeyboardArrowDown,
                                contentDescription = "Expand Status"
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag(AddHuntScreenTestTags.DROPDOWN_STATUS),
                        shape = fieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false }
                    ) {
                        HuntStatus.values().forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.name) },
                                onClick = {
                                    onStatusSelect(status)
                                    showStatusDropdown = false
                                }
                            )
                        }
                    }
                }

                // Difficulty Dropdown
                ExposedDropdownMenuBox(
                    expanded = showDifficultyDropdown,
                    onExpandedChange = { showDifficultyDropdown = it }
                ) {
                    OutlinedTextField(
                        value = uiState.difficulty?.name ?: "",
                        onValueChange = {},
                        label = { Text("Difficulty") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = if (showDifficultyDropdown)
                                    Icons.Outlined.KeyboardArrowUp
                                else
                                    Icons.Outlined.KeyboardArrowDown,
                                contentDescription = "Expand Difficulty"
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag(AddHuntScreenTestTags.DROPDOWN_DIFFICULTY),
                        shape = fieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showDifficultyDropdown,
                        onDismissRequest = { showDifficultyDropdown = false }
                    ) {
                        Difficulty.values().forEach { diff ->
                            DropdownMenuItem(
                                text = { Text(diff.name) },
                                onClick = {
                                    onDifficultySelect(diff)
                                    showDifficultyDropdown = false
                                }
                            )
                        }
                    }
                }

                // Time
                OutlinedTextField(
                    value = uiState.time,
                    onValueChange = onTimeChange,
                    label = { Text("Estimated Time (hours)") },
                    placeholder = { Text("e.g., 1.5") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AddHuntScreenTestTags.INPUT_HUNT_TIME),
                    isError = uiState.invalidTimeMsg != null,
                    supportingText = {
                        uiState.invalidTimeMsg?.let {
                            Text(it, modifier = Modifier.testTag(AddHuntScreenTestTags.ERROR_MESSAGE))
                        }
                    },
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Distance
                OutlinedTextField(
                    value = uiState.distance,
                    onValueChange = onDistanceChange,
                    label = { Text("Distance (km)") },
                    placeholder = { Text("e.g., 2.3") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AddHuntScreenTestTags.INPUT_HUNT_DISTANCE),
                    isError = uiState.invalidDistanceMsg != null,
                    supportingText = {
                        uiState.invalidDistanceMsg?.let {
                            Text(it, modifier = Modifier.testTag(AddHuntScreenTestTags.ERROR_MESSAGE))
                        }
                    },
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Map Location Button with count
                Button(
                    onClick = onSelectLocations,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AddHuntScreenTestTags.BUTTON_SELECT_LOCATION),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    val pointCount = uiState.points.size
                    val label = if (pointCount > 0) {
                        "Select Locations ($pointCount ${if (pointCount == 1) "point" else "points"})"
                    } else {
                        "Select Locations"
                    }
                    Text(label)
                }

                Spacer(modifier = Modifier.height(24.dp))
                // Save Button (larger, rounded, primary color)
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .testTag(AddHuntScreenTestTags.HUNT_SAVE),
                    enabled = uiState.isValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Save Hunt", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    )
}
