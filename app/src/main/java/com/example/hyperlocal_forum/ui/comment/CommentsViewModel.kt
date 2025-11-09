package com.example.hyperlocal_forum.ui.comment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.Comment
import com.example.hyperlocal_forum.data.ForumDao
import com.example.hyperlocal_forum.utils.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CommentsViewModel(
    private val forumDao: ForumDao,
    private val topicId: Long,
    private val authManager: AuthManager
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _showCommentInput = MutableStateFlow(false)
    val showCommentInput: StateFlow<Boolean> = _showCommentInput.asStateFlow()

    private val _newCommentContent = MutableStateFlow("")
    val newCommentContent: StateFlow<String> = _newCommentContent.asStateFlow()

    init {
        loadComments()
    }

    private fun loadComments() {
        viewModelScope.launch {
            forumDao.getTopicWithComments(topicId).collect { topicWithComments ->
                _comments.value = topicWithComments.comments
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
                if (userId != -1L) {
                    val user = forumDao.getUser(userId).first()
                    val newComment = Comment(userId = userId, topicId = topicId, content = _newCommentContent.value, username = user.username)
                    forumDao.insertComment(newComment)
                    _newCommentContent.value = ""
                    _showCommentInput.value = false
                }
            }
        }
    }
}

class CommentsViewModelFactory(
    private val forumDao: ForumDao,
    private val topicId: Long,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CommentsViewModel(forumDao, topicId, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
