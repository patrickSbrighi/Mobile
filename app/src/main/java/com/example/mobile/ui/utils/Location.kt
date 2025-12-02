package com.example.mobile.ui.utils

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import org.osmdroid.util.GeoPoint
import java.util.Locale

fun getUserLocation(context: Context, onResult: (String, GeoPoint?) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                val geoPoint = GeoPoint(location.latitude, location.longitude)

                if (!addresses.isNullOrEmpty()) {
                    val city = addresses[0].locality ?: "Città sconosciuta"
                    val country = addresses[0].countryCode ?: ""
                    onResult("$city, $country", geoPoint)
                } else {
                    onResult("Posizione sconosciuta", geoPoint)
                }
            } else {
                onResult("GPS non disponibile", null)
            }
        }
    } catch (e: SecurityException) {
        onResult("Errore permessi", null)
    }
}