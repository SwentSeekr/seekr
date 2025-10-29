package com.swentseekr.seekr.model.hunt

data class HuntReview(
    val reviewID: String,
    val authorID: String,
    val huntID: String,
    val rating: Double,
    val comment: String,
    val photos: List<PhotoFile>
)
