package com.swentseekr.seekr.model.profile

/** Shared constants for the `ProfileRepositoryFirebaseTest`. */
object Constants {
  const val HUNT_UID = "hunt1"
  const val HUNT_TITLE = "Sample Hunt"
  const val HUNT_DESCRIPTION = "Test Hunt"
  const val AUTHOR_UID = "author1"
  const val AUTHOR_NAME = "Tester"
  const val AUTHOR_BIO = "This is a bio"
  const val REVIEW_RATE = 4.5
  const val SPORT_RATE = 4.0
  const val PROFILE_PICTURE = 0
  const val NEW_USER_NAME = "New User"
  const val MISSING_UID = "unknown_user"
  const val DEFAULT_PROFILE_NAME = "CompleteUser"
  const val DEFAULT_PROFILE_BIO = "Complete bio"
  const val DONE_HUNT_UID = "hunt1"
  const val IMAGE_URL = "http://image.url"
  const val DIFFICULTY_EASY = "EASY"
  const val HUNT_STATUS_FUN = "FUN"
  const val UNCHECKED_CAST = "UNCHECKED_CAST"
  const val LIKED_HUNTS = "likedHunts"
  const val PROFILES = "profiles"
  const val UID = "uid"
  const val ZERO = 0
  const val ONE = 1
  const val PROFILE_RETRIEVE = "Profile should be retrieved after creation"
  const val FIREBASE_USER_NOT_NULL = "FirebaseAuth currentUser should not be null"
  const val DEFAULT_PROFILE_CREATION = "Default profile should be auto-created if missing"
  const val PROFILE_NULL_IF_FIELD_MISS = "Profile should be null if author field is missing"
  const val PATH_HUNT = "hunts"
  const val PATH_PROFILE = "profiles"
  const val WITHOUT_PROFILE_ONBOARDING = "User without profile should need onboarding"
  const val PROFILE_EXIST_CREATE = "Profile should exist or be auto-created"
  const val PROFILE_NOT_ONBOARDING = "User should not have completed onboarding initially"
}
