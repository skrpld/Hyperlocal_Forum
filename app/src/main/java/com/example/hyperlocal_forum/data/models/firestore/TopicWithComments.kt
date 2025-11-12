package com.example.hyperlocal_forum.data.models.firestore

data class TopicWithComments(
    val topic: Topic,
    val comments: List<Comment>
)