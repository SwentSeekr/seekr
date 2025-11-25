package com.swentseekr.seekr.model.authentication

fun interface OnboardingHandler {
  fun completeOnboarding(userId: String, pseudonym: String, bio: String)
}
