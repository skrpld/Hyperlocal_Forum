package com.example.hyperlocal_forum

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hyperlocal_forum.ui.auth.AuthScreen
import com.example.hyperlocal_forum.ui.auth.AuthViewModel
import com.example.hyperlocal_forum.ui.profile.ProfileScreen
import com.example.hyperlocal_forum.ui.profile.ProfileViewModel
import com.example.hyperlocal_forum.ui.topic.detail.TopicDetailScreen
import com.example.hyperlocal_forum.ui.topic.detail.TopicDetailViewModel
import com.example.hyperlocal_forum.ui.topic.edit.TopicEditScreen
import com.example.hyperlocal_forum.ui.topic.edit.TopicEditViewModel
import com.example.hyperlocal_forum.ui.topic.TopicsScreen
import com.example.hyperlocal_forum.ui.topics.TopicsViewModel
import com.example.hyperlocal_forum.utils.AuthManager

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
            val authViewModel: AuthViewModel = hiltViewModel()
            AuthScreen(
                authManager = authManager,
                viewModel = authViewModel,
                onLoginSuccess = { navActions.navigateToTopics() }
            )
        }
        composable(
            route = ForumDestinations.TOPICS_ROUTE
        ) {
            val topicsViewModel: TopicsViewModel = hiltViewModel()
            TopicsScreen(
                viewModel = topicsViewModel,
                navigateToTopic = { topicId -> navActions.navigateToTopic(topicId) },
                navigateToCreateTopic = { navActions.navigateToCreateTopic() },
                navigateToProfile = { navActions.navigateToProfile() }
            )
        }

        composable(
            route = ForumDestinations.TOPIC_DETAIL_ROUTE,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId")
            if (topicId != null) {
                val topicDetailViewModel: TopicDetailViewModel = hiltViewModel()

                LaunchedEffect(key1 = topicId) {
                    topicDetailViewModel.setTopicId(topicId)
                }

                TopicDetailScreen(
                    topicId = topicId,
                    onBack = { navController.navigateUp() }
                )
            }
        }

        composable(route = ForumDestinations.CREATE_TOPIC_ROUTE) {
            val topicEditViewModel: TopicEditViewModel = hiltViewModel()
            TopicEditScreen(
                viewModel = topicEditViewModel,
                onTopicSaved = { navController.navigateUp() },
                onBack = { navController.navigateUp() }
            )
        }

        composable(route = ForumDestinations.PROFILE_ROUTE) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = profileViewModel,
                onBack = { navController.navigateUp() }
            )
        }
    }
}