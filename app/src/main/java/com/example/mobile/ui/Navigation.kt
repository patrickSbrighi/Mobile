package com.example.mobile.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mobile.ui.composables.BottomNavBar
import com.example.mobile.ui.screens.HomeScreen
import com.example.mobile.ui.screens.LoginScreen
import com.example.mobile.ui.screens.ProfileScreen
import com.example.mobile.ui.screens.RegistrationScreen
import com.example.mobile.ui.screens.SearchSreen
import kotlinx.serialization.Serializable
import com.google.firebase.auth.FirebaseAuth

sealed interface Route {
    @Serializable data object Login: Route
    @Serializable data object Registration: Route
    @Serializable data object Home: Route
    @Serializable data object Profile: Route
    @Serializable data object Search: Route
}

@Composable
fun NavGraph(navController: NavHostController){
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

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

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
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
        }
    }
}
