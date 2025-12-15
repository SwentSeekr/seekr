package com.swentseekr.seekr.ui.hunt

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AddPointsMapScreenDefaults {
  const val TITLE_TEXT = "Select Hunt Points"
  const val BACK_CONTENT_DESCRIPTION = "Back"
  const val CONFIRM_BUTTON_LABEL = "Confirm Points"
  val BottomPadding: Dp = 16.dp
  const val DIALOG_TITLE = "Give your checkpoint a name and an optional description."
  const val DESCRIPTION = "Description"
  const val ADD = "Add"
  const val CANCEL = "Cancel"
  const val PLACEHOLDER = "e.g. Louvre museum"
  const val POINTS_NAME = "Point's name"
  const val NOT_EMPTY = "The name cannot be empty"
}

object AddPointsMapScreenTestTags {
  const val CONFIRM_BUTTON = "ConfirmButton"
  const val MAP_VIEW = "MapView"
  const val CANCEL_BUTTON = "CancelButton"
  const val POINT_NAME_FIELD = "PointNameField"
  const val POINT_DESCRIPTION_FIELD = "PointDescriptionField"
}
