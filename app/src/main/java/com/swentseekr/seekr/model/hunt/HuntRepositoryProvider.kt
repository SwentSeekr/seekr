package com.swentseekr.seekr.model.hunt

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
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
                    middlePoints =
                        listOf(
                            Location(40.718, -73.999, "Chinatown"),
                            Location(40.725, -73.98, "East Village")),
                    status = HuntStatus.FUN,
                    title = "City Exploration",
                    description = "Discover hidden gems in the city",
                    time = 2.5,
                    distance = 5.0,
                    difficulty = Difficulty.EASY,
                    authorId = "0",
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
                    authorId = "1",
                    image = 1,
                    reviewRate = 4.0),
                Hunt(
                    uid = getNewUid(),
                    start = Location(34.0522, -118.2437, "Los Angeles Downtown"),
                    end = Location(34.1015, -118.3387, "Hollywood"),
                    middlePoints =
                        listOf(
                            Location(34.065, -118.30, "Koreatown"),
                            Location(34.083, -118.33, "Melrose Ave")),
                    status = HuntStatus.DISCOVER,
                    title = "Hollywood Walk",
                    description = "Explore the stars' homes and iconic streets of LA.",
                    time = 3.0,
                    distance = 6.0,
                    difficulty = Difficulty.INTERMEDIATE,
                    author = Author("Holly Wood", "", 2, 3.5, 4.0),
                    image = 1,
                    reviewRate = 4.0),
                Hunt(
                    uid = getNewUid(),
                    start = Location(48.8566, 2.3522, "Paris Center"),
                    end = Location(48.8606, 2.3376, "Louvre Museum"),
                    middlePoints =
                        listOf(
                            Location(48.853, 2.349, "Île de la Cité"),
                            Location(48.857, 2.341, "Pont Neuf")),
                    status = HuntStatus.DISCOVER,
                    title = "Paris Discovery",
                    description =
                        "A romantic walk through the heart of Paris, ending at the Louvre.",
                    time = 2.0,
                    distance = 4.0,
                    difficulty = Difficulty.INTERMEDIATE,
                    author = Author("Jean Dupont", "", 3, 3.0, 4.2),
                    image = 2,
                    reviewRate = 4.7))

        runBlocking { sampleHunts.forEach { hunt -> addHunt(hunt) } }
      }

  private val _repositoryFirestore: HuntsRepository by lazy {
    HuntsRepositoryFirestore(Firebase.firestore)
  }
  var repository: HuntsRepository = _repositoryFirestore
}
