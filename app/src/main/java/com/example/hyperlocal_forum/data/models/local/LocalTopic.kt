package com.example.hyperlocal_forum.data.models.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class LocalTopic(
    @PrimaryKey
    val id: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)