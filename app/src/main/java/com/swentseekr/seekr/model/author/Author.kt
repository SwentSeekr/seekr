package com.swentseekr.seekr.model.author

import kotlinx.serialization.Serializable

/**
 * Represents an author or user profile within the system.
 *
 * @property hasCompletedOnboarding Indicates whether the user has completed the onboarding flow.
 * @property hasAcceptedTerms Indicates whether the user has accepted the terms and conditions.
 * @property pseudonym The display name or pseudonym chosen by the user.
 * @property bio A short biography or description provided by the user.
 * @property profilePicture Resource ID of the user's local profile picture.
 * @property reviewRate The average rating the user has received for reviews.
 * @property sportRate The average rating the user has received for sports-related activity.
 * @property profilePictureUrl URL to the user's profile picture stored remotely.
 */
@Serializable
data class Author(
    val hasCompletedOnboarding: Boolean = false,
    val hasAcceptedTerms: Boolean = false,
    val pseudonym: String = "",
    val bio: String = "",
    val profilePicture: Int = 0,
    val reviewRate: Double = 0.0,
    val sportRate: Double = 0.0,
    val profilePictureUrl: String = ""
)
