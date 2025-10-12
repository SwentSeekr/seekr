package com.swentseekr.seekr.model.hunt

import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.map.Location
import kotlinx.coroutines.runBlocking

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object HuntRepositoryProvider {
  private val _repository: HuntsRepository =
      HuntsRepositoryLocal().apply {
        val sampleHunts =
            listOf(
                Hunt(
                    uid = getNewUid(),
                    start = Location(40.7128, -74.0060, "New York"),
                    end = Location(40.730610, -73.935242, "Brooklyn"),
                    middlePoints = emptyList(),
                    status = HuntStatus.FUN,
                    title = "City Exploration",
                    description = "Discover hidden gems in the city",
                    time = 2.5,
                    distance = 5.0,
                    difficulty = Difficulty.EASY,
                    author = Author("spike man", "", 1, 2.5, 3.0),
                    image = 0,
                    reviewRate = 4.5),
                Hunt(
                    uid = getNewUid(),
                    start = Location(34.0522, -118.2437, "Los Angeles"),
                    end = Location(34.0522, -118.2437, "Hollywood"),
                    middlePoints = emptyList(),
                    status = HuntStatus.DISCOVER,
                    title = "Hollywood Walk",
                    description = "Explore the stars' homes",
                    time = 3.0,
                    distance = 6.0,
                    difficulty = Difficulty.INTERMEDIATE,
                    author = Author("holly wood", "", 2, 3.5, 4.0),
                    image = 1,
                    reviewRate = 4.0),
            )
        runBlocking { sampleHunts.forEach { hunt -> addHunt(hunt) } }
        // sampleHunts.forEach { hunt -> GlobalScope.launch { addHunt(hunt) } }
      }
  var repository: HuntsRepository = HuntRepositoryProvider._repository
}
