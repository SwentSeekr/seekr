package com.swentseekr.seekr.model.hunt

/**
 * Constants used by [ReviewImageRepository].
 *
 * Provides Firebase Storage paths, file formats, logging tags, and error messages for managing
 * review images.
 */
object ReviewImageRepositoryConstants {
  const val REVIEW_IMAGES_COLLECTION = "review_images"
  const val FORMAT_JPG = ".jpg"
  const val TAG = "ReviewImageRepository"
  const val UPLOAD_IMAGE_ERROR = "Failed to upload review image for user"
  const val DELETE_IMAGE_ERROR = "Failed to delete image at"
  const val PATH_LOCALE = "local://review_image/"
}
