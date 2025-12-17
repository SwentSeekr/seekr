package com.swentseekr.seekr.ui.hunt

/** General messages displayed on Hunt screens, such as success notifications or default titles. */
object BaseHuntScreenMessages {
  const val DEFAULT_TITLE = "Add your Hunt"
  const val HUNT_SAVED = "Hunt saved successfully!"
}

/**
 * Messages used by the BaseHuntViewModel for validation, errors, and logging. Centralizing these
 * strings ensures consistency and easier maintenance.
 */
object BaseHuntViewModelMessages {

  // -------------------
  // Validation messages
  // -------------------
  const val NOT_ALL_FIELD_FILL = "Please fill all required fields before saving the hunt."
  const val MUST_LOGIN = "You must be logged in to perform this action."
  const val FAIL_BUILD = "Failed to build Hunt from UI state."

  // -------------------
  // Logging & identifiers
  // -------------------
  const val BASE_VIEW_MODEL = "BaseHuntViewModel"

  // -------------------
  // Error messages
  // -------------------
  const val ERROR_SAVING = "Error saving Hunt"
  const val FAIL_SAVE = "Failed to save Hunt:"

  // -------------------
  // Field-specific validation
  // -------------------
  const val TITLE_EMPTY = "Title cannot be empty"
  const val DESCRIPTION_EMPTY = "Description cannot be empty"
  const val INVALID_TIME = "Invalid time format"
  const val INVALID_DISTANCE = "Invalid distance format"
  const val INVALID_SET_POINT = "A hunt must have at least a start and end point."
}

/** Default constants used by the BaseHuntViewModel for Hunt validation and logic. */
object BaseHuntViewModelDefault {
  const val MIN_SET_POINT = 2
}
