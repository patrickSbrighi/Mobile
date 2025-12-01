package com.example.mobile.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mobile.ui.composables.BottomNavBar
import com.example.mobile.ui.screens.*
import kotlinx.serialization.Serializable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

sealed interface Route {
    @Serializable data object Login: Route
    @Serializable data object Registration: Route
    @Serializable data object Home: Route
    @Serializable data object Profile: Route
    @Serializable data object Search: Route
    @Serializable data object Create: Route
}

@Composable
fun NavGraph(navController: NavHostController){
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var userRole by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userRole = document.getString("role")
                    }
                }
        }
    }

    val startScreen = if (currentUser != null) {
        Route.Home
    } else {
        Route.Login
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.hierarchy?.any { destination ->
        destination.hasRoute<Route.Home>() ||
                destination.hasRoute<Route.Search>() ||
                destination.hasRoute<Route.Profile>()
    } == true

    val showFab = showBottomBar && userRole == "ORGANIZER"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { navController.navigate(Route.Create) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Crea Evento")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startScreen,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Route.Login> {
                LoginScreen(navController)
            }
            composable<Route.Registration> {
                RegistrationScreen(navController)
            }
            composable<Route.Home> {
                HomeScreen(navController)
            }
            composable<Route.Profile> {
                ProfileScreen(navController)
            }
            composable<Route.Search> {
                SearchSreen(navController)
            }
            composable<Route.Create> {
                CreateScreen(navController)
            }
        }
    }
}