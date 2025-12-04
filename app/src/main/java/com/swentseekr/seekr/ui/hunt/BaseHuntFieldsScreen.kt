package com.swentseekr.seekr.ui.hunt

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.REMOVE_BUTTON_TAG_PREFIX

sealed class OtherImage {
  data class Remote(val url: String) : OtherImage()

  data class Local(val uri: Uri) : OtherImage()
}

val UICons = BaseHuntFieldsUi

/** Field style used to keep ValidatedOutlinedField arguments under the SonarCloud limit. */
data class FieldStyle(
    val modifier: Modifier,
    val shape: Shape,
    val colors: TextFieldColors,
)

@Composable
fun defaultFieldStyle(
    modifier: Modifier = Modifier.fillMaxWidth(),
): FieldStyle {
  val colors =
      OutlinedTextFieldDefaults.colors(
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = UICons.ChangeAlpha),
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface,
          focusedContainerColor = MaterialTheme.colorScheme.surface)

  return FieldStyle(
      modifier = modifier, shape = RoundedCornerShape(UICons.FieldCornerRadius), colors = colors)
}

/** Callbacks for the main hunt fields (title, description, time, distance, etc.). */
data class HuntFieldCallbacks(
    val onTitleChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onTimeChange: (String) -> Unit,
    val onDistanceChange: (String) -> Unit,
    val onDifficultySelect: (Difficulty) -> Unit,
    val onStatusSelect: (HuntStatus) -> Unit,
    val onSelectLocations: () -> Unit,
    val onSave: () -> Unit,
)

/** Callbacks for image-related actions. */
data class ImageCallbacks(
    val onSelectImage: (Uri?) -> Unit,
    val onSelectOtherImages: (List<Uri>) -> Unit,
    val onRemoveOtherImage: (Uri) -> Unit,
    val onRemoveExistingImage: (String) -> Unit,
)

/** Navigation related callbacks (kept extensible, currently only onGoBack). */
data class HuntNavigationCallbacks(
    val onGoBack: () -> Unit = {},
)

@Composable
fun ValidatedOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    errorMsg: String?,
    testTag: String,
    style: FieldStyle = defaultFieldStyle(),
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      placeholder = { Text(placeholder) },
      isError = errorMsg != null,
      supportingText = {
        AnimatedVisibility(visible = errorMsg != null, enter = fadeIn(), exit = fadeOut()) {
          Text(
              errorMsg.orEmpty(),
              modifier = Modifier.testTag(HuntScreenTestTags.ERROR_MESSAGE),
              color = MaterialTheme.colorScheme.error)
        }
      },
      modifier = style.modifier.testTag(testTag),
      shape = style.shape,
      colors = style.colors)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseHuntFieldsScreen(
    title: String = BaseHuntFieldsStrings.TITLE_DEFAULT,
    uiState: HuntUIState,
    fieldCallbacks: HuntFieldCallbacks,
    imageCallbacks: ImageCallbacks,
    navigationCallbacks: HuntNavigationCallbacks = HuntNavigationCallbacks(),
    deleteAction: DeleteAction = DeleteAction(),
) {
  val scrollState = rememberScrollState()

  Scaffold(
      topBar = {
        BaseHuntTopBar(
            title = title, onGoBack = navigationCallbacks.onGoBack, deleteAction = deleteAction)
      },
      modifier = Modifier.testTag(HuntScreenTestTags.ADD_HUNT_SCREEN)) { paddingValues ->
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.surface.copy(
                                        alpha = UICons.ChangeAlpha))))
                    .padding(horizontal = UICons.ColumnHPadding)
                    .padding(paddingValues)) {
              Column(
                  modifier =
                      Modifier.fillMaxSize()
                          .verticalScroll(scrollState)
                          .testTag(HuntScreenTestTags.COLLUMN_HUNT_FIELDS),
                  verticalArrangement = Arrangement.spacedBy(UICons.ColumnVArrangement)) {
                    Spacer(modifier = Modifier.height(UICons.SpacerHeightSmall))

                    val fieldShape = RoundedCornerShape(UICons.FieldCornerRadius)
                    val fieldColors =
                        OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor =
                                MaterialTheme.colorScheme.outline.copy(alpha = UICons.ChangeAlpha),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface)

                    // Title
                    ValidatedOutlinedField(
                        value = uiState.title,
                        onValueChange = fieldCallbacks.onTitleChange,
                        label = BaseHuntFieldsStrings.LABEL_TITLE,
                        placeholder = BaseHuntFieldsStrings.PLACEHOLDER_TITLE,
                        errorMsg = uiState.invalidTitleMsg,
                        testTag = HuntScreenTestTags.INPUT_HUNT_TITLE)

                    // Description
                    ValidatedOutlinedField(
                        value = uiState.description,
                        onValueChange = fieldCallbacks.onDescriptionChange,
                        label = BaseHuntFieldsStrings.LABEL_DESCRIPTION,
                        placeholder = BaseHuntFieldsStrings.PLACEHOLDER_DESCRIPTION,
                        errorMsg = uiState.invalidDescriptionMsg,
                        testTag = HuntScreenTestTags.INPUT_HUNT_DESCRIPTION,
                        style =
                            FieldStyle(
                                modifier = Modifier.fillMaxWidth().height(UICons.DescriptionHeight),
                                shape = fieldShape,
                                colors = fieldColors))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(UICons.RowHArrangement)) {
                          StatusDropdown(
                              status = uiState.status,
                              onStatusSelect = fieldCallbacks.onStatusSelect,
                              shape = fieldShape,
                              colors = fieldColors)

                          DifficultyDropdown(
                              difficulty = uiState.difficulty,
                              onDifficultySelect = fieldCallbacks.onDifficultySelect,
                              shape = fieldShape,
                              colors = fieldColors)
                        }

                    TimeAndDistanceRow(
                        uiState = uiState,
                        fieldShape = fieldShape,
                        fieldColors = fieldColors,
                        onTimeChange = fieldCallbacks.onTimeChange,
                        onDistanceChange = fieldCallbacks.onDistanceChange)

                    SelectLocationsButton(
                        pointsCount = uiState.points.size,
                        onSelectLocations = fieldCallbacks.onSelectLocations)

                    ImagesSection(uiState = uiState, imageCallbacks = imageCallbacks)

                    Spacer(modifier = Modifier.height(UICons.SpacerHeightSmall))

                    SaveHuntButton(enabled = uiState.isValid, onSave = fieldCallbacks.onSave)

                    Spacer(modifier = Modifier.height(UICons.SpacerHeight))
                  }
            }
      }
}

