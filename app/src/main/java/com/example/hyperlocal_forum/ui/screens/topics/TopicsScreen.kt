package com.example.hyperlocal_forum.ui.screens.topics

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.hyperlocal_forum.data.GeoCoordinates
import com.example.hyperlocal_forum.data.models.firestore.Topic
import com.example.hyperlocal_forum.data.models.firestore.User
import com.example.hyperlocal_forum.ui.topics.TopicsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun TopicsScreen(
    viewModel: TopicsViewModel,
    modifier: Modifier = Modifier,
    navigateToTopic: (String) -> Unit,
    navigateToCreateTopic: () -> Unit,
    navigateToProfile: () -> Unit
) {
    val topics by viewModel.topics.collectAsState()
    val usersMap by viewModel.usersMap.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showNearbyOnly by viewModel.showNearbyOnly.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val searchRadius by viewModel.searchRadius.collectAsState()
    val context = LocalContext.current

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var permissionRequestLaunched by rememberSaveable { mutableStateOf(false) }

    val onFilterChange: (Boolean) -> Unit = { showNearby ->
        if (showNearby) {
            val isPermanentlyDenied = !locationPermissionsState.allPermissionsGranted && !locationPermissionsState.shouldShowRationale
            when {
                locationPermissionsState.allPermissionsGranted -> {
                    viewModel.switchToNearbyFilter()
                }
                locationPermissionsState.shouldShowRationale -> {
                    showRationaleDialog = true
                }
                permissionRequestLaunched && isPermanentlyDenied -> {
                    showSettingsDialog = true
                }
                else -> {
                    permissionRequestLaunched = true
                    locationPermissionsState.launchMultiplePermissionRequest()
                }
            }
        } else {
            viewModel.loadAllTopics()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshTopics()
    }

    LaunchedEffect(locationPermissionsState.allPermissionsGranted, permissionRequestLaunched) {
        if (locationPermissionsState.allPermissionsGranted && permissionRequestLaunched) {
            viewModel.switchToNearbyFilter()
            permissionRequestLaunched = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
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
                                enabled = true
                            ) {
                                Text("Nearby")
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = navigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
                actions = {
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

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (showNearbyOnly) {
                TopicFilterSection(
                    userLocation = userLocation,
                    selectedRadius = searchRadius,
                    onRadiusSelected = { radius -> viewModel.setSearchRadius(radius) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (showRationaleDialog) {
                RationaleDialog(
                    onDismiss = { showRationaleDialog = false },
                    onConfirm = {
                        showRationaleDialog = false
                        permissionRequestLaunched = true
                        locationPermissionsState.launchMultiplePermissionRequest()
                    }
                )
            }

            if (showSettingsDialog) {
                PermanentlyDeniedDialog(
                    onDismiss = { showSettingsDialog = false },
                    onGoToSettings = {
                        showSettingsDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", context.packageName, null)
                        intent.data = uri
                        context.startActivity(intent)
                    }
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                TopicList(
                    modifier = Modifier.weight(1f),
                    topics = topics,
                    users = usersMap,
                    onTopicClick = navigateToTopic
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicFilterSection(
    userLocation: GeoCoordinates?,
    selectedRadius: Double,
    onRadiusSelected: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val radiusOptions = listOf(0.5, 2.0, 5.0, 10.0, 20.0)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        SingleChoiceSegmentedButtonRow {
            radiusOptions.forEachIndexed { index, radius ->
                SegmentedButton(
                    selected = selectedRadius == radius,
                    onClick = { onRadiusSelected(radius) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = radiusOptions.size)
                ) {
                    Text("${radius}km")
                }
            }
        }

        if (userLocation == null) {
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
private fun RationaleDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Location Permission Required") },
        text = { Text("To show topics near you, this app needs access to your device's location.") },
        confirmButton = { Button(onClick = onConfirm) { Text("Grant") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun PermanentlyDeniedDialog(onDismiss: () -> Unit, onGoToSettings: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Denied") },
        text = { Text("Location permission was permanently denied. You can grant it in the app settings.") },
        confirmButton = { Button(onClick = onGoToSettings) { Text("Go to Settings") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}


@Composable
fun TopicList(
    topics: List<Topic>,
    users: Map<String, User>,
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
                val authorName = users[topic.userId]?.username ?: "Unknown User"
                TopicItem(
                    topic = topic,
                    authorName = authorName,
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
    authorName: String,
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
                    text = "By $authorName",
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

private fun formatDate(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return formatter.format(date)
}