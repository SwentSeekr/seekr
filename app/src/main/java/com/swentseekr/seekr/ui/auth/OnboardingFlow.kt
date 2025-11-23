package com.swentseekr.seekr.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingFlow(userId: String, authViewModel: AuthViewModel) {
  var step by remember { mutableStateOf(1) }

  when (step) {
    1 -> WelcomeDialog(onContinue = { step = 2 })
    2 -> TermsDialog(onAccepted = { step = 3 })
    3 ->
        ProfileSetupDialog(
            onFinished = { pseudonym, bio ->
              authViewModel.completeOnboarding(userId, pseudonym, bio)
            })
  }
}

@Composable
fun WelcomeDialog(onContinue: () -> Unit) {
  AlertDialog(
      onDismissRequest = {},
      title = {
        Text(text = "Welcome to Seekr 👋", style = MaterialTheme.typography.headlineSmall)
      },
      text = {
        Text(
            text =
                "Thank you for joining Seekr! Let's get started with a quick onboarding to set up your profile.")
      },
      confirmButton = { Button(onClick = onContinue) { Text("Continue") } })
}

@Composable
fun TermsDialog(onAccepted: () -> Unit) {
  var checked by remember { mutableStateOf(false) }

  AlertDialog(
      onDismissRequest = {},
      title = { Text("Terms and conditions") },
      text = {
        Column {
          Text(
              "By using Seekr, you agree to our terms and conditions. Please read them carefully before proceeding.")

          Spacer(modifier = Modifier.height(16.dp))

          Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = { checked = it })
            Text("I agree to the terms and conditions.")
          }
        }
      },
      confirmButton = { Button(onClick = onAccepted, enabled = checked) { Text("I Agree") } })
}

@Composable
fun ProfileSetupDialog(onFinished: (String, String) -> Unit) {
  var pseudonym by remember { mutableStateOf("") }
  var bio by remember { mutableStateOf("") }

  AlertDialog(
      onDismissRequest = {},
      title = { Text("Complete your profile") },
      text = {
        Column {
          OutlinedTextField(
              value = pseudonym,
              onValueChange = { pseudonym = it },
              label = { Text("Pseudonym") },
              singleLine = true)

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
              value = bio, onValueChange = { bio = it }, label = { Text("Bio") }, maxLines = 4)
        }
      },
      confirmButton = {
        Button(onClick = { onFinished(pseudonym, bio) }, enabled = pseudonym.isNotBlank()) {
          Text("Finish")
        }
      })
}
