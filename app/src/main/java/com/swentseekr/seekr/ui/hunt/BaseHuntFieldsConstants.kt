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
}

object BaseHuntFieldsUi {
  val FieldCornerRadius: Dp = 12.dp
  val DescriptionHeight: Dp = 150.dp
  val SaveButtonHeight: Dp = 64.dp
  val SaveButtonCornerRadius: Dp = 16.dp
  val ScreenPadding: Dp = 16.dp
  val SpacerHeight: Dp = 24.dp
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
}
