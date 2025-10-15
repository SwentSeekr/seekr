package com.swentseekr.seekr.model.hunt

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.map.Location
import kotlin.String
import kotlinx.coroutines.tasks.await

const val HUNTS_COLLECTION_PATH = "hunts"

class HuntsRepositoryFirestore(private val db: FirebaseFirestore) : HuntsRepository {
  override fun getNewUid(): String {
    return db.collection(HUNTS_COLLECTION_PATH).document().id
  }

  override suspend fun getAllHunts(): List<Hunt> {
    val currentUserId =
        FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not logged in")
    val snapshot =
        db.collection(HUNTS_COLLECTION_PATH).whereEqualTo("ownerId", currentUserId).get().await()
    return snapshot.mapNotNull { documentToHunt(it) }
  }

  override suspend fun getHunt(huntID: String): Hunt {
    val document = db.collection(HUNTS_COLLECTION_PATH).document(huntID).get().await()
    return documentToHunt(document)
        ?: throw IllegalArgumentException("Hunt with ID $huntID is not found")
  }

  override suspend fun addHunt(hunt: Hunt) {
    db.collection(HUNTS_COLLECTION_PATH).document(hunt.uid).set(hunt).await()
  }

  override suspend fun editHunt(huntID: String, newValue: Hunt) {
    db.collection(HUNTS_COLLECTION_PATH).document(huntID).set(newValue).await()
  }

  override suspend fun deleteHunt(huntID: String) {
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
      val locationData = document.get("location") as? Map<*, *>
      val start =
          locationData?.let {
            Location(
                latitude = it["latitude"] as? Double ?: 0.0,
                longitude = it["longitude"] as? Double ?: 0.0,
                name = it["name"] as? String ?: "")
          } ?: Location(0.0, 0.0, "")
      val end =
          locationData?.let {
            Location(
                latitude = it["latitude"] as? Double ?: 0.0,
                longitude = it["longitude"] as? Double ?: 0.0,
                name = it["name"] as? String ?: "")
          } ?: Location(0.0, 0.0, "")
      val statusString = document.getString("status") ?: return null
      val status = HuntStatus.valueOf(statusString)
      val title = document.getString("title") ?: return null
      val description = document.getString("description") ?: return null
      val time = document.getDouble("time") ?: return null
      val distance = document.getDouble("distance") ?: return null
      val difficultyString = document.getString("difficulty") ?: return null
      val difficulty = Difficulty.valueOf(difficultyString)
      val author =
          Author(
              pseudonym = document.getString("pseudonym") ?: return null,
              bio = document.getString("bio") ?: return null,
              profilePicture = document.getDouble("profilePicture")?.toInt() ?: return null,
              reviewRate = document.getDouble("reviewRate") ?: return null,
              sportRate = (document.getDouble("sportRate") ?: return null))
      val image = document.getDouble("image")?.toInt() ?: return null
      val reviewRate = document.getDouble("reviewRate") ?: return null

      Hunt(
          uid = uid,
          start = start,
          end = end,
          middlePoints = emptyList(),
          status = status,
          title = title,
          description = description,
          time = time,
          distance = distance,
          difficulty = difficulty,
          author = author,
          image = image,
          reviewRate = reviewRate)
    } catch (e: Exception) {
      Log.e("HuntsRepositoryFirestore", "Error converting document to Hunt", e)
      null
    }
  }
}
