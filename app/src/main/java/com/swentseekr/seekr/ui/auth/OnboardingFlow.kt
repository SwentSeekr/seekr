package com.swentseekr.seekr.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.model.authentication.OnboardingHandler
import com.swentseekr.seekr.ui.terms.TermsAndConditionsScreen
import com.swentseekr.seekr.ui.terms.TermsScreenConstants
import com.swentseekr.seekr.ui.terms.TermsScreenStrings
import com.swentseekr.seekr.ui.terms.TermsSection

@Composable
fun OnboardingFlow(
    userId: String,
    onboardingHandler: OnboardingHandler,
    vm: OnboardingViewModel = viewModel(),
    onDone: () -> Unit = {}
) {
  val state by vm.state.collectAsState()


  Box {
    when (state.step) {
      1 -> WelcomeDialog(onContinue = vm::nextStep)
      2 -> TermsDialog(
        onAccepted = vm::validateTerms,
        onTermsClicked = vm::nextStep)
      3 -> FullScreenTermsDialog(
        onContinue = vm::nextStep,
        onCancel = vm::previousStep
      )
      4 ->
          ProfileSetupDialog (
            pseudonymError = state.pseudonymError,
            isCheckingPseudonym = state.isCheckingPseudonym,
            onPseudonymChange = vm::validatePseudonym,
            onFinished = {pseudo, bio ->
            onboardingHandler.completeOnboarding(userId, pseudo, bio)
            onDone() }
          )

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
fun TermsDialog(
  onAccepted: () -> Unit,
  onTermsClicked: () -> Unit = {}
){
  var checked by remember { mutableStateOf(false) }
  val termsAndConditions = OnboardingFlowStrings.TERMS_TITLE


  val annotatedText = buildAnnotatedString {

    append(OnboardingFlowStrings.TERMS_MESSAGE_1 + " ")

    // Add clickable link
    pushLink(
      LinkAnnotation.Clickable(
        tag = OnboardingFlowTestTags.TERMS,
        linkInteractionListener = {
          onTermsClicked()
        }
      )
    )
    withStyle(SpanStyle(color = Color.Blue)) {
      append(termsAndConditions)
    }
    pop()

    append(" " + OnboardingFlowStrings.TERMS_MESSAGE_2)
  }

  AlertDialog(
      onDismissRequest = {},
      title = { Text(OnboardingFlowStrings.TERMS_TITLE) },
      text = {
        Column {
          Text(annotatedText)
          Spacer(modifier = Modifier.height(OnboardingFlowDimensions.SPACING_MEDIUM))
          Row(verticalAlignment = Alignment.CenterVertically) {
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
fun ProfileSetupDialog(
  pseudonymError: String?,
  isCheckingPseudonym: Boolean,
  onPseudonymChange: (String) -> Unit,
  onFinished: (String, String) -> Unit
) {
  var pseudonym by remember { mutableStateOf("") }
  var bio by remember { mutableStateOf("") }

  AlertDialog(
      onDismissRequest = {},
      title = { Text(OnboardingFlowStrings.PROFILE_TITLE) },
      text = {
        Column {
          OutlinedTextField(
              value = pseudonym,
              onValueChange = {
                pseudonym = it
                onPseudonymChange(it) },
              label = { Text(OnboardingFlowStrings.PSEUDONYM_LABEL) },
              singleLine = true,
              isError = pseudonymError != null,
              supportingText = {
                when {
                  isCheckingPseudonym -> {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(OnboardingFlowDimensions.SPACING_SMALL)
                    ) {
                      CircularProgressIndicator(
                        modifier = Modifier.size(OnboardingFlowDimensions.SIZE_MEDIUM),
                        strokeWidth = OnboardingFlowDimensions.STROKE_WIDTH
                      )
                      Text(
                        OnboardingFlowStrings.CHECKING_AVAILABILITY,
                        style = MaterialTheme.typography.bodySmall
                      )
                    }
                  }
                  pseudonymError != null -> {
                    Text(
                      pseudonymError,
                      color = MaterialTheme.colorScheme.error
                    )
                  }
                }
              },
              trailingIcon = {
                AnimatedVisibility(
                  visible = isCheckingPseudonym,
                  enter = fadeIn(),
                  exit = fadeOut()
                ) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(OnboardingFlowDimensions.SIZE_LARGE),
                    strokeWidth = OnboardingFlowDimensions.STROKE_WIDTH
                  )
                }
              },
            )
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
            enabled = pseudonym.isNotBlank() &&
                    pseudonymError == null &&
                    !isCheckingPseudonym,
            modifier = Modifier.testTag(OnboardingFlowTestTags.FINISH_BUTTON)) {
              Text(OnboardingFlowStrings.FINISH_BUTTON)
            }
      },
      modifier = Modifier.testTag(OnboardingFlowTestTags.PROFILE_SETUP_DIALOG))
}

@Composable
fun FullScreenTermsDialog(
    onContinue: () -> Unit,
    onCancel: () -> Unit = {}
) {
  AlertDialog(
    onDismissRequest = { },
    modifier = Modifier.fillMaxSize(),
    title = { Text(TermsScreenStrings.TITLE) },
    text = {
      // Content scrollable
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(TermsScreenConstants.CARD_PADDING)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(TermsScreenConstants.SECTION_SPACING)
      ) {

        TermsSection(
          title = TermsScreenStrings.SECTION_1_TITLE,
          content = TermsScreenStrings.SECTION_1_CONTENT)

        TermsSection(
          title = TermsScreenStrings.SECTION_2_TITLE,
          content = TermsScreenStrings.SECTION_2_CONTENT)

        TermsSection(
          title = TermsScreenStrings.SECTION_3_TITLE,
          content = TermsScreenStrings.SECTION_3_CONTENT)

        TermsSection(
          title = TermsScreenStrings.SECTION_4_TITLE,
          content = TermsScreenStrings.SECTION_4_CONTENT)

        TermsSection(
          title = TermsScreenStrings.SECTION_5_TITLE,
          content = TermsScreenStrings.SECTION_5_CONTENT)

        TermsSection(
          title = TermsScreenStrings.SECTION_6_TITLE,
          content = TermsScreenStrings.SECTION_6_CONTENT)

        Spacer(modifier = Modifier.height(TermsScreenConstants.BOTTOM_SPACER))
      }
    },
    confirmButton = {
      Button(
        onClick = onContinue,
        modifier = Modifier.testTag(OnboardingFlowTestTags.I_AGREE_BUTTON)) {
        Text(OnboardingFlowStrings.TERMS_ACCEPT_BUTTON)
      }
    },
    dismissButton = {
      Button(
        onClick = onCancel,
        modifier = Modifier.testTag(OnboardingFlowTestTags.I_DONT_AGREE_BUTTON)) {
        Text(OnboardingFlowStrings.TERMS_DONT_AGREE_BUTTON)
      }
    }
  )
}



