package com.example.mobile.ui.composables

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mobile.R
import com.example.mobile.ui.utils.resizeDrawableResource
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun OsmLocationPicker(
    startLocation: GeoPoint,
    initialZoom: Double = 15.0,
    onLocationPicked: (GeoPoint) -> Unit
) {
    val context = LocalContext.current

    val pinIcon = remember(context) {
        resizeDrawableResource(context, R.drawable.pin_rosso, 32, 32)
    }

    var pickedPoint by remember { mutableStateOf(startLocation) }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                minZoomLevel = 4.0
                controller.setZoom(initialZoom)
                controller.setCenter(startLocation)

                val eventsReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        p?.let {
                            pickedPoint = it
                            this@apply.overlays.removeAll { o -> o is Marker && o.title == "Picked" }

                            val marker = Marker(this@apply)
                            marker.position = it
                            marker.title = "Picked"
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            if (pinIcon != null) marker.icon = pinIcon

                            this@apply.overlays.add(marker)
                            this@apply.invalidate()

                            onLocationPicked(it)
                        }
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint?): Boolean = false
                }

                val eventsOverlay = MapEventsOverlay(eventsReceiver)
                overlays.add(eventsOverlay)
            }
        },
    )
}