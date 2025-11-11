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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TopicDetailState(
    val topicWithComments: TopicWithComments,
    val author: User
)

@HiltViewModel
class TopicDetailViewModel @Inject constructor(
    private val forumRepository: ForumRepository
) : ViewModel() {

    private var currentTopicId: String? = null

    private val _topicDetailState = MutableStateFlow<TopicDetailState?>(null)
    val topicDetailState: StateFlow<TopicDetailState?> = _topicDetailState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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
                val topicWithComments = forumRepository.getTopicWithComments(topicId).firstOrNull()

                if (topicWithComments != null) {
                    val author = forumRepository.getUser(topicWithComments.topic.userId)

                    val authorToShow = author ?: User(id = topicWithComments.topic.userId, username = "Unknown")

                    _topicDetailState.value = TopicDetailState(topicWithComments, authorToShow)
                } else {
                    _topicDetailState.value = null
                }
            } catch (e: Exception) {
                _topicDetailState.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}