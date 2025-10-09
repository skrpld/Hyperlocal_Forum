package com.example.hyperlocal_forum.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.hyperlocal_forum.data.Topic

@Entity(tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = Topic::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Comment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val topicId: Long,
    val content: String
)