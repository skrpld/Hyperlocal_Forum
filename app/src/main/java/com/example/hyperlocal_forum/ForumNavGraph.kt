package com.example.hyperlocal_forum

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.hyperlocal_forum.ui.auth.AuthManager
import com.example.hyperlocal_forum.ui.profile.ProfileScreen
import com.example.hyperlocal_forum.ui.topic.detail.TopicDetailScreen
import com.example.hyperlocal_forum.ui.topic.edit.TopicEditScreen
import com.example.hyperlocal_forum.ui.topic.TopicsScreen
import com.example.hyperlocal_forum.ui.components.AppDrawer
import kotlinx.coroutines.CoroutineScope

@Composable
fun ForumNavGraph(
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    navController: NavHostController = rememberNavController(),
    startDestination: String = ForumDestinations.TOPICS_ROUTE,
    navActions: ForumNavigationActions = remember(navController) {
        ForumNavigationActions(navController)
    },
    authManager: AuthManager
) {
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

    AppDrawer(
        drawerState = drawerState,
        onLogout = { authManager.logout() }
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier,
        ) {
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
}