package com.swentseekr.seekr.model.profile

import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.profile.Profile

class ProfileUtils {

  /* Validation functions */
  fun isValidPseudonym(pseudonym: String): Boolean {
    return pseudonym.isNotBlank() && pseudonym.length in 3..20
  }

  fun isValidBio(bio: String): Boolean {
    return bio.length <= 150
  }

  fun isValidProfilePicture(profilePicture: Int): Boolean {
    return profilePicture >= 0
  }

  /* Update functions */
  fun addLikedHunt(profile: Profile, hunt: Hunt): Profile {
    val updatedLikedHunts = profile.likedHunts.toMutableList().apply { add(hunt) }
    return profile.copy(likedHunts = updatedLikedHunts)
  }

  fun removeLikedHunt(profile: Profile, hunt: Hunt): Profile {
    val updatedLikedHunts = profile.likedHunts.toMutableList().apply { remove(hunt) }
    return profile.copy(likedHunts = updatedLikedHunts)
  }

  fun addMyHunt(profile: Profile, hunt: Hunt): Profile {
    val updatedMyHunts = profile.myHunts.toMutableList().apply { add(hunt) }
    return profile.copy(myHunts = updatedMyHunts)
  }

  fun removeMyHunt(profile: Profile, hunt: Hunt): Profile {
    val updatedMyHunts = profile.myHunts.toMutableList().apply { remove(hunt) }
    return profile.copy(myHunts = updatedMyHunts)
  }

  fun addDoneHunt(profile: Profile, hunt: Hunt): Profile {
    val updatedDoneHunts = profile.doneHunts.toMutableList().apply { add(hunt) }
    return profile.copy(doneHunts = updatedDoneHunts)
  }

  fun removeDoneHunt(profile: Profile, hunt: Hunt): Profile {
    val updatedDoneHunts = profile.doneHunts.toMutableList().apply { remove(hunt) }
    return profile.copy(doneHunts = updatedDoneHunts)
  }

  /* Update functions */
  fun updatePseudonym(profile: Profile, newPseudonym: String): Profile {
    val updatedAuthor = profile.author.copy(pseudonym = newPseudonym)
    return profile.copy(author = updatedAuthor)
  }

  fun updateBio(profile: Profile, newBio: String): Profile {
    val updatedAuthor = profile.author.copy(bio = newBio)
    return profile.copy(author = updatedAuthor)
  }

  fun updateProfilePicture(profile: Profile, newProfilePicture: Int): Profile {
    val updatedAuthor = profile.author.copy(profilePicture = newProfilePicture)
    return profile.copy(author = updatedAuthor)
  }

  /* Calculation functions */
  fun calculateOverallRating(reviewRate: Double, sportRate: Double): Double {
    return (reviewRate + sportRate) / 2
  }
}
