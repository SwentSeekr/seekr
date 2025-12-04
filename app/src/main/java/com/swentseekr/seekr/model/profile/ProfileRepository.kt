package com.swentseekr.seekr.model.profile

import android.net.Uri
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.profile.Profile

interface ProfileRepository {

  suspend fun createProfile(profile: Profile)

  suspend fun getProfile(userId: String): Profile?

  suspend fun updateProfile(profile: Profile)

  suspend fun getMyHunts(userId: String): List<Hunt>

  suspend fun getDoneHunts(userId: String): List<Hunt>

  suspend fun getLikedHunts(userId: String): List<Hunt>

  suspend fun addDoneHunt(userId: String, hunt: Hunt)

  suspend fun uploadProfilePicture(userId: String, uri: Uri): String

  suspend fun checkUserNeedsOnboarding(userId: String): Boolean

  suspend fun completeOnboarding(userId: String, pseudonym: String, bio: String)

  suspend fun deleteCurrentProfilePicture(userId: String, url: String)
  suspend fun addLikedHunt(userId: String, huntId: String)
  suspend fun removeLikedHunt(userId: String, huntId: String)

}
