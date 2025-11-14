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

    /**
     * Converts a [Map] representation of a hunt back into a [Hunt] object. This function extracts
     * the required values from the map and uses them to create a new [Hunt] object. If the map does
     * not contain necessary information, it returns `null`.
     *
     * @param map A [Map] containing key-value pairs corresponding to the properties of a [Hunt].
     * @return A [Hunt] object created from the map, or `null` if the map does not have enough
     *   information.
     */
    fun mapToHunt(map: Map<*, *>): Hunt? {
      val uid = map[ProfileRepositoryFirestoreConstants.HUNT_FIELD_UID] as? String ?: ""
      val title =
          map[ProfileRepositoryFirestoreConstants.HUNT_FIELD_TITLE] as? String ?: return null
      val description =
          map[ProfileRepositoryFirestoreConstants.HUNT_FIELD_DESCRIPTION] as? String ?: return null
      val time =
          (map[ProfileRepositoryFirestoreConstants.HUNT_FIELD_TIME] as? Number)?.toDouble()
              ?: ProfileRepositoryFirestoreConstants.DEFAULT_HUNT_TIME
      val distance =
          (map[ProfileRepositoryFirestoreConstants.HUNT_FIELD_DISTANCE] as? Number)?.toDouble()
              ?: ProfileRepositoryFirestoreConstants.DEFAULT_HUNT_DISTANCE
      val reviewRate =
          (map[ProfileRepositoryFirestoreConstants.HUNT_FIELD_REVIEW_RATE] as? Number)?.toDouble()
              ?: ProfileRepositoryFirestoreConstants.DEFAULT_HUNT_REVIEW_RATE
      val mainImageUrl =
          map[ProfileRepositoryFirestoreConstants.HUNT_FIELD_MAIN_IMAGE_URL] as? String
              ?: ProfileRepositoryFirestoreConstants.DEFAULT_HUNT_MAIN_IMAGE_URL

      val start =
          (map[ProfileRepositoryFirestoreConstants.HUNT_FIELD_START] as? Map<*, *>)?.toLocation()
              ?: Location(
                  ProfileRepositoryFirestoreConstants.DEFAULT_LOCATION_LAT,
                  ProfileRepositoryFirestoreConstants.DEFAULT_LOCATION_LNG,
                  ProfileRepositoryFirestoreConstants.DEFAULT_LOCATION_NAME)
      val end =
          (map[ProfileRepositoryFirestoreConstants.HUNT_FIELD_END] as? Map<*, *>)?.toLocation()
              ?: Location(
                  ProfileRepositoryFirestoreConstants.DEFAULT_LOCATION_LAT,
                  ProfileRepositoryFirestoreConstants.DEFAULT_LOCATION_LNG,
                  ProfileRepositoryFirestoreConstants.DEFAULT_LOCATION_NAME)
      val middlePoints =
          (map[ProfileRepositoryFirestoreConstants.HUNT_FIELD_MIDDLE_POINTS] as? List<Map<*, *>>)
              ?.map { it.toLocation() } ?: emptyList()

      val difficulty =
          map[ProfileRepositoryFirestoreConstants.HUNT_FIELD_DIFFICULTY]?.let {
            Difficulty.valueOf(it as String)
          } ?: ProfileRepositoryFirestoreConstants.DEFAULT_DIFFICULTY
      val status =
          map[ProfileRepositoryFirestoreConstants.HUNT_FIELD_STATUS]?.let {
            HuntStatus.valueOf(it as String)
          } ?: ProfileRepositoryFirestoreConstants.DEFAULT_STATUS
      val authorId = map[ProfileRepositoryFirestoreConstants.HUNT_FIELD_AUTHOR_ID] as? String ?: ""

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

    /**
     * Extension function to convert a [Map] representing location data into a [Location] object.
     *
     * @return A [Location] object created from the map. If the map does not contain valid data, it
     *   defaults to a location with latitude 0.0, longitude 0.0, and an empty name.
     */
    private fun Map<*, *>.toLocation(): Location =
        Location(
            latitude =
                this[ProfileRepositoryFirestoreConstants.LOCATION_FIELD_LATITUDE] as? Double
                    ?: ProfileRepositoryFirestoreConstants.DEFAULT_LOCATION_LAT,
            longitude =
                this[ProfileRepositoryFirestoreConstants.LOCATION_FIELD_LONGITUDE] as? Double
                    ?: ProfileRepositoryFirestoreConstants.DEFAULT_LOCATION_LNG,
            name =
                this[ProfileRepositoryFirestoreConstants.LOCATION_FIELD_NAME] as? String
                    ?: ProfileRepositoryFirestoreConstants.DEFAULT_LOCATION_NAME)
  }

  private val profilesCollection =
      db.collection(ProfileRepositoryFirestoreConstants.PROFILES_COLLECTION)

  override suspend fun createProfile(profile: Profile) {
    try {
      profilesCollection.document(profile.uid).set(profile).await()
    } catch (e: Exception) {
      Log.e(
          ProfileRepositoryFirestoreConstants.FIRESTORE_WRITE_FAILED_LOG_TAG,
          ProfileRepositoryFirestoreConstants.FIRESTORE_WRITE_FAILED_MESSAGE,
          e)
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
            author =
                Author(
                    ProfileRepositoryFirestoreConstants.DEFAULT_USER_NAME,
                    ProfileRepositoryFirestoreConstants.DEFAULT_USER_BIO,
                    ProfileRepositoryFirestoreConstants.DEFAULT_PROFILE_PICTURE,
                    ProfileRepositoryFirestoreConstants.DEFAULT_REVIEW_RATE,
                    ProfileRepositoryFirestoreConstants.DEFAULT_SPORT_RATE),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())
    profilesCollection.document(userId).set(defaultProfile).await()
    return defaultProfile
  }

  override suspend fun updateProfile(profile: Profile) {
    val currentUser = auth.currentUser ?: return
    Log.i(
        ProfileRepositoryFirestoreConstants.FIRESTORE_WRITE_FAILED_LOG_TAG,
        "Writing profile for UID=${currentUser.uid}")
    profilesCollection.document(currentUser.uid).set(profile).await()
  }

  override suspend fun getMyHunts(userId: String): List<Hunt> {
    val snapshot =
        db.collection(ProfileRepositoryFirestoreConstants.HUNTS_COLLECTION)
            .whereEqualTo(ProfileRepositoryFirestoreConstants.HUNT_FIELD_AUTHOR_ID, userId)
            .get()
            .await()
    return snapshot.documents.mapNotNull { documentToHunt(it) }
  }

  override suspend fun getDoneHunts(userId: String): List<Hunt> {
    val snapshot = profilesCollection.document(userId).get().await()

    @Suppress("UNCHECKED_CAST")
    val doneHuntsData =
        snapshot.get(ProfileRepositoryFirestoreConstants.PROFILE_FIELD_DONE_HUNTS)
            as? List<Map<String, Any?>> ?: emptyList()

    return doneHuntsData.mapNotNull { mapToHunt(it) }
  }

  override suspend fun getLikedHunts(userId: String): List<Hunt> {
    val snapshot = profilesCollection.document(userId).get().await()

    @Suppress("UNCHECKED_CAST")
    val likedHuntsData =
        snapshot.get(ProfileRepositoryFirestoreConstants.PROFILE_FIELD_LIKED_HUNTS)
            as? List<Map<String, Any?>> ?: emptyList()

    return likedHuntsData.mapNotNull { mapToHunt(it) }
  }

  override suspend fun addDoneHunt(userId: String, hunt: Hunt) {
    try {
      val userDocRef = profilesCollection.document(userId)
      val snapshot = userDocRef.get().await()

      @Suppress("UNCHECKED_CAST")
      val currentList =
          snapshot.get(ProfileRepositoryFirestoreConstants.PROFILE_FIELD_DONE_HUNTS)
              as? List<Map<String, Any?>> ?: emptyList()

      val isAlreadyAdded =
          currentList.any { it[ProfileRepositoryFirestoreConstants.HUNT_FIELD_UID] == hunt.uid }
      if (isAlreadyAdded) {
        Log.i(
            ProfileRepositoryFirestoreConstants.FIRESTORE_WRITE_FAILED_LOG_TAG,
            "Hunt '${hunt.title}' is already in the doneHunts list for user $userId")
        return
      }

      val huntData = huntToMap(hunt)
      val updatedList = currentList + huntData

      userDocRef
          .update(ProfileRepositoryFirestoreConstants.PROFILE_FIELD_DONE_HUNTS, updatedList)
          .await()
      Log.i(
          ProfileRepositoryFirestoreConstants.FIRESTORE_WRITE_FAILED_LOG_TAG,
          "Added done hunt '${hunt.title}' for user $userId")
    } catch (e: Exception) {
      Log.e(
          ProfileRepositoryFirestoreConstants.FIRESTORE_WRITE_FAILED_LOG_TAG,
          "Failed to add done hunt for user $userId",
          e)
      throw e
    }
  }

  private fun documentToHunt(document: DocumentSnapshot): Hunt? {
    return try {
      val uid = document.id
      val title =
          document.getString(ProfileRepositoryFirestoreConstants.HUNT_FIELD_TITLE) ?: return null
      val description =
          document.getString(ProfileRepositoryFirestoreConstants.HUNT_FIELD_DESCRIPTION)
              ?: return null
      val time =
          document.getDouble(ProfileRepositoryFirestoreConstants.HUNT_FIELD_TIME)
              ?: ProfileRepositoryFirestoreConstants.DEFAULT_HUNT_TIME
      val distance =
          document.getDouble(ProfileRepositoryFirestoreConstants.HUNT_FIELD_DISTANCE)
              ?: ProfileRepositoryFirestoreConstants.DEFAULT_HUNT_DISTANCE
      val reviewRate =
          document.getDouble(ProfileRepositoryFirestoreConstants.HUNT_FIELD_REVIEW_RATE)
              ?: ProfileRepositoryFirestoreConstants.DEFAULT_HUNT_REVIEW_RATE
      val mainImageUrl =
          document.getString(ProfileRepositoryFirestoreConstants.HUNT_FIELD_MAIN_IMAGE_URL)
              ?: ProfileRepositoryFirestoreConstants.DEFAULT_HUNT_MAIN_IMAGE_URL

      val start =
          (document.get(ProfileRepositoryFirestoreConstants.HUNT_FIELD_START) as? Map<*, *>)
              ?.toLocation()
              ?: Location(
                  ProfileRepositoryFirestoreConstants.DEFAULT_LOCATION_LAT,
                  ProfileRepositoryFirestoreConstants.DEFAULT_LOCATION_LNG,
                  ProfileRepositoryFirestoreConstants.DEFAULT_LOCATION_NAME)
      val end =
          (document.get(ProfileRepositoryFirestoreConstants.HUNT_FIELD_END) as? Map<*, *>)
              ?.toLocation()
              ?: Location(
                  ProfileRepositoryFirestoreConstants.DEFAULT_LOCATION_LAT,
                  ProfileRepositoryFirestoreConstants.DEFAULT_LOCATION_LNG,
                  ProfileRepositoryFirestoreConstants.DEFAULT_LOCATION_NAME)
      val middlePoints =
          (document.get(ProfileRepositoryFirestoreConstants.HUNT_FIELD_MIDDLE_POINTS)
                  as? List<Map<*, *>>)
              ?.map { it.toLocation() } ?: emptyList()

      val difficulty =
          document.getString(ProfileRepositoryFirestoreConstants.HUNT_FIELD_DIFFICULTY)?.let {
            Difficulty.valueOf(it)
          } ?: ProfileRepositoryFirestoreConstants.DEFAULT_DIFFICULTY
      val status =
          document.getString(ProfileRepositoryFirestoreConstants.HUNT_FIELD_STATUS)?.let {
            HuntStatus.valueOf(it)
          } ?: ProfileRepositoryFirestoreConstants.DEFAULT_STATUS
      val authorId =
          document.getString(ProfileRepositoryFirestoreConstants.HUNT_FIELD_AUTHOR_ID) ?: ""

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
    val authorMap =
        document.get(ProfileRepositoryFirestoreConstants.PROFILE_FIELD_AUTHOR) as? Map<*, *>
            ?: return null
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
            .collection(ProfileRepositoryFirestoreConstants.PROFILE_FIELD_DONE_HUNTS)
            .get()
            .await()
            .documents
            .mapNotNull { documentToHunt(it) }
            .toMutableList()
    val likedHunts =
        db.collection("users")
            .document(uid)
            .collection(ProfileRepositoryFirestoreConstants.PROFILE_FIELD_LIKED_HUNTS)
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
