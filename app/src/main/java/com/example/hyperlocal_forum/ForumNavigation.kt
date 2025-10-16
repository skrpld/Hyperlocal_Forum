package com.example.hyperlocal_forum

import androidx.navigation.NavHostController

object ForumDestinations {
    const val TOPICS_ROUTE = "topics"
    const val TOPIC_DETAIL_ROUTE = "topic/{topicId}"
    const val CREATE_TOPIC_ROUTE = "create_topic"
    const val PROFILE_ROUTE = "profile"

    fun topicDetailRoute(topicId: Long) = "topic/$topicId"
}

class ForumNavigationActions(private val navController: NavHostController) {
    fun navigateToTopic(topicId: Long) {
        navController.navigate(ForumDestinations.topicDetailRoute(topicId))
    }

    fun navigateToCreateTopic() {
        navController.navigate(ForumDestinations.CREATE_TOPIC_ROUTE)
    }

    fun navigateToProfile() {
        navController.navigate(ForumDestinations.PROFILE_ROUTE)
    }
}