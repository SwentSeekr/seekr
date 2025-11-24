package com.swentseekr.seekr.model.authentication

interface OnboardingHandler {
  fun completeOnboarding(userId: String, pseudonym: String, bio: String)
}
