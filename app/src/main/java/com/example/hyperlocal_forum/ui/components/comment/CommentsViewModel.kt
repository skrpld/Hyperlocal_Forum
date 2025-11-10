package com.example.hyperlocal_forum.ui.components.comment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.models.firestore.Comment
import com.example.hyperlocal_forum.data.ForumRepository
import com.example.hyperlocal_forum.data.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val forumRepository: ForumRepository,
    private val authManager: AuthManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var currentTopicId: String = ""
        get() = savedStateHandle["topicId"] ?: field

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _showCommentInput = MutableStateFlow(false)
    val showCommentInput: StateFlow<Boolean> = _showCommentInput.asStateFlow()

    private val _newCommentContent = MutableStateFlow("")
    val newCommentContent: StateFlow<String> = _newCommentContent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun setTopicId(topicId: String) {
        if (currentTopicId != topicId) {
            savedStateHandle["topicId"] = topicId
            currentTopicId = topicId
            loadComments()
        }
    }

    private fun loadComments() {
        if (currentTopicId.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                forumRepository.getCommentsForTopic(currentTopicId).collect { commentsList ->
                    _comments.value = commentsList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
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
        if (currentTopicId.isEmpty()) return

        viewModelScope.launch {
            if (_newCommentContent.value.isNotBlank()) {
                val userId = authManager.currentUserId.first()
                if (userId != "-1") {
                    try {
                        val user = forumRepository.getUser(userId.toString())
                        if (user != null) {
                            val newComment = Comment(
                                userId = userId.toString(),
                                topicId = currentTopicId,
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
}