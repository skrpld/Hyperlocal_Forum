package com.example.hyperlocal_forum.ui.topic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.hyperlocal_forum.data.GeoCoordinates
import com.example.hyperlocal_forum.data.firebase.Topic
import com.example.hyperlocal_forum.ui.topics.TopicsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicsScreen(
    viewModel: TopicsViewModel,
    modifier: Modifier = Modifier,
    navigateToTopic: (String) -> Unit,
    navigateToCreateTopic: () -> Unit,
    navigateToProfile: () -> Unit
) {

    val topics by viewModel.topics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showNearbyOnly by viewModel.showNearbyOnly.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Topics") },
                actions = {
                    IconButton(onClick = navigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = { viewModel.refreshTopics() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Topics")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = navigateToCreateTopic) {
                Icon(Icons.Default.Add, contentDescription = "Create Topic")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TopicFilterSection(
                showNearbyOnly = showNearbyOnly,
                onFilterChange = { showNearby ->
                    if (showNearby && userLocation != null) {
                        viewModel.loadNearbyTopics(userLocation!!)
                    } else {
                        viewModel.loadAllTopics()
                    }
                },
                userLocation = userLocation,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                TopicList(
                    modifier = Modifier.weight(1f),
                    topics = topics,
                    onTopicClick = navigateToTopic
                )
            }
        }
    }
}

@Composable
fun TopicFilterSection(
    showNearbyOnly: Boolean,
    onFilterChange: (Boolean) -> Unit,
    userLocation: GeoCoordinates?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = !showNearbyOnly,
                onClick = { onFilterChange(false) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                icon = {
                    Icon(
                        Icons.Default.Public,
                        contentDescription = "All topics",
                        modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                    )
                }
            ) {
                Text("All")
            }
            SegmentedButton(
                selected = showNearbyOnly,
                onClick = { onFilterChange(true) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                icon = {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Nearby topics",
                        modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                    )
                },
                enabled = userLocation != null
            ) {
                Text("Nearby")
            }
        }

        if (showNearbyOnly && userLocation == null) {
            Text(
                text = "Location required for nearby topics",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun TopicList(
    topics: List<Topic>,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (topics.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Public,
                contentDescription = "No topics",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                "No topics yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                "Tap the '+' button to create one",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    } else {
        LazyColumn(modifier = modifier) {
            items(topics) { topic ->
                TopicItem(
                    topic = topic,
                    onTopicClick = { onTopicClick(topic.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun TopicItem(
    topic: Topic,
    onTopicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onTopicClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = topic.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!topic.content.isNullOrEmpty()) {
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = topic.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "By User #${topic.userId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                topic.timestamp?.let { createdAt ->
                    Text(
                        text = formatDate(createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: com.google.firebase.Timestamp): String {
    val date = timestamp.toDate()
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return formatter.format(date)
}