/** Top app bar with back arrow and delete action toggle. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BaseHuntTopBar(
    title: String,
    onGoBack: () -> Unit,
    deleteAction: DeleteAction,
) {
  var showDeleteButton by rememberSaveable { mutableStateOf(false) }

  TopAppBar(
      title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
      navigationIcon = {
        IconButton(onClick = onGoBack) {
          Icon(
              imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
              contentDescription = BaseHuntFieldsStrings.BACK_CONTENT_DESC)
        }
      },
      actions = {
        if (deleteAction.show && deleteAction.onClick != null) {

          if (showDeleteButton) {
            Button(
                onClick = deleteAction.onClick,
                modifier = Modifier.padding(end = 8.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError),
                shape = RoundedCornerShape(999.dp)) {
                  Icon(
                      painter = painterResource(R.drawable.ic_delete),
                      contentDescription = "Delete hunt",
                      modifier = Modifier.size(UICons.IconSize),
                      tint = MaterialTheme.colorScheme.onError)
                  Spacer(modifier = Modifier.width(UICons.SpacerHeightSmall))
                  Text("Delete")
                }
          }

          IconButton(onClick = { showDeleteButton = !showDeleteButton }) {
            Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More actions")
          }
        }
      },
      colors =
          TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.surface,
              titleContentColor = MaterialTheme.colorScheme.onSurface))
}

/** Status dropdown with its own internal expanded state. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.StatusDropdown(
    status: HuntStatus?,
    onStatusSelect: (HuntStatus) -> Unit,
    shape: Shape,
    colors: TextFieldColors,
) {
  var expanded by rememberSaveable { mutableStateOf(false) }

  ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = it },
      modifier = Modifier.weight(UICons.WeightTextField)) {
        OutlinedTextField(
            value = status?.name.orEmpty(),
            onValueChange = {},
            label = { Text(BaseHuntFieldsStrings.LABEL_STATUS) },
            readOnly = true,
            trailingIcon = {
              Icon(
                  imageVector =
                      if (expanded) Icons.Outlined.KeyboardArrowUp
                      else Icons.Outlined.KeyboardArrowDown,
                  contentDescription = BaseHuntFieldsStrings.EXPAND_STATUS_DESC)
            },
            modifier =
                Modifier.menuAnchor().fillMaxWidth().testTag(HuntScreenTestTags.DROPDOWN_STATUS),
            shape = shape,
            colors = colors)
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          HuntStatus.values().forEach { s ->
            DropdownMenuItem(
                text = { Text(s.name) },
                onClick = {
                  onStatusSelect(s)
                  expanded = false
                })
          }
        }
      }
}

/** Difficulty dropdown with its own internal expanded state. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.DifficultyDropdown(
    difficulty: Difficulty?,
    onDifficultySelect: (Difficulty) -> Unit,
    shape: Shape,
    colors: TextFieldColors,
) {
  var expanded by rememberSaveable { mutableStateOf(false) }

  ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = it },
      modifier = Modifier.weight(UICons.WeightTextField)) {
        OutlinedTextField(
            value = difficulty?.name.orEmpty(),
            onValueChange = {},
            label = { Text(BaseHuntFieldsStrings.LABEL_DIFFICULTY) },
            readOnly = true,
            trailingIcon = {
              Icon(
                  imageVector =
                      if (expanded) Icons.Outlined.KeyboardArrowUp
                      else Icons.Outlined.KeyboardArrowDown,
                  contentDescription = BaseHuntFieldsStrings.EXPAND_DIFFICULTY_DESC)
            },
            modifier =
                Modifier.menuAnchor()
                    .fillMaxWidth()
                    .testTag(HuntScreenTestTags.DROPDOWN_DIFFICULTY),
            shape = shape,
            colors = colors)
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          Difficulty.values().forEach { d ->
            DropdownMenuItem(
                text = { Text(d.name) },
                onClick = {
                  onDifficultySelect(d)
                  expanded = false
                })
          }
        }
      }
}

@Composable
private fun TimeAndDistanceRow(
    uiState: HuntUIState,
    fieldShape: Shape,
    fieldColors: TextFieldColors,
    onTimeChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(UICons.RowHArrangement)) {
        ValidatedOutlinedField(
            value = uiState.time,
            onValueChange = onTimeChange,
            label = BaseHuntFieldsStrings.LABEL_TIME,
            placeholder = BaseHuntFieldsStrings.PLACEHOLDER_TIME,
            errorMsg = uiState.invalidTimeMsg,
            testTag = HuntScreenTestTags.INPUT_HUNT_TIME,
            style =
                FieldStyle(
                    modifier = Modifier.weight(UICons.WeightTextField),
                    shape = fieldShape,
                    colors = fieldColors))

        ValidatedOutlinedField(
            value = uiState.distance,
            onValueChange = onDistanceChange,
            label = BaseHuntFieldsStrings.LABEL_DISTANCE,
            placeholder = BaseHuntFieldsStrings.PLACEHOLDER_DISTANCE,
            errorMsg = uiState.invalidDistanceMsg,
            testTag = HuntScreenTestTags.INPUT_HUNT_DISTANCE,
            style =
                FieldStyle(
                    modifier = Modifier.weight(UICons.WeightTextField),
                    shape = fieldShape,
                    colors = fieldColors))
      }
}

@Composable
private fun SelectLocationsButton(
    pointsCount: Int,
    onSelectLocations: () -> Unit,
) {
  ElevatedButton(
      onClick = onSelectLocations,
      modifier =
          Modifier.fillMaxWidth()
              .height(UICons.ButtonHeight)
              .testTag(HuntScreenTestTags.BUTTON_SELECT_LOCATION),
      shape = RoundedCornerShape(UICons.ButtonCornerRadius),
      colors =
          ButtonDefaults.elevatedButtonColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer,
              contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
        val label =
            if (pointsCount > 0) {
              "${BaseHuntFieldsStrings.BUTTON_SELECT_LOCATIONS} ($pointsCount ${
                    if (pointsCount == 1) BaseHuntFieldsStrings.UNIT_POINT
                    else BaseHuntFieldsStrings.UNIT_POINTS
                })"
            } else {
              BaseHuntFieldsStrings.BUTTON_SELECT_LOCATIONS
            }
        Text(label, style = MaterialTheme.typography.titleMedium)
      }
}

@Composable
private fun ImagesSection(
    uiState: HuntUIState,
    imageCallbacks: ImageCallbacks,
) {
  val imagePickerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetContent(),
          onResult = { uri -> imageCallbacks.onSelectImage(uri) })

  val multipleImagesPickerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetMultipleContents(),
          onResult = { uris -> imageCallbacks.onSelectOtherImages(uris) })

  Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(UICons.CardCornerRadius),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = UICons.ChangeAlpha))) {
        Column(
            modifier = Modifier.padding(UICons.CardPadding),
            verticalArrangement = Arrangement.spacedBy(UICons.CardVArrangement)) {
              Text(
                  "Images",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant)

              OutlinedButton(
                  onClick = { imagePickerLauncher.launch("image/*") },
                  modifier = Modifier.fillMaxWidth().height(UICons.ImageButtonHeight),
                  shape = RoundedCornerShape(UICons.ImageButtonCornerRadius)) {
                    Text(
                        BaseHuntFieldsStrings.BUTTON_CHOOSE_IMAGE,
                        style = MaterialTheme.typography.bodyLarge)
                  }

              val imageToDisplay = uiState.mainImageUrl

              AnimatedVisibility(visible = !imageToDisplay.isNullOrBlank()) {
                AsyncImage(
                    model = imageToDisplay,
                    contentDescription = BaseHuntFieldsStrings.CONTENT_DESC_SELECTED_IMAGE,
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(UICons.ImageHeight)
                            .clip(RoundedCornerShape(UICons.ImageCornerRadius))
                            .shadow(4.dp, RoundedCornerShape(UICons.ImageCornerRadius)),
                    placeholder = painterResource(R.drawable.empty_image),
                    error = painterResource(R.drawable.empty_image))
              }

              OutlinedButton(
                  onClick = { multipleImagesPickerLauncher.launch("image/*") },
                  modifier = Modifier.fillMaxWidth().height(UICons.ImageButtonHeight),
                  shape = RoundedCornerShape(UICons.ImageButtonCornerRadius)) {
                    Text(
                        BaseHuntFieldsStrings.BUTTON_CHOOSE_ADDITIONAL_IMAGES,
                        style = MaterialTheme.typography.bodyLarge)
                  }

              val combinedImages: List<OtherImage> =
                  uiState.otherImagesUrls.map { OtherImage.Remote(it) } +
                      uiState.otherImagesUris.map { OtherImage.Local(it) }

              if (combinedImages.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(UICons.CardVArrangement)) {
                  combinedImages.forEach { image ->
                    val tagSuffix =
                        when (image) {
                          is OtherImage.Remote -> image.url
                          is OtherImage.Local -> image.uri.toString()
                        }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(UICons.CardLittleCornerRadius)) {
                          Row(
                              modifier = Modifier.fillMaxWidth().padding(UICons.CardRowPadding),
                              horizontalArrangement = Arrangement.SpaceBetween) {
                                val model =
                                    when (image) {
                                      is OtherImage.Remote -> image.url
                                      is OtherImage.Local -> image.uri
                                    }

                                AsyncImage(
                                    model = model,
                                    contentDescription =
                                        BaseHuntFieldsStrings.CONTENT_DESC_SECONDARY_IMAGE,
                                    modifier =
                                        Modifier.testTag("otherImage_$tagSuffix")
                                            .weight(UICons.ImageWeight)
                                            .height(UICons.ImageLittleHeight)
                                            .clip(
                                                RoundedCornerShape(UICons.ImageLittleCornerRadius)),
                                    placeholder = painterResource(R.drawable.empty_image),
                                    error = painterResource(R.drawable.empty_image))

                                Spacer(modifier = Modifier.width(UICons.SpacerHeightMedium))

                                TextButton(
                                    modifier =
                                        Modifier.testTag("$REMOVE_BUTTON_TAG_PREFIX$tagSuffix"),
                                    onClick = {
                                      when (image) {
                                        is OtherImage.Remote ->
                                            imageCallbacks.onRemoveExistingImage(image.url)
                                        is OtherImage.Local ->
                                            imageCallbacks.onRemoveOtherImage(image.uri)
                                      }
                                    }) {
                                      Icon(
                                          painter = painterResource(R.drawable.ic_delete),
                                          contentDescription =
                                              BaseHuntFieldsStrings.DELETE_ICON_DESC,
                                          tint = MaterialTheme.colorScheme.error,
                                          modifier = Modifier.size(UICons.IconSize))
                                      Spacer(modifier = Modifier.width(UICons.SpacerHeightTiny))
                                      Text(
                                          BaseHuntFieldsStrings.REMOVE,
                                          color = MaterialTheme.colorScheme.error)
                                    }
                              }
                        }
                  }
                }
              }
            }
      }
}

@Composable
private fun SaveHuntButton(
    enabled: Boolean,
    onSave: () -> Unit,
) {
  Button(
      onClick = onSave,
      modifier =
          Modifier.fillMaxWidth()
              .height(UICons.ButtonSaveHeight)
              .shadow(UICons.ButtonShadow, RoundedCornerShape(UICons.ButtonSaveCornerRadius))
              .testTag(HuntScreenTestTags.HUNT_SAVE),
      enabled = enabled,
      shape = RoundedCornerShape(UICons.ButtonSaveCornerRadius),
      colors =
          ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
              disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
              disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
        Text(BaseHuntFieldsStrings.BUTTON_SAVE_HUNT, style = MaterialTheme.typography.titleMedium)
      }
}
