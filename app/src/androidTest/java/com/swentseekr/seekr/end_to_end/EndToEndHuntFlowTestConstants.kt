package com.swentseekr.seekr.end_to_end

/**
 * Constants dedicated to the `EndToEndHuntFlowTest` scenario.
 *
 * Keeping these values centralized ensures the UI assertions and the supporting fake repositories
 * always rely on the exact same literals (title, times, coordinates, etc.).
 */
object EndToEndHuntFlowTestConstants {
  const val HUNT_TITLE = "Gorge de l'Areuse"
  const val HUNT_DESCRIPTION = "End-to-end verification hunt."
  const val HUNT_TIME_HOURS = "1.5"
  const val HUNT_DISTANCE_KM = "3.2"
  const val USER_PSEUDONYM = "QA Pathfinder"
  const val USER_BIO = "Testing hunts straight from the lab."
  const val START_NAME = "Origin Marker"
  const val END_NAME = "Summit Marker"
  const val START_LAT = 46.956361
  const val START_LNG = 6.830579
  const val END_LAT = 46.962813
  const val END_LNG = 6.820793
  const val REVIEW_RATE = 4.8
  const val DEFAULT_USER_ID = "end-to-end-user"
}
