package com.example.mobile.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mobile.ui.screens.HomeScreen
import com.example.mobile.ui.screens.LoginScreen
import com.example.mobile.ui.screens.RegistrationScreen
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Login: Route
    @Serializable data object Registration: Route
    @Serializable data object Home: Route
}

@Composable
fun NavGraph(navController: NavHostController){
    NavHost(
        navController = navController,
        startDestination = Route.Login
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
    }
}
