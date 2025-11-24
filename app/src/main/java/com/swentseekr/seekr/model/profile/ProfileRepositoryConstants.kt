package com.swentseekr.seekr.model.profile

import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntStatus

/**
 * Constants used in [ProfileRepositoryFirestore] and [ProfileRepositoryProvider] to avoid magic
 * values and centralize configuration.
 */
object ProfileRepositoryConstants {

  // Firestore Collection names
  const val PROFILES_COLLECTION = "profiles"
  const val HUNTS_COLLECTION = "hunts"

  // Default Profile values
  const val DEFAULT_USER_NAME = "New User"
  const val DEFAULT_USER_BIO = ""
  const val DEFAULT_PROFILE_PICTURE = 0
  const val DEFAULT_REVIEW_RATE = 0.0
  const val DEFAULT_SPORT_RATE = 0.0

  // Default hunt values
  const val DEFAULT_HUNT_TIME = 0.0
  const val DEFAULT_HUNT_DISTANCE = 0.0
  const val DEFAULT_HUNT_REVIEW_RATE = 0.0
  const val DEFAULT_HUNT_MAIN_IMAGE_URL = ""

  // Default Location values
  const val DEFAULT_LOCATION_LAT = 0.0
  const val DEFAULT_LOCATION_LNG = 0.0
  const val DEFAULT_LOCATION_NAME = ""

  // Default Difficulty and Status values
  val DEFAULT_DIFFICULTY = Difficulty.EASY
  val DEFAULT_STATUS = HuntStatus.FUN

  // Error messages and logging
  const val FIRESTORE_WRITE_FAILED_LOG_TAG = "ProfileRepo"
  const val UPLOAD_FAILED_LOG_TAG = "UploadProfilePicture"

  // Other constants
  const val PROFILE_FIELD_AUTHOR = "author"
  const val PROFILE_FIELD_DONE_HUNTS = "doneHunts"
  const val PROFILE_FIELD_LIKED_HUNTS = "likedHunts"
  const val PROFILE_FIELD_MY_HUNTS = "myHunts"
  const val HUNT_FIELD_UID = "uid"
  const val HUNT_FIELD_AUTHOR_ID = "authorId"
  const val HUNT_FIELD_TITLE = "title"
  const val HUNT_FIELD_DESCRIPTION = "description"
  const val HUNT_FIELD_TIME = "time"
  const val HUNT_FIELD_DISTANCE = "distance"
  const val HUNT_FIELD_REVIEW_RATE = "reviewRate"
  const val HUNT_FIELD_MAIN_IMAGE_URL = "mainImageUrl"
  const val HUNT_FIELD_START = "start"
  const val HUNT_FIELD_END = "end"
  const val HUNT_FIELD_MIDDLE_POINTS = "middlePoints"
  const val HUNT_FIELD_DIFFICULTY = "difficulty"
  const val HUNT_FIELD_STATUS = "status"
  const val LOCATION_FIELD_LATITUDE = "latitude"
  const val LOCATION_FIELD_LONGITUDE = "longitude"
  const val LOCATION_FIELD_NAME = "name"
  const val LOCAL_PROFILE_PICTURE_PREFIX = "local://profile-picture/"
  const val DEFAULT_EMPTY_VALUE = ""
}

/**
 * String templates used in [ProfileRepositoryFirestore] and [ProfileRepositoryProvider] for error
 * messages and logging.
 */
object ProfileRepositoryStrings {
  const val PROFILE_NOT_FOUND = "Profile with ID %s not found"
  const val PROFILE_ALREADY_EXISTS = "Profile with ID %s already exists"
  const val UPLOAD_FAILED = "Failed to upload or update profile picture"
  const val DELETE_FAILED = "Failed to delete profile picture: %s"
  const val FIRESTORE_WRITE_FAILED_MESSAGE = "Firestore write failed"
}
