package com.example.hyperlocal_forum.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUser(userId: Long): Flow<User>

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE username = :username AND passwordHash = :passwordHash")
    suspend fun authenticateUser(username: String, passwordHash: String): User?

    @Insert
    suspend fun insertUser(user: User): Long
}