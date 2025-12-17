package com.swentseekr.seekr.ui.profile

/** Constants used by ProfileViewModel. */
object ProfileViewModelConstants {
  // -----------
  // Test tags
  // -----------

  const val PROFILE_VIEW_MODEL_TEST_TAG = "ProfileViewModel"
  const val PROFILE_TEST_TAG = "PROFILE"

  // -----------
  // Error messages
  // -----------

  const val FAIL_LOAD_PROFILE = "Failed to load profile"
  const val FAIL_LOAD_HUNTS = "Failed to load hunts"
  const val FAIL_LOAD_REVIEWS = "Failed to load reviews for hunt"
  const val FAIL_LIKE = "Failed to toggle liked hunt"

  // -----------
  // Not-found messages
  // -----------

  const val PROFILE_NOT_FOUND = "Profile not found"
  const val NOT_FOUND = "not found"
}

/** Numeric constants used by ProfileViewModel for rating calculations. */
object ProfileViewModelNumbers {

  // -----------
  // Review rating bounds
  // -----------

  const val MIN_REVIEW_RATE = 0.0
  const val MAX_REVIEW_RATE = 5.0

  // -----------
  // Sport rating bounds
  // -----------

  const val MIN_SPORT_RATE = 0.0
  const val MAX_SPORT_RATE = 5.0

  // -----------
  // Sport difficulty thresholds
  // -----------

  const val EASY_SPORT_RATE = 1.0
  const val INTERMEDIATE_SPORT_RATE = 3.0
  const val DIFFICULT_SPORT_RATE = 5.0
}
