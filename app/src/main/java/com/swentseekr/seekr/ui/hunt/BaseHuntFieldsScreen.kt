package com.swentseekr.seekr.ui.hunt

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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

@Composable
fun ValidatedOutlinedField(
    value: String = BaseHuntFieldsStrings.TITLE_DEFAULT,
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
      shape = RoundedCornerShape(UICons.FieldCornerRadius),
      colors =
          OutlinedTextFieldDefaults.colors(
              unfocusedBorderColor =
                  MaterialTheme.colorScheme.outline.copy(alpha = UICons.ChangeAlpha),
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface,
              focusedContainerColor = MaterialTheme.colorScheme.surface))
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
        AnimatedVisibility(visible = errorMsg != null, enter = fadeIn(), exit = fadeOut()) {
          Text(
              errorMsg ?: "",
              modifier = Modifier.testTag(HuntScreenTestTags.ERROR_MESSAGE),
              color = MaterialTheme.colorScheme.error)
        }
      },
      modifier = modifier.testTag(testTag),
      shape = shape,
      colors = colors)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseHuntFieldsScreen(
    title: String = BaseHuntFieldsStrings.TITLE_DEFAULT,
    uiState: HuntUIState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onDifficultySelect: (Difficulty) -> Unit,
    onStatusSelect: (HuntStatus) -> Unit,
    onSelectLocations: () -> Unit,
    onSelectImage: (Uri?) -> Unit,
    onSelectOtherImages: (List<Uri>) -> Unit,
    onRemoveOtherImage: (Uri) -> Unit,
    onRemoveExistingImage: (String) -> Unit,
    onSave: () -> Unit,
    onGoBack: () -> Unit,
) {
  var showStatusDropdown by rememberSaveable { mutableStateOf(false) }
  var showDifficultyDropdown by rememberSaveable { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  val imagePickerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetContent(), onResult = { uri -> onSelectImage(uri) })

  val multipleImagesPickerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetMultipleContents(),
          onResult = { uris -> onSelectOtherImages(uris) })

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
            navigationIcon = {
              IconButton(onClick = onGoBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = BaseHuntFieldsStrings.BACK_CONTENT_DESC)
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface))
      },
      modifier = Modifier.testTag(HuntScreenTestTags.ADD_HUNT_SCREEN)) { paddingValues ->
        Column(
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
                    .padding(paddingValues)
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

              ValidatedOutlinedField(
                  value = uiState.title,
                  onValueChange = onTitleChange,
                  label = BaseHuntFieldsStrings.LABEL_TITLE,
                  placeholder = BaseHuntFieldsStrings.PLACEHOLDER_TITLE,
                  errorMsg = uiState.invalidTitleMsg,
                  testTag = HuntScreenTestTags.INPUT_HUNT_TITLE)

              ValidatedOutlinedField(
                  value = uiState.description,
                  onValueChange = onDescriptionChange,
                  label = BaseHuntFieldsStrings.LABEL_DESCRIPTION,
                  placeholder = BaseHuntFieldsStrings.PLACEHOLDER_DESCRIPTION,
                  errorMsg = uiState.invalidDescriptionMsg,
                  testTag = HuntScreenTestTags.INPUT_HUNT_DESCRIPTION,
                  modifier = Modifier.fillMaxWidth().height(UICons.DescriptionHeight),
                  shape = fieldShape,
                  colors = fieldColors)

              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(UICons.RowHArrangement)) {
                    ExposedDropdownMenuBox(
                        expanded = showStatusDropdown,
                        onExpandedChange = { showStatusDropdown = it },
                        modifier = Modifier.weight(UICons.WeightTextField)) {
                          OutlinedTextField(
                              value = uiState.status?.name ?: "",
                              onValueChange = {},
                              label = { Text(BaseHuntFieldsStrings.LABEL_STATUS) },
                              readOnly = true,
                              trailingIcon = {
                                Icon(
                                    imageVector =
                                        if (showStatusDropdown) Icons.Outlined.KeyboardArrowUp
                                        else Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = BaseHuntFieldsStrings.EXPAND_STATUS_DESC)
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
                        onExpandedChange = { showDifficultyDropdown = it },
                        modifier = Modifier.weight(UICons.WeightTextField)) {
                          OutlinedTextField(
                              value = uiState.difficulty?.name ?: "",
                              onValueChange = {},
                              label = { Text(BaseHuntFieldsStrings.LABEL_DIFFICULTY) },
                              readOnly = true,
                              trailingIcon = {
                                Icon(
                                    imageVector =
                                        if (showDifficultyDropdown) Icons.Outlined.KeyboardArrowUp
                                        else Icons.Outlined.KeyboardArrowDown,
                                    contentDescription =
                                        BaseHuntFieldsStrings.EXPAND_DIFFICULTY_DESC)
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
                  }

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
                        modifier = Modifier.weight(UICons.WeightTextField),
                        shape = fieldShape,
                        colors = fieldColors)

                    ValidatedOutlinedField(
                        value = uiState.distance,
                        onValueChange = onDistanceChange,
                        label = BaseHuntFieldsStrings.LABEL_DISTANCE,
                        placeholder = BaseHuntFieldsStrings.PLACEHOLDER_DISTANCE,
                        errorMsg = uiState.invalidDistanceMsg,
                        testTag = HuntScreenTestTags.INPUT_HUNT_DISTANCE,
                        modifier = Modifier.weight(UICons.WeightTextField),
                        shape = fieldShape,
                        colors = fieldColors)
                  }

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
                    val pointCount = uiState.points.size
                    val label =
                        if (pointCount > 0) {
                          "${BaseHuntFieldsStrings.BUTTON_SELECT_LOCATIONS} ($pointCount ${if (pointCount == 1) BaseHuntFieldsStrings.UNIT_POINT else BaseHuntFieldsStrings.UNIT_POINTS})"
                        } else {
                          BaseHuntFieldsStrings.BUTTON_SELECT_LOCATIONS
                        }
                    Text(label, style = MaterialTheme.typography.titleMedium)
                  }

              Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(UICons.CardCornerRadius),
                  colors =
                      CardDefaults.cardColors(
                          containerColor =
                              MaterialTheme.colorScheme.surfaceVariant.copy(
                                  alpha = UICons.ChangeAlpha))) {
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
                                contentDescription =
                                    BaseHuntFieldsStrings.CONTENT_DESC_SELECTED_IMAGE,
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
                            Column(
                                verticalArrangement =
                                    Arrangement.spacedBy(UICons.CardVArrangement)) {
                                  combinedImages.forEach { image ->
                                    val tagSuffix =
                                        when (image) {
                                          is OtherImage.Remote -> image.url
                                          is OtherImage.Local -> image.uri.toString()
                                        }

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(UICons.CardLittleCornerRadius),
                                    ) {
                                      Row(
                                          modifier =
                                              Modifier.fillMaxWidth()
                                                  .padding(UICons.CardRowPadding),
                                          horizontalArrangement = Arrangement.SpaceBetween) {
                                            val model =
                                                when (image) {
                                                  is OtherImage.Remote -> image.url
                                                  is OtherImage.Local -> image.uri
                                                }

                                            AsyncImage(
                                                model = model,
                                                contentDescription =
                                                    BaseHuntFieldsStrings
                                                        .CONTENT_DESC_SECONDARY_IMAGE,
                                                modifier =
                                                    Modifier.testTag("otherImage_$tagSuffix")
                                                        .weight(UICons.ImageWeight)
                                                        .height(UICons.ImageLittleHeight)
                                                        .clip(
                                                            RoundedCornerShape(
                                                                UICons.ImageLittleCornerRadius)),
                                                placeholder =
                                                    painterResource(R.drawable.empty_image),
                                                error = painterResource(R.drawable.empty_image))

                                            Spacer(
                                                modifier =
                                                    Modifier.width(UICons.SpacerHeightMedium))

                                            TextButton(
                                                modifier =
                                                    Modifier.testTag(
                                                        "$REMOVE_BUTTON_TAG_PREFIX$tagSuffix"),
                                                onClick = {
                                                  when (image) {
                                                    is OtherImage.Remote ->
                                                        onRemoveExistingImage(image.url)
                                                    is OtherImage.Local ->
                                                        onRemoveOtherImage(image.uri)
                                                  }
                                                }) {
                                                  Icon(
                                                      painter =
                                                          painterResource(R.drawable.ic_delete),
                                                      contentDescription =
                                                          BaseHuntFieldsStrings.DELETE_ICON_DESC,
                                                      tint = MaterialTheme.colorScheme.error,
                                                      modifier = Modifier.size(UICons.IconSize))
                                                  Spacer(
                                                      modifier =
                                                          Modifier.width(UICons.SpacerHeightTiny))
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

              Spacer(modifier = Modifier.height(UICons.SpacerHeightSmall))

              Button(
                  onClick = onSave,
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(UICons.ButtonSaveHeight)
                          .shadow(
                              UICons.ButtonShadow,
                              RoundedCornerShape(UICons.ButtonSaveCornerRadius))
                          .testTag(HuntScreenTestTags.HUNT_SAVE),
                  enabled = uiState.isValid,
                  shape = RoundedCornerShape(UICons.ButtonSaveCornerRadius),
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.primary,
                          contentColor = MaterialTheme.colorScheme.onPrimary,
                          disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                          disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    Text(
                        BaseHuntFieldsStrings.BUTTON_SAVE_HUNT,
                        style = MaterialTheme.typography.titleMedium)
                  }

              Spacer(modifier = Modifier.height(UICons.SpacerHeight))
            }
      }
}
