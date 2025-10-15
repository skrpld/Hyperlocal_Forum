package com.example.hyperlocal_forum.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ForumDao {
    @Insert
    suspend fun insertTopic(topic: Topic): Long

    @Insert
    suspend fun insertComment(comment: Comment): Long

    @Query("SELECT * FROM topics")
    fun getAllTopics(): Flow<List<Topic>>

    @Transaction
    @Query("SELECT * FROM topics WHERE id = :topicId")
    fun getTopicWithComments(topicId: Long): Flow<TopicWithComments>
}