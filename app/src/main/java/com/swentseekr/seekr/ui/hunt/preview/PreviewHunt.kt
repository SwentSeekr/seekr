package com.swentseekr.seekr.ui.hunt.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import coil.compose.rememberAsyncImagePainter
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings

val STRINGS = PreviewHuntStrings
val UI_CONST = PreviewHuntUi
val TEST_TAGS = PreviewHuntScreenTestTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewHuntScreen(
    viewModel: PreviewHuntViewModel,
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onGoBack: () -> Unit
) {

  val ui = viewModel.uiState.collectAsState().value
  val scroll = rememberScrollState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(STRINGS.PREVIEW_TITLE) },
            navigationIcon = {
              IconButton(onClick = onGoBack, modifier = Modifier.testTag(TEST_TAGS.BACK_BUTTON)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = BaseHuntFieldsStrings.BACK_CONTENT_DESC)
              }
            })
      },
      modifier = Modifier.testTag(TEST_TAGS.PREVIEW_HUNT_SCREEN)) { innerPadding ->
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(UI_CONST.COLUMM_PADDING)
                    .padding(innerPadding)
                    .verticalScroll(scroll)) {
              // --- Main Image ---
              if (ui.mainImageUrl.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(UI_CONST.IMAGE_CARD_HEIGHT),
                    elevation = CardDefaults.cardElevation(UI_CONST.IMAGE_CARD_ELEVATION)) {
                      Image(
                          painter = rememberAsyncImagePainter(ui.mainImageUrl),
                          contentDescription = null,
                          modifier = Modifier.fillMaxSize(),
                          contentScale = ContentScale.Crop)
                    }
                Spacer(modifier.height(UI_CONST.BIG_SPACER_HEIGHT))
              }

              Text(
                  STRINGS.HUNT_TITLE + { ui.title },
                  modifier = modifier.testTag(TEST_TAGS.HUNT_TITLE))
              Spacer(Modifier.height(UI_CONST.SMALL_SPACER_HEIGHT))

              Text(
                  STRINGS.HUNT_DESCRIPTION, modifier = modifier.testTag(TEST_TAGS.HUNT_DESCRIPTION))
              Text(ui.description)
              Spacer(Modifier.height(UI_CONST.MEDIUM_SPACER_HEIGHT))

              Text(
                  STRINGS.HUNT_TIME + { ui.time }, modifier = modifier.testTag(TEST_TAGS.HUNT_TIME))
              Spacer(Modifier.height(UI_CONST.SMALL_SPACER_HEIGHT))

              Text(
                  STRINGS.HUNT_DISTANCE + { ui.distance },
                  modifier = modifier.testTag(TEST_TAGS.HUNT_DISTANCE))
              Spacer(Modifier.height(UI_CONST.SMALL_SPACER_HEIGHT))

              Text(
                  STRINGS.HUNT_DIFFICULTY + { ui.difficulty?.name ?: STRINGS.NOT_SET },
                  modifier = modifier.testTag(TEST_TAGS.HUNT_DIFFICULTY))
              Spacer(Modifier.height(UI_CONST.SMALL_SPACER_HEIGHT))

              Text(
                  STRINGS.HUNT_STATUS + { ui.status?.name ?: STRINGS.NOT_SET },
                  modifier = modifier.testTag(TEST_TAGS.HUNT_STATUS))
              Spacer(Modifier.height(UI_CONST.SMALL_SPACER_HEIGHT))

              Text(
                  STRINGS.HUNT_POINTS + { ui.points.size },
                  modifier = modifier.testTag(TEST_TAGS.HUNT_POINTS))
              Spacer(Modifier.height(UI_CONST.BIG_SPACER_HEIGHT))

              // --- Other Images Preview ---
              if (ui.otherImagesUris.isNotEmpty()) {
                Text(STRINGS.OTHER_IMAGES)
                Spacer(Modifier.height(UI_CONST.SMALL_SPACER_HEIGHT))

                ui.otherImagesUris.forEach { uri ->
                  Card(
                      modifier =
                          Modifier.fillMaxWidth()
                              .height(UI_CONST.OTHER_IMAGE_CARD_HEIGHT)
                              .padding(vertical = UI_CONST.CARD_PADDING),
                      elevation = CardDefaults.cardElevation(UI_CONST.IMAGE_CARD_ELEVATION)) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop)
                      }
                }

                Spacer(Modifier.height(UI_CONST.BIG_SPACER_HEIGHT))
              }

              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = onConfirm,
                        modifier = modifier.testTag(TEST_TAGS.CONFIRM_BUTTON),
                        enabled = ui.isValid) {
                          Text(STRINGS.CONFIRM_BUTTON)
                        }
                  }
            }
      }
}
