package com.example.hyperlocal_forum.data.firebase

data class TopicWithComments(
    val topic: Topic,
    val comments: List<Comment>
)
