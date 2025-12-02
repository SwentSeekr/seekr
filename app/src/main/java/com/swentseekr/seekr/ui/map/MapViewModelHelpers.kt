package com.swentseekr.seekr.ui.map

import androidx.annotation.VisibleForTesting
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.map.Location
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONArray
import org.json.JSONObject

fun Location.toLatLng(): LatLng = LatLng(latitude, longitude)

fun orderedPointsFor(hunt: Hunt): List<LatLng> = buildList {
  add(LatLng(hunt.start.latitude, hunt.start.longitude))
  hunt.middlePoints.forEach { add(LatLng(it.latitude, it.longitude)) }
  add(LatLng(hunt.end.latitude, hunt.end.longitude))
}

fun nextPointFor(hunt: Hunt?, validatedCount: Int): LatLng? {
  if (hunt == null) return null
  val ordered = orderedPointsFor(hunt)
  val nextIdx = validatedCount.coerceAtLeast(0)
  return ordered.getOrNull(nextIdx)
}

fun distanceMeters(from: LatLng, to: LatLng): Int? =
    try {
      SphericalUtil.computeDistanceBetween(from, to).toInt()
    } catch (_: Exception) {
      null
    }

fun computeDistanceMetersRaw(from: LatLng, to: LatLng): Double =
    SphericalUtil.computeDistanceBetween(from, to)

fun computeDistanceToNextPoint(hunt: Hunt, validatedCount: Int, currentLocation: LatLng): Int? {
  val next = nextPointFor(hunt, validatedCount) ?: return null
  return distanceMeters(currentLocation, next)
}

fun isHuntFullyValidated(hunt: Hunt, validatedCount: Int): Boolean {
  val total = 2 + hunt.middlePoints.size
  return validatedCount >= total
}

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
            add("${UrlParams.ORIGIN}=" + URLEncoder.encode(origin, HttpConstants.UTF_8))
            add("${UrlParams.DESTINATION}=" + URLEncoder.encode(destination, HttpConstants.UTF_8))
            add("${UrlParams.MODE}=" + URLEncoder.encode(travelMode, HttpConstants.UTF_8))
            waypointParam?.let {
              add("${UrlParams.WAYPOINTS}=" + URLEncoder.encode(it, HttpConstants.UTF_8))
            }
            add("${UrlParams.KEY}=" + URLEncoder.encode(apiKey, HttpConstants.UTF_8))
          }
          .joinToString("&")

  return URL("${MapConfig.DirectionsBaseUrl}?$params")
}

@VisibleForTesting
var directionsConnectionFactory: (URL) -> HttpURLConnection = { url ->
  (url.openConnection() as HttpURLConnection).apply {
    requestMethod = HttpConstants.REQUEST_METHOD_GET
    connectTimeout = MapConfig.DirectionsConnectTimeoutMs
    readTimeout = MapConfig.DirectionsReadTimeoutMs
    doInput = true
  }
}

private fun openDirectionsConnection(url: URL): HttpURLConnection = directionsConnectionFactory(url)

private fun parseDirectionsResponse(json: JSONObject): List<LatLng> {
  ensureStatusOk(json)

  val routes = json.getJSONArray(JsonKeys.ROUTES)
  if (routes.length() == 0) return emptyList()

  val firstRoute = routes.getJSONObject(0)
  val legs = firstRoute.getJSONArray(JsonKeys.LEGS)

  val fullPath = buildFullPath(legs)
  if (fullPath.isNotEmpty()) return fullPath

  val overview = firstRoute.getJSONObject(JsonKeys.OVERVIEW_POLYLINE).getString(JsonKeys.POINTS)

  return PolyUtil.decode(overview)
}

private fun ensureStatusOk(json: JSONObject) {
  val status = json.optString(JsonKeys.STATUS)
  if (status != HttpConstants.OK_STATUS_TEXT) {
    val message = json.optString(JsonKeys.ERROR_MESSAGE, status)
    throw IllegalStateException(JsonErrorMessages.DIRECTIONS_API_PREFIX + message)
  }
}

private fun buildFullPath(legs: JSONArray): List<LatLng> {
  val fullPath = mutableListOf<LatLng>()

  for (i in 0 until legs.length()) {
    val leg = legs.getJSONObject(i)
    val steps = leg.getJSONArray(JsonKeys.STEPS)
    appendStepsToPath(steps, fullPath)
  }

  return fullPath
}

private fun appendStepsToPath(steps: JSONArray, fullPath: MutableList<LatLng>) {
  for (j in 0 until steps.length()) {
    val step = steps.getJSONObject(j)
    val poly = step.getJSONObject(JsonKeys.POLYLINE).getString(JsonKeys.POINTS)

    val stepPoints = PolyUtil.decode(poly).toList()

    if (fullPath.isNotEmpty()) {
      fullPath.addAll(stepPoints.drop(1))
    } else {
      fullPath.addAll(stepPoints)
    }
  }
}
