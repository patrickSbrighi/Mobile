package com.example.mobile.ui.composables

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mobile.ui.data.Event

@Composable
fun EventListSection(events: List<Event>, onEventClick: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(events) { event ->
            EventCard(event, onClick = { onEventClick(event.id) })
        }
    }
}

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            if (event.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = Uri.parse(event.imageUrl),
                    contentDescription = "Locandina",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    Alignment.Center
                ) {
                    Text("Locandina", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column(Modifier.padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.small) {
                        Text("🔥 ${event.hype}", Modifier.padding(6.dp, 2.dp), style = MaterialTheme.typography.labelSmall)
                    }
                }
                Text("${event.location} • ${event.date}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun EmptyStateMessage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Nessun evento trovato", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
    }
}