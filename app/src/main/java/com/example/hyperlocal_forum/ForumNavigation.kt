package com.example.hyperlocal_forum

import androidx.navigation.NavHostController

object ForumDestinations {
    const val LOGIN_ROUTE = "login"
    const val TOPICS_ROUTE = "topics"
    const val TOPIC_DETAIL_ROUTE = "topic/{topicId}"
    const val CREATE_TOPIC_ROUTE = "create_topic"
    const val PROFILE_ROUTE = "profile"

    fun topicDetailRoute(topicId: String) = "topic/$topicId"
}

class ForumNavigationActions(private val navController: NavHostController) {
    fun navigateToTopic(topicId: String) {
        navController.navigate(ForumDestinations.topicDetailRoute(topicId))
    }

    fun navigateToCreateTopic() {
        navController.navigate(ForumDestinations.CREATE_TOPIC_ROUTE)
    }

    fun navigateToProfile() {
        navController.navigate(ForumDestinations.PROFILE_ROUTE)
    }

    fun navigateToLogin() {
        navController.navigate(ForumDestinations.LOGIN_ROUTE) {
            popUpTo(ForumDestinations.TOPICS_ROUTE) { inclusive = true }
        }
    }

    fun navigateToTopics() {
        navController.navigate(ForumDestinations.TOPICS_ROUTE) {
            popUpTo(ForumDestinations.LOGIN_ROUTE) { inclusive = true }
        }
    }
}