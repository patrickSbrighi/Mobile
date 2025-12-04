package com.example.mobile.ui.data

enum class UserRole { FAN, ORGANIZER }

data class Event(
    val id: String = "",
    val organizerId: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val date: String = "",
    val time: String = "",
    val genre: String = "",
    val imageUrl: String = "",
    val hype: Int = 0,
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

data class UserProfile(
    val username: String = "",
    val email: String = "",
    val role: String = "FAN",
    val genres: List<String> = emptyList(),
    val city: String = "",
    val profileImageUrl: String = ""
)

data class PlaceResult(
    val displayName: String,
    val lat: Double,
    val lon: Double,
    val address: AddressComponents? = null
)

data class AddressComponents(
    val road: String = "",
    val houseNumber: String = "",
    val city: String = "",
    val province: String = "",
    val country: String = ""
)