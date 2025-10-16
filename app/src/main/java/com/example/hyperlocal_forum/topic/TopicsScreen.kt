package com.example.hyperlocal_forum.topic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hyperlocal_forum.data.ForumDatabase
import com.example.hyperlocal_forum.data.Topic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicsScreen(
    modifier: Modifier = Modifier,
    navigateToTopic: (Long) -> Unit,
    navigateToCreateTopic: () -> Unit
) {
    val context = LocalContext.current
    val forumDao = ForumDatabase.getDatabase(context).forumDao()
    val viewModel: TopicsViewModel = viewModel(factory = TopicsViewModelFactory(forumDao))

    val topics by viewModel.topics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Topics") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = navigateToCreateTopic) {
                Icon(Icons.Default.Add, contentDescription = "Create Topic")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            TopicList(
                modifier = Modifier.padding(paddingValues),
                topics = topics,
                onTopicClick = navigateToTopic
            )
        }
    }
}

@Composable
fun TopicList(
    topics: List<Topic>,
    onTopicClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (topics.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No topics yet. Tap the '+' button to create one.")
        }
    } else {
        LazyColumn(modifier = modifier) {
            items(topics) { topic ->
                ListItem(
                    headlineContent = { Text(topic.title) },
                    modifier = Modifier.clickable { onTopicClick(topic.id) }
                )
                Divider()
            }
        }
    }
}
