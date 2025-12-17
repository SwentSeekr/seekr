package com.swentseekr.seekr.ui.profile

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * UI constants used by the Profile Reviews screen.
 *
 * Centralizes layout spacing, string formats, route templates, and numeric constants required to
 * render review summaries and review lists.
 *
 * Prevents duplication of magic numbers and hardcoded strings across composables.
 */
object ProfileReviewsScreenConstant {

  // -----------
  // Spacing & layout
  // -----------
  val Padding16: Dp = 16.dp
  val Padding08: Dp = 8.dp
  const val SPACER_HEIGHT = 4
  const val HORIZONTAL_DIVIDER_PADDING = 8
  const val COLUMN_PADDING = 16

  // -----------
  // Review counts & formatting
  // -----------
  const val ONE_REVIEW = 1
  const val SINGLE_REVIEW = "%s/%s - %d review"
  const val MULTIPLE_REVIEWS = "%s/%s - %d reviews"
  const val STRING_FORMAT = "%.1f"

  // -----------
  // Keys & routes
  // -----------
  const val HEADER_KEY = "hunt_header_%s"
  const val DETAIL_ROUTE = "hunt/%s"
  const val RATING_FORMAT = "%s/%s"
  const val DIVIDER = "divider_%s"

  // -----------
  // Static strings
  // -----------
  const val REVIEWS = "Reviews"
  const val NO_REVIEW = "No reviews yet"
  const val BACK = "Back"
}

/**
 * Semantic test tags for the Profile Reviews screen.
 *
 * Used in Compose UI tests to identify UI elements deterministically.
 */
object ProfileReviewsTestTags {

  // -----------
  // Screen & state
  // -----------
  const val SCREEN = "PROFILE_REVIEWS_SCREEN"
  const val LOADING = "PROFILE_REVIEWS_LOADING"

  // -----------
  // Top bar
  // -----------
  const val TOP_BAR = "PROFILE_REVIEWS_TOP_BAR"
  const val BACK_BUTTON = "PROFILE_REVIEWS_BACK_BUTTON"

  // -----------
  // Rating summary
  // -----------
  const val RATING_SUMMARY = "PROFILE_REVIEWS_RATING_SUMMARY"
  const val RATING_TEXT = "PROFILE_REVIEWS_RATING_TEXT"

  // -----------
  // Reviews list
  // -----------
  const val DIVIDER = "PROFILE_REVIEWS_DIVIDER"
  const val REVIEWS_LIST = "PROFILE_REVIEWS_LIST"
  const val EMPTY_MESSAGE = "PROFILE_REVIEWS_EMPTY_MESSAGE"

  /**
   * Builds a stable test tag for a specific review card.
   *
   * @param reviewId Unique identifier of the review.
   * @return Test tag for the review card.
   */
  fun reviewCardTag(reviewId: String) = "REVIEW_CARD_$reviewId"
}
