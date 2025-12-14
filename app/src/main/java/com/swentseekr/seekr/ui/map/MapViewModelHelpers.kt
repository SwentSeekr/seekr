package com.swentseekr.seekr.ui.map

import androidx.annotation.VisibleForTesting
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.map.Location
import com.swentseekr.seekr.ui.map.MapScreenDefaults.DEFAULT_MIN_POINT
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONArray
import org.json.JSONObject

/**
 * Converts a domain-level [Location] model into a Google Maps [LatLng] object.
 *
 * @return a LatLng containing the same latitude and longitude.
 */
fun Location.toLatLng(): LatLng = LatLng(latitude, longitude)

/**
 * Returns all hunt points (start → middle → end) in their intended navigation order.
 *
 * @param hunt the hunt containing all checkpoints.
 * @return ordered list of all [LatLng] points representing the hunt path.
 */
fun orderedPointsFor(hunt: Hunt): List<LatLng> = buildList {
  add(LatLng(hunt.start.latitude, hunt.start.longitude))
  hunt.middlePoints.forEach { add(LatLng(it.latitude, it.longitude)) }
  add(LatLng(hunt.end.latitude, hunt.end.longitude))
}

/**
 * Returns the next checkpoint in a hunt based on how many steps are already validated.
 *
 * @param hunt the hunt in progress, or null.
 * @param validatedCount number of checkpoints validated so far.
 * @return the next [LatLng] checkpoint or null if none remain.
 */
fun nextPointFor(hunt: Hunt?, validatedCount: Int): LatLng? {
  if (hunt == null) return null
  val ordered = orderedPointsFor(hunt)
  val nextIdx = validatedCount.coerceAtLeast(0)
  return ordered.getOrNull(nextIdx)
}

/**
 * Safely computes the distance in meters between two map points.
 *
 * @param from starting coordinate.
 * @param to destination coordinate.
 * @return integer distance in meters, or null if computation fails.
 */
fun distanceMeters(from: LatLng, to: LatLng): Int? =
    try {
      SphericalUtil.computeDistanceBetween(from, to).toInt()
    } catch (_: Exception) {
      null
    }

/**
 * Computes the raw distance in meters (as a double) between two points.
 *
 * @throws Exception if the SphericalUtil computation fails.
 */
fun computeDistanceMetersRaw(from: LatLng, to: LatLng): Double =
    SphericalUtil.computeDistanceBetween(from, to)

/**
 * Full utility for computing a user's distance to the next hunt checkpoint.
 *
 * @param hunt the hunt being performed.
 * @param validatedCount current number of validated checkpoints.
 * @param currentLocation user’s current GPS location.
 * @return integer distance in meters, or null if next checkpoint does not exist.
 */
fun computeDistanceToNextPoint(hunt: Hunt, validatedCount: Int, currentLocation: LatLng): Int? {
  val next = nextPointFor(hunt, validatedCount) ?: return null
  return distanceMeters(currentLocation, next)
}

/**
 * Determines whether a hunt has been fully validated.
 *
 * A hunt contains:
 * - 1 start point
 * - N middle points
 * - 1 end point
 *
 * @param hunt the hunt being checked.
 * @param validatedCount number of completed checkpoints.
 * @return true if all required checkpoints have been validated.
 */
fun isHuntFullyValidated(hunt: Hunt, validatedCount: Int): Boolean {
  val total = DEFAULT_MIN_POINT + hunt.middlePoints.size
  return validatedCount >= total
}

/**
 * Requests a Google Directions polyline from origin → destination with optional waypoints.
 *
 * Builds a Directions API request URL, opens a connection, receives the JSON body, parses the
 * returned polyline, and returns a list of decoded [LatLng] coordinates.
 *
 * @param originLat latitude of the origin.
 * @param originLng longitude of the origin.
 * @param destLat latitude of the destination.
 * @param destLng longitude of the destination.
 * @param waypoints list of optional intermediate points `(latitude, longitude)`.
 * @param travelMode Google Directions travel mode (e.g., "walking").
 * @param apiKey Google Maps API key.
 * @return a list of decoded polyline coordinates.
 * @throws IllegalStateException if the API response contains an error.
 */
fun requestDirectionsPolyline(
    originLat: Double,
    originLng: Double,
    destLat: Double,
    destLng: Double,
    waypoints: List<Pair<Double, Double>>,
    travelMode: String,
    apiKey: String
): List<LatLng> {

  val url =
      buildDirectionsUrl(originLat, originLng, destLat, destLng, waypoints, travelMode, apiKey)

  val conn = openDirectionsConnection(url)

  return conn.inputStream.use { stream ->
    val body = stream.bufferedReader().readText()
    val json = JSONObject(body)
    parseDirectionsResponse(json)
  }
}

/**
 * Builds a full Directions API URL with origin, destination, travel mode, and waypoints.
 *
 * @return a properly encoded [URL] ready for HTTP requests.
 */
