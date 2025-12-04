package com.example.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobile.ui.composables.EventListSection
import com.example.mobile.ui.data.Event
import com.example.mobile.ui.data.FirebaseRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, onEventClick: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var allEvents by remember { mutableStateOf<List<Event>>(emptyList()) }

    LaunchedEffect(Unit) {
        FirebaseRepository.listenToEvents { events ->
            allEvents = events
        }
    }

    val filteredEvents = remember(query, allEvents) {
        if (query.isBlank()) {
            emptyList()
        } else {
            allEvents.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.genre.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { },
                active = false,
                onActiveChange = {},
                placeholder = { Text("Cerca eventi, generi...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {}
        }
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            if (query.isNotEmpty()) {
                if (filteredEvents.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.TopCenter) {
                        Text("Nessun risultato trovato", color = MaterialTheme.colorScheme.secondary)
                    }
                } else {
                    EventListSection(events = filteredEvents, onEventClick = onEventClick)
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.TopCenter) {
                    Text("Inizia a digitare per cercare", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}