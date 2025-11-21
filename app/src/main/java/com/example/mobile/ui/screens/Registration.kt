package com.example.mobile.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.mobile.ui.composables.AppBar

@Composable
fun RegistrationScreen(navController: NavController) {
    Scaffold(
        topBar = { AppBar(navController, title = "Login") }
    ) { contentPadding ->
        Text("Registrazione")
    }
}