package com.swentseekr.seekr.ui.offline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview

/**
 * Full-screen offline requirement screen.
 *
 * This screen is displayed when:
 * - The user has no network connectivity, **and**
 * - The app has no sufficient cached data to offer a meaningful offline experience.
 *
 * Responsibilities:
 * - Clearly communicate that online connectivity is required to proceed.
 * - Provide a single, prominent action for the user to open system network settings.
 *
 * Visual structure:
 * - A centered column containing:
 *     - A warning icon.
 *     - A title indicating that the user is offline.
 *     - A short explanatory message.
 *     - A button that opens device settings (via [onOpenSettings]).
 *
 * Layout and colors are delegated to [OfflineConstants] and theme values to ensure a consistent
 * look across all offline-related screens.
 *
 * Typical usage:
 * - Invoked from [com.swentseekr.seekr.ui.navigation.SeekrRootApp] when the device is offline and
 *   no cached profile is available.
 *
 * @param modifier Optional [Modifier] to customize the root [Surface].
 * @param onOpenSettings Callback invoked when the user taps the "Open settings" button. The caller
 *   usually launches an intent to the system's network / wireless settings.
 */
@Composable
fun OfflineRequiredScreen(modifier: Modifier = Modifier, onOpenSettings: () -> Unit) {
  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(
        modifier = Modifier.fillMaxSize().padding(OfflineConstants.SCREEN_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
          Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = OfflineConstants.OFFLINE_REQUIRED_ICON_DESCRIPTION,
              tint = MaterialTheme.colorScheme.onBackground)

          Spacer(modifier = Modifier.height(OfflineConstants.ICON_SPACING))

          Text(
              text = OfflineConstants.OFFLINE_TITLE, style = MaterialTheme.typography.headlineSmall)

          Spacer(modifier = Modifier.height(OfflineConstants.MESSAGE_SPACING))

          Text(text = OfflineConstants.OFFLINE_MESSAGE, style = MaterialTheme.typography.bodyMedium)

          Spacer(modifier = Modifier.height(OfflineConstants.BUTTON_SPACING))

          Button(
              onClick = onOpenSettings,
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = OfflineConstants.BUTTON_CONTAINER_COLOR,
                      contentColor = MaterialTheme.colorScheme.onPrimary)) {
                Text(text = OfflineConstants.OPEN_SETTINGS_BUTTON)
              }
        }
  }
}

/**
 * Design-time preview of [OfflineRequiredScreen].
 *
 * This preview showcases the offline-required UI state with a no-op settings action. It is useful
 * for iterating on layout, typography and spacing without running the app or simulating an actual
 * offline scenario.
 */
@Preview
@Composable
fun OfflineRequiredScreenPreview() {
  OfflineRequiredScreen(onOpenSettings = {})
}
