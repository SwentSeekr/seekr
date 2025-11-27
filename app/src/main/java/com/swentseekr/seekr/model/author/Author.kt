package com.swentseekr.seekr.model.author

import kotlinx.serialization.Serializable

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
