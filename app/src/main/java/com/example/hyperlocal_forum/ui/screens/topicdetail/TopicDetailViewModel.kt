package com.example.hyperlocal_forum.ui.screens.topicdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.AuthManager
import com.example.hyperlocal_forum.data.ForumRepository
import com.example.hyperlocal_forum.data.GeoCoordinates
import com.example.hyperlocal_forum.data.models.firestore.Comment
import com.example.hyperlocal_forum.data.models.firestore.Topic
import com.example.hyperlocal_forum.data.models.firestore.TopicWithComments
import com.example.hyperlocal_forum.data.models.firestore.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TopicDetailState(
    val topicWithComments: TopicWithComments,
    val author: User
)

@HiltViewModel
class TopicDetailViewModel @Inject constructor(
    private val forumRepository: ForumRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private var currentTopicId: String? = null

    private val _topicDetailState = MutableStateFlow<TopicDetailState?>(null)
    val topicDetailState: StateFlow<TopicDetailState?> = _topicDetailState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showCommentInput = MutableStateFlow(false)
    val showCommentInput: StateFlow<Boolean> = _showCommentInput.asStateFlow()

    private val _newCommentContent = MutableStateFlow("")
    val newCommentContent: StateFlow<String> = _newCommentContent.asStateFlow()

    // Состояния для режима редактирования
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _editableTitle = MutableStateFlow("")
    val editableTitle: StateFlow<String> = _editableTitle.asStateFlow()

    private val _editableContent = MutableStateFlow("")
    val editableContent: StateFlow<String> = _editableContent.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _location = MutableStateFlow<GeoCoordinates?>(null)
    val location: StateFlow<GeoCoordinates?> = _location.asStateFlow()


    fun setTopicId(id: String, editMode: Boolean = false) {
        // Если это новый топик
        if (id == "new") {
            currentTopicId = null
            _isEditMode.value = true
            _isLoading.value = false
            _editableTitle.value = ""
            _editableContent.value = ""
            _topicDetailState.value = null
            return
        }

        // Если ID не изменился, ничего не делаем
        if (id == currentTopicId && _isEditMode.value == editMode) {
            return
        }
        currentTopicId = id
        _isEditMode.value = editMode
        loadTopicDetail(id)
    }

    fun onTitleChange(newTitle: String) {
        _editableTitle.value = newTitle
    }

    fun onContentChange(newContent: String) {
        _editableContent.value = newContent
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
        // Если перешли в режим редактирования, заполняем поля текущими данными
        if (_isEditMode.value) {
            _editableTitle.value = _topicDetailState.value?.topicWithComments?.topic?.title ?: ""
            _editableContent.value = _topicDetailState.value?.topicWithComments?.topic?.content ?: ""
        }
    }

    fun saveTopic(onTopicSaved: (String) -> Unit) {
        viewModelScope.launch {
            val userId = authManager.currentUserId.value
            if (userId.isNullOrBlank()) {
                _errorMessage.value = "Вы должны быть авторизованы, чтобы создавать или редактировать топики."
                return@launch
            }

            if (_editableTitle.value.isBlank() || _editableContent.value.isBlank()) {
                _errorMessage.value = "Заголовок и содержимое не могут быть пустыми"
                return@launch
            }

            _isSaving.value = true
            _errorMessage.value = null

            try {
                if (currentTopicId == null) { // Создание нового топика
                    if (_location.value == null) {
                        _errorMessage.value = "Для создания топика требуется доступ к местоположению."
                        _isSaving.value = false
                        return@launch
                    }
                    val topic = Topic(
                        userId = userId,
                        location = _location.value!!,
                        title = _editableTitle.value,
                        content = _editableContent.value
                    )
                    val newTopicId = forumRepository.createTopic(topic)
                    onTopicSaved(newTopicId)

                } else { // Обновление существующего топика
                    val originalTopic = _topicDetailState.value!!.topicWithComments.topic
                    val updatedTopic = originalTopic.copy(
                        // Заголовок не меняем, если топик не новый
                        content = _editableContent.value
                    )
                    forumRepository.updateTopicContent(updatedTopic.id, updatedTopic.content) // Предполагается наличие такого метода
                    onTopicSaved(updatedTopic.id)
                }
                _isSaving.value = false
                _isEditMode.value = false

            } catch (e: Exception) {
                _isSaving.value = false
                _errorMessage.value = "Не удалось сохранить топик: ${e.message}"
            }
        }
    }


    private fun loadTopicDetail(topicId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _topicDetailState.value = null

            try {
                forumRepository.getTopicWithComments(topicId).collect { topicWithComments ->
                    val author = forumRepository.getUser(topicWithComments.topic.userId)
                    val authorToShow = author ?: User(id = topicWithComments.topic.userId, username = "Unknown")

                    _topicDetailState.value = TopicDetailState(topicWithComments, authorToShow)
                    if (_isEditMode.value) {
                        _editableTitle.value = topicWithComments.topic.title
                        _editableContent.value = topicWithComments.topic.content
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _topicDetailState.value = null
                _isLoading.value = false
            }
        }
    }

    fun toggleCommentInput() {
        _showCommentInput.value = !_showCommentInput.value
        if (!_showCommentInput.value) {
            _newCommentContent.value = ""
        }
    }

    fun onNewCommentContentChange(content: String) {
        _newCommentContent.value = content
    }

    fun saveComment() {
        val topicId = currentTopicId ?: return

        viewModelScope.launch {
            if (_newCommentContent.value.isNotBlank()) {
                val userId = authManager.currentUserId.first()

                if (userId != null && userId != "-1") {
                    try {
                        val user = forumRepository.getUser(userId)
                        if (user != null) {
                            val newComment = Comment(
                                userId = userId,
                                topicId = topicId,
                                content = _newCommentContent.value,
                                username = user.username
                            )
                            forumRepository.addComment(newComment)
                            _newCommentContent.value = ""
                            _showCommentInput.value = false
                        }
                    } catch (e: Exception) {
                        // Обработка ошибки
                    }
                }
            }
        }
    }

    fun setLocation(lat: Double, lon: Double) {
        _location.value = GeoCoordinates(lat, lon)
    }
}