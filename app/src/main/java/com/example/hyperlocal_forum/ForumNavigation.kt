package com.example.hyperlocal_forum

import androidx.navigation.NavHostController
import com.example.hyperlocal_forum.ForumDestinations.HOME_ROUTE

object ForumDestinations {
    const val HOME_ROUTE = "home"
}

class ForumNavigationActions(navController: NavHostController) {
    fun navigateToTopic(topicId: String) {
        TODO("Реализовать переход к топику $topicId из $HOME_ROUTE")
    }
}