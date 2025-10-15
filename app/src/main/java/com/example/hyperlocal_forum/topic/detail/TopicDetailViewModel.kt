package com.example.hyperlocal_forum.topic.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.ForumDao
import com.example.hyperlocal_forum.data.TopicWithComments
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TopicDetailViewModel constructor(
    private val forumDao: ForumDao
) : ViewModel() {

    private val _topicWithComments = MutableStateFlow<TopicWithComments?>(null)
    val topicWithComments: StateFlow<TopicWithComments?> = _topicWithComments

    fun loadTopicDetails(topicId: Long) {
        viewModelScope.launch {
            _topicWithComments.value = forumDao.getTopicWithComments(topicId)
        }
    }
}

class TopicDetailViewModelFactory(private val forumDao: ForumDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TopicDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TopicDetailViewModel(forumDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
