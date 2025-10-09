package com.example.hyperlocal_forum.data

import androidx.room.Entity

@Entity(tableName = "topics")
data class Topic(
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val content: String
)