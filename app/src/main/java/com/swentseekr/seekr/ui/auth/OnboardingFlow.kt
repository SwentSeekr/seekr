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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.swentseekr.seekr.model.authentication.OnboardingHandler

object OnboardingFlowTestTags {
  const val WELCOME_DIALOG = "welcome_dialog"
  const val TERMS_DIALOG = "terms_dialog"
  const val PROFILE_SETUP_DIALOG = "profile_setup_dialog"

  const val CONTINUE_BUTTON = "continue_button"
  const val I_AGREE_BUTTON = "i_agree_button"
  const val FINISH_BUTTON = "finish_button"

  const val CHECKBOX_AGREE = "checkbox_agree"
}

@Composable
fun OnboardingFlow(userId: String, onboardingHandler: OnboardingHandler, onDone: () -> Unit = {}) {
  var step by remember { mutableStateOf(1) }

  when (step) {
    1 -> WelcomeDialog(onContinue = { step = 2 })
    2 -> TermsDialog(onAccepted = { step = 3 })
    3 ->
        ProfileSetupDialog { pseudonym, bio ->
          onboardingHandler.completeOnboarding(userId, pseudonym, bio)
          onDone()
        }
  }
}

@Composable
fun WelcomeDialog(onContinue: () -> Unit) {
  AlertDialog(
      onDismissRequest = {},
      title = {
        Text(
            text = OnboardingFlowStrings.WELCOME_TITLE,
            style = MaterialTheme.typography.headlineSmall)
      },
      text = { Text(OnboardingFlowStrings.WELCOME_MESSAGE) },
      confirmButton = {
        Button(
            onClick = onContinue,
            modifier = Modifier.testTag(OnboardingFlowTestTags.CONTINUE_BUTTON)) {
              Text(OnboardingFlowStrings.CONTINUE_BUTTON)
            }
      },
      modifier = Modifier.testTag(OnboardingFlowTestTags.WELCOME_DIALOG))
}

@Composable
fun TermsDialog(onAccepted: () -> Unit) {
  var checked by remember { mutableStateOf(false) }

  AlertDialog(
      onDismissRequest = {},
      title = { Text(OnboardingFlowStrings.TERMS_TITLE) },
      text = {
        Column {
          Text(OnboardingFlowStrings.TERMS_MESSAGE)
          Spacer(modifier = Modifier.height(OnboardingFlowDimensions.SPACING_MEDIUM))
          Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(
                checked = checked,
                onCheckedChange = { checked = it },
                modifier = Modifier.testTag(OnboardingFlowTestTags.CHECKBOX_AGREE))
            Text(OnboardingFlowStrings.TERMS_CHECKBOX)
          }
        }
      },
      confirmButton = {
        Button(
            onClick = onAccepted,
            enabled = checked,
            modifier = Modifier.testTag(OnboardingFlowTestTags.I_AGREE_BUTTON)) {
              Text(OnboardingFlowStrings.TERMS_ACCEPT_BUTTON)
            }
      },
      modifier = Modifier.testTag(OnboardingFlowTestTags.TERMS_DIALOG))
}

@Composable
fun ProfileSetupDialog(onFinished: (String, String) -> Unit) {
  var pseudonym by remember { mutableStateOf("") }
  var bio by remember { mutableStateOf("") }

  AlertDialog(
      onDismissRequest = {},
      title = { Text(OnboardingFlowStrings.PROFILE_TITLE) },
      text = {
        Column {
          OutlinedTextField(
              value = pseudonym,
              onValueChange = { pseudonym = it },
              label = { Text(OnboardingFlowStrings.PSEUDONYM_LABEL) },
              singleLine = true)
          Spacer(modifier = Modifier.height(OnboardingFlowDimensions.SPACING_MEDIUM))
          OutlinedTextField(
              value = bio,
              onValueChange = { bio = it },
              label = { Text(OnboardingFlowStrings.BIO_LABEL) },
              maxLines = 4)
        }
      },
      confirmButton = {
        Button(
            onClick = { onFinished(pseudonym, bio) },
            enabled = pseudonym.isNotBlank(),
            modifier = Modifier.testTag(OnboardingFlowTestTags.FINISH_BUTTON)) {
              Text(OnboardingFlowStrings.FINISH_BUTTON)
            }
      },
      modifier = Modifier.testTag(OnboardingFlowTestTags.PROFILE_SETUP_DIALOG))
}
