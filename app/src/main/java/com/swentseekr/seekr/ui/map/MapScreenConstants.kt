package com.swentseekr.seekr.ui.map

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object MapScreenDefaults {
  const val ZERO_FLOAT: Float = 0f
  const val ZERO_INT: Int = 0
  const val ONE_FLOAT: Float = 1f
  val CardPadding: Dp = 6.dp
  val PopupSpacing: Dp = 6.dp
  val PopupImageSize: Dp = 125.dp
  val PopupImageCornerRadius: Dp = 8.dp
  const val ChipBackgroundAlpha: Float = 0.25f
  const val ChipContentDarkenFactor: Float = 0.7f
  const val BackgroundOpacity: Float = 0.9f

  val ChipHorizontalPadding: Dp = 8.dp
  val ChipVerticalPadding: Dp = 4.dp
  val ChipCornerRadius: Dp = 50.dp
  const val UnitPointSize = 1
  const val MaxLines = 2
  const val MinScore = 2

  const val UserLocationZoom = 16f
  const val FocusedZoom = 15f
  const val BoundsPadding = 100
  const val RouteStrokeWidth = 12f

  val MarkerImageSize = 50.dp
  val MarkerCornerRadius = 8.dp

  val OverlayPadding: Dp = 32.dp
  val OverlayInnerPadding: Dp = 24.dp
  val CardCornerRadius: Dp = 16.dp
  val CardElevation: Dp = 8.dp
  val BackButtonPadding: Dp = 12.dp
  val ProgressBarHeight: Dp = 6.dp
  val ProgressTickSpacing: Dp = 4.dp
  val ProgressSegmentCornerRadius: Dp = 4.dp
  val CustomMarkerBorderWidth: Float = 5f
  val IconPadding: Dp = 16.dp
  val IconSize: Dp = 36.dp
  val IconBackground: Float = 0.6f
}

object MapConfig {
  const val DefaultLat = 46.519962
  const val DefaultLng = 6.633597
  const val DefaultCityName = "Lausanne"

  const val DirectionsConnectTimeoutMs = 15_000
  const val DirectionsReadTimeoutMs = 15_000
  const val DirectionsBaseUrl = "https://maps.googleapis.com/maps/api/directions/json"
  const val TravelModeWalking = "walking"

  const val DefaultValidatedCount = 0
  const val ValidationRadiusMeters = 25
  const val LOCATION_UPDATE_INTERVAL_MS = 2000L
  const val LOCATION_FASTEST_INTERVAL_MS = 1000L
}

object MapScreenStrings {
  const val StartPrefix = "Start: "
  const val EndPrefix = "End: "
  const val HuntImageDescriptionSuffix = " image"
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

  const val NextStopPrefix = "Next stop: "
  const val DistanceMetersSuffix = " m"
  const val ErrorTooFarPrefix = "You are too far from the checkpoint. Minimum distance: "

  const val ErrorLoadHuntsPrefix = "Failed to load hunts: "
  const val ErrorRoutePrefix = "Failed to get route: "
  const val ErrorFinishHuntPrefix = "Failed to finish hunt: "
  const val ErrorIncompleteHunt = "You still have checkpoints to validate."

  const val Fail = "Failed to add done hunt"
  const val IN = " in "
  const val SLASH = " / "
}

object MapScreenTestTags {
  const val GOOGLE_MAP_SCREEN = "mapScreen"
  const val POPUP_CARD = "huntPopupCard"
  const val POPUP_TITLE = "huntPopupTitle"
  const val POPUP_DESC = "huntPopupDesc"
  const val POPUP_IMAGE = "huntPopupImage"
  const val POPUP_META_ROW = "huntPopupMetaRow"
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
  const val NEXT_CHECKPOINT_IMAGE = "nextCheckpointImage"
  const val CLOSE_CHECKPOINT_IMAGE = "closeCheckpointImage"
}
