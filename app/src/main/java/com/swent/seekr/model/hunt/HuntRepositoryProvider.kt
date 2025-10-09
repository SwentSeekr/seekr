package com.swent.seekr.model.hunt

import com.swent.seekr.model.author.Author
import com.swent.seekr.model.map.Location

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
            )

      }
  var repository: HuntsRepository = HuntRepositoryProvider._repository
}
