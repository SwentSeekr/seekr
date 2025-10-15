package com.swentseekr.seekr.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location

// The comment parts are for the like button feature,
// it will be added as soon as the OverviewViewModel and Ui work together
/**
 * Displays a card representing a hunt with title, author, image, difficulty, distance, and time.
 *
 * @param hunt The object containing all hunt details to display.
 */
@Composable
fun HuntCard(
    // huntUiState: HuntUiState,
    // onLikeClick: (String) -> Unit = {}
    hunt: Hunt,
    modifier: Modifier = Modifier
) {
  // val hunt = huntUiState.hunt
  Card(modifier = modifier.padding(8.dp).fillMaxWidth().height(150.dp)) {
    Column {
      Row {
        Text(
            hunt.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f).padding(4.dp))
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Like Button",
            modifier = Modifier.testTag("").padding(4.dp),
            // .clickable { onLikeClick(hunt.uid) },
            // tint = if(huntUiState.isLiked) Color.Red else Color.Gray)
            tint = Color.Red)
      }
      Text("by ${hunt.authorId}", modifier = Modifier.padding(horizontal = 4.dp))
      Row {
        Image(
            painter = painterResource(id = hunt.image),
            contentDescription = "Hunt Picture",
            modifier = Modifier.padding(horizontal = 4.dp).size(100.dp).clip(RectangleShape))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Box(
                  modifier =
                      Modifier.padding(4.dp)
                          .background(Color.Green)
                          .height(20.dp)
                          .width(80.dp)
                          .clip(RectangleShape),
              ) {
                Text(
                    hunt.difficulty.toString(),
                    modifier = Modifier.align(Alignment.Center).padding(2.dp))
              }

              Box(
                  modifier =
                      Modifier.padding(4.dp)
                          .background(Color.White)
                          .height(20.dp)
                          .width(80.dp)
                          .clip(RectangleShape)) {
                    Text(
                        "${hunt.distance} km",
                        modifier = Modifier.align(Alignment.Center).padding(2.dp))
                  }
              Box(
                  modifier =
                      Modifier.padding(4.dp)
                          .background(Color.White)
                          .height(20.dp)
                          .width(80.dp)
                          .clip(RectangleShape),
              ) {
                /*Icon(
                    imageVector = Icons.Filled.Schedule ,
                    contentDescription = "Time Icon",
                    modifier = Modifier.testTag("")
                ),*/
                Text("${hunt.time} min", modifier = Modifier.align(Alignment.Center).padding(2.dp))
              }
            }
      }
    }
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
          image = R.drawable.ic_launcher_foreground, // ou une image de ton projet
          reviewRate = 4.5)
  var isLiked by remember { mutableStateOf(false) }
  HuntCard(
      hunt, Modifier.padding(2.dp)
      // HuntUiState(hunt, isLiked = isLiked),
      // onLikeClick = { isLiked = !isLiked }
      )
}
