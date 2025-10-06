package com.example.hyperlocal_forum

import androidx.navigation.NavHostController
import com.example.hyperlocal_forum.ForumDestinations.TOPICS_ROUTE
import com.example.hyperlocal_forum.ForumDestinations.TOPIC_DETAIL_ROUTE

object ForumDestinations {
    const val TOPICS_ROUTE = "home"
    const val TOPIC_DETAIL_ROUTE = "topic"
}

class ForumNavigationActions(private val navController: NavHostController) {
    fun navigateToTopic(topicId: Long) {
        navController.navigate(TOPIC_DETAIL_ROUTE)
        TODO("Реализовать переход к топику $topicId из $TOPICS_ROUTE")
    }
}