package com.example.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobile.ui.composables.AppBar
import io.ktor.websocket.Frame

@Composable
fun LoginScreen(navController: NavController) {
    Scaffold(
        topBar = { AppBar(navController, title = "Login") },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.tertiary,
                onClick = { navController.navigateUp() }
            ) {
                Icon(Icons.Outlined.Check, "Add Travel")
            }
        },
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = { /*TODO*/ },
                label = { Frame.Text("Destination") },
                modifier = Modifier.run { fillMaxWidth() },
                trailingIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
                    }
                }
            )
            OutlinedTextField(
                value = "",
                onValueChange = { /*TODO*/ },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = "",
                onValueChange = { /*TODO*/ },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.size(24.dp))
            Button(
                onClick = { /*TODO*/ },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Take a picture")
            }
        }
    }
}

