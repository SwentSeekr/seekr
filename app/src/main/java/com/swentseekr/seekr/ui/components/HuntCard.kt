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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location

/**
 * Displays a card representing a hunt with title, author, image, difficulty, distance, and time.
 */
@Composable
fun HuntCard(hunt: Hunt, modifier: Modifier = Modifier) {
  Card(
      modifier =
          modifier
              .padding(8.dp)
              .fillMaxWidth(0.85f)
              .border(2.dp, Color(0xFF60BA37), RoundedCornerShape(12.dp)),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFF8DEB6)),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    hunt.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).padding(4.dp))
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Like Button",
                    modifier = Modifier.testTag("HuntCard_LikeButton").padding(4.dp),
                    tint = Color.Red)
              }

              Text("by ${hunt.authorId}", modifier = Modifier.padding(horizontal = 4.dp))

              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = hunt.mainImageUrl.ifBlank { null },
                        contentDescription = "Hunt Picture",
                        modifier =
                            Modifier.padding(horizontal = 4.dp)
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        placeholder = painterResource(R.drawable.empty_image),
                        error = painterResource(R.drawable.empty_image))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                          InfoBox(hunt.difficulty.toString(), Color.Green)
                          InfoBox("${hunt.distance} km", Color.White)
                          InfoBox("${hunt.time} min", Color.White)
                        }
                  }
            }
      }
}

@Composable
private fun InfoBox(text: String, backgroundColor: Color, modifier: Modifier = Modifier) {
  Box(
      modifier =
          modifier
              .padding(4.dp)
              .background(backgroundColor)
              .height(20.dp)
              .width(80.dp)
              .clip(RectangleShape)) {
        Text(text, modifier = Modifier.align(Alignment.Center).padding(2.dp))
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
