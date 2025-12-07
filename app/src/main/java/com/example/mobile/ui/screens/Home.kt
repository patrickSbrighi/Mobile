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
import com.example.mobile.ui.utils.calculateDistance
import com.example.mobile.ui.utils.getUserLocation
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, onEventClick: (String) -> Unit) {
    val context = LocalContext.current

    var isMapView by remember { mutableStateOf(false) }
    var selectedGenre by remember { mutableStateOf("Tutti") }
    var userCity by remember { mutableStateOf("Rilevamento...") }
    var availableGenres by remember { mutableStateOf(listOf("Tutti", "Più Hype")) }
    var userRole by remember { mutableStateOf<String?>(null) }

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
            if (profile != null) {
                userRole = profile.role
                if (profile.genres.isNotEmpty()) {
                    availableGenres = listOf("Tutti", "Più Hype") + profile.genres
                }
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

        var list = when (selectedGenre) {
            "Tutti", "Più Hype" -> allEvents
            else -> allEvents.filter { it.genre.equals(selectedGenre, ignoreCase = true) }
        }

        list = list.filter {
            try {
                val date = sdf.parse(it.date)
                date != null && (date.time + 86400000) > now
            } catch (e: Exception) { true }
        }

        if (selectedGenre == "Più Hype") {
            list.sortedByDescending { it.hype }
        } else {
            list.sortedBy { event ->
                val distanceKm = if (userGeoPoint != null && event.lat != 0.0 && event.lng != 0.0) {
                    calculateDistance(userGeoPoint!!.latitude, userGeoPoint!!.longitude, event.lat, event.lng)
                } else {
                    10000.0
                }

                val eventDate = try { sdf.parse(event.date)?.time ?: Long.MAX_VALUE } catch (e: Exception) { Long.MAX_VALUE }
                val diffInMillis = eventDate - now
                val daysUntil = TimeUnit.MILLISECONDS.toDays(diffInMillis).toDouble()

                val score = (distanceKm * 1.0) + (daysUntil * 2.0) - (event.hype * 0.1)
                score
            }
        }
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
        val listBottomPadding = if (userRole == "ORGANIZER") 170.dp else 135.dp

        Box(modifier = Modifier
            .padding(top = contentPadding.calculateTopPadding())
            .fillMaxSize()
        ) {
            if (isMapView) {
                OsmUserMap(events = processedEvents, userLocation = userGeoPoint)
            } else {
                if (processedEvents.isEmpty()) {
                    EmptyStateMessage()
                } else {
                    EventListSection(
                        events = processedEvents,
                        onEventClick = onEventClick,
                        bottomPadding = listBottomPadding
                    )
                }
            }
        }
    }
}