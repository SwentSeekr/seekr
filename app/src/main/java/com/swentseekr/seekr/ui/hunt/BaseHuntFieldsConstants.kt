package com.swentseekr.seekr.ui.hunt

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object BaseHuntFieldsStrings {
  const val BACK_CONTENT_DESC = "Back"
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
  const val BUTTON_SELECT_LOCATIONS = "Select Locations"
  const val BUTTON_SAVE_HUNT = "Save Hunt"
  const val UNIT_POINT = "point"
  const val UNIT_POINTS = "points"

  const val TITLE_DEFAULT = "Add your Hunt"
  const val BUTTON_CHOOSE_IMAGE = "Choose Main Image"
  const val CONTENT_DESC_SELECTED_IMAGE = "Selected Hunt Image"
  const val BUTTON_CHOOSE_ADDITIONAL_IMAGES = "Choose Additional Images"
  const val CONTENT_DESC_SECONDARY_IMAGE = "Secondary Image"
  const val REMOVE = "Remove"
  const val DELETE_ICON_DESC = "Delete Image"
  const val REMOVE_BUTTON_TAG_PREFIX = "removeButton_"
}

object BaseHuntFieldsUi {
  val FieldCornerRadius: Dp = 16.dp
  val ChangeAlpha: Float = 0.5f
  val WeightTextField: Float = 1f

  val ColumnHPadding: Dp = 20.dp
  val ColumnVArrangement: Dp = 16.dp
  val RowHArrangement: Dp = 12.dp

  val DescriptionHeight: Dp = 140.dp

  val ImageHeight: Dp = 200.dp
  val ImageLittleHeight: Dp = 100.dp
  val ImageWeight: Float = 1f
  val ImageCornerRadius: Dp = 16.dp
  val ImageLittleCornerRadius: Dp = 8.dp

  val ButtonHeight: Dp = 64.dp
  val ButtonCornerRadius: Dp = 16.dp

  val ButtonSaveHeight: Dp = 56.dp
  val ButtonSaveCornerRadius: Dp = 16.dp

  val ImageButtonHeight: Dp = 48.dp
  val ImageButtonCornerRadius: Dp = 12.dp

  val ButtonShadow: Dp = 8.dp

  val CardCornerRadius: Dp = 20.dp
  val CardLittleCornerRadius: Dp = 12.dp
  val CardPadding: Dp = 20.dp
  val CardVArrangement: Dp = 12.dp

  val CardRowPadding: Dp = 8.dp

  val SpacerHeight: Dp = 16.dp
  val SpacerHeightMedium: Dp = 12.dp
  val SpacerHeightSmall: Dp = 8.dp
  val SpacerHeightTiny: Dp = 4.dp

  val IconSize: Dp = 20.dp
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
