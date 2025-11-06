package com.swentseekr.seekr.ui.hunt

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/** Dialog composable extracted for easier testing and cleaner code. */
@Composable
fun PointNameDialog(show: Boolean, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
  if (!show) return

  var pointName by remember { mutableStateOf("") }
  var hasTypedBefore by remember { mutableStateOf(false) }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Nom du point") },
      text = {
        Column {
          OutlinedTextField(
              value = pointName,
              onValueChange = {
                if (it.isNotBlank()) hasTypedBefore = true
                pointName = it
              },
              placeholder = { Text("e.g. Musée de l’automobile") },
              singleLine = true,
              isError = hasTypedBefore && pointName.isBlank(),
              label = { Text("Nom du point") },
              supportingText = {
                if (hasTypedBefore && pointName.isBlank()) {
                  Text("Le nom ne peut pas être vide", color = MaterialTheme.colorScheme.error)
                }
              },
              modifier =
                  Modifier.fillMaxWidth().testTag(AddPointsMapScreenTestTags.POINT_NAME_FIELD))
        }
      },
      confirmButton = {
        TextButton(onClick = { onConfirm(pointName.trim()) }, enabled = pointName.isNotBlank()) {
          Text("Ajouter")
        }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } })
}
