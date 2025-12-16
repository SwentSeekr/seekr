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

/**
 * Provides stable test tags for rating icons, supporting full, half, and empty rating states.
 */
object RatingTestTags {
    /**
     * Returns a test tag for a full icon at the given index and rating type.
     *
     * @param index The index of the full rating icon (0-based).
     * @param type The type of rating (STAR or SPORT).
     * @return A unique test tag string for the full icon.
     */
  fun full(index: Int, type: RatingType) = "${type.name}${RatingConstantsStrings.FULL}$index"

    /**
     * Returns a test tag for the half icon of a given rating type.
     *
     * @param type The type of rating (STAR or SPORT).
     * @return A unique test tag string for the half icon.
     */
  fun half(type: RatingType) = "${type.name}${RatingConstantsStrings.HALF}"

    /**
     * Returns a test tag for an empty icon at the given index and rating type.
     *
     * @param index The index of the empty rating icon (0-based).
     * @param type The type of rating (STAR or SPORT).
     * @return A unique test tag string for the empty icon.
     */
  fun empty(index: Int, type: RatingType) = "${type.name}${RatingConstantsStrings.EMPTY}$index"
}

/**
 * Displays a rating as a row of icons. Supports both star ratings and sport ratings.
 *
 * @param rating The numeric rating (0.0 - 5.0).
 * @param type The type of rating to display (STAR or SPORT).
 *
 * @return A unique test tag string for the full icon.
 *
 */
@Composable
fun Rating(rating: Double, type: RatingType) {
  check(rating in MIN_RATING..MAX_RATING) {
    "${RatingConstantsStrings.RATING_CONDITION} $MIN_RATING ${RatingConstantsStrings.AND} $MAX_RATING"
  }

  val (fullImage, halfImage, emptyImage) =
      when (type) {
        RatingType.STAR -> Triple(R.drawable.full_star, R.drawable.half_star, R.drawable.empty_star)
        RatingType.SPORT ->
            Triple(R.drawable.full_sport, R.drawable.half_sport, R.drawable.empty_sport)
      }
  Row {
    val full = rating.toInt()
    val hasHalf = (rating - full) >= RatingConstantsDefault.HALF
    val empty =
        (MAX_RATING -
                full -
                if (hasHalf) RatingConstantsDefault.HAS_STAR_HALF
                else RatingConstantsDefault.HAS_NO_STAR_HALF)
            .toInt()

    repeat(full) { index ->
      Image(
          painter = painterResource(fullImage),
          contentDescription = RatingConstantsStrings.FULL_RATING,
          modifier = Modifier.size(ICON_SIZE).testTag(RatingTestTags.full(index, type)))
    }
    if (hasHalf) {
      Image(
          painter = painterResource(halfImage),
          contentDescription = RatingConstantsStrings.HALF_RATING,
          modifier = Modifier.size(ICON_SIZE).testTag(RatingTestTags.half(type)))
    }
    repeat(empty) { index ->
      Image(
          painter = painterResource(emptyImage),
          contentDescription = RatingConstantsStrings.EMPTY_RATING,
          modifier = Modifier.size(ICON_SIZE).testTag(RatingTestTags.empty(index, type)))
    }
  }
}

/** Enum representing the type of rating displayed in [Rating]. */
enum class RatingType {
    /** Star icons.*/
  STAR,
    /** Sport-themed icons.*/
  SPORT
}
