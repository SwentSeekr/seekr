package com.swentseekr.seekr.model.profile

import android.net.Uri
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants
import com.swentseekr.seekr.ui.profile.EditProfileStrings.EMPTY_STRING
import com.swentseekr.seekr.ui.profile.Profile

class ProfileRepositoryLocal : ProfileRepository {
  private val profiles = mutableListOf<Profile>()

  fun size() = profiles.size

  fun addProfile(profile: Profile) {
    profiles.add(profile)
  }

  override suspend fun createProfile(profile: Profile) {
    require(!(profiles.any { it.uid == profile.uid })) {
      String.format(ProfileRepositoryStrings.PROFILE_ALREADY_EXISTS, profile.uid)
    }
    profiles.add(profile)
  }

  override suspend fun getProfile(userId: String): Profile {
    for (i in profiles.indices) {
      if (profiles[i].uid == userId) {
        return profiles[i]
      }
    }
    throw IllegalArgumentException(
        String.format(ProfileRepositoryStrings.PROFILE_NOT_FOUND, userId))
  }

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

  override suspend fun getMyHunts(userId: String): List<Hunt> {
    return getProfile(userId).myHunts.toList()
  }

  override suspend fun getDoneHunts(userId: String): List<Hunt> {
    return getProfile(userId).doneHunts.toList()
  }

  override suspend fun getLikedHunts(userId: String): List<Hunt> {
    return getProfile(userId).likedHunts.toList()
  }

  override suspend fun addDoneHunt(userId: String, hunt: Hunt) {
    val profile =
        profiles.find { it.uid == userId }
            ?: throw IllegalArgumentException(
                String.format(ProfileRepositoryStrings.PROFILE_NOT_FOUND, userId))

    if (profile.doneHunts.none { it.uid == hunt.uid }) {
      profile.doneHunts.add(hunt)
    }
  }

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
}
