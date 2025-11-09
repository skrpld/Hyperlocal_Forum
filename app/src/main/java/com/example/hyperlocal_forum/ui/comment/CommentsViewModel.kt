package com.example.hyperlocal_forum.ui.comment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.firebase.Comment
import com.example.hyperlocal_forum.data.ForumRepository
import com.example.hyperlocal_forum.utils.AuthManager
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
    private val topicId: String,
    private val authManager: AuthManager
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _showCommentInput = MutableStateFlow(false)
    val showCommentInput: StateFlow<Boolean> = _showCommentInput.asStateFlow()

    private val _newCommentContent = MutableStateFlow("")
    val newCommentContent: StateFlow<String> = _newCommentContent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadComments()
    }

    fun setTopicId(id: String) {
        if (topicId != id) {
            loadComments()
        }
    }

    private fun loadComments() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                forumRepository.getCommentsForTopic(topicId).collect { commentsList ->
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
        viewModelScope.launch {
            if (_newCommentContent.value.isNotBlank()) {
                val userId = authManager.currentUserId.first()
                if (userId != "-1") {
                    try {
                        val user = forumRepository.getUser(userId.toString())
                        if (user != null) {
                            val newComment = Comment(
                                userId = userId.toString(),
                                topicId = topicId,
                                content = _newCommentContent.value,
                                username = user.username
                            )
                            forumRepository.addComment(newComment)
                            _newCommentContent.value = ""
                            _showCommentInput.value = false
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }
}
