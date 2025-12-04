package com.example.mobile.ui.utils

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.LocationServices
import org.osmdroid.util.GeoPoint
import java.util.Locale

@Suppress("DEPRECATION")
fun getUserLocation(context: Context, onResult: (String, GeoPoint?) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                val geocoder = Geocoder(context, Locale.getDefault())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val city = addresses[0].locality ?: addresses[0].subAdminArea ?: "Città sconosciuta"
                            val country = addresses[0].countryCode ?: ""
                            onResult("$city, $country", geoPoint)
                        } else {
                            onResult("Posizione sconosciuta", geoPoint)
                        }
                    }
                } else {
                    try {
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val city = addresses[0].locality ?: addresses[0].subAdminArea ?: "Città sconosciuta"
                            val country = addresses[0].countryCode ?: ""
                            onResult("$city, $country", geoPoint)
                        } else {
                            onResult("Posizione sconosciuta", geoPoint)
                        }
                    } catch (e: Exception) {
                        onResult("Errore Geocoder", geoPoint)
                    }
                }
            } else {
                onResult("GPS attivo ma posizione non trovata", null)
            }
        }.addOnFailureListener {
            onResult("Errore recupero posizione", null)
        }
    } catch (e: SecurityException) {
        onResult("Permessi mancanti", null)
    }
}