package com.android.sample.model.hunt

import com.android.sample.model.author.Author
import com.android.sample.model.map.Location

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
