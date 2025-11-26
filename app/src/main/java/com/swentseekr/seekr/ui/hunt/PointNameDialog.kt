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
fun PointNameDialog(show: Boolean, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
  if (!show) return

  var pointName by remember { mutableStateOf("") }
  var pointDescription by remember { mutableStateOf("") }
  var hasTypedBefore by remember { mutableStateOf(false) }

  val isError = hasTypedBefore && pointName.isBlank()

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Give your checkpoint a name and an optional description.") },
      text = {
        Column {
          OutlinedTextField(
              value = pointName,
              onValueChange = {
                if (it.isNotBlank()) hasTypedBefore = true
                pointName = it
              },
              placeholder = { Text("e.g. Louvre museum") },
              singleLine = true,
              isError = isError,
              label = { Text("Point's name") },
              supportingText = {
                if (isError) {
                  Text("The name cannot be empty", color = MaterialTheme.colorScheme.error)
                }
              },
              modifier =
                  Modifier.fillMaxWidth().testTag(AddPointsMapScreenTestTags.POINT_NAME_FIELD))

          OutlinedTextField(
              value = pointDescription,
              onValueChange = { pointDescription = it },
              label = { Text("Description") },
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(AddPointsMapScreenTestTags.POINT_DESCRIPTION_FIELD))
        }
      },
      confirmButton = {
        TextButton(
            onClick = { onConfirm(pointName.trim(), pointDescription.trim()) },
            enabled = pointName.trim().isNotBlank()) {
              Text("Add")
            }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
