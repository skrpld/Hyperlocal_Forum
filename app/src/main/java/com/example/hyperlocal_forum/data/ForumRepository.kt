package com.example.hyperlocal_forum.data

import com.example.hyperlocal_forum.data.firebase.Comment
import com.example.hyperlocal_forum.data.GeoCoordinates
import com.example.hyperlocal_forum.data.firebase.Topic
import com.example.hyperlocal_forum.data.firebase.TopicWithComments
import com.example.hyperlocal_forum.data.firebase.User
import com.example.hyperlocal_forum.data.local.LocalComment
import com.example.hyperlocal_forum.data.local.LocalTopic
import com.example.hyperlocal_forum.data.local.LocalUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ForumRepository(
    private val forumDao: ForumDao
) {
    private val db = FirebaseFirestore.getInstance()

    private val topicsCollection = db.collection("topics")
    private val commentsCollection = db.collection("comments")
    private val usersCollection = db.collection("users")

    suspend fun createTopic(topic: Topic): String {
        val topicData = hashMapOf(
            "userId" to topic.userId,
            "location" to GeoPoint(topic.location.latitude, topic.location.longitude),
            "title" to topic.title,
            "content" to topic.content,
            "timestamp" to topic.timestamp
        )

        val document = topicsCollection.add(topicData).await()

        val localTopic = LocalTopic(
            userId = topic.userId.toLongOrNull() ?: 0L,
            latitude = topic.location.latitude,
            longitude = topic.location.longitude,
            title = topic.title,
            content = topic.content,
            timestamp = topic.timestamp.seconds * 1000
        )
        forumDao.insertTopic(localTopic)

        return document.id
    }

    fun getAllTopics(): Flow<List<Topic>> = flow {
        try {
            val snapshot = topicsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val topics = snapshot.documents.map { doc ->
                val geoPoint = doc.getGeoPoint("location")!!
                Topic(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    location = GeoCoordinates(geoPoint.latitude, geoPoint.longitude),
                    title = doc.getString("title") ?: "",
                    content = doc.getString("content") ?: "",
                    timestamp = doc.getTimestamp("timestamp")!!
                )
            }
            emit(topics)
        } catch (e: Exception) {
            val localTopics = forumDao.getAllTopics().first().map { localTopic ->
                Topic(
                    id = localTopic.id.toString(),
                    userId = localTopic.userId.toString(),
                    location = GeoCoordinates(localTopic.latitude, localTopic.longitude),
                    title = localTopic.title,
                    content = localTopic.content,
                    timestamp = com.google.firebase.Timestamp(localTopic.timestamp / 1000, 0)
                )
            }
            emit(localTopics)
        }
    }

    fun findNearbyTopics(userLocation: GeoCoordinates, radiusInKm: Double = 10.0): Flow<List<Topic>> = flow {
        try {
            val snapshot = topicsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val allTopics = snapshot.documents.map { doc ->
                val geoPoint = doc.getGeoPoint("location")!!
                Topic(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    location = GeoCoordinates(geoPoint.latitude, geoPoint.longitude),
                    title = doc.getString("title") ?: "",
                    content = doc.getString("content") ?: "",
                    timestamp = doc.getTimestamp("timestamp")!!
                )
            }

            val nearbyTopics = allTopics.filter { topic ->
                calculateDistance(userLocation, topic.location) <= radiusInKm
            }

            emit(nearbyTopics)
        } catch (e: Exception) {
            val localTopics = forumDao.getNearbyTopics(
                userLocation.latitude,
                userLocation.longitude
            ).first().map { localTopic ->
                Topic(
                    id = localTopic.id.toString(),
                    userId = localTopic.userId.toString(),
                    location = GeoCoordinates(localTopic.latitude, localTopic.longitude),
                    title = localTopic.title,
                    content = localTopic.content,
                    timestamp = com.google.firebase.Timestamp(localTopic.timestamp / 1000, 0)
                )
            }
            emit(localTopics)
        }
    }

    suspend fun addComment(comment: Comment): String {
        val commentData = hashMapOf(
            "userId" to comment.userId,
            "topicId" to comment.topicId,
            "content" to comment.content,
            "username" to comment.username,
            "timestamp" to comment.timestamp
        )

        val document = commentsCollection.add(commentData).await()

        val localComment = LocalComment(
            userId = comment.userId.toLongOrNull() ?: 0L,
            topicId = comment.topicId.toLongOrNull() ?: 0L,
            content = comment.content,
            username = comment.username,
            timestamp = comment.timestamp.seconds * 1000
        )
        forumDao.insertComment(localComment)

        return document.id
    }

    fun getCommentsForTopic(topicId: String): Flow<List<Comment>> = flow {
        try {
            val snapshot = commentsCollection
                .whereEqualTo("topicId", topicId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            val comments = snapshot.documents.map { doc ->
                Comment(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    topicId = doc.getString("topicId") ?: "",
                    content = doc.getString("content") ?: "",
                    username = doc.getString("username") ?: "",
                    timestamp = doc.getTimestamp("timestamp")!!
                )
            }
            emit(comments)
        } catch (e: Exception) {
            val localComments = forumDao.getTopicWithComments(topicId.toLongOrNull() ?: 0L)
                .first().comments.map { localComment ->
                    Comment(
                        id = localComment.id.toString(),
                        userId = localComment.userId.toString(),
                        topicId = localComment.topicId.toString(),
                        content = localComment.content,
                        username = localComment.username,
                        timestamp = com.google.firebase.Timestamp(localComment.timestamp / 1000, 0)
                    )
                }
            emit(localComments)
        }
    }

    fun getTopicWithComments(topicId: String): Flow<TopicWithComments> = flow {
        try {
            val topicDoc = topicsCollection.document(topicId).get().await()
            val comments = getCommentsForTopic(topicId).first()

            val geoPoint = topicDoc.getGeoPoint("location")!!
            val topic = Topic(
                id = topicDoc.id,
                userId = topicDoc.getString("userId") ?: "",
                location = GeoCoordinates(geoPoint.latitude, geoPoint.longitude),
                title = topicDoc.getString("title") ?: "",
                content = topicDoc.getString("content") ?: "",
                timestamp = topicDoc.getTimestamp("timestamp")!!
            )

            emit(TopicWithComments(topic, comments))
        } catch (e: Exception) {
            val localTopicWithComments = forumDao.getTopicWithComments(topicId.toLongOrNull() ?: 0L).first()
            val topic = Topic(
                id = localTopicWithComments.topic.id.toString(),
                userId = localTopicWithComments.topic.userId.toString(),
                location = GeoCoordinates(
                    localTopicWithComments.topic.latitude,
                    localTopicWithComments.topic.longitude
                ),
                title = localTopicWithComments.topic.title,
                content = localTopicWithComments.topic.content,
                timestamp = com.google.firebase.Timestamp(localTopicWithComments.topic.timestamp / 1000, 0)
            )
            val comments = localTopicWithComments.comments.map { localComment ->
                Comment(
                    id = localComment.id.toString(),
                    userId = localComment.userId.toString(),
                    topicId = localComment.topicId.toString(),
                    content = localComment.content,
                    username = localComment.username,
                    timestamp = com.google.firebase.Timestamp(localComment.timestamp / 1000, 0)
                )
            }
            emit(TopicWithComments(topic, comments))
        }
    }

    suspend fun createUser(user: User): String {
        val userData = hashMapOf(
            "username" to user.username,
            "email" to user.email,
            "timestamp" to user.timestamp
        )

        val document = usersCollection.add(userData).await()

        val localUser = LocalUser(
            username = user.username,
            passwordHash = "",
            email = user.email,
            timestamp = user.timestamp.seconds * 1000
        )
        forumDao.insertUser(localUser)

        return document.id
    }

    suspend fun getUser(userId: String): User? {
        try {
            val snapshot = usersCollection.document(userId).get().await()
            return if (snapshot.exists()) {
                User(
                    id = snapshot.id,
                    username = snapshot.getString("username") ?: "",
                    email = snapshot.getString("email"),
                    timestamp = snapshot.getTimestamp("timestamp")!!
                )
            } else {
                null
            }
        } catch (e: Exception) {
            return forumDao.getUser(userId.toLongOrNull() ?: 0L).first()?.let { localUser ->
                User(
                    id = localUser.id.toString(),
                    username = localUser.username,
                    email = localUser.email,
                    timestamp = com.google.firebase.Timestamp(localUser.timestamp / 1000, 0)
                )
            }
        }
    }

    suspend fun getUserByUsername(username: String): User? {
        try {
            val snapshot = usersCollection
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            return snapshot.documents.firstOrNull()?.let { doc ->
                User(
                    id = doc.id,
                    username = doc.getString("username") ?: "",
                    email = doc.getString("email"),
                    timestamp = doc.getTimestamp("timestamp")!!
                )
            }
        } catch (e: Exception) {
            return forumDao.getUserByUsername(username)?.let { localUser ->
                User(
                    id = localUser.id.toString(),
                    username = localUser.username,
                    email = localUser.email,
                    timestamp = com.google.firebase.Timestamp(localUser.timestamp / 1000, 0)
                )
            }
        }
    }

    suspend fun updatePassword(userId: String, newPassword: String): Boolean {
        return try {
            // Обновление в Firebase
            val userDoc = usersCollection.document(userId)
            userDoc.update("password", newPassword).await()

            // Обновление в локальной базе данных
            val localUserId = userId.toLongOrNull()
            if (localUserId != null) {
                // Здесь предполагается, что у вас есть метод для обновления пароля в DAO
                // Если нет, нужно добавить соответствующий метод в ForumDao
                forumDao.updateUserPassword(localUserId, newPassword)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateUser(user: User): Boolean {
        return try {
            // Обновление в Firebase
            val userData = hashMapOf(
                "username" to user.username,
                "email" to user.email,
                "timestamp" to user.timestamp
            )

            usersCollection.document(user.id).update(userData as Map<String, Any>).await()

            // Обновление в локальной базе данных
            val localUserId = user.id.toLongOrNull()
            if (localUserId != null) {
                val localUser = LocalUser(
                    id = localUserId,
                    username = user.username,
                    passwordHash = "", // Пароль не обновляем здесь
                    email = user.email,
                    timestamp = user.timestamp.seconds * 1000
                )
                forumDao.updateUser(localUser)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun calculateDistance(loc1: GeoCoordinates, loc2: GeoCoordinates): Double {
        val earthRadius = 6371.0 // km

        val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
        val dLon = Math.toRadians(loc2.longitude - loc1.longitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(loc1.latitude)) * cos(Math.toRadians(loc2.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}