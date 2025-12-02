package com.example.mobile.ui.data

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