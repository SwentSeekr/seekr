package com.swentseekr.seekr.model.profile

import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants.MAX_BIO_LENGTH
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants.MAX_PSEUDONYM_LENGTH
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants.MIN_PSEUDONYM_LENGTH
import com.swentseekr.seekr.ui.profile.Profile

/** Utility class for working with [Profile] objects. */
class ProfileUtils {

  // -------------------
  // Validation functions
  // -------------------

  /**
   * Checks whether the given [pseudonym] is valid. Must be non-blank and within the allowed length
   * range.
   *
   * @param pseudonym The pseudonym to validate.
   * @return True if valid, false otherwise.
   */
  fun isValidPseudonym(pseudonym: String): Boolean {
    return pseudonym.isNotBlank() && pseudonym.length in MIN_PSEUDONYM_LENGTH..MAX_PSEUDONYM_LENGTH
  }

  /**
   * Checks whether the given [bio] is valid. Must not exceed the maximum allowed length.
   *
   * @param bio The biography text to validate.
   * @return True if valid, false otherwise.
   */
  fun isValidBio(bio: String): Boolean {
    return bio.length <= MAX_BIO_LENGTH
  }

  /**
   * Checks whether the given [profilePicture] is valid. Must be a non-negative integer
   * (representing a drawable resource ID).
   *
   * @param profilePicture The profile picture resource ID.
   * @return True if valid, false otherwise.
   */
  fun isValidProfilePicture(profilePicture: Int): Boolean {
    return profilePicture >= 0
  }

  // -------------------
  // Internal helper
  // -------------------

  /**
   * Generic helper to update a list of hunts in a profile.
   *
   * @param profile The original profile.
   * @param currentList The current hunt list to modify.
   * @param modifier Lambda to modify the mutable list.
   * @param copyWith Lambda to create a new profile with the updated list.
   * @return A new [Profile] instance with the updated list.
   */
  private fun updateHuntList(
      profile: Profile,
      currentList: List<Hunt>,
      modifier: MutableList<Hunt>.() -> Unit,
      copyWith: (Profile, MutableList<Hunt>) -> Profile
  ): Profile {
    val updatedList = currentList.toMutableList().apply(modifier)
    return copyWith(profile, updatedList)
  }

  // -------------------
  // Add functions
  // -------------------

  /**
   * Adds a [Hunt] to the user's liked hunts list in an immutable way.
   *
   * @param profile The original profile.
   * @param hunt The hunt to add to the liked hunts.
   * @return A new [Profile] instance with the updated liked hunts list.
   */
  fun addLikedHunt(profile: Profile, hunt: Hunt): Profile {
    return updateHuntList(profile, profile.likedHunts, { add(hunt) }) { p, l ->
      p.copy(likedHunts = l)
    }
  }

  /**
   * Adds a [Hunt] to the user's created hunts (myHunts) list in an immutable way.
   *
   * @param profile The original profile.
   * @param hunt The hunt to add to the user's created hunts.
   * @return A new [Profile] instance with the updated myHunts list.
   */
  fun addMyHunt(profile: Profile, hunt: Hunt): Profile {
    return updateHuntList(profile, profile.myHunts, { add(hunt) }) { p, l -> p.copy(myHunts = l) }
  }

  /**
   * Adds a [Hunt] to the user's completed hunts (doneHunts) list in an immutable way.
   *
   * @param profile The original profile.
   * @param hunt The hunt to mark as completed.
   * @return A new [Profile] instance with the updated doneHunts list.
   */
  fun addDoneHunt(profile: Profile, hunt: Hunt): Profile {
    return updateHuntList(profile, profile.doneHunts, { add(hunt) }) { p, l ->
      p.copy(doneHunts = l)
    }
  }

  // -------------------
  // Remove functions
  // -------------------

  /**
   * Removes a [Hunt] from the user's liked hunts list in an immutable way.
   *
   * @param profile The original profile.
   * @param hunt The hunt to remove from liked hunts.
   * @return A new [Profile] instance with the updated liked hunts list.
   */
  fun removeLikedHunt(profile: Profile, hunt: Hunt): Profile {
    return updateHuntList(profile, profile.likedHunts, { remove(hunt) }) { p, l ->
      p.copy(likedHunts = l)
    }
  }

  /**
   * Removes a [Hunt] from the user's created hunts (myHunts) list in an immutable way.
   *
   * @param profile The original profile.
   * @param hunt The hunt to remove from myHunts.
   * @return A new [Profile] instance with the updated myHunts list.
   */
  fun removeMyHunt(profile: Profile, hunt: Hunt): Profile {
    return updateHuntList(profile, profile.myHunts, { remove(hunt) }) { p, l ->
      p.copy(myHunts = l)
    }
  }

  /**
   * Removes a [Hunt] from the user's completed hunts (doneHunts) list in an immutable way.
   *
   * @param profile The original profile.
   * @param hunt The hunt to remove from doneHunts.
   * @return A new [Profile] instance with the updated doneHunts list.
   */
  fun removeDoneHunt(profile: Profile, hunt: Hunt): Profile {
    return updateHuntList(profile, profile.doneHunts, { remove(hunt) }) { p, l ->
      p.copy(doneHunts = l)
    }
  }

  // -------------------
  // Update functions
  // -------------------

  /**
   * Updates the pseudonym of the profile's author.
   *
   * @param profile The profile to update.
   * @param newPseudonym The new pseudonym.
   * @return A new [Profile] with the updated pseudonym.
   */
  fun updatePseudonym(profile: Profile, newPseudonym: String): Profile {
    val updatedAuthor = profile.author.copy(pseudonym = newPseudonym)
    return profile.copy(author = updatedAuthor)
  }

  /**
   * Updates the bio of the profile's author.
   *
   * @param profile The profile to update.
   * @param newBio The new biography text.
   * @return A new [Profile] with the updated bio.
   */
  fun updateBio(profile: Profile, newBio: String): Profile {
    val updatedAuthor = profile.author.copy(bio = newBio)
    return profile.copy(author = updatedAuthor)
  }

  /**
   * Updates the profile picture of the profile's author.
   *
   * @param profile The profile to update.
   * @param newProfilePicture The new profile picture resource ID.
   * @return A new [Profile] with the updated profile picture.
   */
  fun updateProfilePicture(profile: Profile, newProfilePicture: Int): Profile {
    val updatedAuthor = profile.author.copy(profilePicture = newProfilePicture)
    return profile.copy(author = updatedAuthor)
  }
}
