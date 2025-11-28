package com.swentseekr.seekr.ui.hunt

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AddPointsMapScreenDefaults {
  const val TitleText = "Select Hunt Points"
  const val BackContentDescription = "Back"
  const val ConfirmButtonLabel = "Confirm Points"
  val BottomPadding: Dp = 16.dp
  const val dialogTitle = "Give your checkpoint a name and an optional description."
  const val description = "Description"
  const val add = "Add"
  const val cancel = "Cancel"
  const val placeholder = "e.g. Louvre museum"
  const val pointsName = "Point's name"
  const val notEmpty = "The name cannot be empty"
}

object AddPointsMapScreenTestTags {
  const val CONFIRM_BUTTON = "ConfirmButton"
  const val MAP_VIEW = "MapView"
  const val CANCEL_BUTTON = "CancelButton"
  const val POINT_NAME_FIELD = "PointNameField"
  const val POINT_DESCRIPTION_FIELD = "PointDescriptionField"
}
