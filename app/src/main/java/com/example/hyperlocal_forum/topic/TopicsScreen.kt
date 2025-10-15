package com.example.hyperlocal_forum.topic

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hyperlocal_forum.data.ForumDatabase
import com.example.hyperlocal_forum.data.Topic

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TopicsScreen(
    modifier: Modifier = Modifier,
    navigateToTopic: (Long) -> Unit
) {
    val context = LocalContext.current
    val forumDao = ForumDatabase.getDatabase(context).forumDao()
    val viewModel: TopicsViewModel = viewModel(factory = TopicsViewModelFactory(forumDao))

    val topics by viewModel.topics.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) {
        TopicList(topics = topics, onTopicClick = navigateToTopic)
    }
}

@Composable
fun TopicList(
    topics: List<Topic>,
    onTopicClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(topics) { topic ->
            ListItem(
                headlineContent = { Text(topic.title) },
                modifier = Modifier.clickable { onTopicClick(topic.id) }
            )
        }
    }
}