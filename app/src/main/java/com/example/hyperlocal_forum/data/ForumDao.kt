package com.example.hyperlocal_forum.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.hyperlocal_forum.data.models.local.LocalComment
import com.example.hyperlocal_forum.data.models.local.LocalTopic
import com.example.hyperlocal_forum.data.models.local.LocalTopicWithComments
import com.example.hyperlocal_forum.data.models.local.LocalUser
import kotlinx.coroutines.flow.Flow

@Dao
interface ForumDao {
    @Insert
    suspend fun insertTopic(topic: LocalTopic)

    @Insert
    suspend fun insertComment(comment: LocalComment)

    @Query("SELECT * FROM topics")
    fun getAllTopics(): Flow<List<LocalTopic>>

    @Transaction
    @Query("SELECT * FROM topics WHERE id = :topicId")
    fun getTopicWithComments(topicId: String): Flow<LocalTopicWithComments>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUser(userId: String): Flow<LocalUser>

    @Update
    suspend fun updateUser(user: LocalUser)

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): LocalUser?

    @Query("SELECT * FROM users WHERE username = :username AND passwordHash = :passwordHash")
    suspend fun authenticateUser(username: String, passwordHash: String): LocalUser?

    @Insert
    suspend fun insertUser(user: LocalUser)

}