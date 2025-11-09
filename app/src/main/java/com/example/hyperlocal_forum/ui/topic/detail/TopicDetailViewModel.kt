package com.example.hyperlocal_forum.ui.topic.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.ForumRepository
import com.example.hyperlocal_forum.data.firebase.TopicWithComments
import com.example.hyperlocal_forum.data.firebase.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private var _topicId: String? = null

    private val _topicDetailState = MutableStateFlow<TopicDetailState?>(null)
    val topicDetailState: StateFlow<TopicDetailState?> = _topicDetailState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTopicDetail()
    }

    fun setTopicId(id: String) {
        _topicId = id
        loadTopicDetail()
    }

    private fun loadTopicDetail() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _topicId?.let { topicId ->
                    forumRepository.getTopicWithComments(topicId).collect { topicWithComments ->
                        val author = forumRepository.getUser(topicWithComments.topic.userId)
                        if (author != null) {
                            _topicDetailState.value = TopicDetailState(topicWithComments, author)
                        }
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                // Обработка ошибки
            }
        }
    }
}
