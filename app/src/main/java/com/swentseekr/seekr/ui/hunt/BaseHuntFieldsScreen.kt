package com.swentseekr.seekr.ui.hunt

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.R

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

object HuntScreenTestTags {
  const val INPUT_HUNT_TITLE = "inputHuntTitle"
  const val INPUT_HUNT_DESCRIPTION = "inputHuntDescription"
  const val INPUT_HUNT_TIME = "inputHuntTime"
  const val INPUT_HUNT_DISTANCE = "inputHuntDistance"
  const val DROPDOWN_STATUS = "dropdownStatus"
  const val DROPDOWN_DIFFICULTY = "dropdownDifficulty"
  const val BUTTON_SELECT_LOCATION = "buttonSelectLocation"
  const val HUNT_SAVE = "huntSave"
  const val ERROR_MESSAGE = "errorMessage"
  const val ADD_HUNT_SCREEN = "AddHuntScreen"
}

@Composable
fun ValidatedOutlinedField(
    value: String = "Add your Hunt",
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    errorMsg: String?,
    testTag: String
) {
  ValidatedOutlinedField(
      value = value,
      onValueChange = onValueChange,
      label = label,
      placeholder = placeholder,
      errorMsg = errorMsg,
      testTag = testTag,
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(FIELD_CORNER_RADIUS.dp),
      colors =
          OutlinedTextFieldDefaults.colors(
              unfocusedBorderColor = MaterialTheme.colorScheme.outline,
              focusedBorderColor = MaterialTheme.colorScheme.primary))
}

@Composable
private fun ValidatedOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    errorMsg: String?,
    testTag: String,
    modifier: Modifier,
    shape: Shape,
    colors: TextFieldColors
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      placeholder = { Text(placeholder) },
      isError = errorMsg != null,
      supportingText = {
        errorMsg?.let { Text(it, modifier = Modifier.testTag(HuntScreenTestTags.ERROR_MESSAGE)) }
      },
      modifier = modifier.testTag(testTag),
      shape = shape,
      colors = colors)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseHuntFieldsScreen(
    title: String = "Add your Hunt",
    uiState: HuntUIState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onDifficultySelect: (Difficulty) -> Unit,
    onStatusSelect: (HuntStatus) -> Unit,
    onSelectLocations: () -> Unit,
    onSelectImage: (Uri?) -> Unit,
    onSave: () -> Unit,
    onGoBack: () -> Unit,
) {
  var showStatusDropdown by rememberSaveable { mutableStateOf(false) }
  var showDifficultyDropdown by rememberSaveable { mutableStateOf(false) }
    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

  val scrollState = rememberScrollState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            selectedImageUri = uri
            onSelectImage(uri)
        }
    )


    Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(title) },
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
          val fieldColors =
              OutlinedTextFieldDefaults.colors(
                  unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                  focusedBorderColor = MaterialTheme.colorScheme.primary)

            // IMAGE PICKER + PREVIEW
            Text("Main Image", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Bouton de sélection d’image
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Choose Image")
            }

            // Preview de l’image (si sélectionnée)
            if (selectedImageUri != null) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected Hunt Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    placeholder = painterResource(R.drawable.empty_image),
                    error = painterResource(R.drawable.empty_image)
                )
            }

            Spacer(modifier = Modifier.height(SPACER_HEIGHT.dp))

          ValidatedOutlinedField(
              value = uiState.title,
              onValueChange = onTitleChange,
              label = LABEL_TITLE,
              placeholder = PLACEHOLDER_TITLE,
              errorMsg = uiState.invalidTitleMsg,
              testTag = HuntScreenTestTags.INPUT_HUNT_TITLE)

          ValidatedOutlinedField(
              value = uiState.description,
              onValueChange = onDescriptionChange,
              label = LABEL_DESCRIPTION,
              placeholder = PLACEHOLDER_DESCRIPTION,
              errorMsg = uiState.invalidDescriptionMsg,
              testTag = HuntScreenTestTags.INPUT_HUNT_DESCRIPTION,
              modifier = Modifier.fillMaxWidth().height(DESCRIPTION_HEIGHT.dp),
              shape = fieldShape,
              colors = fieldColors)

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
                            .testTag(HuntScreenTestTags.DROPDOWN_STATUS),
                    shape = fieldShape,
                    colors = fieldColors)
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
                            .testTag(HuntScreenTestTags.DROPDOWN_DIFFICULTY),
                    shape = fieldShape,
                    colors = fieldColors)
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

          ValidatedOutlinedField(
              value = uiState.time,
              onValueChange = onTimeChange,
              label = LABEL_TIME,
              placeholder = PLACEHOLDER_TIME,
              errorMsg = uiState.invalidTimeMsg,
              testTag = HuntScreenTestTags.INPUT_HUNT_TIME)

          ValidatedOutlinedField(
              value = uiState.distance,
              onValueChange = onDistanceChange,
              label = LABEL_DISTANCE,
              placeholder = PLACEHOLDER_DISTANCE,
              errorMsg = uiState.invalidDistanceMsg,
              testTag = HuntScreenTestTags.INPUT_HUNT_DISTANCE)

          Button(
              onClick = onSelectLocations,
              modifier = Modifier.fillMaxWidth().testTag(HuntScreenTestTags.BUTTON_SELECT_LOCATION),
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

          Button(
              onClick = onSave,
              modifier =
                  Modifier.fillMaxWidth()
                      .height(SAVE_BUTTON_HEIGHT.dp)
                      .clip(RoundedCornerShape(SAVE_BUTTON_RADIUS.dp))
                      .testTag(HuntScreenTestTags.HUNT_SAVE),
              enabled = uiState.isValid,
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.primary,
                      contentColor = MaterialTheme.colorScheme.onPrimary)) {
                Text(BUTTON_SAVE_HUNT, style = MaterialTheme.typography.titleMedium)
              }
        }
      },
      modifier = Modifier.testTag(HuntScreenTestTags.ADD_HUNT_SCREEN))
}
