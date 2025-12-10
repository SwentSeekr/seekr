package com.swentseekr.seekr.ui.hunt

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.BACK_CONTENT_DESC
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.BUTTON_CAMERA
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.BUTTON_DELETE
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.BUTTON_GALLERY
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.BUTTON_REMOVE
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.BUTTON_REMOVE_IMAGE
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.CONTENT_DESC_ADDITIONAL_IMAGE
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.CONTENT_DESC_MAIN_IMAGE
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.DELETE_ICON_DESC
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.LABEL_ADDITIONAL_IMAGES
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.LABEL_IMAGES
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.LABEL_MAIN_IMAGE
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.REMOVE_BUTTON_TAG_PREFIX
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.UNIT_IMAGE
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.UNIT_IMAGES
import com.swentseekr.seekr.ui.profile.createImageUri

/** Represents an additional image associated with a hunt, which can be either remote or local. */
sealed class OtherImage {

  /**
   * Remote image stored in the backend.
   *
   * @property url Downloadable URL of the image stored in the backend (e.g. Firebase Storage).
   */
  data class Remote(val url: String) : OtherImage()

  /**
   * Local image selected on the device.
   *
   * @property uri Content [Uri] pointing to the selected local image.
   */
  data class Local(val uri: Uri) : OtherImage()
}

/** UI constants used by the base hunt fields layout. */
val UICons = BaseHuntFieldsUi

/**
 * Encapsulates styling for validated text fields.
 *
 * This wrapper helps keep the argument list of [ValidatedOutlinedField] under the SonarCloud
 * parameter limit while centralizing visual configuration.
 *
 * @property modifier [Modifier] applied to the underlying text field.
 * @property shape Shape used for the text field.
 * @property colors Color configuration for the text field.
 */
data class FieldStyle(
    val modifier: Modifier,
    val shape: Shape,
    val colors: TextFieldColors,
)

/**
 * Provides a default [FieldStyle] instance based on the current theme.
 *
 * @param modifier [Modifier] applied to the underlying text field.
 * @return A [FieldStyle] that can be reused across validated text fields.
 */
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

/**
 * Callbacks for the main hunt fields (title, description, time, distance, etc.).
 *
 * Grouping these callbacks keeps the [BaseHuntFieldsScreen] signature concise and easier to
 * maintain.
 *
 * @property onTitleChange Invoked when the title text changes.
 * @property onDescriptionChange Invoked when the description text changes.
 * @property onTimeChange Invoked when the time text changes.
 * @property onDistanceChange Invoked when the distance text changes.
 * @property onDifficultySelect Invoked when a difficulty value is selected.
 * @property onStatusSelect Invoked when a status value is selected.
 * @property onSelectLocations Invoked when the user wants to open the location picker.
 * @property onSave Invoked when the user presses the primary save button.
 */
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

/**
 * Callbacks for image-related actions in the hunt form.
 *
 * @property onSelectImage Invoked when a main image is selected (or cleared with `null`).
 * @property onSelectOtherImages Invoked when one or more additional images are selected.
 * @property onRemoveOtherImage Invoked when a local additional image should be removed.
 * @property onRemoveExistingImage Invoked when a remote additional image URL should be removed.
 */
data class ImageCallbacks(
    val onSelectImage: (Uri?) -> Unit,
    val onSelectOtherImages: (List<Uri>) -> Unit,
    val onRemoveOtherImage: (Uri) -> Unit,
    val onRemoveExistingImage: (String) -> Unit,
    val onRemoveMainImage: () -> Unit
)

/**
 * Navigation-related callbacks for the hunt form.
 *
 * Additional navigation actions can be added here as the flow evolves.
 *
 * @property onGoBack Invoked when the back action in the top bar is pressed.
 */
data class HuntNavigationCallbacks(
    val onGoBack: () -> Unit = {},
)

/**
 * Text field with validation support and reusable styling.
 *
 * Displays an error message in the supporting text area when [errorMsg] is not null.
 *
 * @param value Current text value.
 * @param onValueChange Callback invoked when the text changes.
 * @param label Label displayed above the field.
 * @param placeholder Placeholder text displayed inside the field when empty.
 * @param errorMsg Optional error message shown below the field; when non-null, the field is marked
 *   as invalid.
 * @param testTag Test tag used to identify the field in UI tests.
 * @param style Visual configuration for the field (shape, modifier, colors).
 */
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

