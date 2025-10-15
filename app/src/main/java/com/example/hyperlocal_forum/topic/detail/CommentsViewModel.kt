package com.example.hyperlocal_forum.topic.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.Comment
import com.example.hyperlocal_forum.data.ForumDao
import com.example.hyperlocal_forum.data.TopicWithComments
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommentsViewModel(
    private val forumDao: ForumDao,
    private val topicId: Long
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
                // TODO: Replace 0L with actual userId from the logged-in user session
                val newComment = Comment(userId = 0L, topicId = topicId, content = _newCommentContent.value)
                forumDao.insertComment(newComment)
                _newCommentContent.value = ""
                _showCommentInput.value = false
            }
        }
    }
}

class CommentsViewModelFactory(
    private val forumDao: ForumDao,
    private val topicId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CommentsViewModel(forumDao, topicId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}