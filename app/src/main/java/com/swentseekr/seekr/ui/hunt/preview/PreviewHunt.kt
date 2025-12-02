package com.swentseekr.seekr.ui.hunt.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.DifficultyColor
import com.swentseekr.seekr.ui.components.HuntCardScreenDefaults
import com.swentseekr.seekr.ui.components.HuntCardScreenStrings
import com.swentseekr.seekr.ui.hunt.HuntUIState

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
                    contentDescription = STRINGS.BACK_CONTENT_DESC)
              }
            })
      },
      modifier = Modifier.testTag(TEST_TAGS.PREVIEW_HUNT_SCREEN)) { innerPadding ->
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = HuntCardScreenDefaults.ScreenPaddingHorizontal)
                    .verticalScroll(scroll),
            verticalArrangement = Arrangement.Top) {
              // Main card – try to mirror HuntCardScreen’s card
              Card(
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(
                              top = HuntCardScreenDefaults.ScreenPaddingTop,
                              bottom = HuntCardScreenDefaults.ScreenPaddingBottom)
                          .height(HuntCardScreenDefaults.ScreenHuntCardHeight),
                  colors =
                      CardDefaults.cardColors(
                          containerColor = HuntCardScreenDefaults.CardBackgroundColor),
                  shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius),
                  elevation = CardDefaults.cardElevation(HuntCardScreenDefaults.CardElevation)) {
                    Column(
                        modifier =
                            Modifier.padding(HuntCardScreenDefaults.CardInnerPadding)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                        verticalArrangement =
                            Arrangement.spacedBy(HuntCardScreenDefaults.InfoColumnPadding)) {
                          PreviewHeaderSection(ui)

                          PreviewImageAndStatsSection(ui)

                          PreviewDescriptionSection(ui)

                          PreviewStatusAndPointsSection(ui)
                        }
                  }

              // Confirm button under the card
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
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

/** Title + "by you" header, visually similar to HuntHeaderSection. */
@Composable
private fun PreviewHeaderSection(ui: HuntUIState, modifier: Modifier = Modifier) {
  Column(modifier = modifier.padding(HuntCardScreenDefaults.InfoColumnPadding).fillMaxWidth()) {
    Text(
        text = ui.title.ifBlank { STRINGS.HUNT_TITLE_FALLBACK },
        fontSize = HuntCardScreenDefaults.TitleFontSize,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier =
            Modifier.fillMaxWidth()
                .padding(HuntCardScreenDefaults.InfoTextPadding)
                .testTag(TEST_TAGS.HUNT_TITLE))

    Text(
        text = "${HuntCardScreenStrings.By} ${STRINGS.AUTHOR_PREVIEW}",
        modifier =
            Modifier.padding(horizontal = HuntCardScreenDefaults.InfoTextPadding)
                .testTag(TEST_TAGS.HUNT_AUTHOR_PREVIEW),
        style = MaterialTheme.typography.bodyMedium)

    Spacer(modifier = Modifier.height(HuntCardScreenDefaults.AuthorImageSpacing))
  }
}

/**
 * Row with image preview on the left and stats badges on the right (difficulty, distance, time).
 */
@Composable
private fun PreviewImageAndStatsSection(ui: HuntUIState, modifier: Modifier = Modifier) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
    PreviewImageSection(
        ui = ui,
        modifier =
            Modifier.weight(HuntCardScreenDefaults.ImageCarouselWeight)
                .padding(end = HuntCardScreenDefaults.ImageCarouselPadding))

    PreviewStatsSection(
        ui = ui, modifier = Modifier.weight(HuntCardScreenDefaults.StatsColumnWeight))
  }
}

/**
 * Simple “carousel-like” image area:
 * - main image in a big card
 * - other images in a horizontal strip below.
 */
