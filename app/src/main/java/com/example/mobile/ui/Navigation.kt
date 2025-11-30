package com.example.mobile.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mobile.ui.screens.HomeScreen
import com.example.mobile.ui.screens.LoginScreen
import com.example.mobile.ui.screens.ProfileScreen
import com.example.mobile.ui.screens.RegistrationScreen
import kotlinx.serialization.Serializable
import com.google.firebase.auth.FirebaseAuth

sealed interface Route {
    @Serializable data object Login: Route
    @Serializable data object Registration: Route
    @Serializable data object Home: Route
    @Serializable data object Profile: Route
}

@Composable
fun NavGraph(navController: NavHostController){
    val utenteCorrente = FirebaseAuth.getInstance().currentUser
    val startScreen = if (utenteCorrente != null) {
        Route.Profile
    } else {
        Route.Login
    }

    NavHost(
        navController = navController,
        startDestination = startScreen
    ){
        composable<Route.Login>{
            LoginScreen(navController)
        }
        composable<Route.Registration>{
            RegistrationScreen(navController)
        }
        composable<Route.Home>{
            HomeScreen(navController)
        }
        composable<Route.Profile>{
            ProfileScreen(navController)
        }
    }
}
