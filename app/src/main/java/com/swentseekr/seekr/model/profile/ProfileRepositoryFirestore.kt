package com.swentseekr.seekr.model.profile

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestore
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.profile.Profile
import kotlinx.coroutines.tasks.await

class ProfileRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val huntsRepository: HuntsRepositoryFirestore = HuntsRepositoryFirestore(db)
) : ProfileRepository {

  companion object {
    fun huntToMap(hunt: Hunt): Map<String, Any?> =
        mapOf(
            "uid" to hunt.uid,
            "title" to hunt.title,
            "description" to hunt.description,
            "start" to
                mapOf(
                    "latitude" to hunt.start.latitude,
                    "longitude" to hunt.start.longitude,
                    "name" to hunt.start.name),
            "end" to
                mapOf(
                    "latitude" to hunt.end.latitude,
                    "longitude" to hunt.end.longitude,
                    "name" to hunt.end.name),
            "middlePoints" to
                hunt.middlePoints.map {
                  mapOf("latitude" to it.latitude, "longitude" to it.longitude, "name" to it.name)
                },
            "difficulty" to hunt.difficulty.name,
            "status" to hunt.status.name,
            "authorId" to hunt.authorId,
            "time" to hunt.time,
            "distance" to hunt.distance,
            "reviewRate" to hunt.reviewRate,
            "mainImageUrl" to hunt.mainImageUrl)

    fun mapToHunt(map: Map<*, *>): Hunt? {
      val uid = map["uid"] as? String ?: ""
      val title = map["title"] as? String ?: return null
      val description = map["description"] as? String ?: return null
      val time = (map["time"] as? Number)?.toDouble() ?: 0.0
      val distance = (map["distance"] as? Number)?.toDouble() ?: 0.0
      val reviewRate = (map["reviewRate"] as? Number)?.toDouble() ?: 0.0
      val mainImageUrl = map["mainImageUrl"] as? String ?: ""

      val start = (map["start"] as? Map<*, *>)?.toLocation() ?: Location(0.0, 0.0, "")
      val end = (map["end"] as? Map<*, *>)?.toLocation() ?: Location(0.0, 0.0, "")
      val middlePoints =
          (map["middlePoints"] as? List<Map<*, *>>)?.map { it.toLocation() } ?: emptyList()

      val difficulty =
          (map["difficulty"] as? String)?.let { Difficulty.valueOf(it) } ?: Difficulty.EASY
      val status = (map["status"] as? String)?.let { HuntStatus.valueOf(it) } ?: HuntStatus.FUN
      val authorId = map["authorId"] as? String ?: ""

      return Hunt(
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
          reviewRate = reviewRate)
    }

    private fun Map<*, *>.toLocation(): Location =
        Location(
            latitude = this["latitude"] as? Double ?: 0.0,
            longitude = this["longitude"] as? Double ?: 0.0,
            name = this["name"] as? String ?: "")
  }

  private val profilesCollection = db.collection("profiles")

  override suspend fun createProfile(profile: Profile) {
    try {
      profilesCollection.document(profile.uid).set(profile).await()
    } catch (e: Exception) {
      Log.e("ProfileRepo", "Firestore write failed", e)
      throw e
    }
  }

  override suspend fun getProfile(userId: String): Profile? {
    val doc = profilesCollection.document(userId).get().await()
    if (doc.exists()) {
      return documentToProfile(doc)
    }
    // Auto-create a default profile if missing
    val defaultProfile = createDefaultProfile(userId)
    return defaultProfile
  }

  private suspend fun createDefaultProfile(userId: String): Profile {
    val defaultProfile =
        Profile(
            uid = userId,
            author = Author("New User", "", 0, 0.0, 0.0),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())
    profilesCollection.document(userId).set(defaultProfile).await()
    return defaultProfile
  }

  override suspend fun updateProfile(profile: Profile) {
    val currentUser = auth.currentUser ?: return
    Log.i("ProfileRepo", "Writing profile for UID=${currentUser.uid}")
    profilesCollection.document(currentUser.uid).set(profile).await()
  }

  override suspend fun getMyHunts(userId: String): List<Hunt> {
    val snapshot = db.collection("hunts").whereEqualTo("authorId", userId).get().await()
    return snapshot.documents.mapNotNull { documentToHunt(it) }
  }

  override suspend fun getDoneHunts(userId: String): List<Hunt> {
    val snapshot = profilesCollection.document(userId).get().await()

    @Suppress("UNCHECKED_CAST")
    val doneHuntsData = snapshot.get("doneHunts") as? List<Map<String, Any?>> ?: emptyList()

    return doneHuntsData.mapNotNull { mapToHunt(it) }
  }

  override suspend fun getLikedHunts(userId: String): List<Hunt> {
    val snapshot = profilesCollection.document(userId).get().await()

    @Suppress("UNCHECKED_CAST")
    val doneHuntsData = snapshot.get("likedHunts") as? List<Map<String, Any?>> ?: emptyList()

    return doneHuntsData.mapNotNull { mapToHunt(it) }
  }

  override suspend fun addDoneHunt(userId: String, hunt: Hunt) {
    try {
      val userDocRef = profilesCollection.document(userId)
      val snapshot = userDocRef.get().await()

      @Suppress("UNCHECKED_CAST")
      val currentList = snapshot.get("doneHunts") as? List<Map<String, Any?>> ?: emptyList()

      val isAlreadyAdded = currentList.any { it["uid"] == hunt.uid }
      if (isAlreadyAdded) {
        Log.i(
            "ProfileRepo", "Hunt '${hunt.title}' is already in the doneHunts list for user $userId")
        return
      }

      val huntData = huntToMap(hunt)
      val updatedList = currentList + huntData

      userDocRef.update("doneHunts", updatedList).await()
      Log.i("ProfileRepo", "Added done hunt '${hunt.title}' for user $userId")
    } catch (e: Exception) {
      Log.e("ProfileRepo", "Failed to add done hunt for user $userId", e)
      throw e
    }
  }

  private fun documentToHunt(document: DocumentSnapshot): Hunt? {
    return try {
      val uid = document.id
      val title = document.getString("title") ?: return null
      val description = document.getString("description") ?: return null
      val time = document.getDouble("time") ?: 0.0
      val distance = document.getDouble("distance") ?: 0.0
      val reviewRate = document.getDouble("reviewRate") ?: 0.0
      val mainImageUrl = document.getString("mainImageUrl") ?: ""

      val start = (document.get("start") as? Map<*, *>)?.toLocation() ?: Location(0.0, 0.0, "")
      val end = (document.get("end") as? Map<*, *>)?.toLocation() ?: Location(0.0, 0.0, "")
      val middlePoints =
          (document.get("middlePoints") as? List<Map<*, *>>)?.map { it.toLocation() } ?: emptyList()
      val difficulty =
          document.getString("difficulty")?.let { Difficulty.valueOf(it) } ?: Difficulty.EASY
      val status = document.getString("status")?.let { HuntStatus.valueOf(it) } ?: return null
      val authorId = document.getString("authorId") ?: ""

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
          reviewRate = reviewRate)
    } catch (e: Exception) {
      null
    }
  }

  private suspend fun documentToProfile(document: DocumentSnapshot): Profile? {
    if (!document.exists()) return null

    val uid = document.id
    val authorMap = document.get("author") as? Map<*, *> ?: return null
    val author =
        Author(
            pseudonym = authorMap["pseudonym"] as? String ?: "",
            bio = authorMap["bio"] as? String ?: "",
            profilePicture = (authorMap["profilePicture"] as? Long ?: 0L).toInt(),
            reviewRate = authorMap["reviewRate"] as? Double ?: 0.0,
            sportRate = authorMap["sportRate"] as? Double ?: 0.0)

    // Fetch associated hunts only for this user
    val myHunts = huntsRepository.getAllHunts().filter { it.authorId == uid }.toMutableList()
    val doneHunts =
        db.collection("users")
            .document(uid)
            .collection("doneHunts")
            .get()
            .await()
            .documents
            .mapNotNull { documentToHunt(it) }
            .toMutableList()
    val likedHunts =
        db.collection("users")
            .document(uid)
            .collection("likedHunts")
            .get()
            .await()
            .documents
            .mapNotNull { documentToHunt(it) }
            .toMutableList()

    return Profile(
        uid = uid,
        author = author,
        myHunts = myHunts,
        doneHunts = doneHunts,
        likedHunts = likedHunts)
  }
}
