package com.swentseekr.seekr.model.hunt

object HuntsRepositoryFirestoreConstantsStrings {
  const val AUTHOR_ID = "authorId"
  const val HUNT = "Hunt with ID"
  const val NOT_FOUND = "is not found"
  const val TAG = "HuntsRepositoryFirestore"
  const val ERROR_IMAGE_UPLOADING = "Image upload failed for hunt"
  const val ERROR_CLEANUP = "Cleanup after failed upload failed"
  const val START = "start"
  const val LATITUDE = "latitude"
  const val LONGITUDE = "longitude"
  const val NAME = "name"
  const val DESCRIPTION = "description"
  const val IMAGE_INDEX = "imageIndex"
  const val END = "end"
  const val MIDDLE_POINT = "middlePoints"
  const val STATUS = "status"
  const val TITLE = "title"
  const val TIME = "time"
  const val DISTANCE = "distance"
  const val DIFFICULTY = "difficulty"
  const val MAIN_IMAGE = "mainImageUrl"
  const val OTHER_IMAGE = "otherImagesUrls"
  const val RATING_REVIEW = "reviewRate"
  const val ERROR_CONVERTING = "Error converting document to Hunt"
  const val HUNT_REVIEW_REPLY_COLLECTION_PATH = "huntReviewReplies"
  const val FIELD_HUNT_ID = "huntId"
  const val FIRESTORE_PHOTOS_FIELD = "photos"
  const val FAILED_REVIEW_PHOTO_DELETION = "Failed to delete review photo: "
  const val FIELD_HUNT_IMAGE_DELETION = "Failed to delete hunt images for hunt ID= "
  const val HUNTS_COLLECTION_PATH = "hunts"
}

object HuntsRepositoryFirestoreConstantsDefault {
  const val ZERO: Double = 0.0
}
