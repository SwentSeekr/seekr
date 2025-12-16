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

/**
 * Composable screen to preview a hunt before confirmation.
 *
 * Displays all hunt details including images, stats, description, map, status, and points. Reuses
 * existing HuntCardScreen components like [HuntImageCarousel], [ModernStatCard],
 * [ModernMapSection], and [ModernDifficultyBadge].
 *
 * @param viewModel ViewModel providing the [HuntUIState] to display.
 * @param modifier Modifier for styling this composable.
 * @param onConfirm Lambda invoked when the user confirms the preview.
 * @param onGoBack Lambda invoked when the user navigates back.
 */
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
      containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = modifier.fillMaxSize().padding(innerPadding).verticalScroll(scroll),
            verticalArrangement = Arrangement.Top) {
              PreviewHeroSection(ui)
              PreviewStatsSection(ui)
              PreviewDescriptionCard(ui)
              PreviewMapCard(ui)
              PreviewStatusPointsCard(ui)
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
 * Hero section displaying the main image carousel, difficulty badge, title, and author.
 *
 * @param ui The current [HuntUIState] used to render the section.
 */
@Composable
private fun PreviewHeroSection(ui: HuntUIState) {
  val previewHunt = uiStateToHunt(ui)

  Box(modifier = Modifier.fillMaxWidth().aspectRatio(HuntCardScreenDefaults.AspectRatioHero)) {
    HuntImageCarousel(hunt = previewHunt, modifier = Modifier.fillMaxWidth())

    ModernDifficultyBadge(
        difficulty = previewHunt.difficulty,
        modifier = Modifier.align(Alignment.TopStart).padding(HuntCardScreenDefaults.Padding16))

    Column(
        modifier =
            Modifier.align(Alignment.BottomStart).padding(HuntCardScreenDefaults.Padding20)) {
          Text(
              text = ui.title.ifBlank { STRINGS.HUNT_TITLE_FALLBACK },
              style = MaterialTheme.typography.headlineLarge,
              color = MaterialTheme.colorScheme.onPrimary,
              lineHeight = HuntCardScreenDefaults.LineHeight,
              modifier = Modifier.testTag(TEST_TAGS.HUNT_TITLE))

          Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding8))

          Text(
              text = "${HuntCardScreenStrings.BY} ${STRINGS.AUTHOR_PREVIEW}",
              style = MaterialTheme.typography.bodyMedium,
              color =
                  MaterialTheme.colorScheme.onPrimary.copy(alpha = HuntCardScreenDefaults.ALPHA),
              fontWeight = FontWeight.Medium,
              modifier = Modifier.testTag(TEST_TAGS.HUNT_AUTHOR_PREVIEW))
        }
  }
}

/**
 * Stats section showing distance and time. Reuses ModernStatCard from HuntCardScreen.
 *
 * @param ui The current [HuntUIState] used to render the section.
 */
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
            label = HuntCardScreenStrings.DISTANCE_LABEL,
            value = ui.distance.ifBlank { STRINGS.NOT_SET },
            unit = if (ui.distance.isNotBlank()) HuntCardScreenStrings.DISTANCE_UNIT else "",
            modifier =
                Modifier.weight(HuntCardScreenDefaults.CardWeight).testTag(TEST_TAGS.HUNT_DISTANCE))

        Spacer(modifier = Modifier.width(HuntCardScreenDefaults.Padding12))

        ModernStatCard(
            label = HuntCardScreenStrings.DURATION_LABEL,
            value = ui.time.ifBlank { STRINGS.NOT_SET },
            unit =
                if (ui.time.isNotBlank()) HuntCardScreenStrings.HOUR_UNIT
                else PreviewHuntStrings.TIME_BLANK,
            modifier =
                Modifier.weight(HuntCardScreenDefaults.CardWeight).testTag(TEST_TAGS.HUNT_TIME))
      }
}

/**
 * Description card. Reuses ModernDescriptionSection from HuntCardScreen.
 *
 * @param ui The current [HuntUIState] used to render the section.
 */
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
              text = HuntCardScreenStrings.DESCRIPTION_LABEL,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface)

          Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding12))

          Text(
              text = descriptionText,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
}

/**
 * Map preview card. Reuses ModernMapSection from HuntCardScreen if start point is available.
 *
 * @param ui The current [HuntUIState] used to render the section.
 */
@Composable
private fun PreviewMapCard(ui: HuntUIState) {
  if (ui.points.isNotEmpty()) {
    val previewHunt = uiStateToHunt(ui)
    ModernMapSection(hunt = previewHunt)
  }
}

/**
 * Status and points information card.
 *
 * @param ui The current [HuntUIState] used to render the section.
 */
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
              text = PreviewHuntStrings.DETAILS_HUNT,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface)

          Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding12))

          Row {
            Text(
                text = "${STRINGS.HUNT_STATUS} ",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = ui.status?.name ?: STRINGS.NOT_SET,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(TEST_TAGS.HUNT_STATUS))
          }

          Spacer(modifier = Modifier.height(HuntCardScreenDefaults.Padding8))

          Row {
            Text(
                text = "${STRINGS.HUNT_POINTS} ",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = ui.points.size.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(TEST_TAGS.HUNT_POINTS))
          }
        }
      }
}
/**
 * Helper function to convert HuntUIState to Hunt for reusing existing components. This creates a
 * temporary Hunt object with the preview data.
 *
 * @param ui The current [HuntUIState] used to render the section.
 */
private fun uiStateToHunt(ui: HuntUIState): Hunt {
  val defaultLocation =
      Location(
          latitude = PreviewHuntConstantsDefault.DEFAULT_LATITUDE,
          longitude = PreviewHuntConstantsDefault.DEFAULT_LONGITUDE,
          name = PreviewHuntStrings.PREVIEW_LOCATION)

  val start = ui.points.firstOrNull() ?: defaultLocation
  val end = ui.points.lastOrNull() ?: defaultLocation
  val middlePoints =
      if (ui.points.size > PreviewHuntConstantsDefault.MIDDLE_VALUE_MIN_VALUE)
          ui.points.subList(
              PreviewHuntConstantsDefault.ONE, ui.points.size - PreviewHuntConstantsDefault.ONE)
      else emptyList()

  return Hunt(
      uid = PreviewHuntStrings.HUNT_ID,
      start = start,
      end = end,
      middlePoints = middlePoints,
      status = ui.status ?: HuntStatus.FUN,
      title = ui.title.ifBlank { STRINGS.HUNT_TITLE_FALLBACK },
      description = ui.description.ifBlank { STRINGS.NO_DESCRIPTION },
      time = ui.time.toDoubleOrNull() ?: PreviewHuntConstantsDefault.DEFAULT_VALUE_TIME,
      distance = ui.distance.toDoubleOrNull() ?: PreviewHuntConstantsDefault.DEFAULT_VALUE_DISTANCE,
      difficulty = ui.difficulty ?: Difficulty.EASY,
      authorId = PreviewHuntStrings.AUTHOR_ID,
      otherImagesUrls = ui.otherImagesUris.map { it.toString() },
      mainImageUrl = ui.mainImageUrl,
      reviewRate = PreviewHuntConstantsDefault.DEFAULT_VALUE_RATING)
}
