package com.example.hyperlocal_forum

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.hyperlocal_forum.ui.auth.AuthManager
import com.example.hyperlocal_forum.ui.auth.AuthScreen
import com.example.hyperlocal_forum.ui.profile.ProfileScreen
import com.example.hyperlocal_forum.ui.topic.detail.TopicDetailScreen
import com.example.hyperlocal_forum.ui.topic.edit.TopicEditScreen
import com.example.hyperlocal_forum.ui.topic.TopicsScreen
@Composable
fun ForumNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    isLoggedIn: Boolean,
    authManager: AuthManager,
    navActions: ForumNavigationActions = remember(navController) {
        ForumNavigationActions(navController)
    },
) {
    val startDestination = if (isLoggedIn) {
        ForumDestinations.TOPICS_ROUTE
    } else {
        ForumDestinations.LOGIN_ROUTE
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(ForumDestinations.LOGIN_ROUTE) {
            AuthScreen(
                authManager = authManager,
                onLoginSuccess = { navActions.navigateToTopics() }
            )
        }
        composable(
            route = ForumDestinations.TOPICS_ROUTE
        ) {
            TopicsScreen(
                navigateToTopic = { topicId -> navActions.navigateToTopic(topicId) },
                navigateToCreateTopic = { navActions.navigateToCreateTopic() },
                navigateToProfile = { navActions.navigateToProfile() }
            )
        }

        composable(
            route = ForumDestinations.TOPIC_DETAIL_ROUTE,
            arguments = listOf(navArgument("topicId") { type = NavType.LongType })
        ) {
            val topicId = it.arguments?.getLong("topicId")
            if (topicId != null) {
                TopicDetailScreen(
                    topicId = topicId,
                    onBack = { navController.navigateUp() }
                )
            }
        }
        
        composable(route = ForumDestinations.CREATE_TOPIC_ROUTE) {
            TopicEditScreen(
                onTopicSaved = { navController.navigateUp() },
                onBack = { navController.navigateUp() }
            )
        }

        composable(route = ForumDestinations.PROFILE_ROUTE) {
            ProfileScreen(authManager = authManager)
        }
    }
}