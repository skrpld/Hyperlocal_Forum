package com.example.hyperlocal_forum.ui.screens.topicdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.ForumRepository
import com.example.hyperlocal_forum.data.models.firestore.TopicWithComments
import com.example.hyperlocal_forum.data.models.firestore.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.hyperlocal_forum.data.AuthManager
import com.example.hyperlocal_forum.data.models.firestore.Comment
import kotlinx.coroutines.flow.first

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
                    }
                }
            }
        }
    }

    fun setTopicId(id: String) {
        if (id == currentTopicId) {
            return
        }
        currentTopicId = id
        loadTopicDetail(id)
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
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _topicDetailState.value = null
                _isLoading.value = false
            }
        }
    }
}