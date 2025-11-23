package com.swentseekr.seekr.model.profile

import android.net.Uri
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.profile.Profile

class ProfileRepositoryLocal : ProfileRepository {
  private val profiles = mutableListOf<Profile>()

  fun size() = profiles.size

  fun addProfile(profile: Profile) {
    profiles.add(profile)
  }

  override suspend fun createProfile(profile: Profile) {
    require(!(profiles.any { it.uid == profile.uid })) {
      "Profile with ID ${profile.uid} already exists"
    }
    profiles.add(profile)
  }

  override suspend fun getProfile(userId: String): Profile {
    for (i in profiles.indices) {
      if (profiles[i].uid == userId) {
        return profiles[i]
      }
    }
    throw IllegalArgumentException("Profile with ID $userId not found")
  }

  override suspend fun updateProfile(profile: Profile) {
    for (i in profiles.indices) {
      if (profiles[i].uid == profile.uid) {
        profiles[i] = profile
        return
      }
    }
    throw IllegalArgumentException("Profile with ID ${profile.uid} is not found")
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
            ?: throw IllegalArgumentException("Profile with ID $userId not found")

    if (profile.doneHunts.none { it.uid == hunt.uid }) {
      profile.doneHunts.add(hunt)
    }
  }

  override suspend fun uploadProfilePicture(userId: String, uri: Uri): String {
    val fakeUrl = "local://profile-picture/$userId"
    val profile =
        profiles.find { it.uid == userId }
            ?: throw IllegalArgumentException("Profile with ID $userId not found")
    val updatedProfile = profile.copy(author = profile.author.copy(profilePictureUrl = fakeUrl))
    updateProfile(updatedProfile)
    return fakeUrl
  }

  override suspend fun checkUserNeedsOnboarding(userId: String): Boolean {
    TODO("Not yet implemented")
  }

  override suspend fun completeOnboarding(userId: String, pseudonym: String, bio: String) {
    TODO("Not yet implemented")
  }
}
