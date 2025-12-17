package com.swentseekr.seekr.ui.hunt.review

import androidx.compose.ui.unit.dp

/** Constant string values and test tags used in [ReviewImagesScreen] and related components. */
object ReviewImagesScreenConstantsStrings {

  // -------------------
  // Top bar
  // -------------------
  const val TITLE = "Images Review"
  const val BACK_BUTTON_TAG = "back_button"
  const val BACK_BUTTON_DESCRIPTION = "Back"
  const val TOP_BAR_TEST_TAG = "TOP_BAR_TEST_TAG"

  // -------------------
  // Main screen
  // -------------------
  const val REVIEW_IMAGES_SCREEN_TEST_TAG = "REVIEW_IMAGES_SCREEN"
  const val REVIEW_IMAGES_COLUMN_TEST_TAG = "REVIEW_IMAGES_COLUMN"

  // -------------------
  // Pager and images
  // -------------------
  const val REVIEW_IMAGE_PAGER_TEST_TAG = "ReviewImagePager"
  const val REVIEW_IMAGE_BOX_TEST_TAG = "ReviewImageBox_"
  const val REVIEW_IMAGE_ASYC_TEST_TAG = "ReviewImage_"
  const val REVIEW_IMAGE_TEXT_BOTTOM_TEST_TAG = "ReviewImageIndexText"

  // -------------------
  // Full-screen viewer
  // -------------------
  const val REVIEW_IMAGE_FULL_SCREEN_DIALOG_TEST_TAG = "FullScreenImageDialog"
  const val REVIEW_IMAGE_FULL_SCREEN_PAGER_TEST_TAG = "FullScreenImagePager"
  const val REVIEW_IMAGE_FULL_SCREEN_IMAGE_DESCRIPTION = "Fullscreen image"
  const val CLOSE_CONTENT_DESCRIPTION = "Close"
}

/** Numeric and dimension constants used in [ReviewImagesScreen] and related components. */
object ReviewImagesScreenConstants {

  // -------------------
  // Layout padding and spacing
  // -------------------
  val PaddingColumn = 16.dp
  val PaddingImage = 8.dp
  val SpacerHeight = 12.dp

  // -------------------
  // Image and pager dimensions
  // -------------------
  val ImageCornerRadius = 8.dp
  val PagerHeight = 500.dp
  val RoundShape = 16.dp
  val ImageSize = 400.dp

  // -------------------
  // Full-screen viewer settings
  // -------------------
  const val START_INDEX = 0
  const val ONE = 1
  const val USE_PLATFORM_DEFAULT_WIDTH = false
  const val FULL_SCREEN_OVERLAY_START_ALPHA = 0.6f
  val FullScreenPadding = 8.dp
}
