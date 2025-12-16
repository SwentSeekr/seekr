package com.swentseekr.seekr.ui.auth

/**
 * Contains string constants and helper functions for messages used in [AuthViewModel].
 *
 * These messages represent common authentication states and errors.
 */
object AuthViewModelMessages {
  const val SIGN_IN_CANCELLED = "Sign-in cancelled"
  const val NO_PROVIDER_AVAILABLE =
      "Google Play Services is missing or outdated. Update it and try again."
  const val NO_GOOGLE_ACCOUNT =
      "No Google account is available on this device. Add one in system settings and retry."
  const val DEFAULT_SIGN_IN_FAILURE = "Unable to complete sign-in right now."
  const val CREDENTIAL_FAILURE_PREFIX = "Failed to get credentials"
  const val UNEXPECTED_ERROR_PREFIX = "Unexpected error"
  const val NO_PROVIDER = "no provider"
  const val ONBOARDING = "New user should need onboarding"

    /**
     * Constructs a detailed credential failure message.
     *
     * @param detail Optional additional detail about the failure.
     * @return Full failure message string.
     */
  fun credentialFailure(detail: String?) =
      if (detail.isNullOrBlank()) CREDENTIAL_FAILURE_PREFIX
      else "$CREDENTIAL_FAILURE_PREFIX: $detail"

    /**
     * Constructs a detailed unexpected error message.
     *
     * @param detail Optional additional detail about the error.
     * @return Full error message string.
     */
  fun unexpectedFailure(detail: String?) =
      if (detail.isNullOrBlank()) UNEXPECTED_ERROR_PREFIX else "$UNEXPECTED_ERROR_PREFIX: $detail"
}
