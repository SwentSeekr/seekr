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
  const val CHIP_BACKGROUND_ALPHA: Float = 0.25f
  const val CHIP_CONTENT_DARKEN_FACTOR: Float = 0.7f
  const val BACKGROUND_OPACITY: Float = 0.9f

  val ChipHorizontalPadding: Dp = 8.dp
  val ChipVerticalPadding: Dp = 4.dp
  val ChipCornerRadius: Dp = 50.dp
  const val UNIT_POINT_SIZE = 1
  const val MAX_LINES = 2
  const val MIN_SCORE = 2

  const val USER_LOCATION_ZOOM = 16f
  const val FOCUSED_ZOOM = 15f
  const val BOUNDS_PADDING = 100
  const val ROUTE_STROKE_WIDTH = 12f

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
  const val CUSTOM_MARKER_BORDER_WIDTH: Float = 5f
  val IconPadding: Dp = 16.dp
  val IconSize: Dp = 36.dp
  const val ICON_BACKGROUND_ALPHA: Float = 0.6f
  const val DEFAULT_MIN_POINT = 2
  const val ONE = 1
}

object MapConfig {
  const val DEFAULT_LAT = 46.519962
  const val DEFAULT_LNG = 6.633597
  const val DEFAULT_CITY_NAME = "Lausanne"

  const val DIRECTIONS_CONNECT_TIMEOUT_MS = 15_000
  const val DIRECTIONS_READ_TIMEOUT_MS = 15_000
  const val DIRECTIONS_BASE_URL = "https://maps.googleapis.com/maps/api/directions/json"
  const val TRAVEL_MODE_WALKING = "walking"

  const val DEFAULT_VALIDATED_COUNT = 0
  const val VALIDATION_RADIUS_METERS = 25
  const val LOCATION_UPDATE_INTERVAL_MS = 2000L
  const val LOCATION_FASTEST_INTERVAL_MS = 1000L
}

object MapScreenStrings {
  const val START_PREFIX = "Start: "
  const val END_PREFIX = "End: "
  const val HUNT_IMAGE_DESCRIPTION_SUFFIX = " image"
  const val PERMISSION_EXPLANATION =
      "Seekr needs access to your location to display hunts near you on the map!"
  const val GRANT_PERMISSION = "Grant Location Permission"
  const val BACK_TO_ALL_HUNTS = "Back to all hunts"
  const val CANCEL = "Cancel"
  const val VIEW_HUNT = "View Hunt"
  const val PROGRESS = "Progress: "
  const val START_HUNT = "Start Hunt"
  const val VALIDATE = "Validate"
  const val FINISH_HUNT = "Finish hunt"
  const val STOP_HUNT = "Stop hunt"

  const val STOP_HUNT_TITLE = "Stop current hunt?"
  const val STOP_HUNT_MESSAGE =
      "Are you sure you want to stop this hunt? Your current progress will be lost."
  const val CONFIRM_STOP_HUNT = "Stop"

  const val NEXT_STOP_PREFIX = "Next stop: "
  const val DISTANCE_METERS_SUFFIX = " m"
  const val ERROR_TOO_FAR_PREFIX = "You are too far from the checkpoint. Minimum distance: "

  const val ERROR_LOAD_HUNTS_PREFIX = "Failed to load hunts: "
  const val ERROR_ROUTE_PREFIX = "Failed to get route: "
  const val ERROR_FINISH_HUNT_PREFIX = "Failed to finish hunt: "
  const val ERROR_INCOMPLETE_HUNT = "You still have checkpoints to validate."

  const val FAIL = "Failed to add done hunt"
  const val IN = " in "
  const val SLASH = " / "

  const val ERROR_RESOURCE_ID = "Resource ID"
  const val BEEN_LOADED = "could not be loaded as a drawable."
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
