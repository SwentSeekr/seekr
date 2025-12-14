package com.swentseekr.seekr.model.hunt

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestoreConstantsDefault.ZERO
import com.swentseekr.seekr.model.map.Location
import kotlin.String
import kotlinx.coroutines.tasks.await

const val HUNTS_COLLECTION_PATH = "hunts"

class HuntsRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val imageRepo: IHuntsImageRepository = HuntsImageRepository()
) : HuntsRepository {
  override fun getNewUid(): String {
    return db.collection(HUNTS_COLLECTION_PATH).document().id
  }

  override suspend fun getAllHunts(): List<Hunt> {
    val snapshot = db.collection(HUNTS_COLLECTION_PATH).get().await()
    return snapshot.mapNotNull { documentToHunt(it) }
  }

  override suspend fun getAllMyHunts(authorID: String): List<Hunt> {
    val snapshot =
        db.collection(HUNTS_COLLECTION_PATH)
            .whereEqualTo(HuntsRepositoryFirestoreConstantsStrings.AUTHOR_ID, authorID)
            .get()
            .await()
    return snapshot.mapNotNull { documentToHunt(it) }
  }

  override suspend fun getHunt(huntID: String): Hunt {
    val document = db.collection(HUNTS_COLLECTION_PATH).document(huntID).get().await()
    return documentToHunt(document)
        ?: throw IllegalArgumentException(
            "${HuntsRepositoryFirestoreConstantsStrings.HUNT} $huntID ${HuntsRepositoryFirestoreConstantsStrings.NOT_FOUND}")
  }

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

  override suspend fun deleteHunt(huntID: String) {
    imageRepo.deleteAllHuntImages(huntID)
    db.collection(HUNTS_COLLECTION_PATH).document(huntID).delete().await()
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
