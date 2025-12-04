package com.swentseekr.seekr.ui.hunt.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.components.HuntCardScreenDefaults
import com.swentseekr.seekr.ui.components.HuntCardScreenStrings
import com.swentseekr.seekr.ui.components.HuntImageCarousel
import com.swentseekr.seekr.ui.components.ModernDifficultyBadge
import com.swentseekr.seekr.ui.components.ModernMapSection
import com.swentseekr.seekr.ui.components.ModernStatCard
import com.swentseekr.seekr.ui.hunt.HuntUIState

val STRINGS = PreviewHuntStrings
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
      modifier = Modifier.testTag(TEST_TAGS.PREVIEW_HUNT_SCREEN),
      containerColor = HuntCardScreenDefaults.ScreenBackground) { innerPadding ->
        Column(
            modifier = modifier.fillMaxSize().padding(innerPadding).verticalScroll(scroll),
            verticalArrangement = Arrangement.Top) {
              // Hero Section with Image Carousel
              PreviewHeroSection(ui)

              // Stats Section
              PreviewStatsSection(ui)

              // Description Section
              PreviewDescriptionCard(ui)

              // Map Section
              PreviewMapCard(ui)

              // Status and Points Section
              PreviewStatusPointsCard(ui)

              // Confirm button
              Row(
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(
                              horizontal = HuntCardScreenDefaults.Padding20,
                              vertical = HuntCardScreenDefaults.Padding12),
                  horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onConfirm,
                        modifier = modifier.testTag(TEST_TAGS.CONFIRM_BUTTON),
                        enabled = ui.isValid,
                        shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)) {
                          Text(STRINGS.CONFIRM_BUTTON)
                        }
                  }

              Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding40))
            }
      }
}

/**
 * Hero section with image carousel, difficulty badge, title and author. Reuses HuntImageCarousel
 * and ModernDifficultyBadge from HuntCardScreen.
 */
@Composable
private fun PreviewHeroSection(ui: HuntUIState) {
  // Convert UIState to Hunt for the carousel
  val previewHunt = uiStateToHunt(ui)

  Box(modifier = Modifier.fillMaxWidth().aspectRatio(HuntCardScreenDefaults.AspectRatioHero)) {
    // Reuse the existing HuntImageCarousel
    HuntImageCarousel(hunt = previewHunt, modifier = Modifier.fillMaxWidth())

    // Difficulty badge overlay
    ModernDifficultyBadge(
        difficulty = previewHunt.difficulty,
        modifier = Modifier.align(Alignment.TopStart).padding(HuntCardScreenDefaults.Padding16))

    // Title and author overlay
    Column(
        modifier =
            Modifier.align(Alignment.BottomStart).padding(HuntCardScreenDefaults.Padding20)) {
          Text(
              text = ui.title.ifBlank { STRINGS.HUNT_TITLE_FALLBACK },
              fontSize = HuntCardScreenDefaults.TitleFontSize,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimary,
              lineHeight = HuntCardScreenDefaults.LineHeight,
              modifier = Modifier.testTag(TEST_TAGS.HUNT_TITLE))

          Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding8))

          Text(
              text = "${HuntCardScreenStrings.By} ${STRINGS.AUTHOR_PREVIEW}",
              fontSize = HuntCardScreenDefaults.AuthorFontSize,
              color =
                  MaterialTheme.colorScheme.onPrimary.copy(alpha = HuntCardScreenDefaults.Alpha),
              fontWeight = FontWeight.Medium,
              modifier = Modifier.testTag(TEST_TAGS.HUNT_AUTHOR_PREVIEW))
        }
  }
}

/** Stats section showing distance and time. Reuses ModernStatCard from HuntCardScreen. */
@Composable
private fun PreviewStatsSection(ui: HuntUIState) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(
                  horizontal = HuntCardScreenDefaults.Padding20,
                  vertical = HuntCardScreenDefaults.Padding20),
      horizontalArrangement = Arrangement.SpaceEvenly) {
        ModernStatCard(
            label = HuntCardScreenStrings.DistanceLabel,
            value = ui.distance.ifBlank { STRINGS.NOT_SET },
            unit = if (ui.distance.isNotBlank()) HuntCardScreenStrings.DistanceUnit else "",
            modifier =
                Modifier.weight(HuntCardScreenDefaults.CardWeight).testTag(TEST_TAGS.HUNT_DISTANCE))

        Spacer(modifier = Modifier.width(HuntCardScreenDefaults.Padding12))

        ModernStatCard(
            label = HuntCardScreenStrings.DurationLabel,
            value = ui.time.ifBlank { STRINGS.NOT_SET },
            unit = if (ui.time.isNotBlank()) HuntCardScreenStrings.HourUnit else "",
            modifier =
                Modifier.weight(HuntCardScreenDefaults.CardWeight).testTag(TEST_TAGS.HUNT_TIME))
      }
}

