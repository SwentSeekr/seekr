package com.swentseekr.seekr.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.swentseekr.seekr.ui.theme.Green

@Composable
fun PermissionRequestPopup(onRequestPermission: () -> Unit) {
    Box(
        modifier =
            Modifier.fillMaxSize()
                .background(MapScreenDefaults.OverlayScrimColor)
                .padding(MapScreenDefaults.OverlayPadding)
                .testTag(MapScreenTestTags.PERMISSION_POPUP),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(MapScreenDefaults.CardPadding),
            shape = RoundedCornerShape(MapScreenDefaults.CardCornerRadius),
            elevation = CardDefaults.cardElevation(MapScreenDefaults.CardElevation)
        ) {
            Column(
                modifier = Modifier.padding(MapScreenDefaults.OverlayInnerPadding).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = MapScreenStrings.PermissionExplanation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier =
                        Modifier.padding(bottom = MapScreenDefaults.CardPadding)
                            .testTag(MapScreenTestTags.EXPLAIN)
                )
                TextButton(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = Green),
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(top = MapScreenDefaults.PopupSpacing)
                            .testTag(MapScreenTestTags.GRANT_LOCATION_PERMISSION)
                ) {
                    Text(MapScreenStrings.GrantPermission, color = Color.White)
                }
            }
        }
    }
}
