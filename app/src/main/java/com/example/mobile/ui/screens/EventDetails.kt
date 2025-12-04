package com.example.mobile.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mobile.ui.composables.OsmUserMap
import com.example.mobile.ui.data.Event
import com.example.mobile.ui.data.FirebaseRepository

val FireColor = Color(0xFFFF5722)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(navController: NavController, eventId: String) {
    var event by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUser = FirebaseRepository.getCurrentUser()
    val context = LocalContext.current

    LaunchedEffect(eventId) {
        FirebaseRepository.getEventById(eventId) { fetchedEvent ->
            event = fetchedEvent
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.title ?: "Dettagli Evento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        },
        floatingActionButton = {
            if (event != null && currentUser != null) {
                val isHyped = event!!.hypedBy.contains(currentUser.uid)

                val containerColor = if(isHyped) FireColor else MaterialTheme.colorScheme.surfaceContainerHigh
                val contentColor = if(isHyped) Color.White else MaterialTheme.colorScheme.onSurface

                ExtendedFloatingActionButton(
                    onClick = {
                        FirebaseRepository.toggleHype(eventId) {
                            FirebaseRepository.getEventById(eventId) { updated -> event = updated }
                        }
                    },
                    containerColor = containerColor,
                    contentColor = contentColor,
                    icon = { Text("🔥", fontSize = 24.sp) },
                    text = { Text("Hype") }
                )
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (event == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Evento non trovato")
            }
        } else {
            val e = event!!
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {
                if (e.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = Uri.parse(e.imageUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nessuna immagine")
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = e.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "🔥 ${e.hype}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    AssistChip(onClick = {}, label = { Text(e.genre) })

                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(e.date, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.width(16.dp))
                        Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(e.time, style = MaterialTheme.typography.bodyLarge)
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(e.location, style = MaterialTheme.typography.bodyLarge)
                    }

                    HorizontalDivider(Modifier.padding(vertical = 16.dp))

                    Text("Descrizione", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(e.description, style = MaterialTheme.typography.bodyMedium)

                    Spacer(Modifier.height(24.dp))

                    Text("Mappa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                    ) {
                        if (e.lat != 0.0 && e.lng != 0.0) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                OsmUserMap(events = listOf(e), userLocation = null)
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable {
                                            val gmmIntentUri = Uri.parse("geo:${e.lat},${e.lng}?q=${e.lat},${e.lng}(${Uri.encode(e.title)})")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                            mapIntent.setPackage("com.google.android.apps.maps")
                                            try {
                                                context.startActivity(mapIntent)
                                            } catch (ex: Exception) {
                                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${e.lat},${e.lng}"))
                                                context.startActivity(browserIntent)
                                            }
                                        }
                                )
                            }
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Mappa non disponibile")
                            }
                        }
                    }

                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}