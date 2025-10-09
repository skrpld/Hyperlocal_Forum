package com.example.hyperlocal_forum.data

import androidx.room.Embedded
import androidx.room.Relation

data class TopicWithComments(
    @Embedded
    val topic: Topic,

    @Relation(
        parentColumn = "id",
        entityColumn = "topicId"
    )
    val comments: List<Comment>
)
