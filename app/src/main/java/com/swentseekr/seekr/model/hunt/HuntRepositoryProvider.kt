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
  val defaultHunt =
      Hunt(
          uid = HuntRepositoryProviderConstantsString.HUNT_ID,
          start = Location(HuntRepositoryProviderConstantsDefault.START_LATITUDE,
              HuntRepositoryProviderConstantsDefault.START_LONGITUDE,
              HuntRepositoryProviderConstantsString.START_NAME),
          end = Location(HuntRepositoryProviderConstantsDefault.END_LATITUDE, HuntRepositoryProviderConstantsDefault.END_LONGITUDE,
              HuntRepositoryProviderConstantsString.END_NAME),
          middlePoints =
              listOf(
                  Location(HuntRepositoryProviderConstantsDefault.SECOND_LATITUDE, HuntRepositoryProviderConstantsDefault.SECONDE_LONGITUDE,
                      HuntRepositoryProviderConstantsString.SECOND_NAME),
                  Location(HuntRepositoryProviderConstantsDefault.OTHER_LATITUDE, HuntRepositoryProviderConstantsDefault.OTHER_LONGITUDE,
                      HuntRepositoryProviderConstantsString.OTHER_NAME)),
          status = HuntStatus.FUN,
          title = HuntRepositoryProviderConstantsString.TITLE,
          description = HuntRepositoryProviderConstantsString.DESCRIPTION,
          time = HuntRepositoryProviderConstantsDefault.TIME,
          distance = HuntRepositoryProviderConstantsDefault.DISTANCE,
          difficulty = Difficulty.EASY,
          authorId = HuntRepositoryProviderConstantsString.AUTHOR_ID,
          mainImageUrl = HuntRepositoryProviderConstantsDefault.IMAGE.toString(),
          reviewRate = HuntRepositoryProviderConstantsDefault.RATING)

    // Unused anymore so no need magic values can remove this
  private val _repository: HuntsRepository =
      HuntsRepositoryLocal().apply {
        val sampleHunts =
            listOf(
                defaultHunt,
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
                    mainImageUrl = 1.toString(),
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
                    authorId = "2",
                    mainImageUrl = 1.toString(),
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
                    authorId = "3",
                    mainImageUrl = 2.toString(),
                    reviewRate = 4.7))

        runBlocking { sampleHunts.forEach { hunt -> addHunt(hunt) } }
      }

  private val _repositoryFirestore: HuntsRepository by lazy {
    HuntsRepositoryFirestore(Firebase.firestore)
  }
  var repository: HuntsRepository = _repositoryFirestore
}
