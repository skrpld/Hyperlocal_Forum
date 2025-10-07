package com.example.hyperlocal_forum.data.source.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = LocalTopic::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LocalComment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val topicId: Long,
    val content: String
)
