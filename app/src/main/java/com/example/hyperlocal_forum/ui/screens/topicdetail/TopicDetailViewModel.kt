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

    private val _isCurrentUserOwner = MutableStateFlow(false)
    val isCurrentUserOwner: StateFlow<Boolean> = _isCurrentUserOwner.asStateFlow()

    val currentUserId: StateFlow<String?> = authManager.currentUserId

    /**
     * Sets the topic ID to be displayed and loads its details.
     * If the ID is "new", it prepares the ViewModel for creating a new topic.
     * Prevents reloading if the same topic ID and edit mode are provided again.
     * @param id The ID of the topic to load, or "new" to create a new one.
     * @param editMode Whether to start in edit mode. Defaults to false.
     */
    fun setTopicId(id: String, editMode: Boolean = false) {
        if (id == "new") {
            currentTopicId = null
            _isEditMode.value = true
            _isLoading.value = false
            _editableTitle.value = ""
            _editableContent.value = ""
            _topicDetailState.value = null
            _isCurrentUserOwner.value = true
            return
        }

        if (id == currentTopicId && _isEditMode.value == editMode) {
            return
        }
        currentTopicId = id
        _isEditMode.value = editMode
        loadTopicDetail(id)
    }

    /**
     * Updates the editable title state.
     * @param newTitle The new title string.
     */
    fun onTitleChange(newTitle: String) {
        _editableTitle.value = newTitle
    }

    /**
     * Updates the editable content state.
     * @param newContent The new content string.
     */
    fun onContentChange(newContent: String) {
        _editableContent.value = newContent
    }

    /**
     * Toggles the edit mode for the topic if the current user is the owner.
     * When entering edit mode, it populates the editable fields with the current topic data.
     */
    fun toggleEditMode() {
        if (_isCurrentUserOwner.value) {
            _isEditMode.value = !_isEditMode.value
            if (_isEditMode.value) {
                _editableTitle.value = _topicDetailState.value?.topicWithComments?.topic?.title ?: ""
                _editableContent.value = _topicDetailState.value?.topicWithComments?.topic?.content ?: ""
            }
        }
    }

    /**
     * Saves a new or existing topic.
     * Validates user authentication and input fields.
     * Creates a new topic if `currentTopicId` is null, otherwise updates the existing one.
     * @param onTopicSaved A callback function that receives the saved topic's ID.
     */
    fun saveTopic(onTopicSaved: (String) -> Unit) {
        viewModelScope.launch {
            val userId = authManager.currentUserId.value
            if (userId.isNullOrBlank()) {
                _errorMessage.value = "You must be logged in to create or edit topics."
                return@launch
            }

            if (_editableTitle.value.isBlank() || _editableContent.value.isBlank()) {
                _errorMessage.value = "Title and content cannot be empty"
                return@launch
            }

            _isSaving.value = true
            _errorMessage.value = null

            try {
                if (currentTopicId == null) {
                    if (_location.value == null) {
                        _errorMessage.value = "Location access is required to create a topic."
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

                } else {
                    if (_isCurrentUserOwner.value) {
                        val originalTopic = _topicDetailState.value!!.topicWithComments.topic
                        val updatedTopic = originalTopic.copy(
                            content = _editableContent.value
                        )
                        forumRepository.updateTopicContent(updatedTopic.id, updatedTopic.content)
                        onTopicSaved(updatedTopic.id)
                    } else {
                        _errorMessage.value = "You do not have permission to edit this topic."
                    }
                }
                _isSaving.value = false
                _isEditMode.value = false

            } catch (e: Exception) {
                _isSaving.value = false
                _errorMessage.value = "Failed to save topic: ${e.message}"
            }
        }
    }

    /**
     * Deletes the current topic if the user is the owner.
     * @param onTopicDeleted A callback function to be executed after deletion.
     */
    fun deleteTopic(onTopicDeleted: () -> Unit) {
        viewModelScope.launch {
            if (currentTopicId != null && _isCurrentUserOwner.value) {
                try {
                    forumRepository.deleteTopic(currentTopicId!!)
                    onTopicDeleted()
                } catch (e: Exception) {
                    _errorMessage.value = "Error deleting topic: ${e.message}"
                }
            }
        }
    }

    /**
     * Loads the details for a given topic ID, including its content, author, and comments.
     * Updates the UI state with the fetched data.
     * @param topicId The ID of the topic to load.
     */
    private fun loadTopicDetail(topicId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _topicDetailState.value = null

            try {
                forumRepository.getTopicWithComments(topicId).collect { topicWithComments ->
                    val author = forumRepository.getUser(topicWithComments.topic.userId)
                    val authorToShow = author ?: User(id = topicWithComments.topic.userId, username = "Unknown")

                    _topicDetailState.value = TopicDetailState(topicWithComments, authorToShow)
                    _isCurrentUserOwner.value = authManager.currentUserId.value == topicWithComments.topic.userId
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

    /**
     * Toggles the visibility of the new comment input field.
     * Clears the input field when it's hidden.
     */
    fun toggleCommentInput() {
        _showCommentInput.value = !_showCommentInput.value
        if (!_showCommentInput.value) {
            _newCommentContent.value = ""
        }
    }

    /**
     * Updates the state for the new comment's content.
     * @param content The new content string for the comment.
     */
    fun onNewCommentContentChange(content: String) {
        _newCommentContent.value = content
    }

    /**
     * Saves a new comment to the current topic.
     * It requires the user to be logged in and the comment content to be non-blank.
     */
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
                        // Handle error
                    }
                }
            }
        }
    }

    /**
     * Deletes a comment if the current user is the author of the comment.
     * @param commentId The ID of the comment to delete.
     */
    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            try {
                val commentToDelete = _topicDetailState.value?.topicWithComments?.comments?.find { it.id == commentId }
                if (commentToDelete?.userId == authManager.currentUserId.value) {
                    forumRepository.deleteComment(commentId)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting comment: ${e.message}"
            }
        }
    }

    /**
     * Sets the geographical location coordinates.
     * @param lat The latitude.
     * @param lon The longitude.
     */
    fun setLocation(lat: Double, lon: Double) {
        _location.value = GeoCoordinates(lat, lon)
    }
}