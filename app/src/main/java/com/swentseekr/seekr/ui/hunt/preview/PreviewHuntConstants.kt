package com.swentseekr.seekr.ui.hunt.preview

/** Contains all string constants used in the Preview Hunt screen. */
object PreviewHuntStrings {
  // -----------------
  // Titles and labels
  // -----------------
  const val PREVIEW_TITLE = "Preview hunt"
  const val HUNT_TITLE = "Hunt Title: "
  const val HUNT_DESCRIPTION = "Hunt Description"
  const val HUNT_STATUS = "Status: "
  const val HUNT_POINTS = "Points selected: "
  const val DETAILS_HUNT = "Hunt Details"

  // -----------------
  // Fallbacks and defaults
  // -----------------
  const val NOT_SET = "Not set"
  const val HUNT_TITLE_FALLBACK = "Untitled hunt"
  const val AUTHOR_PREVIEW = "you"
  const val NO_DESCRIPTION = "No description provided yet."

  // -----------------
  // Navigation and buttons
  // -----------------
  const val BACK_CONTENT_DESC = "Go back"
  const val CONFIRM_BUTTON = "Confirm"
  const val TIME_BLANK = ""

  // -----------------
  // Preview-specific constants
  // -----------------
  const val PREVIEW_LOCATION = "Preview Location"
  const val HUNT_ID = "preview"
  const val AUTHOR_ID = "preview"
}

object PreviewHuntConstantsDefault {
  // -----------------
  // Default values
  // -----------------
  const val DEFAULT_VALUE_TIME: Double = 0.0
  const val DEFAULT_VALUE_DISTANCE: Double = 0.0
  const val DEFAULT_VALUE_RATING: Double = 0.0

  // -----------------
  // Default coordinates
  // -----------------
  const val DEFAULT_LATITUDE: Double = 0.0
  const val DEFAULT_LONGITUDE: Double = 0.0

  // -----------------
  // Indexing helpers
  // -----------------
  const val MIDDLE_VALUE_MIN_VALUE = 2
  const val ONE = 1
}

/** Contains default numeric constants used in the Preview Hunt screen. */
object PreviewHuntScreenTestTags {
  // -----------------
  // Screen-level tags
  // -----------------
  const val PREVIEW_HUNT_SCREEN = "previewHuntScreen"
  const val BACK_BUTTON = "previewHuntBackButton"
  const val CONFIRM_BUTTON = "previewHuntConfirmButton"

  // -----------------
  // UI element tags
  // -----------------
  const val HUNT_TITLE = "previewHuntTitle"
  const val HUNT_AUTHOR_PREVIEW = "previewHuntAuthor"
  const val HUNT_DESCRIPTION = "previewHuntDescription"
  const val HUNT_TIME = "previewHuntTime"
  const val HUNT_DISTANCE = "previewHuntDistance"
  const val HUNT_STATUS = "previewHuntStatus"
  const val HUNT_POINTS = "previewHuntPoints"
}
