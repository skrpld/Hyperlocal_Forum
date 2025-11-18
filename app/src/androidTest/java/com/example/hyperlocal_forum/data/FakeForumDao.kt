package com.example.hyperlocal_forum.data

import com.example.hyperlocal_forum.data.models.local.LocalComment
import com.example.hyperlocal_forum.data.models.local.LocalTopic
import com.example.hyperlocal_forum.data.models.local.LocalTopicWithComments
import com.example.hyperlocal_forum.data.models.local.LocalUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

// Реализуем ForumDao для тестов
class FakeForumDao : ForumDao {

    private val users = mutableListOf<LocalUser>()
    private val topics = MutableStateFlow<List<LocalTopic>>(emptyList())
    private val comments = mutableListOf<LocalComment>()

    override suspend fun insertTopic(topic: LocalTopic) {
        val currentTopics = topics.value.toMutableList()
        currentTopics.removeAll { it.id == topic.id } // Удаляем старый, если обновляем
        currentTopics.add(topic)
        topics.value = currentTopics
    }

    override suspend fun updateTopic(topic: LocalTopic) {
        insertTopic(topic) // Для простоты используем ту же логику
    }

    override suspend fun insertComment(comment: LocalComment) {
        comments.add(comment)
    }

    override suspend fun deleteTopic(topicId: String) {
        val currentTopics = topics.value.toMutableList()
        currentTopics.removeAll { it.id == topicId }
        topics.value = currentTopics
    }

    override suspend fun deleteCommentsForTopic(topicId: String) {
        comments.removeAll { it.topicId == topicId }
    }

    override suspend fun deleteComment(commentId: String) {
        comments.removeAll { it.id == commentId }
    }

    override fun getAllTopics(): Flow<List<LocalTopic>> {
        return topics.asStateFlow()
    }

    override fun getTopicWithComments(topicId: String): Flow<LocalTopicWithComments> {
        return getAllTopics().map { topicList ->
            val topic = topicList.find { it.id == topicId }
            val relevantComments = comments.filter { it.topicId == topicId }
            if (topic != null) {
                LocalTopicWithComments(topic, relevantComments)
            } else {
                // Возвращаем пустой объект, если топик не найден
                LocalTopicWithComments(
                    LocalTopic("", "", 0.0, 0.0, "", "", 0L),
                    emptyList()
                )
            }
        }
    }

    override fun getUser(userId: String): Flow<LocalUser> {
        return MutableStateFlow(users.find { it.id == userId }).map { it!! }
    }

    override suspend fun updateUser(user: LocalUser) {
        users.removeAll { it.id == user.id }
        users.add(user)
    }

    override suspend fun getUserByUsername(username: String): LocalUser? {
        return users.find { it.username == username }
    }

    override suspend fun authenticateUser(username: String, passwordHash: String): LocalUser? {
        return users.find { it.username == username && it.passwordHash == passwordHash }
    }

    override suspend fun insertUser(user: LocalUser) {
        users.removeAll { it.id == user.id || it.username == user.username }
        users.add(user)
    }
}