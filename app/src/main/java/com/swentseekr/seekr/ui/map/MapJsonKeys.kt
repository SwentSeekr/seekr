package com.swentseekr.seekr.ui.map

/**
 * Centralized constants for JSON keys, HTTP values, and URL parameter names. Extracted to remove
 * magic strings from MapViewModelHelpers.
 */
object JsonKeys {
  const val ROUTES = "routes"
  const val LEGS = "legs"
  const val STEPS = "steps"
  const val POLYLINE = "polyline"
  const val POINTS = "points"
  const val STATUS = "status"
  const val ERROR_MESSAGE = "error_message"
  const val OVERVIEW_POLYLINE = "overview_polyline"
}

object JsonErrorMessages {
  const val DIRECTIONS_API_PREFIX = "Directions API error: "
}

object UrlParams {
  const val ORIGIN = "origin"
  const val DESTINATION = "destination"
  const val MODE = "mode"
  const val WAYPOINTS = "waypoints"
  const val KEY = "key"
  const val VIA_PREFIX = "via:"
  const val SEPARATOR = "|"
}

object HttpConstants {
  const val REQUEST_METHOD_GET = "GET"
  const val UTF_8 = "UTF-8"

  const val OK_STATUS_TEXT = "OK"
}
