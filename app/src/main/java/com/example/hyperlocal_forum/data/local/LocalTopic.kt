package com.example.hyperlocal_forum.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class LocalTopic(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
