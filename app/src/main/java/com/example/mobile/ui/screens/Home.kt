package com.example.mobile.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.preference.PreferenceManager
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
import com.example.mobile.ui.data.FirebaseFunction
import com.example.mobile.ui.composables.*
import com.example.mobile.ui.utils.getUserLocation
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current

    Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
    Configuration.getInstance().userAgentValue = context.packageName

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
        FirebaseFunction.getUserProfile { profile ->
            if (profile != null && profile.genres.isNotEmpty()) {
                availableGenres = listOf("Tutti") + profile.genres
            }
        }

        FirebaseFunction.listenToEvents { events ->
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

    val filteredEvents = if (selectedGenre == "Tutti") allEvents else allEvents.filter { it.genre.equals(selectedGenre, ignoreCase = true) }

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
                OsmUserMap(events = filteredEvents, userLocation = userGeoPoint)
            } else {
                if (filteredEvents.isEmpty()) {
                    EmptyStateMessage()
                } else {
                    EventListSection(filteredEvents)
                }
            }
        }
    }
}