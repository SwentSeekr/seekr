package com.swentseekr.seekr.model.map

import kotlinx.serialization.Serializable

/**
 * Represents a geographic location within the system.
 *
 * @property latitude The latitude of the location in decimal degrees.
 * @property longitude The longitude of the location in decimal degrees.
 * @property name The display name of the location.
 * @property description Optional detailed description of the location.
 * @property imageIndex Optional index referencing an image associated with this location.
 */
@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val description: String = "",
    val imageIndex: Int? = null
)
