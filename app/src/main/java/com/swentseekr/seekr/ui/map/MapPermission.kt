package com.swentseekr.seekr.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * A full-screen overlay popup that prompts the user to grant location permissions.
 *
 * This popup is displayed when:
 * - Location permission is required for map features (e.g., showing user's position)
 * - The user has not yet granted location access
 *
 * The popup contains:
 * - A message explaining why permissions are needed
 * - A button for requesting the required permission
 *
 * It uses accessibility and test-friendly tags for UI testing.
 *
 * @param onRequestPermission callback invoked when the user presses **Grant Permission**.
 */
@Composable
fun PermissionRequestPopup(onRequestPermission: () -> Unit) {
  Box(
      modifier =
          Modifier.fillMaxSize()
              .padding(MapScreenDefaults.OverlayPadding)
              .testTag(MapScreenTestTags.PERMISSION_POPUP),
      contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(MapScreenDefaults.CardPadding),
            shape = RoundedCornerShape(MapScreenDefaults.CardCornerRadius),
            elevation = CardDefaults.cardElevation(MapScreenDefaults.CardElevation)) {
              Column(
                  modifier = Modifier.padding(MapScreenDefaults.OverlayInnerPadding).fillMaxWidth(),
                  horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = MapScreenStrings.PERMISSION_EXPLANATION,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier =
                            Modifier.padding(bottom = MapScreenDefaults.CardPadding)
                                .testTag(MapScreenTestTags.EXPLAIN))

                    TextButton(
                        onClick = onRequestPermission,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary),
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(top = MapScreenDefaults.PopupSpacing)
                                .testTag(MapScreenTestTags.GRANT_LOCATION_PERMISSION)) {
                          Text(
                              MapScreenStrings.GRANT_PERMISSION,
                              color = MaterialTheme.colorScheme.onSurface)
                        }
                  }
            }
      }
}
