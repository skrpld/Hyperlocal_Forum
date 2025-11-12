package com.example.hyperlocal_forum.data.models.firestore

import com.google.firebase.Timestamp

data class Comment(
    val id: String = "",
    val userId: String,
    val topicId: String,
    val content: String,
    val username: String,
    val timestamp: Timestamp = Timestamp.now()
)