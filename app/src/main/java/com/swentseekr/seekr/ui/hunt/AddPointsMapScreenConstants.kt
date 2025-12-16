package com.swentseekr.seekr.ui.hunt

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Default constant values used in the Add Points Map screen. */
object AddPointsMapScreenDefaults {

  // -------------------
  // Screen Titles & Labels
  // -------------------
  const val TITLE_TEXT = "Select Hunt Points"
  const val BACK_CONTENT_DESCRIPTION = "Back"
  const val CONFIRM_BUTTON_LABEL = "Confirm Points"

  // -------------------
  // Layout & Padding
  // -------------------
  val BottomPadding: Dp = 16.dp

  // -------------------
  // Dialog Strings
  // -------------------
  const val DIALOG_TITLE = "Give your checkpoint a name and an optional description."
  const val DESCRIPTION = "Description"
  const val ADD = "Add"
  const val CANCEL = "Cancel"
  const val PLACEHOLDER = "e.g. Louvre museum"
  const val POINTS_NAME = "Point's name"
  const val NOT_EMPTY = "The name cannot be empty"
}

/** Test tags used in the Add Points Map screen for UI testing. */
object AddPointsMapScreenTestTags {

  // -------------------
  // Buttons
  // -------------------
  const val CONFIRM_BUTTON = "ConfirmButton"
  const val CANCEL_BUTTON = "CancelButton"

  // -------------------
  // Input Fields
  // -------------------
  const val POINT_NAME_FIELD = "PointNameField"
  const val POINT_DESCRIPTION_FIELD = "PointDescriptionField"

  // -------------------
  // Views
  // -------------------
  const val MAP_VIEW = "MapView"
}