private fun buildDirectionsUrl(
    originLat: Double,
    originLng: Double,
    destLat: Double,
    destLng: Double,
    waypoints: List<Pair<Double, Double>>,
    travelMode: String,
    apiKey: String
): URL {

  val origin = "$originLat,$originLng"
  val destination = "$destLat,$destLng"

  val waypointParam =
      if (waypoints.isNotEmpty()) {
        waypoints.joinToString(separator = UrlParams.SEPARATOR) { (lat, lng) ->
          "${UrlParams.VIA_PREFIX}$lat,$lng"
        }
      } else null

  val params =
      buildList {
            add(
                "${UrlParams.ORIGIN}${UrlParams.EQUAL}" +
                    URLEncoder.encode(origin, HttpConstants.UTF_8))
            add(
                "${UrlParams.DESTINATION}${UrlParams.EQUAL}" +
                    URLEncoder.encode(destination, HttpConstants.UTF_8))
            add(
                "${UrlParams.MODE}${UrlParams.EQUAL}" +
                    URLEncoder.encode(travelMode, HttpConstants.UTF_8))
            waypointParam?.let {
              add(
                  "${UrlParams.WAYPOINTS}${UrlParams.EQUAL}" +
                      URLEncoder.encode(it, HttpConstants.UTF_8))
            }
            add(
                "${UrlParams.KEY}${UrlParams.EQUAL}" +
                    URLEncoder.encode(apiKey, HttpConstants.UTF_8))
          }
          .joinToString(UrlParams.AND)

  return URL("${MapConfig.DIRECTIONS_BASE_URL}?$params")
}

/**
 * Factory used for opening HTTP connections.
 *
 * Overridable for unit tests through dependency injection.
 */
@VisibleForTesting
var directionsConnectionFactory: (URL) -> HttpURLConnection = { url ->
  (url.openConnection() as HttpURLConnection).apply {
    requestMethod = HttpConstants.REQUEST_METHOD_GET
    connectTimeout = MapConfig.DIRECTIONS_CONNECT_TIMEOUT_MS
    readTimeout = MapConfig.DIRECTIONS_READ_TIMEOUT_MS
    doInput = true
  }
}

/** Opens an HTTP connection to a Directions API URL. */
private fun openDirectionsConnection(url: URL): HttpURLConnection = directionsConnectionFactory(url)

/**
 * Parses the full Directions API JSON body and extracts an ordered list of route coordinates.
 *
 * Parsing priority:
 * 1. Try parsing full leg → steps → polyline detail path
 * 2. Fall back to overview polyline if steps are unavailable
 *
 * @param json full JSON response from Directions API.
 * @return ordered list of polyline path points.
 * @throws IllegalStateException if the response contains a non-OK status.
 */
private fun parseDirectionsResponse(json: JSONObject): List<LatLng> {
  ensureStatusOk(json)

  val routes = json.getJSONArray(JsonKeys.ROUTES)
  if (routes.length() == 0) return emptyList()

  val firstRoute = routes.getJSONObject(0)
  val legs = firstRoute.getJSONArray(JsonKeys.LEGS)

  val fullPath = buildFullPath(legs)
  if (fullPath.isNotEmpty()) return fullPath

  // Fallback: use overview polyline
  val overview = firstRoute.getJSONObject(JsonKeys.OVERVIEW_POLYLINE).getString(JsonKeys.POINTS)

  return PolyUtil.decode(overview)
}

/**
 * Ensures a Directions API JSON response has status=OK, otherwise throws exception.
 *
 * @throws IllegalStateException if API returned an error.
 */
private fun ensureStatusOk(json: JSONObject) {
  val status = json.optString(JsonKeys.STATUS)
  if (status != HttpConstants.OK_STATUS_TEXT) {
    val message = json.optString(JsonKeys.ERROR_MESSAGE, status)
    throw IllegalStateException(JsonErrorMessages.DIRECTIONS_API_PREFIX + message)
  }
}

/**
 * Builds a full, step-by-step path from the `"legs"` array of a Directions API response.
 *
 * @param legs the `"legs"` array from the route object.
 * @return flat list of [LatLng] step coordinates.
 */
private fun buildFullPath(legs: JSONArray): List<LatLng> {
  val fullPath = mutableListOf<LatLng>()

  for (i in 0 until legs.length()) {
    val leg = legs.getJSONObject(i)
    val steps = leg.getJSONArray(JsonKeys.STEPS)
    appendStepsToPath(steps, fullPath)
  }

  return fullPath
}

/**
 * Appends decoded step-level polylines to a route list.
 *
 * Ensures no duplicate joining points by dropping the first point when extending.
 *
 * @param steps `"steps"` array containing encoded polylines.
 * @param fullPath mutable list to append coordinates to.
 */
private fun appendStepsToPath(steps: JSONArray, fullPath: MutableList<LatLng>) {
  for (j in 0 until steps.length()) {
    val step = steps.getJSONObject(j)
    val poly = step.getJSONObject(JsonKeys.POLYLINE).getString(JsonKeys.POINTS)

    val stepPoints = PolyUtil.decode(poly).toList()

    // Avoid repeating the connection point
    if (fullPath.isNotEmpty()) {
      fullPath.addAll(stepPoints.drop(1))
    } else {
      fullPath.addAll(stepPoints)
    }
  }
}
