package com.swentseekr.seekr.ui.map

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.swentseekr.seekr.ui.map.MapScreenStrings.BEEN_LOADED
import com.swentseekr.seekr.ui.map.MapScreenStrings.ERROR_RESOURCE_ID

/**
 * Converts a vector drawable resource into a [BitmapDescriptor] that can be used as a custom marker
 * icon on Google Maps.
 *
 * This is necessary because Google Maps does not directly support vector drawables as marker icons;
 * they must be converted into a bitmap first.
 *
 * ### How it works:
 * - Retrieves the vector drawable from the provided resource ID.
 * - Creates a bitmap with matching intrinsic width and height.
 * - Draws the vector onto a [Canvas] backed by that bitmap.
 * - Converts the resulting bitmap into a Google Maps-compatible [BitmapDescriptor].
 *
 * @param context the [Context] used to access drawable resources.
 * @param vectorResId the resource ID of the vector drawable (e.g. `R.drawable.ic_marker`).
 * @return a [BitmapDescriptor] suitable for use in `MarkerOptions.icon()`.
 */
fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
  val vectorDrawable =
      ContextCompat.getDrawable(context, vectorResId)
          ?: error("$ERROR_RESOURCE_ID $vectorResId $BEEN_LOADED")
  vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)

  val bitmap = createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)

  val canvas = Canvas(bitmap)
  vectorDrawable.draw(canvas)

  return BitmapDescriptorFactory.fromBitmap(bitmap)
}
