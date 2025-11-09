package com.example.hyperlocal_forum.data.firebase

import com.example.hyperlocal_forum.data.GeoCoordinates
import com.google.firebase.Timestamp

data class Topic(
    val id: String = "",
    val userId: String,
    val location: GeoCoordinates,
    val title: String,
    val content: String,
    val timestamp: Timestamp = Timestamp.now()
)
