package com.swentseekr.seekr.ui.hunt

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object BaseHuntFieldsStrings {

  // Top bar
  const val BACK_CONTENT_DESC = "Back"

  // Fields
  const val LABEL_TITLE = "Title"
  const val PLACEHOLDER_TITLE = "Enter hunt name"
  const val LABEL_DESCRIPTION = "Description"
  const val PLACEHOLDER_DESCRIPTION = "Describe your hunt"
  const val LABEL_STATUS = "Status"
  const val EXPAND_STATUS_DESC = "Expand Status"
  const val LABEL_DIFFICULTY = "Difficulty"
  const val EXPAND_DIFFICULTY_DESC = "Expand Difficulty"
  const val LABEL_TIME = "Estimated Time (hours)"
  const val PLACEHOLDER_TIME = "e.g., 1.5"
  const val LABEL_DISTANCE = "Distance (km)"
  const val PLACEHOLDER_DISTANCE = "e.g., 2.3"

  // Screens
  const val TITLE_DEFAULT = "Add your Hunt"

  // Buttons
  const val BUTTON_SELECT_LOCATIONS = "Select Locations"
  const val BUTTON_SAVE_HUNT = "Save Hunt"
  const val BUTTON_GALLERY = "Gallery"
  const val BUTTON_CAMERA = "Camera"
  const val BUTTON_REMOVE_IMAGE = "Remove image"
  const val BUTTON_REMOVE = "Remove"
  const val BUTTON_DELETE = "Delete"

  // Image labels
  const val LABEL_MAIN_IMAGE = "Main image"
  const val LABEL_ADDITIONAL_IMAGES = "Additional images"
  const val CONTENT_DESC_SELECTED_IMAGE = "Selected Hunt Image"
  const val CONTENT_DESC_SECONDARY_IMAGE = "Secondary Image"
  const val DELETE_ICON_DESC = "Delete Image"

  const val LABEL_IMAGES = "Images"
  const val UNIT_IMAGE = "image"
  const val UNIT_IMAGES = "images"
  const val CONTENT_DESC_MAIN_IMAGE = "Main image preview"
  const val CONTENT_DESC_ADDITIONAL_IMAGE = "Additional image preview"

  // Units
  const val UNIT_POINT = "point"
  const val UNIT_POINTS = "points"

  // Tags
  const val REMOVE_BUTTON_TAG_PREFIX = "removeButton_"
}

object BaseHuntFieldsUi {

  // Layout
  val ColumnHPadding: Dp = 20.dp
  val ColumnVArrangement: Dp = 16.dp
  val RowHArrangement: Dp = 12.dp

  // Fields
  val FieldCornerRadius: Dp = 16.dp
  val ChangeAlpha: Float = 0.5f
  val Alpha03: Float = 0.3f
  val WeightTextField: Float = 1f
  val DescriptionHeight: Dp = 140.dp

  // Images
  val ImageHeight: Dp = 200.dp
  val ImageThumbSize: Dp = 80.dp
  val ImageCornerRadius: Dp = 16.dp
  val ImageThumbCornerRadius: Dp = 8.dp

  // Buttons
  val ButtonHeight: Dp = 64.dp
  val ButtonCornerRadius: Dp = 16.dp

  val ImageButtonHeight: Dp = 48.dp
  val ImageButtonCornerRadius: Dp = 12.dp

  val ButtonSaveHeight: Dp = 56.dp
  val ButtonSaveCornerRadius: Dp = 16.dp
  val ButtonShadow: Dp = 8.dp

  // Cards
  val CardCornerRadius: Dp = 20.dp
  val CardPadding: Dp = 20.dp
  val CardVArrangement: Dp = 16.dp
  val CardRowPadding: Dp = 12.dp

  // Spacers
  val SpacerHeight: Dp = 16.dp
  val SpacerHeightMedium: Dp = 12.dp
  val SpacerHeightSmall: Dp = 8.dp
  val SpacerHeightTiny: Dp = 4.dp

  // Icons
  val IconSize: Dp = 20.dp

  // Divider
  val DividerThickness: Dp = 1.dp
}

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
  const val COLLUMN_HUNT_FIELDS = "ColumnHuntFields"
}
