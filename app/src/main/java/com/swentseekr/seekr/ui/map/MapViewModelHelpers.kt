package com.swentseekr.seekr.ui.map

import com.google.android.gms.maps.model.LatLng
import com.swentseekr.seekr.model.map.Location

fun Location.toLatLng(): LatLng = LatLng(latitude, longitude)