/**
 * Main layout for the hunt form fields.
 *
 * This composable renders:
 * - Text inputs (title, description, time, distance).
 * - Status and difficulty dropdowns.
 * - Location selection button.
 * - Image selection and management.
 * - Save button.
 *
 * @param title Title displayed in the top bar.
 * @param uiState Current UI state of the hunt form.
 * @param fieldCallbacks Group of callbacks handling field changes and save action.
 * @param imageCallbacks Group of callbacks handling image selection and removal.
 * @param navigationCallbacks Callbacks related to navigation events (e.g. back navigation).
 * @param deleteAction Configuration for the optional delete action in the top bar. The delete
 *   button is shown only when [DeleteAction.show] is `true` and [DeleteAction.onClick] is non-null.
 */
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

                    // Title field.
                    ValidatedOutlinedField(
                        value = uiState.title,
                        onValueChange = fieldCallbacks.onTitleChange,
                        label = BaseHuntFieldsStrings.LABEL_TITLE,
                        placeholder = BaseHuntFieldsStrings.PLACEHOLDER_TITLE,
                        errorMsg = uiState.invalidTitleMsg,
                        testTag = HuntScreenTestTags.INPUT_HUNT_TITLE)

                    // Description field.
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

                    // Status and difficulty dropdowns.
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

                    // Time and distance fields.
                    TimeAndDistanceRow(
                        uiState = uiState,
                        fieldShape = fieldShape,
                        fieldColors = fieldColors,
                        onTimeChange = fieldCallbacks.onTimeChange,
                        onDistanceChange = fieldCallbacks.onDistanceChange)

                    // Location selection button.
                    SelectLocationsButton(
                        pointsCount = uiState.points.size,
                        onSelectLocations = fieldCallbacks.onSelectLocations)

                    // Image selection and management section.
                    ImagesSection(uiState = uiState, imageCallbacks = imageCallbacks)

                    Spacer(modifier = Modifier.height(UICons.SpacerHeightSmall))

                    // Save button at the bottom of the form.
                    SaveHuntButton(enabled = uiState.isValid, onSave = fieldCallbacks.onSave)

                    Spacer(modifier = Modifier.height(UICons.SpacerHeight))
                  }
            }
      }
}

