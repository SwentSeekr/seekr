package com.swentseekr.seekr.model.profile

import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.*
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.profile.Profile

fun mockProfileData(): Profile {
  val author =
      Author(
          pseudonym = "Spike Man",
          bio = "Avid adventurer and puzzle solver.",
          profilePicture = 0,
          reviewRate = 4.5,
          sportRate = 4.8)

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
          author = author,
          image = R.drawable.ic_launcher_foreground,
          reviewRate = 4.5)

  return Profile(
      uid = "user123",
      author = author,
      myHunts = mutableListOf(sampleHunt),
      doneHunts = mutableListOf(),
      likedHunts = mutableListOf())
}
