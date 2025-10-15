package com.example.hyperlocal_forum.topic

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TopicDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: TopicDetailViewModel = viewModel(),
    topicId: Long
) {
    LaunchedEffect(topicId) {
        viewModel.loadTopicDetails(topicId)
    }

    val topicWithComments by viewModel.topicWithComments.collectAsState()

    Scaffold(modifier = modifier.fillMaxSize()) {
        topicWithComments?.let { data ->
            LazyColumn {
                item {
                    TopicHeader(title = data.topic.title, content = data.topic.content)
                }
                items(data.comments) { comment ->
                    ListItem(
                        headlineContent = { Text(comment.content) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicHeader(title: String, content: String) {
    Card(
        modifier = Modifier.padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            Text(text = content, style = MaterialTheme.typography.bodyLarge)
        }
    }
}