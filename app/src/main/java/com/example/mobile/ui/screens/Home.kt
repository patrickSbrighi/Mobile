package com.example.mobile.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.mobile.ui.data.Event
import com.example.mobile.ui.data.FirebaseRepository
import com.example.mobile.ui.composables.*
import com.example.mobile.ui.utils.getUserLocation
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, onEventClick: (String) -> Unit) {
    val context = LocalContext.current

    var isMapView by remember { mutableStateOf(false) }
    var selectedGenre by remember { mutableStateOf("Tutti") }
    var userCity by remember { mutableStateOf("Rilevamento...") }
    var availableGenres by remember { mutableStateOf(listOf("Tutti")) }

    var userGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var allEvents by remember { mutableStateOf<List<Event>>(emptyList()) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getUserLocation(context) { city, geoPoint ->
                userCity = city
                userGeoPoint = geoPoint
            }
        } else {
            userCity = "Posizione negata"
        }
    }

    LaunchedEffect(Unit) {
        FirebaseRepository.getUserProfile { profile ->
            if (profile != null && profile.genres.isNotEmpty()) {
                availableGenres = listOf("Tutti") + profile.genres
            }
        }

        FirebaseRepository.listenToEvents { events ->
            allEvents = events
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getUserLocation(context) { city, geoPoint ->
                userCity = city
                userGeoPoint = geoPoint
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val processedEvents = remember(allEvents, selectedGenre, userGeoPoint) {
        val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val now = System.currentTimeMillis()

        var list = if (selectedGenre == "Tutti") allEvents else allEvents.filter { it.genre.equals(selectedGenre, ignoreCase = true) }

        list = list.filter {
            try {
                val date = sdf.parse(it.date)
                date != null && (date.time + 86400000) > now
            } catch (e: Exception) { true }
        }

        list.sortedWith(Comparator { e1, e2 ->
            if (userGeoPoint != null && e1.lat != 0.0 && e2.lat != 0.0) {
                val dist1 = calculateDistance(userGeoPoint!!.latitude, userGeoPoint!!.longitude, e1.lat, e1.lng)
                val dist2 = calculateDistance(userGeoPoint!!.latitude, userGeoPoint!!.longitude, e2.lat, e2.lng)

                if (kotlin.math.abs(dist1 - dist2) > 10.0) {
                    return@Comparator dist1.compareTo(dist2)
                }
            }

            try {
                val d1 = sdf.parse(e1.date)?.time ?: Long.MAX_VALUE
                val d2 = sdf.parse(e2.date)?.time ?: Long.MAX_VALUE
                d1.compareTo(d2)
            } catch (e: Exception) { 0 }
        })
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("Undrgrnd Hype", fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(userCity, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { isMapView = !isMapView }) {
                            Icon(if (isMapView) Icons.Default.List else Icons.Default.Place, "Toggle View")
                        }
                    }
                )

                if (!isMapView) {
                    CategoryFilterBar(availableGenres, selectedGenre) { selectedGenre = it }
                    HorizontalDivider()
                }
            }
        }
    ) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding).fillMaxSize()) {
            if (isMapView) {
                OsmUserMap(events = processedEvents, userLocation = userGeoPoint)
            } else {
                if (processedEvents.isEmpty()) {
                    EmptyStateMessage()
                } else {
                    EventListSection(events = processedEvents, onEventClick = onEventClick)
                }
            }
        }
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