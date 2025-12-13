package com.swentseekr.seekr.model.authentication

/** Functional interface for handling user onboarding completion. */
fun interface OnboardingHandler {

  /**
   * Function called when a user completes the onboarding process.
   *
   * @param userId Unique identifier of the user.
   * @param pseudonym The pseudonym chosen by the user.
   * @param bio A short biography or description provided by the user.
   */
  fun completeOnboarding(userId: String, pseudonym: String, bio: String)
}
