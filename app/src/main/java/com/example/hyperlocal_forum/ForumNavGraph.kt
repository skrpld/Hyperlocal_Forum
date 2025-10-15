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
import com.example.hyperlocal_forum.topic.edit.TopicEditScreen
import com.example.hyperlocal_forum.topic.TopicsScreen
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
    }
) {
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

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
                navigateToCreateTopic = { navActions.navigateToCreateTopic() }
            )
        }

        composable(route = ForumDestinations.TOPIC_DETAIL_ROUTE) {
            TODO("Реализовать экран с деталями топика")
        }
        
        composable(route = ForumDestinations.CREATE_TOPIC_ROUTE) {
            TopicEditScreen(onTopicSaved = { navController.navigateUp() })
        }
    }
}