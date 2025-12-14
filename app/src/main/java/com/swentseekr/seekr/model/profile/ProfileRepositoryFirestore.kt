package com.swentseekr.seekr.model.profile

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.hunt.HuntsRepositoryFirestore
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.model.profile.ProfileRepositoryConstants.DEFAULT_EMPTY_VALUE
import com.swentseekr.seekr.ui.profile.Profile
import kotlinx.coroutines.tasks.await

/**
 * Firestore implementation of [ProfileRepository].
 *
 * @property db The Firestore instance used for storing profiles.
 * @property auth The FirebaseAuth instance used for user authentication.
 * @property huntsRepository Repository for managing hunts, used to fetch hunts by user.
 * @property storage FirebaseStorage instance used for uploading and deleting profile pictures.
 */
class ProfileRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val huntsRepository: HuntsRepositoryFirestore = HuntsRepositoryFirestore(db),
    private val storage: FirebaseStorage
) : ProfileRepository {

  companion object {

    /**
     * Converts a [Hunt] object into a [Map] suitable for Firestore storage.
     *
     * @param hunt The hunt to convert.
     * @return A map containing the hunt's data.
     */
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
      val uid = map[ProfileRepositoryConstants.HUNT_FIELD_UID] as? String ?: DEFAULT_EMPTY_VALUE
      val title = map[ProfileRepositoryConstants.HUNT_FIELD_TITLE] as? String ?: return null
      val description =
          map[ProfileRepositoryConstants.HUNT_FIELD_DESCRIPTION] as? String ?: return null
      val time =
          (map[ProfileRepositoryConstants.HUNT_FIELD_TIME] as? Number)?.toDouble()
              ?: ProfileRepositoryConstants.DEFAULT_HUNT_TIME
      val distance =
          (map[ProfileRepositoryConstants.HUNT_FIELD_DISTANCE] as? Number)?.toDouble()
              ?: ProfileRepositoryConstants.DEFAULT_HUNT_DISTANCE
      val reviewRate =
          (map[ProfileRepositoryConstants.HUNT_FIELD_REVIEW_RATE] as? Number)?.toDouble()
              ?: ProfileRepositoryConstants.DEFAULT_HUNT_REVIEW_RATE
      val mainImageUrl =
          map[ProfileRepositoryConstants.HUNT_FIELD_MAIN_IMAGE_URL] as? String
              ?: ProfileRepositoryConstants.DEFAULT_HUNT_MAIN_IMAGE_URL

      val start =
          (map[ProfileRepositoryConstants.HUNT_FIELD_START] as? Map<*, *>)?.toLocation()
              ?: Location(
                  ProfileRepositoryConstants.DEFAULT_LOCATION_LAT,
                  ProfileRepositoryConstants.DEFAULT_LOCATION_LNG,
                  ProfileRepositoryConstants.DEFAULT_LOCATION_NAME)
      val end =
          (map[ProfileRepositoryConstants.HUNT_FIELD_END] as? Map<*, *>)?.toLocation()
              ?: Location(
                  ProfileRepositoryConstants.DEFAULT_LOCATION_LAT,
                  ProfileRepositoryConstants.DEFAULT_LOCATION_LNG,
                  ProfileRepositoryConstants.DEFAULT_LOCATION_NAME)
      val middlePoints =
          (map[ProfileRepositoryConstants.HUNT_FIELD_MIDDLE_POINTS] as? List<*>)
              ?.filterIsInstance<Map<*, *>>()
              ?.map { it.toLocation() } ?: emptyList()
      val difficulty =
          map[ProfileRepositoryConstants.HUNT_FIELD_DIFFICULTY]?.let {
            Difficulty.valueOf(it as String)
          } ?: ProfileRepositoryConstants.DEFAULT_DIFFICULTY
      val status =
          map[ProfileRepositoryConstants.HUNT_FIELD_STATUS]?.let {
            HuntStatus.valueOf(it as String)
          } ?: ProfileRepositoryConstants.DEFAULT_STATUS
      val authorId =
          map[ProfileRepositoryConstants.HUNT_FIELD_AUTHOR_ID] as? String ?: DEFAULT_EMPTY_VALUE

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
                this[ProfileRepositoryConstants.LOCATION_FIELD_LATITUDE] as? Double
                    ?: ProfileRepositoryConstants.DEFAULT_LOCATION_LAT,
            longitude =
                this[ProfileRepositoryConstants.LOCATION_FIELD_LONGITUDE] as? Double
                    ?: ProfileRepositoryConstants.DEFAULT_LOCATION_LNG,
            name =
                this[ProfileRepositoryConstants.LOCATION_FIELD_NAME] as? String
                    ?: ProfileRepositoryConstants.DEFAULT_LOCATION_NAME,
            description =
                this[ProfileRepositoryConstants.LOCATION_FIELD_DESCRIPTION] as? String ?: "")
  }

  private val profilesCollection = db.collection(ProfileRepositoryConstants.PROFILES_COLLECTION)

  /**
   * Creates a new profile in Firestore.
   *
   * @param profile The profile to create.
   * @throws Exception if Firestore write fails.
   */
  override suspend fun createProfile(profile: Profile) {
    try {
      profilesCollection.document(profile.uid).set(profile).await()
    } catch (e: Exception) {
      Log.e(
          ProfileRepositoryConstants.FIRESTORE_WRITE_FAILED_LOG_TAG,
          ProfileRepositoryStrings.FIRESTORE_WRITE_FAILED_MESSAGE,
          e)
      throw e
    }
  }

  /**
   * Fetches a profile for a given user ID. If the profile does not exist, a default profile is
   * automatically created.
   *
   * @param userId The user's unique ID.
   * @return The [Profile] object.
   * @throws Exception if Firestore read/write fails.
   */
  override suspend fun getProfile(userId: String): Profile? {
    val doc = profilesCollection.document(userId).get().await()
    if (doc.exists()) {
      return documentToProfile(doc)
    }
    val defaultProfile = createDefaultProfile(userId)
    return defaultProfile
  }

  /**
   * Returns a list of all pseudonyms currently used in profiles.
   *
   * @return List of pseudonyms.
   */
  override suspend fun getAllPseudonyms(): List<String> {
    return try {
      val snapshot = profilesCollection.get().await()

      snapshot.documents.mapNotNull { doc ->
        val author = doc["author"] as? Map<*, *> ?: return@mapNotNull null
        val pseudonym = author["pseudonym"] as? String
        pseudonym?.takeIf { it.isNotBlank() }
      }
    } catch (e: Exception) {
      Log.e(
          ProfileRepositoryConstants.FIRESTORE_WRITE_FAILED_LOG_TAG,
          "Failed to fetch pseudonyms",
          e)
      emptyList()
    }
  }

  /**
   * Creates a default profile for a user if one does not exist in Firestore.
   *
   * @param userId The user's unique ID.
   * @return The created [Profile] object.
   * @throws Exception if Firestore write fails.
   */
  private suspend fun createDefaultProfile(userId: String): Profile {
    val defaultProfile =
        Profile(
            uid = userId,
            author =
                Author(
                    hasCompletedOnboarding = false,
                    hasAcceptedTerms = false,
                    pseudonym = ProfileRepositoryConstants.DEFAULT_USER_NAME,
                    bio = ProfileRepositoryConstants.DEFAULT_USER_BIO,
                    profilePicture = ProfileRepositoryConstants.DEFAULT_PROFILE_PICTURE,
                    reviewRate = ProfileRepositoryConstants.DEFAULT_REVIEW_RATE,
                    sportRate = ProfileRepositoryConstants.DEFAULT_SPORT_RATE,
                    profilePictureUrl = DEFAULT_EMPTY_VALUE),
            myHunts = mutableListOf(),
            doneHunts = mutableListOf(),
            likedHunts = mutableListOf())
    profilesCollection.document(userId).set(defaultProfile).await()
    return defaultProfile
  }

  /**
   * Updates the user's profile with new author information.
   *
   * @param profile The updated profile data.
   * @throws Exception if Firestore write fails.
   */
  override suspend fun updateProfile(profile: Profile) {
    val currentUser = auth.currentUser ?: return
    Log.i(
        ProfileRepositoryConstants.FIRESTORE_WRITE_FAILED_LOG_TAG,
        "Writing profile for UID=${currentUser.uid}")
    val userDocRef = profilesCollection.document(currentUser.uid)
    userDocRef
        .update(
            mapOf(
                "author.pseudonym" to profile.author.pseudonym,
                "author.bio" to profile.author.bio,
                "author.profilePicture" to profile.author.profilePicture,
                "author.profilePictureUrl" to profile.author.profilePictureUrl,
                "author.reviewRate" to profile.author.reviewRate,
                "author.sportRate" to profile.author.sportRate))
        .await()
  }

  /**
   * Retrieves all hunts created by the user.
   *
   * @param userId The user's ID.
   * @return List of [Hunt] objects.
   * @throws Exception if Firestore read fails.
   */
  override suspend fun getMyHunts(userId: String): List<Hunt> {
    val snapshot =
        db.collection(ProfileRepositoryConstants.HUNTS_COLLECTION)
            .whereEqualTo(ProfileRepositoryConstants.HUNT_FIELD_AUTHOR_ID, userId)
            .get()
            .await()
    return snapshot.documents.mapNotNull { documentToHunt(it) }
  }

  /**
   * Uploads a new profile picture for the user and updates the Firestore document.
   *
   * @param userId The user's ID.
   * @param uri The [Uri] of the picture to upload.
   * @return The download URL of the uploaded image.
   * @throws Exception if storage or Firestore operations fail.
   */
  override suspend fun uploadProfilePicture(userId: String, uri: Uri): String {
    val storageRef = storage.reference.child("profile_pictures/$userId.jpg")
    val docRef = db.collection("profiles").document(userId)

    return try {
      storageRef.putFile(uri).await()
      val url = storageRef.downloadUrl.await().toString()
      docRef.update("author.profilePictureUrl", url).await()
      url
    } catch (e: Exception) {
      Log.e(
          ProfileRepositoryConstants.UPLOAD_FAILED_LOG_TAG,
          ProfileRepositoryStrings.UPLOAD_FAILED,
          e)
      throw e
    }
  }

  /**
   * Deletes the current profile picture from Firebase Storage.
   *
   * @param userId The user's ID.
   * @param url The URL of the profile picture to delete.
   */
  override suspend fun deleteCurrentProfilePicture(userId: String, url: String) {
    if (url.isNotEmpty()) {
      try {
        storage.getReferenceFromUrl(url).delete().await()
      } catch (e: Exception) {
        Log.e(
            ProfileRepositoryConstants.FIRESTORE_WRITE_FAILED_LOG_TAG,
            String.format(ProfileRepositoryStrings.DELETE_FAILED, e.message))
      }
    }
  }

  /**
   * Retrieves all hunts that the user has completed.
   *
   * @param userId The user's ID.
   * @return List of completed [Hunt] objects.
   */
  override suspend fun getDoneHunts(userId: String): List<Hunt> {
    val snapshot = profilesCollection.document(userId).get().await()

    @Suppress("UNCHECKED_CAST")
    val doneHuntsData =
        snapshot[ProfileRepositoryConstants.PROFILE_FIELD_DONE_HUNTS] as? List<Map<String, Any?>>
            ?: emptyList()

    return doneHuntsData.mapNotNull { mapToHunt(it) }
  }

  /**
   * Retrieves all hunts liked by the user.
   *
   * @param userId The user's ID.
   * @return List of liked [Hunt] objects.
   */
  override suspend fun getLikedHunts(userId: String): List<Hunt> {
    val snapshot = profilesCollection.document(userId).get().await()

    @Suppress("UNCHECKED_CAST")
    val likedHuntsData =
        snapshot[ProfileRepositoryConstants.PROFILE_FIELD_LIKED_HUNTS] as? List<Map<String, Any?>>
            ?: emptyList()

    return likedHuntsData.mapNotNull { mapToHunt(it) }
  }

  /**
   * Marks a hunt as completed for the user.
   *
   * @param userId The user's ID.
   * @param hunt The hunt to mark as done.
   * @throws Exception if Firestore update fails.
   */
  override suspend fun addDoneHunt(userId: String, hunt: Hunt) {
    try {
      val userDocRef = profilesCollection.document(userId)
      val snapshot = userDocRef.get().await()

      @Suppress("UNCHECKED_CAST")
      val currentList =
          snapshot[ProfileRepositoryConstants.PROFILE_FIELD_DONE_HUNTS] as? List<Map<String, Any?>>
              ?: emptyList()

      val isAlreadyAdded =
          currentList.any { it[ProfileRepositoryConstants.HUNT_FIELD_UID] == hunt.uid }
      if (isAlreadyAdded) {
        Log.i(
            ProfileRepositoryConstants.FIRESTORE_WRITE_FAILED_LOG_TAG,
            "Hunt '${hunt.title}' is already in the doneHunts list for user $userId")
        return
      }

      val huntData = huntToMap(hunt)
      val updatedList = currentList + huntData

      userDocRef.update(ProfileRepositoryConstants.PROFILE_FIELD_DONE_HUNTS, updatedList).await()
      Log.i(
          ProfileRepositoryConstants.FIRESTORE_WRITE_FAILED_LOG_TAG,
          "Added done hunt '${hunt.title}' for user $userId")
    } catch (e: Exception) {
      Log.e(
          ProfileRepositoryConstants.FIRESTORE_WRITE_FAILED_LOG_TAG,
          "Failed to add done hunt for user $userId",
          e)
      throw e
    }
  }

  /**
   * Converts a Firestore [DocumentSnapshot] into a [Hunt] object.
   *
   * @param document The Firestore document snapshot representing a hunt.
   * @return A [Hunt] object or null if essential fields are missing or an error occurs.
   */
  private fun documentToHunt(document: DocumentSnapshot): Hunt? {
    return try {
      val uid = document.id
      val title = document.getString(ProfileRepositoryConstants.HUNT_FIELD_TITLE) ?: return null
      val description =
          document.getString(ProfileRepositoryConstants.HUNT_FIELD_DESCRIPTION) ?: return null
      val time =
          document.getDouble(ProfileRepositoryConstants.HUNT_FIELD_TIME)
              ?: ProfileRepositoryConstants.DEFAULT_HUNT_TIME
      val distance =
          document.getDouble(ProfileRepositoryConstants.HUNT_FIELD_DISTANCE)
              ?: ProfileRepositoryConstants.DEFAULT_HUNT_DISTANCE
      val reviewRate =
          document.getDouble(ProfileRepositoryConstants.HUNT_FIELD_REVIEW_RATE)
              ?: ProfileRepositoryConstants.DEFAULT_HUNT_REVIEW_RATE
      val mainImageUrl =
          document.getString(ProfileRepositoryConstants.HUNT_FIELD_MAIN_IMAGE_URL)
              ?: ProfileRepositoryConstants.DEFAULT_HUNT_MAIN_IMAGE_URL

      val start =
          (document[ProfileRepositoryConstants.HUNT_FIELD_START] as? Map<*, *>)?.toLocation()
              ?: Location(
                  ProfileRepositoryConstants.DEFAULT_LOCATION_LAT,
                  ProfileRepositoryConstants.DEFAULT_LOCATION_LNG,
                  ProfileRepositoryConstants.DEFAULT_LOCATION_NAME)
      val end =
          (document[ProfileRepositoryConstants.HUNT_FIELD_END] as? Map<*, *>)?.toLocation()
              ?: Location(
                  ProfileRepositoryConstants.DEFAULT_LOCATION_LAT,
                  ProfileRepositoryConstants.DEFAULT_LOCATION_LNG,
                  ProfileRepositoryConstants.DEFAULT_LOCATION_NAME)
      val middlePoints =
          (document[ProfileRepositoryConstants.HUNT_FIELD_MIDDLE_POINTS] as? List<*>)
              ?.filterIsInstance<Map<*, *>>()
              ?.map { it.toLocation() } ?: emptyList()

      val difficulty =
          document.getString(ProfileRepositoryConstants.HUNT_FIELD_DIFFICULTY)?.let {
            Difficulty.valueOf(it)
          } ?: ProfileRepositoryConstants.DEFAULT_DIFFICULTY
      val status =
          document.getString(ProfileRepositoryConstants.HUNT_FIELD_STATUS)?.let {
            HuntStatus.valueOf(it)
          } ?: ProfileRepositoryConstants.DEFAULT_STATUS
      val authorId =
          document.getString(ProfileRepositoryConstants.HUNT_FIELD_AUTHOR_ID) ?: DEFAULT_EMPTY_VALUE

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

  /**
   * Converts a Firestore [DocumentSnapshot] into a [Profile] object.
   *
   * @param document The Firestore document snapshot representing a user's profile.
   * @return A [Profile] object or null if essential fields are missing.
   */
  private suspend fun documentToProfile(document: DocumentSnapshot): Profile? {
    if (!document.exists()) return null

    val uid = document.id
    val authorMap =
        document[ProfileRepositoryConstants.PROFILE_FIELD_AUTHOR] as? Map<*, *> ?: return null
    val author =
        Author(
            hasCompletedOnboarding = authorMap["hasCompletedOnboarding"] as? Boolean ?: false,
            hasAcceptedTerms = authorMap["hasAcceptedTerms"] as? Boolean ?: false,
            pseudonym = authorMap["pseudonym"] as? String ?: DEFAULT_EMPTY_VALUE,
            bio = authorMap["bio"] as? String ?: DEFAULT_EMPTY_VALUE,
            profilePicture = (authorMap["profilePicture"] as? Long ?: 0L).toInt(),
            reviewRate = authorMap["reviewRate"] as? Double ?: 0.0,
            sportRate = authorMap["sportRate"] as? Double ?: 0.0,
            profilePictureUrl = authorMap["profilePictureUrl"] as? String ?: DEFAULT_EMPTY_VALUE)

    val myHunts = huntsRepository.getAllHunts().filter { it.authorId == uid }.toMutableList()
    val doneHunts =
        db.collection("users")
            .document(uid)
            .collection(ProfileRepositoryConstants.PROFILE_FIELD_DONE_HUNTS)
            .get()
            .await()
            .documents
            .mapNotNull { documentToHunt(it) }
            .toMutableList()
    @Suppress("UNCHECKED_CAST")
    val likedHuntsData =
        document[ProfileRepositoryConstants.PROFILE_FIELD_LIKED_HUNTS] as? List<Map<String, Any?>>
            ?: emptyList()

    val likedHunts = likedHuntsData.mapNotNull { mapToHunt(it) }.toMutableList()

    return Profile(
        uid = uid,
        author = author,
        myHunts = myHunts,
        doneHunts = doneHunts,
        likedHunts = likedHunts)
  }

  /**
   * Checks if the user still needs to complete onboarding.
   *
   * @param userId The user's ID.
   * @return True if onboarding is needed, false otherwise.
   */
  override suspend fun checkUserNeedsOnboarding(userId: String): Boolean {
    val profile = getProfile(userId)

    return !(profile?.author?.hasCompletedOnboarding ?: false)
  }

  /**
   * Completes the onboarding process for a user.
   *
   * @param userId The user's ID.
   * @param pseudonym Chosen pseudonym.
   * @param bio User bio.
   * @throws Exception if Firestore update fails.
   */
  override suspend fun completeOnboarding(userId: String, pseudonym: String, bio: String) {
    val updates =
        mapOf(
            "author.hasCompletedOnboarding" to true,
            "author.hasAcceptedTerms" to true,
            "author.pseudonym" to pseudonym,
            "author.bio" to bio)

    profilesCollection.document(userId).update(updates).await()
  }

  /**
   * Adds a hunt to the user's liked hunts.
   *
   * @param userId The user's ID.
   * @param huntId The hunt's ID.
   * @throws Exception if Firestore update fails.
   */
  override suspend fun addLikedHunt(userId: String, huntId: String) {
    try {
      val hunt = huntsRepository.getHunt(huntId)
      val userDocRef = profilesCollection.document(userId)
      val snapshot = userDocRef.get().await()

      @Suppress("UNCHECKED_CAST")
      val currentList =
          snapshot[ProfileRepositoryConstants.PROFILE_FIELD_LIKED_HUNTS] as? List<Map<String, Any?>>
              ?: emptyList()

      val alreadyLiked =
          currentList.any { it[ProfileRepositoryConstants.HUNT_FIELD_UID] == hunt.uid }

      if (alreadyLiked) {
        Log.i(
            ProfileRepositoryConstants.FIRESTORE_WRITE_FAILED_LOG_TAG,
            "Hunt '${hunt.title}' is already liked by user $userId")
        return
      }

      val updatedList = currentList + huntToMap(hunt)

      userDocRef.update(ProfileRepositoryConstants.PROFILE_FIELD_LIKED_HUNTS, updatedList).await()
    } catch (e: Exception) {
      Log.e(
          ProfileRepositoryConstants.FIRESTORE_WRITE_FAILED_LOG_TAG,
          "Failed to add liked hunt for user $userId",
          e)
      throw e
    }
  }

  /**
   * Removes a hunt from the user's liked hunts.
   *
   * @param userId The user's ID.
   * @param huntId The hunt's ID.
   * @throws Exception if Firestore update fails.
   */
  override suspend fun removeLikedHunt(userId: String, huntId: String) {
    try {
      val snapshot = profilesCollection.document(userId).get().await()

      @Suppress("UNCHECKED_CAST")
      val likedHunts =
          snapshot[ProfileRepositoryConstants.PROFILE_FIELD_LIKED_HUNTS] as? List<Map<String, Any?>>
              ?: return

      val updated = likedHunts.filterNot { it[ProfileRepositoryConstants.HUNT_FIELD_UID] == huntId }

      profilesCollection
          .document(userId)
          .update(ProfileRepositoryConstants.PROFILE_FIELD_LIKED_HUNTS, updated)
          .await()
    } catch (e: Exception) {
      Log.e(
          ProfileRepositoryConstants.FIRESTORE_WRITE_FAILED_LOG_TAG,
          "Failed to remove liked hunt for user $userId",
          e)
      throw e
    }
  }
}
