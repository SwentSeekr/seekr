package com.swentseekr.seekr.model.map

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val description: String = ""
)
