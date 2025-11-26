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
import com.swentseekr.seekr.model.profile.mockProfileData
import com.swentseekr.seekr.ui.profile.Profile

/**
 * Overview screen displayed when the user is offline.
 *
 * Shows an informational card explaining offline capabilities and a primary action
 * to access downloaded hunts.
 *
 * @param profile The current user profile, if available. Currently not rendered but
 * may be used in future enhancements (e.g., personalized messaging).
 * @param modifier Modifier used to adjust layout or apply additional styling
 * from the parent composable.
 * @param onShowDownloadedHunts Callback invoked when the user taps the action button
 * to navigate to the list of offline-available hunts.
 */
@Composable
fun OfflineOverviewScreen(
    profile: Profile?,
    modifier: Modifier = Modifier,
    onShowDownloadedHunts: () -> Unit = {},
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = OfflineConstants.SCREEN_PADDING),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Top spacer to position the informational card lower on the screen
            Spacer(modifier = Modifier.height(OfflineConstants.OVERVIEW_TOP_SPACER_HEIGHT))

            // Informational card explaining offline behaviour and availability
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(OfflineConstants.OFFLINE_CARD_WIDTH_RATIO)
                        .height(OfflineConstants.OFFLINE_CARD_HEIGHT)
                        .background(
                            color = OfflineConstants.LIGHT_GREEN_BACKGROUND,
                            shape = OfflineConstants.CARD_SHAPE),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Warning icon to visually highlight the offline state
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(OfflineConstants.OFFLINE_ICON_SIZE)
                    )
                    Spacer(modifier = Modifier.height(OfflineConstants.ICON_SPACING))
                    // Short descriptive message about offline access to hunts, centered in the card
                    Text(
                        text = OfflineConstants.OFFLINE_OVERVIEW_MESSAGE,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Spacer to push the action button closer to the bottom of the screen
            Spacer(modifier = Modifier.height(OfflineConstants.OVERVIEW_BUTTON_TOP_SPACER_HEIGHT))

            // Primary action allowing the user to continue with downloaded hunts only
            Button(
                onClick = onShowDownloadedHunts,
                shape = OfflineConstants.BUTTON_SHAPE,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = OfflineConstants.BUTTON_CONTAINER_COLOR),
                modifier = Modifier.fillMaxWidth(OfflineConstants.BUTTON_WIDTH_RATIO)
            ) {
                Text(text = OfflineConstants.SHOW_DOWNLOADED_HUNTS_BUTTON)
            }

            // Bottom spacer to provide breathing room below the button
            Spacer(modifier = Modifier.height(OfflineConstants.OVERVIEW_BUTTON_BOTTOM_SPACER_HEIGHT))
        }
    }
}

/**
 * Design-time preview of [OfflineOverviewScreen] using mock profile data.
 */
@Preview
@Composable
fun OfflineOverviewScreenPreview() {
    OfflineOverviewScreen(
        profile = mockProfileData(),
        onShowDownloadedHunts = {}
    )
}