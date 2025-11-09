package com.example.hyperlocal_forum.data

import com.example.hyperlocal_forum.data.models.firestore.Comment
import com.example.hyperlocal_forum.data.models.firestore.Topic
import com.example.hyperlocal_forum.data.models.firestore.TopicWithComments
import com.example.hyperlocal_forum.data.models.firestore.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeForumDao : ForumDao {
    private val users = mutableListOf<User>()
    private val topics = mutableListOf<Topic>()
    private val comments = mutableListOf<Comment>()

    fun initUsers(initialUsers: List<User>) {
        users.clear()
        users.addAll(initialUsers)
    }

    override suspend fun insertTopic(topic: Topic): Long {
        topics.add(topic)
        return topics.size.toLong()
    }

    override suspend fun insertComment(comment: Comment): Long {
        comments.add(comment)
        return comments.size.toLong()
    }

    override fun getAllTopics(): Flow<List<Topic>> = flowOf(topics)

    override fun getTopicWithComments(topicId: Long): Flow<TopicWithComments> {
        val topic = topics.find { it.id == topicId }
        return if (topic != null) {
            val topicComments = comments.filter { it.topicId == topicId }
            flowOf(TopicWithComments(topic, topicComments))
        } else {
            flowOf()
        }
    }

    override fun getUser(userId: Long): Flow<User> {
        return flowOf(users.find { it.id == userId }!!)
    }

    override suspend fun updateUser(user: User) {
        val index = users.indexOfFirst { it.id == user.id }
        if (index != -1) {
            users[index] = user
        }
    }

    override suspend fun getUserByUsername(username: String): User? {
        return users.find { it.username == username }
    }

    override suspend fun authenticateUser(username: String, passwordHash: String): User? {
        return users.find { it.username == username && it.passwordHash == passwordHash }
    }

    override suspend fun insertUser(user: User): Long {
        val newId = (users.maxOfOrNull { it.id } ?: 0L) + 1
        users.add(user.copy(id = newId))
        return newId
    }
}