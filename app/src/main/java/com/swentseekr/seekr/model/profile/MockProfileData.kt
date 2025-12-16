package com.swentseekr.seekr.model.profile

import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.profile.Profile

/**
 * Utility functions to create sample [Profile], [Author], [Hunt], and [HuntReview] objects.
 *
 * These are used for testing, previews, and prototyping UI components.
 */

/** Returns a sample [Profile] containing one sample hunt and a sample author. */
fun mockProfileData(): Profile {

  val sampleHunt =
      Hunt(
          uid = "hunt123",
          start = Location(40.7128, -74.0060, "New York"),
          end = Location(40.730610, -73.935242, "Brooklyn"),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = "City Exploration",
          description = "Discover hidden gems in the city",
          time = 2.5,
          distance = 5.0,
          difficulty = Difficulty.DIFFICULT,
          authorId = "0",
          mainImageUrl = R.drawable.ic_launcher_foreground.toString(),
          otherImagesUrls = emptyList(),
          reviewRate = 4.5)

  return Profile(
      uid = "user123",
      author = sampleAuthor(),
      myHunts = mutableListOf(sampleHunt),
      doneHunts = mutableListOf(),
      likedHunts = mutableListOf())
}

/** Returns a sample [Author] object for testing. */
fun sampleAuthor() =
    Author(
        pseudonym = "Spike Man",
        bio = "Adventurer",
        profilePicture = R.drawable.profile_picture,
        reviewRate = 4.5,
        sportRate = 4.8)

/**
 * Returns a sample [Profile] with custom hunt lists and a unique [uid].
 *
 * @param myHunts Hunts created by the user.
 * @param doneHunts Hunts completed by the user.
 * @param likedHunts Hunts liked by the user.
 * @param uid Unique ID of the profile.
 */
fun sampleProfile(
    myHunts: List<Hunt> = emptyList(),
    doneHunts: List<Hunt> = emptyList(),
    likedHunts: List<Hunt> = emptyList(),
    uid: String = "user123"
): Profile {
  return Profile(
      uid = uid,
      author = sampleAuthor(),
      myHunts = myHunts.toMutableList(),
      doneHunts = doneHunts.toMutableList(),
      likedHunts = likedHunts.toMutableList())
}

/**
 * Returns a [Profile] with a custom pseudonym for the author.
 *
 * @param uid Unique ID of the profile.
 * @param pseudonym The pseudonym of the author.
 */
fun sampleProfileWithPseudonym(uid: String, pseudonym: String): Profile {
  return Profile(
      uid = uid,
      author =
          Author(
              pseudonym = pseudonym,
              bio = "Adventurer",
              profilePicture = R.drawable.profile_picture,
              reviewRate = 4.5,
              sportRate = 4.8),
      myHunts = mutableListOf(),
      doneHunts = mutableListOf(),
      likedHunts = mutableListOf())
}

/**
 * Creates a simple [Hunt] object with default values for testing or mock data.
 *
 * @param uid The unique identifier for the hunt.
 * @param title The title of the hunt.
 * @return A [Hunt] instance with the provided [uid] and [title], and default values for all other
 *   properties.
 */
fun createHunt(uid: String, title: String) =
    Hunt(
        uid = uid,
        start = Location(0.0, 0.0, "Start"),
        end = Location(1.0, 1.0, "End"),
        middlePoints = emptyList(),
        status = HuntStatus.FUN,
        title = title,
        description = "Desc $title",
        time = 1.0,
        distance = 2.0,
        difficulty = Difficulty.EASY,
        authorId = "0",
        mainImageUrl = R.drawable.empty_user.toString(),
        otherImagesUrls = emptyList(),
        reviewRate = 4.0)

/** Returns an empty [Profile] with no hunts. */
fun emptyProfile(): Profile {
  return sampleProfile(myHunts = emptyList(), doneHunts = emptyList(), likedHunts = emptyList())
}

/**
 * Creates a [Hunt] with a custom [reviewRate] and [difficulty].
 *
 * @param uid Hunt unique ID.
 * @param title Hunt title.
 * @param reviewRate Average review score.
 * @param difficulty Hunt difficulty.
 */
fun createHuntWithRateAndDifficulty(
    uid: String,
    title: String,
    reviewRate: Double = 4.0,
    difficulty: Difficulty = Difficulty.EASY
): Hunt {
  return Hunt(
      uid = uid,
      start = Location(0.0, 0.0, "Start"),
      end = Location(1.0, 1.0, "End"),
      middlePoints = emptyList(),
      status = HuntStatus.FUN,
      title = title,
      description = "Description for $title",
      time = 1.0,
      distance = 2.0,
      difficulty = difficulty,
      authorId = "0",
      mainImageUrl = R.drawable.empty_user.toString(),
      otherImagesUrls = emptyList(),
      reviewRate = reviewRate)
}

/**
 * Creates a [HuntReview] for testing purposes.
 *
 * @param reviewId Review unique ID.
 * @param authorId ID of the user who wrote the review.
 * @param huntId ID of the hunt being reviewed.
 * @param rating Review rating.
 * @param comment Review text.
 * @param photos List of photo URLs attached to the review.
 */
fun createReview(
    reviewId: String = "review123",
    authorId: String = "user123",
    huntId: String = "hunt123",
    rating: Double = 4.5,
    comment: String = "Great hunt! Really enjoyed the experience.",
    photos: List<String> = emptyList()
): HuntReview {
  return HuntReview(
      reviewId = reviewId,
      authorId = authorId,
      huntId = huntId,
      rating = rating,
      comment = comment,
      photos = photos)
}

/**
 * Creates a [Hunt] for overview testing.
 *
 * @param uid Hunt unique ID.
 * @param title Hunt title.
 * @param description Hunt description.
 * @param time Hunt duration in hours.
 * @param distance Hunt distance in km.
 */
fun createOverviewTestHunt(
    uid: String,
    title: String,
    description: String,
    time: Double,
    distance: Double
): Hunt {
  return Hunt(
      uid = uid,
      start = Location(46.5197, 6.6323, "Start Point"),
      end = Location(46.5207, 6.6333, "End Point"),
      middlePoints = emptyList(),
      status = HuntStatus.FUN,
      title = title,
      description = description,
      time = time,
      distance = distance,
      difficulty = Difficulty.EASY,
      authorId = "author_123",
      mainImageUrl = "",
      reviewRate = 4.5)
}
