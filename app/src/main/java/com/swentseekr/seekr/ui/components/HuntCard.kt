package com.swentseekr.seekr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.DifficultyColor
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.theme.RedLike
import com.swentseekr.seekr.ui.theme.White

/**
 * Displays a card representing a hunt with title, author, image, difficulty, distance, and time.
 */
@Composable
fun HuntCard(hunt: Hunt, modifier: Modifier = Modifier) {
  Card(
      modifier =
          modifier
              .padding(HuntCardScreenDefaults.CardPadding)
              .fillMaxWidth(HuntCardScreenDefaults.CardWidthFraction)
              .border(
                  HuntCardScreenDefaults.CardBorderWidthSmall,
                  MaterialTheme.colorScheme.primary,
                  RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)),
      colors = CardDefaults.cardColors(containerColor = HuntCardScreenDefaults.CardBackgroundColor),
      elevation =
          CardDefaults.cardElevation(defaultElevation = HuntCardScreenDefaults.CardElevation),
      shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        start = HuntCardScreenDefaults.ColumnStartingPadding,
                        end = HuntCardScreenDefaults.ColumnEndingPadding,
                        top = HuntCardScreenDefaults.ColumnTopPadding,
                        bottom = HuntCardScreenDefaults.ColumnBottomPadding)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    hunt.title,
                    fontSize = HuntCardScreenDefaults.TitleFontSize,
                    fontWeight = FontWeight.Bold,
                    modifier =
                        Modifier.weight(HuntCardScreenDefaults.TitleWeight)
                            .padding(HuntCardScreenDefaults.InfoTextPadding))
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = HuntCardScreenStrings.LikeButton,
                    modifier =
                        Modifier.testTag(HuntCardScreenStrings.LikeButton)
                            .padding(HuntCardScreenDefaults.InfoColumnPadding),
                    tint = RedLike)
              }

              Text(
                  "${HuntCardScreenStrings.By} ${hunt.authorId}",
                  modifier = Modifier.padding(horizontal = HuntCardScreenDefaults.InfoTextPadding))

              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model =
                            hunt.mainImageUrl.takeIf {
                              it.isNotBlank()
                            }, // hunt.mainImageUrl.ifBlank { null },
                        contentDescription = HuntCardScreenStrings.HuntPictureDescription,
                        modifier =
                            Modifier.padding(horizontal = HuntCardScreenDefaults.ImagePadding)
                                .size(HuntCardScreenDefaults.ImageSize)
                                .clip(RoundedCornerShape(HuntCardScreenDefaults.ImageRoundness)),
                        placeholder = painterResource(R.drawable.empty_image),
                        error = painterResource(R.drawable.empty_image))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                          StatsBox(hunt.difficulty.toString(), DifficultyColor(hunt.difficulty))
                          Spacer(
                              modifier = modifier.height(HuntCardScreenDefaults.spacerHeightSmall))
                          StatsBox("${hunt.distance} ${HuntCardScreenStrings.DistanceUnit}", White)
                          Spacer(
                              modifier = modifier.height(HuntCardScreenDefaults.spacerHeightSmall))
                          StatsBox("${hunt.time} ${HuntCardScreenStrings.TimeUnit}", White)
                        }
                  }
            }
      }
}

@Composable
fun StatsBox(title: String, backColor: Color, modifier: Modifier = Modifier) {
  Box(
      modifier =
          modifier
              .background(backColor, RoundedCornerShape(HuntCardScreenDefaults.statBoxCornerRadius))
              .height(HuntCardScreenDefaults.statBoxHeight)
              .width(HuntCardScreenDefaults.statBoxWidth)
              .clip(RoundedCornerShape(HuntCardScreenDefaults.statBoxCornerRadius))
              .padding(HuntCardScreenDefaults.statBoxPadding),
  ) {
    Text(
        title,
        textAlign = TextAlign.Center,
        modifier = modifier.align(Alignment.Center).padding(HuntCardScreenDefaults.TextPadding),
    )
  }
}

/** Preview of [HuntCard] composable for Android Studio. */
@Preview
@Composable
fun HuntCardPreview() {
  val hunt =
      Hunt(
          uid = "hunt123",
          start = Location(40.7128, -74.0060, "New York"),
          end = Location(40.730610, -73.935242, "Brooklyn"),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = "City Exploration",
          description = "Discover hidden gems in the city",
          time = 2.5,
          distance = 5.0,
          difficulty = Difficulty.DIFFICULT,
          authorId = "0",
          mainImageUrl = "",
          otherImagesUrls = emptyList(),
          reviewRate = 4.5)
  HuntCard(hunt, Modifier.padding(2.dp))
}
