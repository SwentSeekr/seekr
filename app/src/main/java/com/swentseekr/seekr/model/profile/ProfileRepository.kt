package com.swentseekr.seekr.model.profile

import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.profile.Profile

interface ProfileRepository {

  suspend fun createProfile(profile: Profile)

  suspend fun getProfile(userId: String): Profile?

  suspend fun updateProfile(profile: Profile)

  suspend fun getMyHunts(userId: String): List<Hunt>

  suspend fun getDoneHunts(userId: String): List<Hunt>

  suspend fun getLikedHunts(userId: String): List<Hunt>
}
