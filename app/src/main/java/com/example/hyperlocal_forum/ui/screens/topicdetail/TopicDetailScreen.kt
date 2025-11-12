package com.example.hyperlocal_forum.ui.screens.topicdetail

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hyperlocal_forum.ui.components.comment.Comments

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topicId: String,
    viewModel: TopicDetailViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val topicDetailState by viewModel.topicDetailState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showCommentInput by viewModel.showCommentInput.collectAsState()
    val newCommentContent by viewModel.newCommentContent.collectAsState()

    LaunchedEffect(topicId) {
        viewModel.setTopicId(topicId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "by ${topicDetailState?.author?.username ?: "Unknown"}"
                    )
                },
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
            when {
                isLoading && topicDetailState == null -> { // Показываем индикатор только при первой загрузке
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                topicDetailState != null -> {
                    val data = topicDetailState!!
                    Column(modifier = Modifier.fillMaxSize()) {
                        TopicHeader(
                            title = data.topicWithComments.topic.title,
                            content = data.topicWithComments.topic.content
                        )
                        Comments(
                            modifier = Modifier.weight(1f),
                            comments = data.topicWithComments.comments,
                            showCommentInput = showCommentInput,
                            newCommentContent = newCommentContent,
                            onCommentContentChange = viewModel::onNewCommentContentChange,
                            onSaveComment = viewModel::saveComment,
                            onToggleCommentInput = viewModel::toggleCommentInput
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Topic not found")
                    }
                }
            }
        }
    }
}

// Функции TopicHeader и TopicHeaderPreview остаются без изменений
@Composable
private fun TopicHeader(title: String, content: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = content,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun TopicHeaderPreview() {
    TopicHeader(
        title = "This is a sample topic title",
        content = "This is a sample topic content."
    )
}