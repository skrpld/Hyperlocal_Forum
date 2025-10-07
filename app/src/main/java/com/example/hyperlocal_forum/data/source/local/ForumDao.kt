package com.example.hyperlocal_forum.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ForumDao {
    @Insert
    suspend fun insertTopic(topic: LocalTopic): Long

    @Insert
    suspend fun insertComment(comment: LocalComment): Long

    @Query("SELECT * FROM topics")
    suspend fun getAllTopics(): List<LocalTopic>

    @Transaction
    @Query("SELECT * FROM topics WHERE id = :topicId")
    suspend fun getTopicWithComments(topicId: Long): LocalTopicWithComments
}