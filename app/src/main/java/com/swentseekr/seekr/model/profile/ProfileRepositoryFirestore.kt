package com.swentseekr.seekr.model.profile

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.profile.Profile
import kotlinx.coroutines.tasks.await

class ProfileRepositoryFirestore(private val db: FirebaseFirestore) : ProfileRepository {
    private val profilesCollection = db.collection("profiles")

  override suspend fun getProfile(userId: String): Profile? {
    val document = profilesCollection.document(userId).get().await()
    if (!document.exists()) {
      return null
    }
    val author = (document.get("author") as? Map<*, *>)?.toAuthor() ?: defaultAuthor()
    return Profile(
        uid = userId,
        author = author,
        myHunts = getMyHunts(userId).toMutableList(),
        doneHunts = getDoneHunts(userId).toMutableList(),
        likedHunts = getLikedHunts(userId).toMutableList()
    )
  }

  override suspend fun updateProfile(profile: Profile) {
      val authorMap = mapOf(
          "pseudonym" to profile.author.pseudonym,
          "bio" to profile.author.bio,
          "profilePicture" to profile.author.profilePicture,
          "reviewRate" to profile.author.reviewRate,
          "sportRate" to profile.author.sportRate
      )
      profilesCollection.document(profile.uid).set(mapOf("author" to authorMap)).await()
    //profilesCollection.document(profile.uid).set(profile).await()
  }

  override suspend fun getMyHunts(userId: String): List<Hunt> {
    val snapshot = db.collection("hunts").whereEqualTo("authorId", userId).get().await()
    return snapshot.documents.mapNotNull { documentToHunt(it) }
  }

  override suspend fun getDoneHunts(userId: String): List<Hunt> {
    val snapshot = db.collection("users").document(userId).collection("doneHunts").get().await()
    return snapshot.documents.mapNotNull { documentToHunt(it) }
  }

  override suspend fun getLikedHunts(userId: String): List<Hunt> {
    val snapshot = db.collection("users").document(userId).collection("likedHunts").get().await()
    return snapshot.documents.mapNotNull { documentToHunt(it) }
  }

  private fun documentToHunt(document: DocumentSnapshot): Hunt? {
    return try {
      val uid = document.id
      val title = document.getString("title") ?: return null
      val description = document.getString("description") ?: return null
      val time = document.getDouble("time") ?: 0.0
      val distance = document.getDouble("distance") ?: 0.0
      val reviewRate = document.getDouble("reviewRate") ?: 0.0
      val image = (document.get("image") as? Long)?.toInt() ?: 0

      // Locations
      val start = (document.get("start") as? Map<*, *>)?.toLocation() ?: defaultLocation()
      val end = (document.get("end") as? Map<*, *>)?.toLocation() ?: defaultLocation()
      val middlePoints =
          (document.get("middlePoints") as? List<Map<*, *>>)?.map { it.toLocation() } ?: emptyList()

      val difficulty = Difficulty.valueOf(document.getString("difficulty") ?: Difficulty.EASY.name)
      val status = HuntStatus.valueOf(document.getString("status") ?: return null)
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
          image = image,
          reviewRate = reviewRate)
    } catch (e: Exception) {
      null
    }
  }

  private fun Map<*, *>.toAuthor(): Author =
      Author(
          pseudonym = this["pseudonym"] as? String ?: "",
          bio = this["bio"] as? String ?: "",
          profilePicture = (this["profilePicture"] as? Long ?: 0L).toInt(),
          reviewRate = this["reviewRate"] as? Double ?: 0.0,
          sportRate = this["sportRate"] as? Double ?: 0.0)

  private fun Map<*, *>.toLocation(): Location =
      Location(
          latitude = this["latitude"] as? Double ?: 0.0,
          longitude = this["longitude"] as? Double ?: 0.0,
          name = this["name"] as? String ?: "")

  private fun defaultAuthor() = Author("", "", 0, 0.0, 0.0)

  private fun defaultLocation() = Location(0.0, 0.0, "")
}
