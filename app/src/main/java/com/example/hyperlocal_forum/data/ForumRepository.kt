package com.example.hyperlocal_forum.data

import android.util.Log
import com.example.hyperlocal_forum.data.models.firestore.Comment
import com.example.hyperlocal_forum.data.models.firestore.Topic
import com.example.hyperlocal_forum.data.models.firestore.TopicWithComments
import com.example.hyperlocal_forum.data.models.firestore.User
import com.example.hyperlocal_forum.data.models.local.LocalComment
import com.example.hyperlocal_forum.data.models.local.LocalTopic
import com.example.hyperlocal_forum.data.models.local.LocalUser
import com.example.hyperlocal_forum.di.GeoUtils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

private const val TAG = "ForumRepository"

class ForumRepository(
    private val forumDao: ForumDao
) {
    private val db = FirebaseFirestore.getInstance()

    private val topicsCollection = db.collection("topics")
    private val commentsCollection = db.collection("comments")
    private val usersCollection = db.collection("users")

    suspend fun checkServerAvailability(): Boolean {
        return try {
            db.collection("connectivity_check").document("one").get().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createTopic(topic: Topic): String {
        val fullGeohash = GeoUtils.getGeoHashForLocation(topic.location, 9)

        val geohashes = mutableMapOf<String, String>()
        for (precision in 9 downTo 2) {
            geohashes["p$precision"] = fullGeohash.substring(0, precision)
        }
        Log.d(TAG, "Creating topic with location: ${topic.location}, geohashes: $geohashes")

        val topicData = hashMapOf(
            "userId" to topic.userId,
            "location" to GeoPoint(topic.location.latitude, topic.location.longitude),
            "title" to topic.title,
            "content" to topic.content,
            "timestamp" to topic.timestamp,
            "geohashes" to geohashes
        )

        val document = topicsCollection.add(topicData).await()

        val localTopic = LocalTopic(
            id = document.id,
            userId = topic.userId,
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

            val topics = snapshot.documents.mapNotNull { doc ->
                val geoPoint = doc.getGeoPoint("location")
                val timestamp = doc.getTimestamp("timestamp")
                if (geoPoint != null && timestamp != null) {
                    Topic(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        location = GeoCoordinates(geoPoint.latitude, geoPoint.longitude),
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        timestamp = timestamp
                    )
                } else {
                    null
                }
            }
            emit(topics)
        } catch (e: Exception) {
            val localTopics = forumDao.getAllTopics().first().map { localTopic ->
                Topic(
                    id = localTopic.id,
                    userId = localTopic.userId,
                    location = GeoCoordinates(localTopic.latitude, localTopic.longitude),
                    title = localTopic.title,
                    content = localTopic.content,
                    timestamp = Timestamp(localTopic.timestamp / 1000, 0)
                )
            }
            emit(localTopics)
        }
    }

    fun findNearbyTopics(userLocation: GeoCoordinates, radiusInKm: Double = 1.0): Flow<List<Topic>> = flow {
        try {
            val precision = GeoUtils.getPrecisionForRadius(radiusInKm)
            val nearbyHashes = GeoUtils.getNearbyGeoHashes(userLocation, radiusInKm)

            // Это поле теперь будет корректно найдено в Firestore,
            // т.к. мы сохранили все нужные уровни точности в createTopic
            val fieldToQuery = "geohashes.p$precision"

            Log.d(TAG, "Searching with nearby hashes: $nearbyHashes on field '$fieldToQuery' for location: $userLocation")

            val snapshot = topicsCollection
                .whereIn(fieldToQuery, nearbyHashes)
                .get()
                .await()

            Log.d(TAG, "Topics from geohash query returned: ${snapshot.documents.size} documents.")

            val topicsFromGeohashQuery = snapshot.documents.mapNotNull { doc ->
                val geoPoint = doc.getGeoPoint("location")
                val timestamp = doc.getTimestamp("timestamp")
                if (geoPoint != null && timestamp != null) {
                    Topic(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        location = GeoCoordinates(geoPoint.latitude, geoPoint.longitude),
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        timestamp = timestamp
                    )
                } else {
                    null
                }
            }

            val nearbyTopics = topicsFromGeohashQuery.filter { topic ->
                val distance = GeoUtils.distanceBetween(userLocation, topic.location)
                Log.d(TAG, "Topic '${topic.title}' is $distance km away. Included: ${distance <= radiusInKm}")
                distance <= radiusInKm
            }.sortedByDescending { it.timestamp }

            Log.d(TAG, "Final nearby topics count after distance filtering: ${nearbyTopics.size}")
            emit(nearbyTopics)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch from Firebase, falling back to cache.", e)
            val topicsFromCache = forumDao.getAllTopics()
                .first()
                .map { localTopic ->
                    Topic(
                        id = localTopic.id,
                        userId = localTopic.userId,
                        location = GeoCoordinates(localTopic.latitude, localTopic.longitude),
                        title = localTopic.title,
                        content = localTopic.content,
                        timestamp = Timestamp(localTopic.timestamp / 1000, 0)
                    )
                }

            val nearbyTopics = topicsFromCache
                .filter { topic ->
                    GeoUtils.distanceBetween(userLocation, topic.location) <= radiusInKm
                }
                .sortedByDescending { it.timestamp }

            Log.d(TAG, "Found ${nearbyTopics.size} topics from cache.")
            emit(nearbyTopics)
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
            id = document.id,
            userId = comment.userId,
            topicId = comment.topicId,
            content = comment.content,
            username = comment.username,
            timestamp = comment.timestamp.seconds * 1000
        )
        forumDao.insertComment(localComment)

        return document.id
    }

    fun getCommentsForTopic(topicId: String): Flow<List<Comment>> = callbackFlow {
        val listener = try {
            commentsCollection
                .whereEqualTo("topicId", topicId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val comments = snapshot.documents.mapNotNull { doc ->
                            doc.getTimestamp("timestamp")?.let { timestamp ->
                                Comment(
                                    id = doc.id,
                                    userId = doc.getString("userId") ?: "",
                                    topicId = doc.getString("topicId") ?: "",
                                    content = doc.getString("content") ?: "",
                                    username = doc.getString("username") ?: "",
                                    timestamp = timestamp
                                )
                            }
                        }
                        trySend(comments).isSuccess
                    }
                }
        } catch (e: Exception) {
            close(e)
            null
        }
        awaitClose { listener?.remove() }
    }.catch { e ->
        Log.e(TAG, "Error listening for comments, falling back to cache", e)
        val localComments = forumDao.getTopicWithComments(topicId)
            .firstOrNull()?.comments?.map { localComment ->
                Comment(
                    id = localComment.id,
                    userId = localComment.userId,
                    topicId = localComment.topicId,
                    content = localComment.content,
                    username = localComment.username,
                    timestamp = Timestamp(localComment.timestamp / 1000, 0)
                )
            } ?: emptyList()
        emit(localComments)
    }

    fun getTopicWithComments(topicId: String): Flow<TopicWithComments> = flow {
        try {
            val topicDoc = topicsCollection.document(topicId).get().await()

            if (topicDoc.exists()) {
                val geoPoint = topicDoc.getGeoPoint("location")
                val timestamp = topicDoc.getTimestamp("timestamp")
                val userId = topicDoc.getString("userId")
                val title = topicDoc.getString("title")
                val content = topicDoc.getString("content")

                if (geoPoint != null && timestamp != null && userId != null && title != null && content != null) {
                    val topic = Topic(
                        id = topicDoc.id,
                        userId = userId,
                        location = GeoCoordinates(geoPoint.latitude, geoPoint.longitude),
                        title = title,
                        content = content,
                        timestamp = timestamp
                    )

                    getCommentsForTopic(topicId).collect { comments ->
                        emit(TopicWithComments(topic, comments))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val localTopicWithComments = forumDao.getTopicWithComments(topicId).firstOrNull()
            if (localTopicWithComments != null) {
                val topic = Topic(
                    id = localTopicWithComments.topic.id,
                    userId = localTopicWithComments.topic.userId,
                    location = GeoCoordinates(
                        localTopicWithComments.topic.latitude,
                        localTopicWithComments.topic.longitude
                    ),
                    title = localTopicWithComments.topic.title,
                    content = localTopicWithComments.topic.content,
                    timestamp = Timestamp(localTopicWithComments.topic.timestamp / 1000, 0)
                )
                val comments = localTopicWithComments.comments.map { localComment ->
                    Comment(
                        id = localComment.id,
                        userId = localComment.userId,
                        topicId = localComment.topicId,
                        content = localComment.content,
                        username = localComment.username,
                        timestamp = Timestamp(localComment.timestamp / 1000, 0)
                    )
                }
                emit(TopicWithComments(topic, comments))
            }
        }
    }

    suspend fun createUser(user: User): String {
        val userData = hashMapOf(
            "username" to user.username,
            "email" to user.email,
            "timestamp" to user.timestamp
        )

        val userId = if (user.id.isNotBlank()) {
            usersCollection.document(user.id).set(userData).await()
            user.id
        } else {
            usersCollection.add(userData).await().id
        }

        val localUser = LocalUser(
            id = userId,
            username = user.username,
            passwordHash = "",
            email = user.email,
            timestamp = user.timestamp.seconds * 1000
        )
        forumDao.insertUser(localUser)

        return userId
    }

    suspend fun getUser(userId: String): User? {
        try {
            val snapshot = usersCollection.document(userId).get().await()
            return if (snapshot.exists()) {
                val timestamp = snapshot.getTimestamp("timestamp")
                if (timestamp != null) {
                    User(
                        id = snapshot.id,
                        username = snapshot.getString("username") ?: "Unknown",
                        email = snapshot.getString("email"),
                        timestamp = timestamp
                    )
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            return forumDao.getUser(userId).firstOrNull()?.let { localUser ->
                User(
                    id = localUser.id,
                    username = localUser.username,
                    email = localUser.email,
                    timestamp = Timestamp(localUser.timestamp / 1000, 0)
                )
            }
        }
    }

    suspend fun getUsers(userIds: List<String>): Map<String, User> {
        if (userIds.isEmpty()) {
            return emptyMap()
        }

        val usersMap = mutableMapOf<String, User>()

        try {
            userIds.chunked(30).forEach { chunk ->
                val snapshot = usersCollection.whereIn(FieldPath.documentId(), chunk).get().await()
                snapshot.documents.forEach { doc ->
                    val timestamp = doc.getTimestamp("timestamp")
                    if (timestamp != null) {
                        val user = User(
                            id = doc.id,
                            username = doc.getString("username") ?: "Unknown",
                            email = doc.getString("email"),
                            timestamp = timestamp
                        )
                        usersMap[doc.id] = user
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return usersMap
    }

    suspend fun getUserByUsername(username: String): User? {
        try {
            val snapshot = usersCollection
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            return snapshot.documents.firstOrNull()?.let { doc ->
                val timestamp = doc.getTimestamp("timestamp")
                if (timestamp != null) {
                    User(
                        id = doc.id,
                        username = doc.getString("username") ?: "",
                        email = doc.getString("email"),
                        timestamp = timestamp
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            return forumDao.getUserByUsername(username)?.let { localUser ->
                User(
                    id = localUser.id,
                    username = localUser.username,
                    email = localUser.email,
                    timestamp = Timestamp(localUser.timestamp / 1000, 0)
                )
            }
        }
    }

    suspend fun updateUser(user: User): Boolean {
        return try {
            val userData = hashMapOf(
                "username" to user.username,
                "email" to user.email,
                "timestamp" to user.timestamp
            )

            usersCollection.document(user.id).update(userData as Map<String, Any>).await()

            val localUser = LocalUser(
                id = user.id,
                username = user.username,
                passwordHash = "",
                email = user.email,
                timestamp = user.timestamp.seconds * 1000
            )
            forumDao.updateUser(localUser)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updatePassword(userId: String, newPassword: String):Boolean {
        return try {
            FirebaseAuth.getInstance().currentUser?.updatePassword(newPassword)?.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}