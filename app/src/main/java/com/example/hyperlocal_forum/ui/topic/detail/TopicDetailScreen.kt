package com.example.hyperlocal_forum.ui.topic.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hyperlocal_forum.data.ForumDatabase
import com.example.hyperlocal_forum.ui.auth.AuthManager
import com.example.hyperlocal_forum.ui.comment.Comments

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    modifier: Modifier = Modifier,
    topicId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val forumDao = ForumDatabase.getDatabase(context).forumDao()
    val authManager = AuthManager(context, forumDao)
    val viewModel: TopicDetailViewModel = viewModel(factory = TopicDetailViewModelFactory(forumDao, topicId))

    val topicDetailState by viewModel.topicDetailState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("by ${topicDetailState?.author?.username ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            topicDetailState?.let { data ->
                Column(modifier = Modifier.fillMaxSize()) {
                    TopicHeader(
                        title = data.topicWithComments.topic.title,
                        content = data.topicWithComments.topic.content
                    )
                    Comments(
                        topicId = data.topicWithComments.topic.id,
                        forumDao = forumDao,
                        comments = data.topicWithComments.comments,
                        authManager = authManager)
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun TopicHeader(title: String, content: String) {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(8.dp))
        Card(
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = content,
                modifier = Modifier.padding(8.dp))
        }
    }
}

@Preview
@Composable
fun TopicDetailScreenPreview() {
    TopicDetailScreen(topicId = 1, onBack = {})
}

@Preview
@Composable
fun TopicHeaderPreview() {
    TopicHeader(title = "This is a sample topic title", content = "This is a sample topic content.")
}
