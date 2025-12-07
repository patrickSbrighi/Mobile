package com.example.mobile.ui.utils

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.widget.Toast
import com.example.mobile.ui.data.DEFAULT_LOCATION
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

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

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

fun performSearchAndShowMap(
    address: String, civico: String, city: String, province: String, country: String,
    isManualSearchRequired: Boolean,
    scope: kotlinx.coroutines.CoroutineScope,
    context: Context,
    onResult: (GeoPoint, Double, Boolean) -> Unit
) {
    if (!isManualSearchRequired) {
        onResult(DEFAULT_LOCATION, 15.0, false)
        return
    }

    val queryParts = mutableListOf<String>()
    if (address.isNotBlank()) queryParts.add(address)
    if (civico.isNotBlank()) queryParts.add(civico)
    if (city.isNotBlank()) queryParts.add(city)
    if (province.isNotBlank()) queryParts.add(province)
    if (country.isNotBlank()) queryParts.add(country)

    val query = queryParts.joinToString(", ")

    if (query.length < 3) {
        Toast.makeText(context, "Inserisci almeno Città o Stato", Toast.LENGTH_SHORT).show()
        onResult(DEFAULT_LOCATION, 10.0, false)
        return
    }

    scope.launch {
        val results = searchPlaces(query)
        if (results.isNotEmpty()) {
            onResult(GeoPoint(results[0].lat, results[0].lon), 18.0, true)
        } else {
            Toast.makeText(context, "Zona non trovata, mappa centrata di default", Toast.LENGTH_SHORT).show()
            onResult(DEFAULT_LOCATION, 10.0, true)
        }
    }
}