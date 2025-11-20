package com.swentseekr.seekr.model.profile

import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants.MAX_BIO_LENGTH
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants.MAX_PSEUDONYM_LENGTH
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants.MIN_PSEUDONYM_LENGTH
import com.swentseekr.seekr.ui.profile.Profile

class ProfileUtils {

  /* Validation functions */
  fun isValidPseudonym(pseudonym: String): Boolean {
    return pseudonym.isNotBlank() && pseudonym.length in MIN_PSEUDONYM_LENGTH..MAX_PSEUDONYM_LENGTH
  }

  fun isValidBio(bio: String): Boolean {
    return bio.length <= MAX_BIO_LENGTH
  }

  fun isValidProfilePicture(profilePicture: Int): Boolean {
    return profilePicture >= 0
  }

  /* Helper to update hunt lists */
  private fun updateHuntList(
      profile: Profile,
      currentList: List<Hunt>,
      modifier: MutableList<Hunt>.() -> Unit,
      copyWith: (Profile, MutableList<Hunt>) -> Profile
  ): Profile {
    val updatedList = currentList.toMutableList().apply(modifier)
    return copyWith(profile, updatedList)
  }

  /* Add functions */
  fun addLikedHunt(profile: Profile, hunt: Hunt): Profile {
    return updateHuntList(profile, profile.likedHunts, { add(hunt) }) { p, l ->
      p.copy(likedHunts = l)
    }
  }

  fun addMyHunt(profile: Profile, hunt: Hunt): Profile {
    return updateHuntList(profile, profile.myHunts, { add(hunt) }) { p, l -> p.copy(myHunts = l) }
  }

  fun addDoneHunt(profile: Profile, hunt: Hunt): Profile {
    return updateHuntList(profile, profile.doneHunts, { add(hunt) }) { p, l ->
      p.copy(doneHunts = l)
    }
  }

  /* Remove functions */
  fun removeLikedHunt(profile: Profile, hunt: Hunt): Profile {
    return updateHuntList(profile, profile.likedHunts, { remove(hunt) }) { p, l ->
      p.copy(likedHunts = l)
    }
  }

  fun removeMyHunt(profile: Profile, hunt: Hunt): Profile {
    return updateHuntList(profile, profile.myHunts, { remove(hunt) }) { p, l ->
      p.copy(myHunts = l)
    }
  }

  fun removeDoneHunt(profile: Profile, hunt: Hunt): Profile {
    return updateHuntList(profile, profile.doneHunts, { remove(hunt) }) { p, l ->
      p.copy(doneHunts = l)
    }
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
}
