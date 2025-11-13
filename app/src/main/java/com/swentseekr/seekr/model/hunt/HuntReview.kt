package com.swentseekr.seekr.model.hunt

data class HuntReview(
    val reviewId: String = "",
    val authorId: String = "",
    val huntId: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val photos: List<String> = emptyList()
)
