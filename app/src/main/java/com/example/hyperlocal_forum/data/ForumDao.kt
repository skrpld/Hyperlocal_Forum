package com.example.hyperlocal_forum.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ForumDao {
    @Insert
    suspend fun insertTopic(topic: Topic): Long

    @Insert
    suspend fun insertComment(comment: Comment): Long

    @Query("SELECT * FROM topics")
    suspend fun getAllTopics(): List<Topic>

    @Transaction
    @Query("SELECT * FROM topics WHERE id = :topicId")
    suspend fun getTopicWithComments(topicId: Long): TopicWithComments
}