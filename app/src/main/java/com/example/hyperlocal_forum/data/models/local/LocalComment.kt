package com.example.hyperlocal_forum.data.models.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = LocalTopic::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["topicId"])]
)
data class LocalComment(
    @PrimaryKey
    val id: String,
    val userId: String,
    val topicId: String,
    val content: String,
    val username: String,
    val timestamp: Long = System.currentTimeMillis()
)