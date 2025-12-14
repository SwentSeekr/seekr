package com.swentseekr.seekr.ui.hunt.preview

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object PreviewHuntStrings {
  const val PREVIEW_TITLE = "Preview hunt"
  const val HUNT_TITLE = "Hunt Title: "
  const val HUNT_DESCRIPTION = "Hunt Description"
  const val HUNT_TIME = "Estimated Time: "
  const val HUNT_DISTANCE = "Distance in Km: "
  const val HUNT_DIFFICULTY = "Difficulty: "
  const val HUNT_STATUS = "Status: "
  const val HUNT_POINTS = "Points selected: "
  const val OTHER_IMAGES = "Other Images: "
  const val NOT_SET = "Not set"

  const val HUNT_TITLE_FALLBACK = "Untitled hunt"
  const val AUTHOR_PREVIEW = "you"
  const val NO_DESCRIPTION = "No description provided yet."
  const val BACK_CONTENT_DESC = "Go back"

  const val CONFIRM_BUTTON = "Confirm"
  const val TIME_BLANK = ""
  const val DETAILS_HUNT = "Hunt Details"
  const val PREVIEW_LOCATION = "Preview Location"
  const val HUNT_ID = "preview"
  const val AUTHOR_ID = "preview"
}

object PreviewHuntConstantsDefault {
  const val DEFAULT_VALUE_TIME: Double = 0.0
  const val DEFAULT_VALUE_DISTANCE: Double = 0.0
  const val DEFAULT_VALUE_RATING: Double = 0.0
  const val DEFAULT_LATITUDE: Double = 0.0
  const val DEFAULT_LONGITUDE: Double = 0.0
  const val MIDDLE_VALUE_MIN_VALUE = 2
  const val ONE = 1
}

object PreviewHuntUi {
  // Only keep what is actually used in PreviewHuntScreen:
  val SMALL_SPACER_HEIGHT: Dp = 8.dp

  // Thumbnails:
  val THUMBNAIL_SIZE: Dp = 56.dp
  val THUMBNAIL_SPACING: Dp = 8.dp
}

object PreviewHuntScreenTestTags {
  const val PREVIEW_HUNT_SCREEN = "previewHuntScreen"
  const val BACK_BUTTON = "previewHuntBackButton"
  const val CONFIRM_BUTTON = "previewHuntConfirmButton"

  const val HUNT_TITLE = "previewHuntTitle"
  const val HUNT_AUTHOR_PREVIEW = "previewHuntAuthor"
  const val HUNT_DESCRIPTION = "previewHuntDescription"
  const val HUNT_TIME = "previewHuntTime"
  const val HUNT_DISTANCE = "previewHuntDistance"
  const val HUNT_DIFFICULTY = "previewHuntDifficulty"
  const val HUNT_STATUS = "previewHuntStatus"
  const val HUNT_POINTS = "previewHuntPoints"
}
