package com.swentseekr.seekr.ui.auth

import androidx.compose.ui.unit.dp

/**
 * Constants for test tags used in the onboarding Compose UI.
 *
 * These tags help identify UI elements during automated testing.
 */
object OnboardingFlowTestTags {

  // ----------------------
  // Dialogs
  // ----------------------
  const val WELCOME_DIALOG = "welcome_dialog"
  const val TERMS_DIALOG = "terms_dialog"
  const val TERMS = "terms"
  const val PROFILE_SETUP_DIALOG = "profile_setup_dialog"
  const val TERMS_CONDITION_DIALOG = "terms_condition_dialog"

  // ----------------------
  // Buttons
  // ----------------------
  const val CONTINUE_BUTTON = "continue_button"
  const val I_AGREE_BUTTON = "i_agree_button"
  const val I_DONT_AGREE_BUTTON = "i_dont_agree_button"
  const val FINISH_BUTTON = "finish_button"

  // ----------------------
  // Inputs / Fields
  // ----------------------
  const val CHECKBOX_AGREE = "checkbox_agree"
}

/**
 * String resources used in the onboarding flow dialogs and UI elements.
 *
 * Includes titles, messages, labels, button texts, and error messages.
 */
object OnboardingFlowStrings {

  // ----------------------
  // Welcome Dialog
  // ----------------------
  const val WELCOME_TITLE = "Welcome to Seekr 👋"
  const val WELCOME_MESSAGE =
      "Thank you for joining Seekr! Let's get started with a quick onboarding to set up your profile."
  const val CONTINUE_BUTTON = "Continue"

  // ----------------------
  // Terms Dialog
  // ----------------------
  const val TERMS_TITLE = "Terms and conditions"
  const val TERMS_MESSAGE_1 = "By using Seekr, you agree to our "
  const val TERMS_MESSAGE_2 = ". Please read them carefully before proceeding."
  const val TERMS_CHECKBOX = "I agree to the terms and conditions."
  const val TERMS_ACCEPT_BUTTON = "I Agree"
  const val TERMS_DONT_AGREE_BUTTON = "I Don't Agree"

  // ----------------------
  // Profile Setup Dialog
  // ----------------------
  const val PROFILE_TITLE = "Complete your profile"
  const val PSEUDONYM_LABEL = "Pseudonym"
  const val BIO_LABEL = "Bio"
  const val FINISH_BUTTON = "Finish"
  const val ERROR_PSEUDONYM_INVALID =
      "Pseudonym is invalid. It must be between 3 and 30 characters."
  const val ERROR_PSEUDONYM_TAKEN = "This pseudonym is already taken. Please choose another one."
  const val CHECKING_AVAILABILITY = "Checking availability..."
  const val SPACER = " "
  const val INITIAL = ""
}

/**
 * Dimension constants used in the onboarding Compose UI.
 *
 * Includes spacing, sizes, and stroke widths for consistent UI layout.
 */
object OnboardingFlowDimensions {
  val SPACING_MEDIUM = 16.dp
  val SPACING_SMALL = 8.dp

  val SIZE_MEDIUM = 16.dp
  val SIZE_LARGE = 20.dp

  val STROKE_WIDTH = 2.dp
}
