package com.example.hyperlocal_forum.data.models.firestore

import com.google.firebase.Timestamp

data class User(
    val id: String = "",
    val username: String,
    val email: String? = null,
    val timestamp: Timestamp = Timestamp.now()
)