package com.swentseekr.seekr.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location

object HuntCardScreenTestTags {
  const val GO_BACK_BUTTON = "GoBackButton"
  const val TITLE_TEXT = "TitleText"
  const val AUTHOR_TEXT = "AuthorText"
  const val IMAGE = "HuntImage"
  const val DIFFICULTY_BOX = "DifficultyBox"
  const val DISTANCE_BOX = "DistanceBox"
  const val TIME_BOX = "TimeBox"
  const val DESCRIPTION_TEXT = "DescriptionText"
  const val MAP_CONTAINER = "MapContainer"
  const val BEGIN_BUTTON = "BeginButton"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuntCardScreen(
    // huntId: String,
    modifier: Modifier = Modifier,
    // huntCardViewModel: HuntCardViewModel = viewModel(),
    onGoBack: () -> Unit = {}
) {
  // val uiState by huntCardViewModel.uiState.collectAsState()

  // val hunt = huntCardViewModel.loadHunt(huntId)
  val author = "SpikeMan" // huntCardViewModel.loadAuthor(hunt.authorId)
  val hunt =
      Hunt(
          uid = "hunt123",
          start = Location(40.7128, -74.0060, "New York"),
          end = Location(40.730610, -73.935242, "Brooklyn"),
          middlePoints = emptyList(),
          status = HuntStatus.FUN,
          title = "City Exploration",
          description =
              "Discover hidden gems in the city åß∂ƒ@ªº∆¬±“#Ç[]|{}≠¿´‘¶–…«¬∆øπ§¢æ‘¡°œ∑€®️†Ωn hello kitty",
          time = 2.5,
          distance = 5.0,
          difficulty = Difficulty.DIFFICULT,
          authorId = "0",
          image = R.drawable.ic_launcher_foreground,
          reviewRate = 4.5)
  Scaffold(
      // BAR GOBACK ARROW
      topBar = {
        TopAppBar(
            title = { Text("") },
            navigationIcon = {
              IconButton(
                  modifier = Modifier.testTag(HuntCardScreenTestTags.GO_BACK_BUTTON),
                  onClick = { onGoBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back")
                  }
            },
            modifier = Modifier.background(Color.LightGray))
      },
      modifier = modifier.fillMaxSize()) { innerPadding ->

        // COLUMN FOR INFORMATIONS
        Box(modifier = modifier.fillMaxWidth().padding(8.dp)) {
          LazyColumn(modifier.padding(innerPadding).fillMaxSize()) {
            // ROW WITH IMAGE, TITLE, AUTHOR, DIFFICULTY, DISTANCE, TIME
            item {
              Column(modifier = modifier.padding(8.dp).fillMaxWidth().fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                      hunt.title,
                      fontSize = 20.sp,
                      fontWeight = FontWeight.Bold,
                      modifier =
                          Modifier.weight(1f)
                              .padding(4.dp)
                              .testTag(HuntCardScreenTestTags.TITLE_TEXT))
                }
                Text(
                    "by $author",
                    modifier =
                        Modifier.padding(horizontal = 4.dp)
                            .testTag(HuntCardScreenTestTags.AUTHOR_TEXT))
                Row {
                  Image(
                      painter = painterResource(id = hunt.image),
                      contentDescription = "Hunt Picture",
                      modifier =
                          Modifier.padding(horizontal = 4.dp)
                              .size(100.dp)
                              .clip(RectangleShape)
                              .testTag(HuntCardScreenTestTags.IMAGE))
                  Column(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier =
                                Modifier.padding(4.dp)
                                    .background(Color.Green)
                                    .height(20.dp)
                                    .width(80.dp)
                                    .clip(RectangleShape)
                                    .testTag(HuntCardScreenTestTags.DIFFICULTY_BOX),
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
                                    .clip(RectangleShape)
                                    .testTag(HuntCardScreenTestTags.DISTANCE_BOX),
                        ) {
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
                                    .clip(RectangleShape)
                                    .testTag(HuntCardScreenTestTags.TIME_BOX),
                        ) {
                          Text(
                              "${hunt.time} min",
                              modifier = Modifier.align(Alignment.Center).padding(2.dp))
                        }
                      }
                }
              }
            }

            // DESCRIPTION
            item {
              Text(
                  hunt.description,
                  modifier =
                      Modifier.padding(8.dp).testTag(HuntCardScreenTestTags.DESCRIPTION_TEXT))
            }
            // MAP WITH START POINT
            item {
              var mapLoaded by remember { mutableStateOf(true) }

              if (mapLoaded) {
                val startPosition = LatLng(hunt.start.latitude, hunt.start.longitude)
                val cameraPositionState = rememberCameraPositionState {
                  position = CameraPosition.fromLatLngZoom(startPosition, 12f)
                }

                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(400.dp)
                            .padding(8.dp)
                            .testTag(HuntCardScreenTestTags.MAP_CONTAINER)) {
                      GoogleMap(
                          modifier = Modifier.matchParentSize(),
                          cameraPositionState = cameraPositionState) {
                            Marker(
                                state = MarkerState(position = startPosition),
                                title = "Départ : ${hunt.start.name}",
                                snippet = "Point de départ de la chasse")
                          }
                    }
              }
            }
            // BOUTON BEGIN HUNT
            item {
              Row(
                  modifier = modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceEvenly,
              ) {
                Button(
                    {},
                    modifier =
                        modifier.width(120.dp).testTag(HuntCardScreenTestTags.BEGIN_BUTTON)) {
                      Text("Begin Hunt")
                    }
              }
            }
          }
        }
        // POSSIBILITY OF REVIEW IF ALREADY DONE ?
      }
}

@Preview
@Composable
fun HuntCardScreenPreview() {
  HuntCardScreen()
}
