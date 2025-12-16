package com.swentseekr.seekr.model.hunt

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreConstantsDefault.ZERO
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreConstantsStrings.FIELD_HUNT_ID
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreConstantsStrings.HUNT_REVIEW_REPLY_COLLECTION_PATH
import com.swentseekr.seekr.model.hunt.review.HuntReviewReplyFirestoreConstants.FIELD_REVIEW_ID
import com.swentseekr.seekr.model.map.Location
import kotlin.String
import kotlinx.coroutines.tasks.await

const val HUNTS_COLLECTION_PATH = "hunts"

/**
 * Firestore implementation of [HuntsRepository] that manages persistence of [Hunt] objects in
 * Firestore and handles associated image uploads and deletions via [IHuntsImageRepository].
 *
 * @property db Firestore instance used for hunt persistence.
 * @property imageRepo Repository used for managing hunt images in Firebase Storage.
 */
class HuntsRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val imageRepo: IHuntsImageRepository = HuntsImageRepository(),
    private val reviewImageRepo: IReviewImageRepository = ReviewImageRepository()
) : HuntsRepository {

  /**
   * Generates a new unique hunt identifier using Firestore.
   *
   * @return A unique string suitable for use as a hunt ID.
   */
  override fun getNewUid(): String {
    return db.collection(HUNTS_COLLECTION_PATH).document().id
  }

  /**
   * Retrieves all hunts stored in Firestore.
   *
   * @return A list of all valid [Hunt] objects.
   */
  override suspend fun getAllHunts(): List<Hunt> {
    val snapshot = db.collection(HUNTS_COLLECTION_PATH).get().await()
    return snapshot.mapNotNull { documentToHunt(it) }
  }

  /**
   * Retrieves all hunts created by a specific author.
   *
   * @param authorID The ID of the hunt author.
   * @return A list of hunts authored by the given user.
   */
  override suspend fun getAllMyHunts(authorID: String): List<Hunt> {
    val snapshot =
        db.collection(HUNTS_COLLECTION_PATH)
            .whereEqualTo(HuntsRepositoryFirestoreConstantsStrings.AUTHOR_ID, authorID)
            .get()
            .await()
    return snapshot.mapNotNull { documentToHunt(it) }
  }

  /**
   * Retrieves a single hunt by its unique ID.
   *
   * @param huntID The ID of the hunt to retrieve.
   * @return The corresponding [Hunt].
   * @throws IllegalArgumentException if the hunt does not exist.
   */
  override suspend fun getHunt(huntID: String): Hunt {
    val document = db.collection(HUNTS_COLLECTION_PATH).document(huntID).get().await()
    return documentToHunt(document)
        ?: throw IllegalArgumentException(
            "${HuntsRepositoryFirestoreConstantsStrings.HUNT} $huntID ${HuntsRepositoryFirestoreConstantsStrings.NOT_FOUND}")
  }

  /**
   * Adds a new hunt to Firestore and uploads associated images.
   *
   * Image uploads are performed before writing to Firestore.
   *
   * @param hunt The hunt data to store.
   * @param mainImageUri Optional URI of the main image.
   * @param otherImageUris Optional list of URIs for additional images.
   * @throws Exception if image upload fails.
   */
  override suspend fun addHunt(hunt: Hunt, mainImageUri: Uri?, otherImageUris: List<Uri>) {
    var mainImageUrl = ""
    var otherImagesUrls: List<String> = emptyList()

    try {
      // Upload main image if provided
      if (mainImageUri != null) {
        mainImageUrl = imageRepo.uploadMainImage(hunt.uid, mainImageUri)
      }

      // Upload other images if provided
      if (otherImageUris.isNotEmpty()) {
        otherImagesUrls = imageRepo.uploadOtherImages(hunt.uid, otherImageUris)
      }
    } catch (e: Exception) {
      Log.e(
          HuntsRepositoryFirestoreConstantsStrings.TAG,
          "${HuntsRepositoryFirestoreConstantsStrings.ERROR_IMAGE_UPLOADING} ${hunt.uid}",
          e)

      // Optionally clean up any uploaded images if partial success occurred
      try {
        imageRepo.deleteAllHuntImages(hunt.uid)
      } catch (cleanupError: Exception) {
        Log.w(
            HuntsRepositoryFirestoreConstantsStrings.TAG,
            HuntsRepositoryFirestoreConstantsStrings.ERROR_CLEANUP,
            cleanupError)
      }

      // Rethrow to signal failure to upper layers (so ViewModel can show an error)
      throw e
    }

    // Only write to Firestore if uploads succeeded
    val huntWithImages = hunt.copy(mainImageUrl = mainImageUrl, otherImagesUrls = otherImagesUrls)
    db.collection(HUNTS_COLLECTION_PATH).document(hunt.uid).set(huntWithImages).await()
  }

  /**
   * Updates an existing hunt and manages image changes.
   *
   * @param huntID The ID of the hunt to update.
   * @param newValue The updated hunt data.
   * @param mainImageUri Optional new main image.
   * @param addedOtherImages New secondary images to upload.
   * @param removedOtherImages URLs of images to delete.
   * @param removedMainImageUrl URL of the main image to delete, if any.
   */
  override suspend fun editHunt(
      huntID: String,
      newValue: Hunt,
      mainImageUri: Uri?,
      addedOtherImages: List<Uri>,
      removedOtherImages: List<String>,
      removedMainImageUrl: String?
  ) {

    removedOtherImages.forEach { url -> imageRepo.deleteImageByUrl(url) }

    if (removedMainImageUrl != null) {
      imageRepo.deleteImageByUrl(removedMainImageUrl)
    }

    var finalMainImageUrl = newValue.mainImageUrl
    if (mainImageUri != null) {
      finalMainImageUrl = imageRepo.uploadMainImage(huntID, mainImageUri)
    }

    val newOtherImageUrls =
        if (addedOtherImages.isNotEmpty()) imageRepo.uploadOtherImages(huntID, addedOtherImages)
        else emptyList()

    val remainingOldImages = newValue.otherImagesUrls.filterNot { it in removedOtherImages }

    val finalOtherImages = remainingOldImages + newOtherImageUrls

    val updatedHunt =
        newValue.copy(mainImageUrl = finalMainImageUrl, otherImagesUrls = finalOtherImages)

    db.collection(HUNTS_COLLECTION_PATH).document(huntID).set(updatedHunt).await()
  }

  /**
   * Deletes a hunt and all its associated images.
   *
   * @param huntID The ID of the hunt to delete.
   */
  override suspend fun deleteHunt(huntID: String) {
    // 1) Load all reviews for this hunt
    val reviewsSnapshot =
        db.collection(HUNT_REVIEW_COLLECTION_PATH).whereEqualTo(FIELD_HUNT_ID, huntID).get().await()

    // Map to structured data so we can delete storage photos too
    val reviews =
        reviewsSnapshot.documents.mapNotNull { doc ->
          val reviewId = doc.id
          val photos = (doc.get("photos") as? List<*>)?.filterIsInstance<String>().orEmpty()
          reviewId to photos
        }

    // 2) For each review: delete replies, delete review photos, delete review doc
    for ((reviewId, photoUrls) in reviews) {
      // 2a) delete replies belonging to this review
      deleteRepliesForReview(reviewId)

      // 2b) delete review photos in storage
      photoUrls.forEach { url -> reviewImageRepo.deleteReviewPhoto(url) }

      // 2c) delete review document itself
      db.collection(HUNT_REVIEW_COLLECTION_PATH).document(reviewId).delete().await()
    }

    // 3) delete hunt images in storage
    imageRepo.deleteAllHuntImages(huntID)

    // 4) delete hunt document
    db.collection(HUNTS_COLLECTION_PATH).document(huntID).delete().await()
  }

  private suspend fun deleteRepliesForReview(reviewId: String) {
    val query =
        db.collection(HUNT_REVIEW_REPLY_COLLECTION_PATH).whereEqualTo(FIELD_REVIEW_ID, reviewId)

    var snapshot = query.get().await()

    while (!snapshot.isEmpty) {
      val batch = db.batch()

      // Firestore batch hard limit is 500 writes; chunk to be safe
      snapshot.documents.take(450).forEach { doc -> batch.delete(doc.reference) }

      batch.commit().await()

      snapshot = query.get().await()
    }
  }

  /**
   * Converts a Firestore document to a Hunt object.
   *
   * @param document The Firestore document to convert.
   * @return The Hunt object.
   */
  private fun documentToHunt(document: DocumentSnapshot): Hunt? {
    return try {
      val uid = document.id

      val startData = document.get(HuntsRepositoryFirestoreConstantsStrings.START) as? Map<*, *>
      val start =
          startData?.let {
            Location(
                latitude = it[HuntsRepositoryFirestoreConstantsStrings.LATITUDE] as? Double ?: ZERO,
                longitude =
                    it[HuntsRepositoryFirestoreConstantsStrings.LONGITUDE] as? Double ?: ZERO,
                name = it[HuntsRepositoryFirestoreConstantsStrings.NAME] as? String ?: "",
                description =
                    it[HuntsRepositoryFirestoreConstantsStrings.DESCRIPTION] as? String ?: "",
                imageIndex =
                    (it[HuntsRepositoryFirestoreConstantsStrings.IMAGE_INDEX] as? Long)?.toInt())
          } ?: Location(0.0, 0.0, "")

      val endData = document.get(HuntsRepositoryFirestoreConstantsStrings.END) as? Map<*, *>
      val end =
          endData?.let {
            Location(
                latitude = it[HuntsRepositoryFirestoreConstantsStrings.LATITUDE] as? Double ?: ZERO,
                longitude =
                    it[HuntsRepositoryFirestoreConstantsStrings.LONGITUDE] as? Double ?: ZERO,
                name = it[HuntsRepositoryFirestoreConstantsStrings.NAME] as? String ?: "",
                description =
                    it[HuntsRepositoryFirestoreConstantsStrings.DESCRIPTION] as? String ?: "",
                imageIndex =
                    (it[HuntsRepositoryFirestoreConstantsStrings.IMAGE_INDEX] as? Long)?.toInt())
          } ?: Location(ZERO, ZERO, "")
      val middlePointsData =
          document.get(HuntsRepositoryFirestoreConstantsStrings.MIDDLE_POINT) as? List<Map<*, *>>
      val middlePoints =
          middlePointsData?.map {
            Location(
                latitude = it[HuntsRepositoryFirestoreConstantsStrings.LATITUDE] as? Double ?: ZERO,
                longitude =
                    it[HuntsRepositoryFirestoreConstantsStrings.LONGITUDE] as? Double ?: ZERO,
                name = it[HuntsRepositoryFirestoreConstantsStrings.NAME] as? String ?: "",
                description =
                    it[HuntsRepositoryFirestoreConstantsStrings.DESCRIPTION] as? String ?: "",
                imageIndex =
                    (it[HuntsRepositoryFirestoreConstantsStrings.IMAGE_INDEX] as? Long)?.toInt())
          } ?: emptyList()
      val statusString =
          document.getString(HuntsRepositoryFirestoreConstantsStrings.STATUS) ?: return null
      val status = HuntStatus.valueOf(statusString)
      val title = document.getString(HuntsRepositoryFirestoreConstantsStrings.TITLE) ?: return null
      val description =
          document.getString(HuntsRepositoryFirestoreConstantsStrings.DESCRIPTION) ?: return null
      val time = document.getDouble(HuntsRepositoryFirestoreConstantsStrings.TIME) ?: return null
      val distance =
          document.getDouble(HuntsRepositoryFirestoreConstantsStrings.DISTANCE) ?: return null
      val difficultyString =
          document.getString(HuntsRepositoryFirestoreConstantsStrings.DIFFICULTY) ?: return null
      val difficulty = Difficulty.valueOf(difficultyString)
      val authorId =
          document.getString(HuntsRepositoryFirestoreConstantsStrings.AUTHOR_ID) ?: return null
      val mainImageUrl =
          document.getString(HuntsRepositoryFirestoreConstantsStrings.MAIN_IMAGE) ?: ""
      val otherImagesUrls =
          (document.get(HuntsRepositoryFirestoreConstantsStrings.OTHER_IMAGE) as? List<*>)
              ?.filterIsInstance<String>() ?: emptyList()
      val reviewRate =
          document.getDouble(HuntsRepositoryFirestoreConstantsStrings.RATING_REVIEW) ?: return null

      Hunt(
          uid = uid,
          start = start,
          end = end,
          middlePoints = middlePoints,
          status = status,
          title = title,
          description = description,
          time = time,
          distance = distance,
          difficulty = difficulty,
          authorId = authorId,
          mainImageUrl = mainImageUrl,
          otherImagesUrls = otherImagesUrls,
          reviewRate = reviewRate)
    } catch (e: Exception) {
      Log.e(
          HuntsRepositoryFirestoreConstantsStrings.TAG,
          HuntsRepositoryFirestoreConstantsStrings.ERROR_CONVERTING,
          e)
      null
    }
  }
}