/** Description card. Reuses ModernDescriptionSection from HuntCardScreen. */
@Composable
private fun PreviewDescriptionCard(ui: HuntUIState) {
  val descriptionText = ui.description.ifBlank { STRINGS.NO_DESCRIPTION }

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(
                  horizontal = HuntCardScreenDefaults.Padding20,
                  vertical = HuntCardScreenDefaults.Padding12)
              .testTag(TEST_TAGS.HUNT_DESCRIPTION),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
      elevation =
          CardDefaults.cardElevation(defaultElevation = HuntCardScreenDefaults.CardElevation),
      shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)) {
        Column(modifier = Modifier.padding(HuntCardScreenDefaults.Padding20)) {
          Text(
              text = HuntCardScreenStrings.DescriptionLabel,
              fontSize = HuntCardScreenDefaults.SmallFontSize,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface)

          Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding12))

          Text(
              text = descriptionText,
              fontSize = HuntCardScreenDefaults.DescriptionFontSize,
              lineHeight = HuntCardScreenDefaults.DescriptionLineHeight,
              color = HuntCardScreenDefaults.ParagraphGray)
        }
      }
}

/** Map preview card. Reuses ModernMapSection from HuntCardScreen if start point is available. */
@Composable
private fun PreviewMapCard(ui: HuntUIState) {
  // Only show map if there's a valid start point
  if (ui.points.isNotEmpty()) {
    val previewHunt = uiStateToHunt(ui)
    ModernMapSection(hunt = previewHunt)
  }
}

/** Status and points information card. */
@Composable
private fun PreviewStatusPointsCard(ui: HuntUIState) {
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(
                  horizontal = HuntCardScreenDefaults.Padding20,
                  vertical = HuntCardScreenDefaults.Padding12),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
      elevation =
          CardDefaults.cardElevation(defaultElevation = HuntCardScreenDefaults.CardElevation),
      shape = RoundedCornerShape(HuntCardScreenDefaults.CornerRadius)) {
        Column(modifier = Modifier.padding(HuntCardScreenDefaults.Padding20)) {
          Text(
              text = "Hunt Details",
              fontSize = HuntCardScreenDefaults.SmallFontSize,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface)

          Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding12))

          // Status row
          Row {
            Text(
                text = "${STRINGS.HUNT_STATUS} ",
                fontWeight = FontWeight.SemiBold,
                fontSize = HuntCardScreenDefaults.DescriptionFontSize,
                color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = ui.status?.name ?: STRINGS.NOT_SET,
                fontSize = HuntCardScreenDefaults.DescriptionFontSize,
                color = HuntCardScreenDefaults.ParagraphGray,
                modifier = Modifier.testTag(TEST_TAGS.HUNT_STATUS))
          }

          Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding8))

          // Points row
          Row {
            Text(
                text = "${STRINGS.HUNT_POINTS} ",
                fontWeight = FontWeight.SemiBold,
                fontSize = HuntCardScreenDefaults.DescriptionFontSize,
                color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = ui.points.size.toString(),
                fontSize = HuntCardScreenDefaults.DescriptionFontSize,
                color = HuntCardScreenDefaults.ParagraphGray,
                modifier = Modifier.testTag(TEST_TAGS.HUNT_POINTS))
          }
        }
      }
}
/**
 * Helper function to convert HuntUIState to Hunt for reusing existing components. This creates a
 * temporary Hunt object with the preview data.
 */
private fun uiStateToHunt(ui: HuntUIState): Hunt {
  val defaultLocation = Location(latitude = 0.0, longitude = 0.0, name = "Preview Location")

  val start = ui.points.firstOrNull() ?: defaultLocation
  val end = ui.points.lastOrNull() ?: defaultLocation
  val middlePoints =
      if (ui.points.size > 2) ui.points.subList(1, ui.points.size - 1) else emptyList()

  return Hunt(
      uid = "preview",
      start = start,
      end = end,
      middlePoints = middlePoints,
      status = ui.status ?: HuntStatus.FUN, // fallback status for preview
      title = ui.title.ifBlank { STRINGS.HUNT_TITLE_FALLBACK },
      description = ui.description.ifBlank { STRINGS.NO_DESCRIPTION },
      time = ui.time.toDoubleOrNull() ?: 0.0,
      distance = ui.distance.toDoubleOrNull() ?: 0.0,
      difficulty = ui.difficulty ?: Difficulty.EASY,
      authorId = "preview",
      otherImagesUrls = ui.otherImagesUris.map { it.toString() },
      mainImageUrl = ui.mainImageUrl,
      reviewRate = 0.0 // preview has no real rating
      )
}
