package com.swentseekr.seekr.ui.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

/**
 * Overview screen displayed when the user is offline.
 *
 * This composable is shown in place of the regular overview when:
 * - The device has no active internet connection, and
 * - The app cannot fetch fresh content from the backend.
 *
 * Responsibilities:
 * - Inform the user that they are currently offline and that content may be limited.
 * - Provide a primary action to view any content that has been previously downloaded and is
 *   available for offline usage (e.g. cached hunts).
 *
 * Visual structure:
 * - A full-screen [Surface] using the theme background color.
 * - A centered card containing:
 *     - A warning icon.
 *     - A short explanatory offline message.
 * - A button, placed below the card, that allows navigation to "downloaded hunts".
 *
 * Layout and styling details (dimensions, shapes, colors) are delegated to [OfflineConstants] to
 * ensure consistency across all offline UI components.
 *
 * This composable is typically invoked from the root navigation when the app detects that it is
 * offline and wishes to show an offline entry point to the user.
 *
 * @param modifier Optional [Modifier] used to customize the root container.
 * @param onShowDownloadedHunts Callback invoked when the user taps the "Show downloaded hunts"
 *   button. The caller is responsible for navigating to the appropriate offline list screen.
 */
@Composable
fun OfflineOverviewScreen(
    modifier: Modifier = Modifier,
    onShowDownloadedHunts: () -> Unit = {},
) {
  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = OfflineConstants.SCREEN_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom) {
          Spacer(modifier = Modifier.height(OfflineConstants.OVERVIEW_TOP_SPACER_HEIGHT))

          Box(
              modifier =
                  Modifier.fillMaxWidth(OfflineConstants.OFFLINE_CARD_WIDTH_RATIO)
                      .height(OfflineConstants.OFFLINE_CARD_HEIGHT)
                      .background(
                          color = OfflineConstants.LIGHT_GREEN_BACKGROUND,
                          shape = OfflineConstants.CARD_SHAPE),
              contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                      Icon(
                          imageVector = Icons.Default.Warning,
                          contentDescription = OfflineConstants.OVERVIEW_ICON,
                          modifier = Modifier.size(OfflineConstants.OFFLINE_ICON_SIZE),
                          tint = MaterialTheme.colorScheme.onBackground // black
                          )
                      Spacer(modifier = Modifier.height(OfflineConstants.ICON_SPACING))
                      Text(
                          text = OfflineConstants.OFFLINE_OVERVIEW_MESSAGE,
                          style = MaterialTheme.typography.titleMedium,
                          modifier = Modifier.fillMaxWidth(),
                          textAlign = TextAlign.Center)
                    }
              }

          Spacer(modifier = Modifier.height(OfflineConstants.OVERVIEW_BUTTON_TOP_SPACER_HEIGHT))

          Button(
              onClick = onShowDownloadedHunts,
              shape = OfflineConstants.BUTTON_SHAPE,
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = OfflineConstants.BUTTON_CONTAINER_COLOR,
                      contentColor = MaterialTheme.colorScheme.onPrimary // white text
                      ),
              modifier = Modifier.fillMaxWidth(OfflineConstants.BUTTON_WIDTH_RATIO)) {
                Text(text = OfflineConstants.SHOW_DOWNLOADED_HUNTS_BUTTON)
              }

          Spacer(modifier = Modifier.height(OfflineConstants.OVERVIEW_BUTTON_BOTTOM_SPACER_HEIGHT))
        }
  }
}

/**
 * Design-time preview of [OfflineOverviewScreen].
 *
 * This preview showcases the offline overview layout, including:
 * - The offline information card.
 * - The "Show downloaded hunts" action button.
 *
 * It is intended for visual inspection and UI iteration in the IDE without requiring a running app
 * or a real offline state.
 */
@Preview
@Composable
fun OfflineOverviewScreenPreview() {
  OfflineOverviewScreen(onShowDownloadedHunts = {})
}
