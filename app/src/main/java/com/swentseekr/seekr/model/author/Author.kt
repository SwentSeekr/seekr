package com.swentseekr.seekr.model.author

data class Author(
    val pseudonym: String,
    val bio: String,
    val profilePicture: Int,
    val reviewRate: Double,
    val sportRate: Double
)
