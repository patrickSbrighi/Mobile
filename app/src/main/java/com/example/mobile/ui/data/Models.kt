package com.example.mobile.ui.data

enum class UserRole { FAN, ORGANIZER }

data class Event(
    val id: String = "",
    val title: String = "",
    val location: String = "",
    val date: String = "",
    val genre: String = "",
    val hype: Int = 0,
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

data class UserProfile(
    val username: String = "",
    val email: String = "",
    val role: String = "FAN",
    val genres: List<String> = emptyList(),
    val city: String = ""
)