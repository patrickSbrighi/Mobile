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
import androidx.navigation.toRoute
import com.example.mobile.ui.composables.BottomNavBar
import com.example.mobile.ui.data.FirebaseRepository
import com.example.mobile.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    val currentUser = FirebaseRepository.getCurrentUser()
    var userRole by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            FirebaseRepository.getUserRole { role ->
                userRole = role
            }
        }
    }

    val startScreen = if (currentUser != null) Route.Home else Route.Login

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
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
        ) {
            composable<Route.Login> {
                LoginScreen(navController)
            }
            composable<Route.Registration> {
                RegistrationScreen(navController)
            }
            composable<Route.Home> {
                HomeScreen(
                    navController = navController,
                    onEventClick = { eventId ->
                        navController.navigate(Route.EventDetail(eventId))
                    }
                )
            }
            composable<Route.Profile> {
                ProfileScreen(navController, userRole = userRole)
            }
            composable<Route.Search> {
                SearchScreen(
                    navController = navController,
                    onEventClick = { eventId ->
                        navController.navigate(Route.EventDetail(eventId))
                    }
                )
            }
            composable<Route.Create> {
                CreateScreen(navController)
            }
            composable<Route.EventDetail> { backStackEntry ->
                val detail: Route.EventDetail = backStackEntry.toRoute()
                EventDetailScreen(navController, detail.eventId)
            }
        }
    }
}