package com.swentseekr.seekr.ui.map

/**
 * Centralized constants for JSON keys, HTTP values, and URL parameter names. Extracted to remove
 * magic strings from MapViewModelHelpers.
 */
object JsonKeys {

  //-------------------
  // Route and path structure keys
  //-------------------
  const val ROUTES = "routes"
  const val LEGS = "legs"
  const val STEPS = "steps"
  const val POLYLINE = "polyline"
  const val POINTS = "points"

  //-------------------
  // Status and error keys
  //-------------------
  const val STATUS = "status"
  const val ERROR_MESSAGE = "error_message"
  const val OVERVIEW_POLYLINE = "overview_polyline"
}

/**
 * Centralized error message prefixes related to Map/Google Directions API calls.
 */
object JsonErrorMessages {
  const val DIRECTIONS_API_PREFIX = "Directions API error: "
}


/**
 * URL parameter names and separators used in constructing Google Directions API requests.
 */
object UrlParams {

  //-------------------
  // URL parameter separators
  //-------------------
  const val AND = "&"
  const val EQUAL = "="
  const val SEPARATOR = "|"


  //-------------------
  // URL parameter names
  //-------------------

  const val ORIGIN = "origin"
  const val DESTINATION = "destination"
  const val MODE = "mode"
  const val WAYPOINTS = "waypoints"
  const val KEY = "key"
  const val VIA_PREFIX = "via:"
}

/**
 * HTTP constants used in network requests for Maps API.
 */
object HttpConstants {

  //-------------------
  // HTTP methods
  //-------------------
  const val REQUEST_METHOD_GET = "GET"

  //-------------------
  // Encoding and status
  //-------------------
  const val UTF_8 = "UTF-8"

  const val OK_STATUS_TEXT = "OK"
}
