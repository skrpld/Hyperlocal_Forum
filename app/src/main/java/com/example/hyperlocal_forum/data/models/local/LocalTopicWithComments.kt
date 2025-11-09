package com.example.hyperlocal_forum.data.models.local

import androidx.room.Embedded
import androidx.room.Relation

data class LocalTopicWithComments(
    @Embedded
    val topic: LocalTopic,

    @Relation(
        parentColumn = "id",
        entityColumn = "topicId"
    )
    val comments: List<LocalComment>
)