/**
 * Top app bar for the hunt form, including navigation and optional delete action.
 *
 * The delete button is displayed only when [deleteAction.show] is `true` and [deleteAction.onClick]
 * is non-null. When the "Delete" button is tapped, the callback is invoked directly with no
 * built-in confirmation.
 *
 * @param title Title text displayed in the top bar.
 * @param onGoBack Callback invoked when the back button is pressed.
 * @param deleteAction Configuration for the optional delete action.
 */
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
              contentDescription = BACK_CONTENT_DESC)
        }
      },
      actions = {
        if (deleteAction.show && deleteAction.onClick != null) {

          if (showDeleteButton) {
            Button(
                onClick = deleteAction.onClick,
                modifier = Modifier.padding(end = UICons.SpacerHeightSmall),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError),
                shape = RoundedCornerShape(UICons.ButtonCornerRadius)) {
                  Icon(
                      painter = painterResource(R.drawable.ic_delete),
                      contentDescription = DELETE_ICON_DESC,
                      modifier = Modifier.size(UICons.IconSize))
                  Spacer(modifier = Modifier.width(UICons.SpacerHeightSmall))
                  Text(BUTTON_DELETE)
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

/**
 * Status dropdown with its own internal expanded state.
 *
 * Defined as a [RowScope] extension to allow the use of [Modifier.weight].
 *
 * @param status Currently selected status, or `null` if none.
 * @param onStatusSelect Invoked when the user selects a new [HuntStatus].
 * @param shape Shape used for the text field.
 * @param colors Color configuration for the text field.
 */
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

/**
 * Difficulty dropdown with its own internal expanded state.
 *
 * Defined as a [RowScope] extension to allow the use of [Modifier.weight].
 *
 * @param difficulty Currently selected difficulty, or `null` if none.
 * @param onDifficultySelect Invoked when the user selects a new [Difficulty].
 * @param shape Shape used for the text field.
 * @param colors Color configuration for the text field.
 */
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

/**
 * Row containing time and distance fields.
 *
 * @param uiState Current [HuntUIState], used for values and error messages.
 * @param fieldShape Shape applied to both fields.
 * @param fieldColors Color configuration applied to both fields.
 * @param onTimeChange Invoked when the time text changes.
 * @param onDistanceChange Invoked when the distance text changes.
 */
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

/**
 * Button used to trigger location selection, with label reflecting the number of points selected.
 *
 * @param pointsCount Number of currently selected points.
 * @param onSelectLocations Invoked when the button is pressed.
 */
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

/**
 * Section responsible for main and secondary image selection, preview, and removal.
 *
 * @param uiState Current [HuntUIState], used to determine current images.
 * @param imageCallbacks Callbacks invoked when images are selected or removed.
 */
/**
 * Section améliorée pour la sélection d'images principales et additionnelles avec une UI moderne et
 * professionnelle.
 */
@Composable
private fun ImagesSection(
    uiState: HuntUIState,
    imageCallbacks: ImageCallbacks,
) {
  val context = LocalContext.current

  // GALLERY
  val imagePickerLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageCallbacks.onSelectImage(uri)
      }

  val multipleImagesLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        imageCallbacks.onSelectOtherImages(uris)
      }

  // CAMERA
  var mainCameraUri by remember { mutableStateOf<Uri?>(null) }
  val mainCameraLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && mainCameraUri != null) {
          imageCallbacks.onSelectImage(mainCameraUri!!)
        }
      }

  var otherCameraUri by remember { mutableStateOf<Uri?>(null) }
  val otherCameraLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && otherCameraUri != null) {
          imageCallbacks.onSelectOtherImages(listOf(otherCameraUri!!))
        }
      }

  val combinedImages =
      uiState.otherImagesUrls.map { OtherImage.Remote(it) } +
          uiState.otherImagesUris.map { OtherImage.Local(it) }

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .shadow(UICons.SpacerHeightTiny, RoundedCornerShape(UICons.CardCornerRadius)),
      shape = RoundedCornerShape(UICons.CardCornerRadius),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.padding(UICons.CardPadding),
            verticalArrangement = Arrangement.spacedBy(UICons.CardVArrangement)) {

              // HEADER
              Text(
                  LABEL_IMAGES,
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface)

              // MAIN IMAGE
              Column(verticalArrangement = Arrangement.spacedBy(UICons.SpacerHeightSmall)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                      Text(
                          LABEL_MAIN_IMAGE,
                          style = MaterialTheme.typography.labelLarge,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)

                      if (!uiState.mainImageUrl.isNullOrBlank()) {
                        Text(
                            "1 $UNIT_IMAGE",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                      }
                    }

                if (uiState.mainImageUrl.isNullOrBlank()) {
                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(UICons.RowHArrangement)) {
                        ImageActionButton(
                            label = BUTTON_GALLERY,
                            icon = R.drawable.gallerie_image,
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(UICons.WeightTextField))

                        ImageActionButton(
                            label = BUTTON_CAMERA,
                            icon = R.drawable.camera_icon,
                            onClick = {
                              val uri = createImageUri(context)
                              if (uri != null) {
                                mainCameraUri = uri
                                mainCameraLauncher.launch(uri)
                              }
                            },
                            modifier = Modifier.weight(UICons.WeightTextField))
                      }
                } else {
                  MainImagePreview(
                      imageUrl = uiState.mainImageUrl,
                      onDelete = { imageCallbacks.onRemoveMainImage() })
                }
              }

              // DIVIDER
              HorizontalDivider(
                  thickness = UICons.DividerThickness,
                  color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = UICons.ChangeAlpha))

              // ADDITIONAL IMAGES
              Column(verticalArrangement = Arrangement.spacedBy(UICons.SpacerHeightSmall)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                      Text(
                          LABEL_ADDITIONAL_IMAGES,
                          style = MaterialTheme.typography.labelLarge,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)

                      if (combinedImages.isNotEmpty()) {
                        Text(
                            "${combinedImages.size} ${
                                if (combinedImages.size == 1) UNIT_IMAGE else UNIT_IMAGES
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                      }
                    }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(UICons.RowHArrangement)) {
                      ImageActionButton(
                          label = BUTTON_GALLERY,
                          icon = R.drawable.gallerie_image,
                          onClick = { multipleImagesLauncher.launch("image/*") },
                          modifier = Modifier.weight(UICons.WeightTextField))

                      ImageActionButton(
                          label = BUTTON_CAMERA,
                          icon = R.drawable.camera_icon,
                          onClick = {
                            val uri = createImageUri(context)
                            if (uri != null) {
                              otherCameraUri = uri
                              otherCameraLauncher.launch(uri)
                            }
                          },
                          modifier = Modifier.weight(UICons.WeightTextField))
                    }

                AdditionalImagesList(images = combinedImages, imageCallbacks = imageCallbacks)
              }
            }
      }
}

