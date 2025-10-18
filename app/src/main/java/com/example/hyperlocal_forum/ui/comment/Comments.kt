package com.example.hyperlocal_forum.ui.comment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hyperlocal_forum.data.Comment
import com.example.hyperlocal_forum.data.ForumDao

@Composable
fun Comments(
    modifier: Modifier = Modifier,
    topicId: Long,
    forumDao: ForumDao,
    comments: List<Comment>
) {
    val commentsViewModel: CommentsViewModel = viewModel(factory = CommentsViewModelFactory(forumDao, topicId))
    val showCommentInput by commentsViewModel.showCommentInput.collectAsState()
    val newCommentContent by commentsViewModel.newCommentContent.collectAsState()

    Column(modifier = modifier) {
        Text(
            text = "Comments",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(comments) { comment ->
                Card(
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = comment.content,
                        modifier = Modifier.padding(8.dp))
                }
            }
        }

        if (showCommentInput) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newCommentContent,
                    onValueChange = { commentsViewModel.onNewCommentContentChange(it) },
                    label = { Text("Your comment") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                IconButton(onClick = { commentsViewModel.saveComment() }) {
                    Icon(Icons.Default.Send, contentDescription = "Send Comment")
                }
            }
        } else {
            Button(onClick = { commentsViewModel.toggleCommentInput() }, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Add Comment")
                Text("Add Comment")
            }
        }
    }
}