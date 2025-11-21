package com.example.mobile.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mobile.ui.screens.LoginScreen
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Login: Route
    @Serializable data object Registration: Route
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
            //RegistrationScreen(navController)
        }
    }
}
