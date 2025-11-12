package com.example.hyperlocal_forum.ui.screens.topicdetail

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.hyperlocal_forum.ui.components.comment.Comments
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topicId: String,
    viewModel: TopicDetailViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onTopicSaved: (String) -> Unit
) {
    val topicDetailState by viewModel.topicDetailState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showCommentInput by viewModel.showCommentInput.collectAsState()
    val newCommentContent by viewModel.newCommentContent.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

        if (isGranted) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    loc?.let {
                        viewModel.setLocation(it.latitude, it.longitude)
                    }
                }
            }
        }
    }

    // Инициализация ViewModel при первом запуске
    LaunchedEffect(topicId) {
        viewModel.setTopicId(topicId, topicId == "new")

        // Запрос местоположения только при создании нового топика
        if (topicId == "new") {
            val fineLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            val coarseLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

            if (fineLocationPermission == PackageManager.PERMISSION_GRANTED || coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    loc?.let {
                        viewModel.setLocation(it.latitude, it.longitude)
                    }
                }
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            isEditMode && topicId == "new" -> "Создание топика"
                            isEditMode -> "Редактирование топика"
                            else -> "by ${topicDetailState?.author?.username ?: "Unknown"}"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Показываем кнопку редактирования только в режиме просмотра и если топик загружен
                    if (!isEditMode && topicDetailState != null) {
                        IconButton(onClick = { viewModel.toggleEditMode() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать топик")
                        }
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
                // Индикатор загрузки только при первом входе
                isLoading && topicDetailState == null && !isEditMode -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                // Режим редактирования
                isEditMode -> {
                    EditView(
                        viewModel = viewModel,
                        isNewTopic = topicId == "new",
                        onTopicSaved = onTopicSaved
                    )
                }
                // Режим просмотра
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
                // Топик не найден
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Топик не найден")
                    }
                }
            }
        }
    }
}

@Composable
fun EditView(viewModel: TopicDetailViewModel, isNewTopic: Boolean, onTopicSaved: (String) -> Unit) {
    val title by viewModel.editableTitle.collectAsState()
    val content by viewModel.editableContent.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val location by viewModel.location.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Заголовок можно редактировать только при создании нового топика
        OutlinedTextField(
            value = title,
            onValueChange = viewModel::onTitleChange,
            label = { Text("Заголовок") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving && isNewTopic,
            singleLine = true
        )

        // Card для контента, который будет динамически изменять размер
        Card(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = content,
                onValueChange = viewModel::onContentChange,
                label = { Text("Содержимое") },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                enabled = !isSaving,
            )
        }


        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (isNewTopic && location == null && !isSaving) {
            Text(
                text = "Для создания топика требуется доступ к местоположению. Пожалуйста, предоставьте разрешение.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = { viewModel.saveTopic(onTopicSaved) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving && (if(isNewTopic) location != null else true)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Сохранить")
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