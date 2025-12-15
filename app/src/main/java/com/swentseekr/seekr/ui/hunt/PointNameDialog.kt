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
      title = { Text(AddPointsMapScreenDefaults.DIALOG_TITLE) },
      text = {
        Column {
          OutlinedTextField(
              value = pointName,
              onValueChange = {
                if (it.isNotBlank()) hasTypedBefore = true
                pointName = it
              },
              placeholder = { Text(AddPointsMapScreenDefaults.PLACEHOLDER) },
              singleLine = true,
              isError = isError,
              label = { Text(AddPointsMapScreenDefaults.POINTS_NAME) },
              supportingText = {
                if (isError) {
                  Text(
                      AddPointsMapScreenDefaults.NOT_EMPTY, color = MaterialTheme.colorScheme.error)
                }
              },
              modifier =
                  Modifier.fillMaxWidth().testTag(AddPointsMapScreenTestTags.POINT_NAME_FIELD))

          OutlinedTextField(
              value = pointDescription,
              onValueChange = { pointDescription = it },
              label = { Text(AddPointsMapScreenDefaults.DESCRIPTION) },
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(AddPointsMapScreenTestTags.POINT_DESCRIPTION_FIELD))
        }
      },
      confirmButton = {
        TextButton(
            onClick = { onConfirm(pointName.trim(), pointDescription.trim()) },
            enabled = pointName.trim().isNotBlank()) {
              Text(AddPointsMapScreenDefaults.ADD)
            }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) { Text(AddPointsMapScreenDefaults.CANCEL) }
      })
}
