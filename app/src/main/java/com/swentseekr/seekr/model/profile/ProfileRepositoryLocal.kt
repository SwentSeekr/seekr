package com.swentseekr.seekr.model.profile

import android.net.Uri
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants
import com.swentseekr.seekr.ui.profile.EditProfileStrings.EMPTY_STRING
import com.swentseekr.seekr.ui.profile.Profile

/**
 * A local in-memory repository for managing [Profile] objects. This class is used primarily for
 * testing or local data storage without a backend.
 */
open class ProfileRepositoryLocal : ProfileRepository {
  private val profiles = mutableListOf<Profile>()

  fun size() = profiles.size

  /**
   * Adds a new [profile] to the repository.
   *
   * @param profile The profile to add.
   */
  fun addProfile(profile: Profile) {
    profiles.add(profile)
  }

  /**
   * Creates a new profile in the repository.
   *
   * @param profile The profile to create.
   * @throws IllegalArgumentException If a profile with the same UID already exists.
   */
  override suspend fun createProfile(profile: Profile) {
    require(!(profiles.any { it.uid == profile.uid })) {
      String.format(ProfileRepositoryStrings.PROFILE_ALREADY_EXISTS, profile.uid)
    }
    profiles.add(profile)
  }
  /**
   * Retrieves a [Profile] by its user ID.
   *
   * @param userId The UID of the profile to retrieve.
   * @return The [Profile] corresponding to [userId].
   * @throws IllegalArgumentException If no profile with the given UID exists.
   */
  override suspend fun getProfile(userId: String): Profile {
    for (i in profiles.indices) {
      if (profiles[i].uid == userId) {
        return profiles[i]
      }
    }
    throw IllegalArgumentException(
        String.format(ProfileRepositoryStrings.PROFILE_NOT_FOUND, userId))
  }

  /**
   * Updates an existing profile.
   *
   * @param profile The updated profile data.
   * @throws IllegalArgumentException If the profile does not exist.
   */
  override suspend fun updateProfile(profile: Profile) {
    for (i in profiles.indices) {
      if (profiles[i].uid == profile.uid) {
        profiles[i] = profile
        return
      }
    }
    throw IllegalArgumentException(
        String.format(ProfileRepositoryStrings.PROFILE_NOT_FOUND, profile.uid))
  }

  /**
   * Returns the list of hunts created by the user.
   *
   * @param userId UID of the profile.
   * @return List of [Hunt] objects created by the user.
   */
  override suspend fun getMyHunts(userId: String): List<Hunt> {
    return getProfile(userId).myHunts.toList()
  }

  /**
   * Returns the list of hunts the user has completed.
   *
   * @param userId UID of the profile.
   * @return List of [Hunt] objects marked as done by the user.
   */
  override suspend fun getDoneHunts(userId: String): List<Hunt> {
    return getProfile(userId).doneHunts.toList()
  }

  /**
   * Returns the list of hunts the user has liked.
   *
   * @param userId UID of the profile.
   * @return List of [Hunt] objects liked by the user.
   */
  override suspend fun getLikedHunts(userId: String): List<Hunt> {
    return getProfile(userId).likedHunts.toList()
  }

  /**
   * Marks a [Hunt] as done for the given user.
   *
   * @param userId UID of the profile.
   * @param hunt The hunt to mark as done.
   * @throws IllegalArgumentException If the profile does not exist.
   */
  override suspend fun addDoneHunt(userId: String, hunt: Hunt) {
    val profile =
        profiles.find { it.uid == userId }
            ?: throw IllegalArgumentException(
                String.format(ProfileRepositoryStrings.PROFILE_NOT_FOUND, userId))
    if (profile.doneHunts.none { it.uid == hunt.uid }) {
      profile.doneHunts.add(hunt)
    }
  }
  /**
   * Simulates uploading a profile picture for the user.
   *
   * @param userId UID of the profile.
   * @param uri The URI of the image to upload.
   * @return A fake URL representing the uploaded profile picture.
   * @throws IllegalArgumentException If the profile does not exist.
   */
  override suspend fun uploadProfilePicture(userId: String, uri: Uri): String {
    val fakeUrl = ProfileRepositoryConstants.LOCAL_PROFILE_PICTURE_PREFIX + userId
    val profile =
        profiles.find { it.uid == userId }
            ?: throw IllegalArgumentException(
                String.format(ProfileRepositoryStrings.PROFILE_NOT_FOUND, userId))
    val updatedProfile = profile.copy(author = profile.author.copy(profilePictureUrl = fakeUrl))
    updateProfile(updatedProfile)
    return fakeUrl
  }

  /**
   * Deletes the current profile picture if it matches the given URL.
   *
   * @param userId UID of the profile.
   * @param url URL of the profile picture to delete.
   * @throws IllegalArgumentException If the profile does not exist.
   */
  override suspend fun deleteCurrentProfilePicture(userId: String, url: String) {
    val profile =
        profiles.find { it.uid == userId }
            ?: throw IllegalArgumentException(
                String.format(ProfileRepositoryStrings.PROFILE_NOT_FOUND, userId))

    if (profile.author.profilePictureUrl == url) {
      val updatedProfile =
          profile.copy(
              author =
                  profile.author.copy(
                      profilePictureUrl = EMPTY_STRING,
                      profilePicture = EditProfileNumberConstants.PROFILE_PIC_DEFAULT))

      updateProfile(updatedProfile)
    }
  }

  /**
   * Checks if the user needs to go through onboarding.
   *
   * @param userId UID of the profile.
   * @return Always returns true in this local implementation.
   */
  override suspend fun checkUserNeedsOnboarding(userId: String): Boolean {
    return true
  }

  /**
   * Marks onboarding as completed for the user.
   *
   * @param userId UID of the profile.
   * @param pseudonym The user's pseudonym.
   * @param bio The user's biography.
   */
  override suspend fun completeOnboarding(userId: String, pseudonym: String, bio: String) {
    return
  }

  override suspend fun addLikedHunt(userId: String, huntId: String) {
    return
  }

  override suspend fun removeLikedHunt(userId: String, huntId: String) {
    return
  }
}