@Composable
private fun ImageActionButton(
    label: String,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  OutlinedButton(
      onClick = onClick,
      modifier = modifier.height(UICons.ImageButtonHeight),
      shape = RoundedCornerShape(UICons.ImageButtonCornerRadius),
      colors =
          ButtonDefaults.outlinedButtonColors(
              containerColor = MaterialTheme.colorScheme.surface,
              contentColor = MaterialTheme.colorScheme.onSurface),
      border =
          BorderStroke(
              UICons.SpacerHeightTiny,
              MaterialTheme.colorScheme.outline.copy(alpha = UICons.Alpha03))) {
        Image(
            painter = painterResource(icon),
            contentDescription = label,
            modifier = Modifier.size(UICons.IconSize),
        )
        Spacer(Modifier.width(UICons.SpacerHeightSmall))
        Text(label, style = MaterialTheme.typography.labelLarge)
      }
}

@Composable
private fun MainImagePreview(
    imageUrl: String?,
    onDelete: () -> Unit,
) {
  Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(UICons.ImageCornerRadius),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = UICons.Alpha03))) {
        Column(modifier = Modifier.padding(UICons.CardRowPadding)) {
          AsyncImage(
              model = imageUrl,
              contentDescription = CONTENT_DESC_MAIN_IMAGE,
              modifier =
                  Modifier.fillMaxWidth()
                      .height(UICons.ImageHeight)
                      .clip(RoundedCornerShape(UICons.ImageCornerRadius)),
              contentScale = ContentScale.Crop,
              placeholder = painterResource(R.drawable.empty_image),
              error = painterResource(R.drawable.empty_image))

          Spacer(modifier = Modifier.height(UICons.SpacerHeightSmall))

          TextButton(
              onClick = onDelete,
              modifier = Modifier.fillMaxWidth().testTag("delete_main_image"),
              colors =
                  ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = DELETE_ICON_DESC,
                    modifier = Modifier.size(UICons.IconSize))
                Spacer(modifier = Modifier.width(UICons.SpacerHeightSmall))
                Text(BUTTON_REMOVE_IMAGE, style = MaterialTheme.typography.labelLarge)
              }
        }
      }
}

@Composable
private fun AdditionalImagesList(
    images: List<OtherImage>,
    imageCallbacks: ImageCallbacks,
) {
  if (images.isEmpty()) return

  Column(verticalArrangement = Arrangement.spacedBy(UICons.SpacerHeightSmall)) {
    images.forEach { image ->
      AdditionalImageItem(
          image = image,
          imageCallbacks = imageCallbacks,
      )
    }
  }
}

@Composable
private fun AdditionalImageItem(
    image: OtherImage,
    imageCallbacks: ImageCallbacks,
) {
  val tagSuffix =
      when (image) {
        is OtherImage.Remote -> image.url
        is OtherImage.Local -> image.uri.toString()
      }

  val model: Any =
      when (image) {
        is OtherImage.Remote -> image.url
        is OtherImage.Local -> image.uri
      }

  Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(UICons.ImageThumbCornerRadius),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = UICons.Alpha03))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(UICons.CardRowPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
              AsyncImage(
                  model = model,
                  contentDescription = CONTENT_DESC_ADDITIONAL_IMAGE,
                  modifier =
                      Modifier.testTag("otherImage_$tagSuffix")
                          .size(UICons.ImageThumbSize)
                          .clip(RoundedCornerShape(UICons.ImageThumbCornerRadius)),
                  contentScale = ContentScale.Crop,
                  placeholder = painterResource(R.drawable.empty_image),
                  error = painterResource(R.drawable.empty_image))

              Spacer(modifier = Modifier.width(UICons.SpacerHeight))

              TextButton(
                  modifier = Modifier.testTag("$REMOVE_BUTTON_TAG_PREFIX$tagSuffix"),
                  onClick = {
                    when (image) {
                      is OtherImage.Remote -> imageCallbacks.onRemoveExistingImage(image.url)
                      is OtherImage.Local -> imageCallbacks.onRemoveOtherImage(image.uri)
                    }
                  },
                  colors =
                      ButtonDefaults.textButtonColors(
                          contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = DELETE_ICON_DESC,
                        modifier = Modifier.size(UICons.IconSize))
                    Spacer(modifier = Modifier.width(UICons.SpacerHeightSmall))
                    Text(BUTTON_REMOVE, style = MaterialTheme.typography.labelLarge)
                  }
            }
      }
}

/**
 * Primary save action for the hunt form.
 *
 * @param enabled Whether the button is enabled.
 * @param onSave Invoked when the user presses the button.
 */
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
