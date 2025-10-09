package com.swentseekr.seekr.hunt

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
import com.swentseekr.seekr.model.author.Author
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location

@Composable
fun HuntCard(hunt: Hunt) {
  Card(modifier = Modifier.padding(8.dp).fillMaxWidth().height(150.dp)) {
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
            tint = Color.Red)
        // if (/hunt.isLiked/) Color.Red else Color.Gray)
      }
      Text("by ${hunt.author.pseudonym}", modifier = Modifier.padding(horizontal = 4.dp))
      Row {
        Image(
            painter = painterResource(id = hunt.image),
            contentDescription = "Hunt Picture",
            modifier = Modifier.padding(horizontal = 4.dp).size(100.dp).clip(RectangleShape))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
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
    // Rating(rating = hunt.review, RatingType.FUN)
  }
}

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
          author = Author("spike man", "", 1, 2.5, 3.0),
          image = R.drawable.ic_launcher_foreground, // ou une image de ton projet
          reviewRate = 4.5)
  HuntCard(hunt)
}
