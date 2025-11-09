package com.example.hyperlocal_forum.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.hyperlocal_forum.data.local.LocalComment
import com.example.hyperlocal_forum.data.local.LocalTopic
import com.example.hyperlocal_forum.data.local.LocalTopicWithComments
import com.example.hyperlocal_forum.data.local.LocalUser
import kotlinx.coroutines.flow.Flow

@Dao
interface ForumDao {
    @Insert
    suspend fun insertTopic(topic: LocalTopic): Long

    @Insert
    suspend fun insertComment(comment: LocalComment): Long

    @Query("SELECT * FROM topics")
    fun getAllTopics(): Flow<List<LocalTopic>>

    @Transaction
    @Query("SELECT * FROM topics WHERE id = :topicId")
    fun getTopicWithComments(topicId: Long): Flow<LocalTopicWithComments>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUser(userId: Long): Flow<LocalUser>

    @Update
    suspend fun updateUser(user: LocalUser)

    @Query("UPDATE users SET passwordHash = :password WHERE id = :userId")
    suspend fun updateUserPassword(userId: Long, password: String)

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): LocalUser?

    @Query("SELECT * FROM users WHERE username = :username AND passwordHash = :passwordHash")
    suspend fun authenticateUser(username: String, passwordHash: String): LocalUser?

    @Insert
    suspend fun insertUser(user: LocalUser): Long

    @Query("""
        SELECT * FROM topics 
        WHERE ABS(latitude - :lat) < 0.1 AND ABS(longitude - :lon) < 0.1
        ORDER BY timestamp DESC
    """)
    fun getNearbyTopics(lat: Double, lon: Double): Flow<List<LocalTopic>>
}