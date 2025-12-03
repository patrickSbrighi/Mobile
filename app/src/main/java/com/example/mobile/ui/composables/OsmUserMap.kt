package com.example.mobile.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mobile.R
import com.example.mobile.ui.data.Event
import com.example.mobile.ui.utils.resizeDrawableResource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.example.mobile.ui.data.DEFAULT_LOCATION

@Composable
fun OsmUserMap(events: List<Event>, userLocation: GeoPoint?) {
    val context = LocalContext.current

    val startPoint = userLocation ?: DEFAULT_LOCATION

    val smallUserIcon = remember(context) {
        resizeDrawableResource(context, R.drawable.ic_user_pointer, 16, 16)
    }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(14.0)
                controller.setCenter(startPoint)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            events.forEach { event ->
                if (event.lat != 0.0 && event.lng != 0.0) {
                    val marker = Marker(mapView)
                    marker.position = GeoPoint(event.lat, event.lng)
                    marker.title = event.title
                    marker.snippet = event.genre
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(marker)
                }
            }

            if (userLocation != null) {
                val userMarker = Marker(mapView)
                userMarker.position = userLocation
                userMarker.title = "TU SEI QUI"
                userMarker.snippet = "Posizione attuale"
                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                if (smallUserIcon != null) {
                    userMarker.icon = smallUserIcon
                }

                mapView.overlays.add(userMarker)
            }
            mapView.invalidate()
        }
    )
}