package com.example.hyperlocal_forum

import androidx.navigation.NavHostController
import com.example.hyperlocal_forum.ForumDestinations.TOPICS_ROUTE

object ForumDestinations {
    const val TOPICS_ROUTE = "topics"
    const val TOPIC_DETAIL_ROUTE = "topic/{topicId}"
    const val CREATE_TOPIC_ROUTE = "create_topic"

    fun topicDetailRoute(topicId: Long) = "topic/$topicId"
}

class ForumNavigationActions(private val navController: NavHostController) {
    fun navigateToTopics() {
        navController.navigate(TOPICS_ROUTE) {
            popUpTo(TOPICS_ROUTE) { inclusive = true }
        }
    }

    fun navigateToTopic(topicId: Long) {
        navController.navigate(ForumDestinations.topicDetailRoute(topicId))
    }

    fun navigateToCreateTopic() {
        navController.navigate(ForumDestinations.CREATE_TOPIC_ROUTE)
    }
}