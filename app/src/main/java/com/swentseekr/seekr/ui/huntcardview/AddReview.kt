package com.swentseekr.seekr.ui.huntcardview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location

object AddReviewScreenTestTags {
  const val GO_BACK_BUTTON = "HuntCardReview_GoBackButton"
  const val INFO_COLLUMN = "HuntCardReview_InfoCollumn"
  const val RATE_TEXTFIELD = "HuntCardReview_RateTextField"
  const val COMMENT_TEXTFIELD = "HuntCardReview_CommentTextField"
  const val BUTTONS_ROW = "HuntCardReview_ButtonsRow"
  const val CANCEL_BUTTON = "HuntCardReview_CancelButton"
  const val DONE_BUTTON = "HuntCardReview_DoneButton"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    huntId: String,
    modifier: Modifier = Modifier,
    // reviewViewModel: HuntCardReviewViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onDone: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
  // val uiState by reviewViewModel.uiState.collectAsState()
  // LaunchedEffect(huntId) { reviewViewModel.loadHunt(huntId) }
  // val hunt = uiState.hunt
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
  var rating by remember { mutableStateOf("") }
  var comment by remember { mutableStateOf("") }
  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "Add Review Hunt",
                  fontSize = 24.sp,
                  fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                  modifier = modifier.padding(vertical = 16.dp))
            },
            navigationIcon = {
              IconButton(
                  modifier = modifier.testTag(AddReviewScreenTestTags.GO_BACK_BUTTON),
                  onClick = onGoBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back")
                  }
            },
            modifier = modifier.background(Color.LightGray))
      },
      modifier = modifier.fillMaxWidth()) { innerPadding ->
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .testTag(AddReviewScreenTestTags.INFO_COLLUMN),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              Spacer(modifier = modifier.height(16.dp))
              Text(
                  text = hunt.title,
                  fontSize = 24.sp,
                  fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                  modifier = modifier.padding(vertical = 4.dp))
              Text(
                  text = hunt.uid,
                  fontSize = 14.sp,
              )
              Spacer(modifier = modifier.height(16.dp))

              OutlinedTextField(
                  value = rating,
                  onValueChange = { rating = it },
                  modifier =
                      modifier
                          .fillMaxWidth(0.9f)
                          .padding(vertical = 8.dp)
                          .testTag(AddReviewScreenTestTags.RATE_TEXTFIELD),
                  placeholder = { Text("Rate between 1 and 5") },
                  singleLine = true,
                  shape = RoundedCornerShape(12.dp))

              Box(modifier = modifier.fillMaxWidth(0.9f).padding()) {
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .padding(vertical = 8.dp)
                            .testTag(AddReviewScreenTestTags.COMMENT_TEXTFIELD),
                    placeholder = { Text("Leave a comment...") },
                    textStyle = LocalTextStyle.current.copy(lineHeight = 20.sp),
                    singleLine = false,
                    maxLines = 15,
                    shape = RoundedCornerShape(12.dp))
              }

              Row(
                  modifier =
                      modifier
                          .fillMaxWidth()
                          .padding()
                          .align(Alignment.CenterHorizontally)
                          .testTag(AddReviewScreenTestTags.BUTTONS_ROW),
                  horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = onCancel,
                        modifier = modifier.testTag(AddReviewScreenTestTags.CANCEL_BUTTON)) {
                          Text("Cancel")
                        }
                    Button(
                        onClick = onDone,
                        modifier = modifier.testTag(AddReviewScreenTestTags.DONE_BUTTON)) {
                          Text("Done")
                        }
                  }
            }
      }
}

@Preview
@Composable
fun AddReviewScreenPreview() {
  AddReviewScreen("hunt123")
}
