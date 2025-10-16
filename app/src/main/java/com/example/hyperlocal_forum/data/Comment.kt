package com.example.hyperlocal_forum.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = Topic::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["topicId"])]
)
data class Comment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val topicId: Long,
    val content: String
)
