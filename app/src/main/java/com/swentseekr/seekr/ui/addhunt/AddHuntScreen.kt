package com.swentseekr.seekr.ui.addhunt

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntStatus

object AddHuntScreenTestTags {
    const val INPUT_HUNT_TITLE = "inputHuntTitle"
    const val INPUT_HUNT_DESCRIPTION = "inputHuntDescription"
    const val INPUT_HUNT_TIME = "inputHuntTime"
    const val INPUT_HUNT_DISTANCE = "inputHuntDistance"
    const val DROPDOWN_STATUS = "dropdownStatus"
    const val DROPDOWN_DIFFICULTY = "dropdownDifficulty"
    const val BUTTON_START_LOCATION = "buttonStartLocation"
    const val BUTTON_END_LOCATION = "buttonEndLocation"
    const val BUTTON_MIDDLE_POINTS = "buttonMiddlePoints"
    const val HUNT_SAVE = "huntSave"
    const val ERROR_MESSAGE = "errorMessage"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHuntScreen(
    addHuntViewModel: AddHuntViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onDone: () -> Unit = {},
    onSelectLocation: (String) -> Unit = {} // "start", "end", or "middle"
) {
    val uiState by addHuntViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Dropdown expanded states
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showDifficultyDropdown by remember { mutableStateOf(false) }

    // Handle error toast
    LaunchedEffect(uiState.errorMsg) {
        uiState.errorMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            addHuntViewModel.clearErrorMsg()
        }
    }

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
                    .padding(16.dp)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { addHuntViewModel.setTitle(it) },
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
                        .testTag(AddHuntScreenTestTags.INPUT_HUNT_TITLE)
                )

                // Description
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { addHuntViewModel.setDescription(it) },
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
                        .testTag(AddHuntScreenTestTags.INPUT_HUNT_DESCRIPTION)
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
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag(AddHuntScreenTestTags.DROPDOWN_STATUS)
                    )
                    ExposedDropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false }
                    ) {
                        HuntStatus.values().forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.name) },
                                onClick = {
                                    addHuntViewModel.setStatus(status)
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
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag(AddHuntScreenTestTags.DROPDOWN_DIFFICULTY)
                    )
                    ExposedDropdownMenu(
                        expanded = showDifficultyDropdown,
                        onDismissRequest = { showDifficultyDropdown = false }
                    ) {
                        Difficulty.values().forEach { diff ->
                            DropdownMenuItem(
                                text = { Text(diff.name) },
                                onClick = {
                                    addHuntViewModel.setDifficulty(diff)
                                    showDifficultyDropdown = false
                                }
                            )
                        }
                    }
                }

                // Time
                OutlinedTextField(
                    value = uiState.time,
                    onValueChange = { addHuntViewModel.setTime(it) },
                    label = { Text("Estimated Time (hours)") },
                    placeholder = { Text("e.g., 1.5") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AddHuntScreenTestTags.INPUT_HUNT_TIME)
                )

                // Distance
                OutlinedTextField(
                    value = uiState.distance,
                    onValueChange = { addHuntViewModel.setDistance(it) },
                    label = { Text("Distance (km)") },
                    placeholder = { Text("e.g., 2.3") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AddHuntScreenTestTags.INPUT_HUNT_DISTANCE)
                )

                // Map Location Buttons
                Button(
                    onClick = { onSelectLocation("start") },
                    modifier = Modifier.fillMaxWidth().testTag(AddHuntScreenTestTags.BUTTON_START_LOCATION)
                ) {
                    Text("Select Locations")
                }



                Spacer(Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = {
                        if (addHuntViewModel.addHunt()) {
                            onDone()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AddHuntScreenTestTags.HUNT_SAVE),
                    enabled = uiState.isValid
                ) {
                    Text("Save Hunt")
                }
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewAddHuntScreen() {
    AddHuntScreen()
}
