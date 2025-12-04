package com.example.mobile.ui

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Login: Route
    @Serializable data object Registration: Route
    @Serializable data object Home: Route
    @Serializable data object Profile: Route
    @Serializable data object Search: Route
    @Serializable data object Create: Route
}