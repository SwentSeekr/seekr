package com.swentseekr.seekr.ui.preview

import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.hunt.HuntUIState

/** Shared constants for the `Preview` tests. */
object PreviewTestConstants {
  val exampleUiState =
      HuntUIState(
          title = "Treasure Hunt",
          description = "A fun adventure",
          time = "30",
          distance = "2.5",
          difficulty = Difficulty.INTERMEDIATE,
          status = HuntStatus.FUN,
          points =
              listOf(Location(1.0, 1.0, "A"), Location(2.0, 2.0, "B"), Location(3.0, 3.0, "C")),
          mainImageUrl = "",
          otherImagesUris = emptyList(),
          reviewRate = 4.5)

  val invalidUiState =
      HuntUIState(
          title = "",
          description = "",
          time = "",
          distance = "",
          difficulty = null,
          status = null,
          points = emptyList())
}
