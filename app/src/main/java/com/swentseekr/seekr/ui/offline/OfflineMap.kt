package com.swentseekr.seekr.ui.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

/**
 * Offline placeholder for the map tab.
 *
 * This screen is displayed when:
 * - The user navigates to the map tab, and
 * - The app is in offline mode (no network connectivity).
 *
 * Responsibilities:
 * - Provide a clear, lightweight indication that the map is unavailable offline.
 * - Use a visually distinct card with an icon and explanatory message, without attempting to load
 *   any map data or network resources.
 *
 * Visual structure:
 * - Fullscreen [Surface] using the theme background color.
 * - Centered card with:
 *     - Warning icon.
 *     - Localized offline message text.
 *
 * The exact dimensions, shapes, and colors are delegated to [OfflineConstants] so they can be
 * reused across offline UI components.
 *
 * This composable is typically used from the offline navigation graph:
 * [com.swentseekr.seekr.ui.navigation.SeekrOfflineNavHost].
 *
 * @param modifier Optional [Modifier] for customizing the root [Surface] container.
 */
@Composable
fun OfflineMapScreen(modifier: Modifier = Modifier) {
  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Box(
          modifier =
              Modifier.fillMaxWidth(OfflineConstants.OFFLINE_CARD_WIDTH_RATIO)
                  .height(OfflineConstants.OFFLINE_CARD_HEIGHT)
                  .background(
                      color = MaterialTheme.colorScheme.surfaceContainer,
                      shape = OfflineConstants.CARD_SHAPE),
          contentAlignment = Alignment.Center) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
                  Icon(
                      imageVector = Icons.Default.Warning,
                      contentDescription = OfflineConstants.MAP_ICON,
                      modifier = Modifier.size(OfflineConstants.OFFLINE_ICON_SIZE),
                      tint = MaterialTheme.colorScheme.onBackground
                      )
                  Spacer(modifier = Modifier.height(OfflineConstants.ICON_SPACING))
                  Text(
                      text = OfflineConstants.OFFLINE_MAP_MESSAGE,
                      style = MaterialTheme.typography.titleMedium,
                      modifier = Modifier.fillMaxWidth(),
                      textAlign = TextAlign.Center)
                }
          }
    }
  }
}
