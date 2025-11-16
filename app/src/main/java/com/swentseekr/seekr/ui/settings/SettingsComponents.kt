package com.swentseekr.seekr.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/**
 * Displays a settings item with a title and a corresponding value.
 *
 * @param title The label of the setting.
 * @param value The value associated with the setting.
 * @param modifier Optional [Modifier] for styling and layout adjustments.
 */
@Composable
fun SettingsItem(title: String, value: String, modifier: Modifier = Modifier) {
  Row(
      modifier = modifier.fillMaxWidth().height(SettingsScreenDefaults.ITEMS_SPACING),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Text(text = title, fontWeight = FontWeight.Medium)
        Text(text = value, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
}

/**
 * Displays a toggleable setting with a switch.
 *
 * @param title The label of the setting.
 * @param checked Whether the toggle is on or off.
 * @param onToggle Callback triggered when the toggle changes.
 * @param modifier Optional [Modifier] for styling and layout adjustments.
 */
@Composable
fun SettingsToggleItem(
    title: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
  Row(
      modifier = Modifier.fillMaxWidth().height(SettingsScreenDefaults.ITEMS_SPACING),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontWeight = FontWeight.Medium)
        Switch(checked = checked, onCheckedChange = onToggle, modifier = modifier)
      }
}

/**
 * Displays a settings item that can be clicked to navigate or trigger an action.
 *
 * @param title The label of the setting.
 * @param onClick Callback triggered when the item is clicked.
 * @param modifier Optional [Modifier] for styling and layout adjustments.
 */
@Composable
fun SettingsArrowItem(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .height(SettingsScreenDefaults.ITEMS_SPACING)
              .clickable { onClick() }
              .padding(vertical = SettingsScreenDefaults.COMPONENTS_PADDING),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontWeight = FontWeight.Medium)
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
      }
}
