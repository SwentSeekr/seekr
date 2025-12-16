package com.swentseekr.seekr.model.profile

import android.net.Uri
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.profile.Profile

/** Repository interface for managing user profiles. */
interface ProfileRepository {

  /**
   * Creates a new profile in the repository.
   *
   * @param profile The [Profile] object to create.
   */
  suspend fun createProfile(profile: Profile)

  /**
   * Retrieves a profile by user ID.
   *
   * @param userId The unique identifier of the user.
   * @return The [Profile] associated with the user, or null if not found.
   */
  suspend fun getProfile(userId: String): Profile?

  /**
   * Returns a list of all pseudonyms currently in use.
   *
   * @return List of pseudonym strings.
   */
  suspend fun getAllPseudonyms(): List<String>

  /**
   * Updates an existing profile.
   *
   * @param profile The updated [Profile] object.
   */
  suspend fun updateProfile(profile: Profile)

  /**
   * Retrieves all hunts created by the user.
   *
   * @param userId The user's unique ID.
   * @return List of [Hunt] objects created by the user.
   */
  suspend fun getMyHunts(userId: String): List<Hunt>

  /**
   * Retrieves all hunts that the user has completed.
   *
   * @param userId The user's unique ID.
   * @return List of [Hunt] objects completed by the user.
   */
  suspend fun getDoneHunts(userId: String): List<Hunt>

  /**
   * Retrieves all hunts that the user has liked.
   *
   * @param userId The user's unique ID.
   * @return List of [Hunt] objects liked by the user.
   */
  suspend fun getLikedHunts(userId: String): List<Hunt>

  /**
   * Marks a hunt as completed for the user.
   *
   * @param userId The user's unique ID.
   * @param hunt The [Hunt] that has been completed.
   */
  suspend fun addDoneHunt(userId: String, hunt: Hunt)

  /**
   * Uploads a profile picture for the user.
   *
   * @param userId The user's unique ID.
   * @param uri The [Uri] of the image to upload.
   * @return The URL of the uploaded profile picture.
   */
  suspend fun uploadProfilePicture(userId: String, uri: Uri): String

  /**
   * Checks whether the user still needs to complete onboarding.
   *
   * @param userId The user's unique ID.
   * @return True if onboarding is required, false otherwise.
   */
  suspend fun checkUserNeedsOnboarding(userId: String): Boolean

  /**
   * Completes the onboarding process for the user.
   *
   * @param userId The user's unique ID.
   * @param pseudonym The pseudonym chosen by the user.
   * @param bio The bio provided by the user.
   */
  suspend fun completeOnboarding(userId: String, pseudonym: String, bio: String)

  /**
   * Deletes the current profile picture of the user.
   *
   * @param userId The user's unique ID.
   * @param url The URL of the profile picture to delete.
   */
  suspend fun deleteCurrentProfilePicture(userId: String, url: String)

  /**
   * Adds a hunt to the user's list of liked hunts.
   *
   * @param userId The user's unique ID.
   * @param huntId The unique ID of the hunt to like.
   */
  suspend fun addLikedHunt(userId: String, huntId: String)

  /**
   * Removes a hunt from the user's list of liked hunts.
   *
   * @param userId The user's unique ID.
   * @param huntId The unique ID of the hunt to remove from likes.
   */
  suspend fun removeLikedHunt(userId: String, huntId: String)
}
