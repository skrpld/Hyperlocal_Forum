package com.example.hyperlocal_forum.data.source.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class LocalTopic(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val content: String
)
