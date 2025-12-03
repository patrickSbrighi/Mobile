package com.example.mobile.ui.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

suspend fun getCoordinatesFromAddress(address: String): Pair<Double, Double>? {
    return withContext(Dispatchers.IO) {
        try {
            val query = URLEncoder.encode(address, "UTF-8")
            val urlString = "https://nominatim.openstreetmap.org/search?q=$query&format=json&limit=1"
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "UndrgrndHypeApp/1.0")

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(response)

                if (jsonArray.length() > 0) {
                    val firstResult = jsonArray.getJSONObject(0)
                    val lat = firstResult.getString("lat").toDouble()
                    val lon = firstResult.getString("lon").toDouble()
                    return@withContext Pair(lat, lon)
                }
            }
            return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}