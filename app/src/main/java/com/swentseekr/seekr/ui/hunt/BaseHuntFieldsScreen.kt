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
              contentDescription = BaseHuntFieldsStrings.BACK_CONTENT_DESC)
        }
      },
      actions = {
        if (deleteAction.show && deleteAction.onClick != null) {

          // Delete button toggled via the overflow icon.
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

  // Build a single list of all images (remote + local)
  val combinedImages: List<OtherImage> =
      uiState.otherImagesUrls.map { OtherImage.Remote(it) } +
          uiState.otherImagesUris.map { OtherImage.Local(it) }

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
              ImagesSectionHeader()

              MainImagePickerButton(onClick = { imagePickerLauncher.launch("image/*") })

              MainImagePreview(
                  imageUrl = uiState.mainImageUrl,
                  onDelete = { imageCallbacks.onRemoveMainImage() })

              AdditionalImagesPickerButton(
                  onClick = { multipleImagesPickerLauncher.launch("image/*") })

              AdditionalImagesList(
                  images = combinedImages,
                  imageCallbacks = imageCallbacks,
              )
            }
      }
}

/** Title of the images section. */
@Composable
private fun ImagesSectionHeader() {
  Text(
      "Images",
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/**
 * Button to pick the main image.
 *
 * @param onClick Callback invoked when the user taps the button to choose a main image.
 */
@Composable
private fun MainImagePickerButton(
    onClick: () -> Unit,
) {
  OutlinedButton(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth().height(UICons.ImageButtonHeight),
      shape = RoundedCornerShape(UICons.ImageButtonCornerRadius)) {
        Text(BaseHuntFieldsStrings.BUTTON_CHOOSE_IMAGE, style = MaterialTheme.typography.bodyLarge)
      }
}

/**
 * Preview of the main image if one is available.
 *
 * @param imageUrl URL of the main image to preview, or `null` / blank to hide the preview.
 */
@Composable
private fun MainImagePreview(
    imageUrl: String?,
    onDelete: () -> Unit,
) {
  AnimatedVisibility(visible = !imageUrl.isNullOrBlank()) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      AsyncImage(
          model = imageUrl,
          contentDescription = BaseHuntFieldsStrings.CONTENT_DESC_SELECTED_IMAGE,
          modifier =
              Modifier.weight(1f)
                  .height(UICons.ImageHeight)
                  .clip(RoundedCornerShape(UICons.ImageCornerRadius))
                  .shadow(4.dp, RoundedCornerShape(UICons.ImageCornerRadius)),
          placeholder = painterResource(R.drawable.empty_image),
          error = painterResource(R.drawable.empty_image))

      Spacer(modifier = Modifier.width(12.dp))

      TextButton(onClick = onDelete, modifier = Modifier.testTag("delete_main_image")) {
        Icon(
            painter = painterResource(R.drawable.ic_delete),
            contentDescription = "Delete main image",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(UICons.IconSize))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Remove", color = MaterialTheme.colorScheme.error)
      }
    }
  }
}

/**
 * Button to pick additional (secondary) images.
 *
 * @param onClick Callback invoked when the user taps the button to choose additional images.
 */
@Composable
private fun AdditionalImagesPickerButton(
    onClick: () -> Unit,
) {
  OutlinedButton(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth().height(UICons.ImageButtonHeight),
      shape = RoundedCornerShape(UICons.ImageButtonCornerRadius)) {
        Text(
            BaseHuntFieldsStrings.BUTTON_CHOOSE_ADDITIONAL_IMAGES,
            style = MaterialTheme.typography.bodyLarge)
      }
}

/**
 * List of additional images (remote + local) with delete actions.
 *
 * @param images List of [OtherImage] instances to display.
 * @param imageCallbacks Callbacks used when an image is removed.
 */
@Composable
private fun AdditionalImagesList(
    images: List<OtherImage>,
    imageCallbacks: ImageCallbacks,
) {
  if (images.isEmpty()) return

  Column(verticalArrangement = Arrangement.spacedBy(UICons.CardVArrangement)) {
    images.forEach { image ->
      AdditionalImageItem(
          image = image,
          imageCallbacks = imageCallbacks,
      )
    }
  }
}

/**
 * Single additional image row with preview and remove button.
 *
 * @param image Image to display, either [OtherImage.Remote] or [OtherImage.Local].
 * @param imageCallbacks Callbacks used when the user presses the remove button.
 */
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
      shape = RoundedCornerShape(UICons.CardLittleCornerRadius)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(UICons.CardRowPadding),
            horizontalArrangement = Arrangement.SpaceBetween) {
              AsyncImage(
                  model = model,
                  contentDescription = BaseHuntFieldsStrings.CONTENT_DESC_SECONDARY_IMAGE,
                  modifier =
                      Modifier.testTag("otherImage_$tagSuffix")
                          .weight(UICons.ImageWeight)
                          .height(UICons.ImageLittleHeight)
                          .clip(RoundedCornerShape(UICons.ImageLittleCornerRadius)),
                  placeholder = painterResource(R.drawable.empty_image),
                  error = painterResource(R.drawable.empty_image))

              Spacer(modifier = Modifier.width(UICons.SpacerHeightMedium))

              TextButton(
                  modifier = Modifier.testTag("$REMOVE_BUTTON_TAG_PREFIX$tagSuffix"),
                  onClick = {
                    when (image) {
                      is OtherImage.Remote -> imageCallbacks.onRemoveExistingImage(image.url)
                      is OtherImage.Local -> imageCallbacks.onRemoveOtherImage(image.uri)
                    }
                  }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = BaseHuntFieldsStrings.DELETE_ICON_DESC,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(UICons.IconSize))
                    Spacer(modifier = Modifier.width(UICons.SpacerHeightTiny))
                    Text(BaseHuntFieldsStrings.REMOVE, color = MaterialTheme.colorScheme.error)
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
