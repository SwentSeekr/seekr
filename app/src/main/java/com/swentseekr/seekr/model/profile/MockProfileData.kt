package com.swentseekr.seekr.model.profile

import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.profile.Profile

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
          image = R.drawable.ic_launcher_foreground,
          reviewRate = 4.5)

  return Profile(
      uid = "user123",
      author = sampleAuthor(),
      myHunts = mutableListOf(sampleHunt),
      doneHunts = mutableListOf(),
      likedHunts = mutableListOf())
}

fun sampleAuthor() =
    Author(
        pseudonym = "Spike Man",
        bio = "Adventurer",
        profilePicture = R.drawable.profile_picture,
        reviewRate = 4.5,
        sportRate = 4.8)

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
        image = R.drawable.empty_user,
        reviewRate = 4.0)

fun emptyProfile(): Profile {
  return sampleProfile(myHunts = emptyList(), doneHunts = emptyList(), likedHunts = emptyList())
}
