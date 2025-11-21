package com.swentseekr.seekr.ui.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object MapScreenDefaults {

  const val Base = 0
  const val UnitPointSize = 1
  const val MaxLines = 2
  const val MinScore = 2
  const val UserLocationZoom = 16f
  const val FocusedZoom = 15f
  const val BoundsPadding = 100
  const val RouteStrokeWidth = 12f
  val OverlayScrimColor: Color = Color(0x80000000)
  val OverlayPadding: Dp = 32.dp
  val OverlayDoublePadding: Dp = 64.dp
  val OverlayInnerPadding: Dp = 24.dp
  val CardPadding: Dp = 16.dp
  val CardCornerRadius: Dp = 16.dp
  val CardElevation: Dp = 8.dp
  val BackButtonPadding: Dp = 12.dp
  val PopupSpacing: Dp = 8.dp
}

object MapScreenStrings {
  const val StartPrefix = "Start: "
  const val EndPrefix = "End: "
  const val PermissionExplanation =
      "Seekr needs access to your location to display hunts near you on the map!"
  const val GrantPermission = "Grant Location Permission"
  const val BackToAllHunts = "Back to all hunts"
  const val Cancel = "Cancel"
  const val ViewHunt = "View Hunt"
  const val Progress = "Progress: "
  const val StartHunt = "Start Hunt"
  const val Validate = "Validate"
  const val FinishHunt = "Finish hunt"
  const val StopHunt = "Stop hunt"
  const val StopHuntTitle = "Stop current hunt?"
  const val StopHuntMessage =
      "Are you sure you want to stop this hunt? Your current progress will be lost."
  const val ConfirmStopHunt = "Stop"
  const val Fail = "Failed to add done hunt"
}

/**
 * Test tags used by instrumented tests to target key UI elements on the Map screen.
 *
 * These tags are applied to:
 * - [GOOGLE_MAP_SCREEN]: the `GoogleMap` composable root.
 * - [POPUP_CARD], [POPUP_TITLE], [POPUP_DESC]: the bottom popup and its content shown after a
 *   marker tap.
 * - [BUTTON_VIEW], [BUTTON_CANCEL]: actions in the popup.
 * - [BUTTON_BACK]: "Back to all hunts" shown in focused mode.
 */
object MapScreenTestTags {
  const val GOOGLE_MAP_SCREEN = "mapScreen"
  const val POPUP_CARD = "huntPopupCard"
  const val POPUP_TITLE = "huntPopupTitle"
  const val POPUP_DESC = "huntPopupDesc"
  const val BUTTON_CANCEL = "huntPopupCancel"
  const val BUTTON_VIEW = "huntPopupView"
  const val BUTTON_BACK = "backToAllHunts"
  const val MAP_SCREEN = "MapScreen"
  const val PERMISSION_POPUP = "permissionPopup"
  const val GRANT_LOCATION_PERMISSION = "grantLocationPermission"
  const val EXPLAIN = "explain"
  const val START = "start"
  const val PROGRESS = "progress"
  const val VALIDATE = "validate"
  const val FINISH = "finish"
  const val STOP_POPUP = "stopPopup"
  const val CONFIRM = "confirm"
}
