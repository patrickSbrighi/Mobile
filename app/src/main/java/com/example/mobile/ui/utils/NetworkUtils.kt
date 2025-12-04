package com.example.mobile.ui.utils

import com.example.mobile.ui.data.AddressComponents
import com.example.mobile.ui.data.PlaceResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

suspend fun searchPlaces(query: String): List<PlaceResult> {
    return withContext(Dispatchers.IO) {
        try {
            val cleanedQuery = query.replace(Regex("[,\\s]+"), " ").trim()
            val encodedQuery = URLEncoder.encode(cleanedQuery, "UTF-8")
            val urlString = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=5&addressdetails=1"
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "UndrgrndHypeApp/1.0")

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(response)
                val results = mutableListOf<PlaceResult>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val lat = obj.getString("lat").toDouble()
                    val lon = obj.getString("lon").toDouble()
                    val displayName = obj.getString("display_name")

                    var components: AddressComponents? = null
                    if (obj.has("address")) {
                        val addrObj = obj.getJSONObject("address")
                        components = AddressComponents(
                            road = addrObj.optString("road", ""),
                            houseNumber = addrObj.optString("house_number", ""),
                            city = addrObj.optString("city", addrObj.optString("town", addrObj.optString("village", ""))),
                            province = addrObj.optString("county", ""),
                            country = addrObj.optString("country", "")
                        )
                    }

                    results.add(PlaceResult(displayName, lat, lon, components))
                }
                return@withContext results
            }
            return@withContext emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }
}

suspend fun getAddressFromCoordinates(lat: Double, lon: Double): PlaceResult? {
    return withContext(Dispatchers.IO) {
        try {
            val urlString = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon&addressdetails=1"
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "UndrgrndHypeApp/1.0")

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(response)

                val displayName = obj.getString("display_name")

                var components: AddressComponents? = null
                if (obj.has("address")) {
                    val addrObj = obj.getJSONObject("address")
                    components = AddressComponents(
                        road = addrObj.optString("road", ""),
                        houseNumber = addrObj.optString("house_number", ""),
                        city = addrObj.optString("city", addrObj.optString("town", addrObj.optString("village", ""))),
                        province = addrObj.optString("county", ""),
                        country = addrObj.optString("country", "")
                    )
                }
                return@withContext PlaceResult(displayName, lat, lon, components)
            }
            return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}