@Composable
private fun PreviewImageSection(ui: HuntUIState, modifier: Modifier = Modifier) {
  val images: List<Any> = buildList {
    val main = ui.mainImageUrl.takeIf { it.isNotBlank() }
    if (main != null) {
      add(main)
    } else {
      add(R.drawable.empty_image)
    }

    ui.otherImagesUris.map { it.toString() }.filter { it.isNotBlank() }.forEach { add(it) }
  }

  Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    // Main big image
    Card(
        modifier =
            Modifier.fillMaxWidth()
                .height(HuntCardScreenDefaults.ImageCarouselHeight)
                .clip(RoundedCornerShape(HuntCardScreenDefaults.ImageCarouselCornerRadius)),
        elevation = CardDefaults.cardElevation(HuntCardScreenDefaults.CardElevation)) {
          val first = images.firstOrNull()
          if (first is String) {
            AsyncImage(
                model = first,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                placeholder = painterResource(R.drawable.empty_image),
                error = painterResource(R.drawable.empty_image),
                contentScale = ContentScale.Crop)
          } else {
            Image(
                painter = painterResource(R.drawable.empty_image),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop)
          }
        }

    // Small thumbnails for other images (if any)
    if (images.size > 1) {
      Spacer(modifier = Modifier.height(HuntCardScreenDefaults.SectionSpacing))
      Row(
          modifier = Modifier.horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(UI_CONST.THUMBNAIL_SPACING)) {
            images.drop(1).forEach { img ->
              Card(
                  modifier = Modifier.size(UI_CONST.THUMBNAIL_SIZE),
                  elevation = CardDefaults.cardElevation(HuntCardScreenDefaults.CardElevation),
                  shape = RoundedCornerShape(HuntCardScreenDefaults.ImageCarouselCornerRadius)) {
                    if (img is String) {
                      AsyncImage(
                          model = img,
                          contentDescription = null,
                          modifier = Modifier.fillMaxSize(),
                          placeholder = painterResource(R.drawable.empty_image),
                          error = painterResource(R.drawable.empty_image),
                          contentScale = ContentScale.Crop)
                    } else {
                      Image(
                          painter = painterResource(R.drawable.empty_image),
                          contentDescription = null,
                          modifier = Modifier.fillMaxSize(),
                          contentScale = ContentScale.Crop)
                    }
                  }
            }
          }
    }
  }
}

/** Stats badges: difficulty, distance, time – trying to mimic the HuntCard stats column. */
@Composable
private fun PreviewStatsSection(ui: HuntUIState, modifier: Modifier = Modifier) {
  Column(
      modifier = modifier,
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement =
          Arrangement.spacedBy(HuntCardScreenDefaults.BadgePadding, Alignment.CenterVertically)) {
        val difficultyLabel = ui.difficulty?.name ?: STRINGS.NOT_SET
        val difficultyColor =
            ui.difficulty?.let { DifficultyColor(it) } ?: HuntCardScreenDefaults.NeutralBadgeColor

        PreviewStatBox(
            label = difficultyLabel,
            color = difficultyColor,
            modifier = Modifier.testTag(TEST_TAGS.HUNT_DIFFICULTY))

        PreviewStatBox(
            label =
                if (ui.distance.isNotBlank()) {
                  "${ui.distance} ${HuntCardScreenStrings.DistanceUnit}"
                } else {
                  STRINGS.NOT_SET
                },
            color = HuntCardScreenDefaults.NeutralBadgeColor,
            modifier = Modifier.testTag(TEST_TAGS.HUNT_DISTANCE))

        PreviewStatBox(
            label =
                if (ui.time.isNotBlank()) {
                  "${ui.time} ${HuntCardScreenStrings.HourUnit}"
                } else {
                  STRINGS.NOT_SET
                },
            color = HuntCardScreenDefaults.NeutralBadgeColor,
            modifier = Modifier.testTag(TEST_TAGS.HUNT_TIME))
      }
}

/** Small pill-like stat box used only in preview. */
@Composable
private fun PreviewStatBox(label: String, color: Color, modifier: Modifier = Modifier) {
  Box(
      modifier =
          modifier
              .clip(RoundedCornerShape(HuntCardScreenDefaults.statBoxCornerRadius))
              .background(color)
              .padding(HuntCardScreenDefaults.statBoxPadding)
              .size(
                  width = HuntCardScreenDefaults.statBoxWidth,
                  height = HuntCardScreenDefaults.statBoxHeight),
      contentAlignment = Alignment.Center) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
      }
}

/** Description + tags for tests. */
@Composable
private fun PreviewDescriptionSection(ui: HuntUIState, modifier: Modifier = Modifier) {
  val descriptionText = ui.description.ifBlank { STRINGS.NO_DESCRIPTION }

  Text(
      text = descriptionText,
      style = MaterialTheme.typography.bodyMedium,
      modifier =
          modifier
              .padding(HuntCardScreenDefaults.SectionSpacing)
              .testTag(TEST_TAGS.HUNT_DESCRIPTION))
}

/** Status + number of points. */
@Composable
private fun PreviewStatusAndPointsSection(ui: HuntUIState, modifier: Modifier = Modifier) {
  Column(
      modifier =
          modifier.fillMaxWidth().padding(horizontal = HuntCardScreenDefaults.SectionSpacing)) {
        Row {
          Text(
              text = "${STRINGS.HUNT_STATUS} ",
              fontWeight = FontWeight.SemiBold,
          )
          Text(
              text = ui.status?.name ?: STRINGS.NOT_SET,
              modifier = Modifier.testTag(TEST_TAGS.HUNT_STATUS),
          )
        }

        Spacer(modifier = Modifier.height(UI_CONST.SMALL_SPACER_HEIGHT))

        Row {
          Text(
              text = "${STRINGS.HUNT_POINTS} ",
              fontWeight = FontWeight.SemiBold,
          )
          Text(
              text = ui.points.size.toString(),
              modifier = Modifier.testTag(TEST_TAGS.HUNT_POINTS),
          )
        }
      }
}
