package com.swent.seekr.model.hunt

import com.swent.seekr.model.author.Author
import com.swent.seekr.model.map.Location

data class Hunt(
    val start: Location,
    val end: Location,
    val middlePoints: List<Location>,
    val status: HuntStatus,
    val title: String,
    val description: String,
    val time: Double,
    val distance: Double,
    val difficulty: Difficulty,
    val author: Author,
    val image: Int,
)

enum class HuntStatus {
  FUN,
  DISCOVER,
  SPORT
}

enum class Difficulty {
  EASY,
  INTERMEDIATE,
  DIFFICULT
}
