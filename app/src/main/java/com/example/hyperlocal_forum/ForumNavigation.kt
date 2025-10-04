package com.example.hyperlocal_forum

import androidx.navigation.NavHostController
import com.example.hyperlocal_forum.ForumDestinations.HOME_ROUTE
import com.example.hyperlocal_forum.ForumDestinations.TOPIC_ROUTE

object ForumDestinations {
    const val HOME_ROUTE = "home"
    const val TOPIC_ROUTE = "topic"
}

class ForumNavigationActions(private val navController: NavHostController) {
    fun navigateToTopic(topicId: Long) {
        navController.navigate(TOPIC_ROUTE)
        TODO("Реализовать переход к топику $topicId из $HOME_ROUTE")
    }
}