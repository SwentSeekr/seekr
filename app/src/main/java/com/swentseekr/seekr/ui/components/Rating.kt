package com.swentseekr.seekr.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.swentseekr.seekr.R

val ICON_SIZE = 20.dp
const val MAX_RATING = 5.0
const val MIN_RATING = 0.0

object RatingTestTags {
  fun full(index: Int, type: RatingType) = "${type.name}_FULL_$index"

  fun half(type: RatingType) = "${type.name}_HALF"

  fun empty(index: Int, type: RatingType) = "${type.name}_EMPTY_$index"
}

/**
 * Displays a rating as a row of icons. Supports both star ratings and sport ratings.
 *
 * @param rating The numeric rating (0.0 - 5.0).
 * @param type The type of rating to display (STAR or SPORT).
 */
@Composable
fun Rating(rating: Double, type: RatingType) {
  check(rating in MIN_RATING..MAX_RATING) { "Rating must be between $MIN_RATING and $MAX_RATING" }

  val (fullImage, halfImage, emptyImage) =
      when (type) {
        RatingType.STAR -> Triple(R.drawable.full_star, R.drawable.half_star, R.drawable.empty_star)
        RatingType.SPORT ->
            Triple(R.drawable.full_sport, R.drawable.half_sport, R.drawable.empty_sport)
      }
  Row {
    val full = rating.toInt()
    val hasHalf = (rating - full) >= 0.5
    val empty = (MAX_RATING - full - if (hasHalf) 1 else 0).toInt()

    repeat(full) { index ->
      Image(
          painter = painterResource(fullImage),
          contentDescription = "Full Rating",
          modifier = Modifier.size(ICON_SIZE).testTag(RatingTestTags.full(index, type)))
    }
    if (hasHalf) {
      Image(
          painter = painterResource(halfImage),
          contentDescription = "Half Rating",
          modifier = Modifier.size(ICON_SIZE).testTag(RatingTestTags.half(type)))
    }
    repeat(empty) { index ->
      Image(
          painter = painterResource(emptyImage),
          contentDescription = "Empty Rating",
          modifier = Modifier.size(ICON_SIZE).testTag(RatingTestTags.empty(index, type)))
    }
  }
}

/** Enum representing the type of rating displayed in [Rating]. */
enum class RatingType {
  STAR,
  SPORT
}
