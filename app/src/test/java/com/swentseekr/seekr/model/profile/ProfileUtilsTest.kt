package com.swentseekr.seekr.model.profile

import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.profile.EditProfileNumberConstants
import com.swentseekr.seekr.ui.profile.Profile
import org.junit.Assert.*
import org.junit.Test

class ProfileUtilsTest {

  private val utils = ProfileUtils()

  private fun author(
      pseudonym: String = "Alice",
      bio: String = "bio",
      profilePicture: Int = 1,
      reviewRate: Double = 0.0,
      sportRate: Double = 0.0
  ) =
      Author(
          pseudonym = pseudonym,
          bio = bio,
          profilePicture = profilePicture,
          reviewRate = reviewRate,
          sportRate = sportRate)

  private fun location(lat: Double = 0.0, lon: Double = 0.0) = Location(lat, lon, "Loc")

  private fun hunt(uid: String = "h1", title: String = "Title", reviewRate: Double = 0.0) =
      Hunt(
          uid = uid,
          start = location(),
          end = location(),
          middlePoints = emptyList(),
          status = HuntStatus.DISCOVER,
          title = title,
          description = "",
          time = 0.0,
          distance = 0.0,
          difficulty = Difficulty.EASY,
          authorId = "author1",
          mainImageUrl = "",
          reviewRate = reviewRate)

  private fun profile(
      uid: String = "p1",
      author: Author = author(),
      my: MutableList<Hunt> = mutableListOf(),
      done: MutableList<Hunt> = mutableListOf(),
      liked: MutableList<Hunt> = mutableListOf()
  ) = Profile(uid = uid, author = author, myHunts = my, doneHunts = done, likedHunts = liked)

  @Test
  fun isValidPseudonym() {
    assertTrue(utils.isValidPseudonym("Bob"))
    assertFalse(utils.isValidPseudonym(""))
    assertFalse(utils.isValidPseudonym("ab")) // too short
    val tooLong = "a".repeat(EditProfileNumberConstants.MAX_PSEUDONYM_LENGTH + 1)
    assertFalse(utils.isValidPseudonym(tooLong)) // too long
  }

  @Test
  fun isValidBio() {
    assertTrue(utils.isValidBio(""))
    val ok = "a".repeat(EditProfileNumberConstants.MAX_BIO_LENGTH)
    assertTrue(utils.isValidBio(ok))
    val tooLong = "a".repeat(EditProfileNumberConstants.MAX_BIO_LENGTH + 1)
    assertFalse(utils.isValidBio(tooLong))
  }

  @Test
  fun isValidProfilePicture() {
    assertTrue(utils.isValidProfilePicture(0))
    assertTrue(utils.isValidProfilePicture(5))
    assertFalse(utils.isValidProfilePicture(-1))
  }

  @Test
  fun addAndRemoveLikedHunt() {
    val h = hunt(uid = "liked1", title = "Liked")
    val p = profile()

    val afterAdd = utils.addLikedHunt(p, h)
    assertTrue(afterAdd.likedHunts.contains(h))
    assertFalse(p.likedHunts.contains(h)) // original unchanged

    val afterRemove = utils.removeLikedHunt(afterAdd, h)
    assertFalse(afterRemove.likedHunts.contains(h))
  }

  @Test
  fun addAndRemoveMyHunt() {
    val h = hunt(uid = "my1", title = "My")
    val p = profile()

    val afterAdd = utils.addMyHunt(p, h)
    assertTrue(afterAdd.myHunts.contains(h))
    assertFalse(p.myHunts.contains(h))

    val afterRemove = utils.removeMyHunt(afterAdd, h)
    assertFalse(afterRemove.myHunts.contains(h))
  }

  @Test
  fun addAndRemoveDoneHunt() {
    val h = hunt(uid = "done1", title = "Done")
    val p = profile()

    val afterAdd = utils.addDoneHunt(p, h)
    assertTrue(afterAdd.doneHunts.contains(h))
    assertFalse(p.doneHunts.contains(h))

    val afterRemove = utils.removeDoneHunt(afterAdd, h)
    assertFalse(afterRemove.doneHunts.contains(h))
  }

  @Test
  fun updatePseudonym() {
    val origAuthor = author(pseudonym = "Old")
    val p = profile(author = origAuthor)

    val updated = utils.updatePseudonym(p, "New")
    assertEquals("New", updated.author.pseudonym)
    assertEquals(origAuthor.bio, updated.author.bio)
    assertEquals(origAuthor.profilePicture, updated.author.profilePicture)
  }

  @Test
  fun updateBio() {
    val origAuthor = author(bio = "OldBio")
    val p = profile(author = origAuthor)

    val updated = utils.updateBio(p, "NewBio")
    assertEquals("NewBio", updated.author.bio)
    assertEquals(origAuthor.pseudonym, updated.author.pseudonym)
  }

  @Test
  fun updateProfilePicture() {
    val origAuthor = author(profilePicture = 2)
    val p = profile(author = origAuthor)

    val updated = utils.updateProfilePicture(p, 99)
    assertEquals(99, updated.author.profilePicture)
    assertEquals(origAuthor.pseudonym, updated.author.pseudonym)
  }
}
