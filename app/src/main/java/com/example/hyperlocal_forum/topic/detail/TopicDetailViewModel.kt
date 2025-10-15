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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TopicDetailViewModel(
    private val forumDao: ForumDao,
    private val topicId: Long
) : ViewModel() {

    val topicWithComments: StateFlow<TopicWithComments?> = forumDao.getTopicWithComments(topicId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}

class TopicDetailViewModelFactory(
    private val forumDao: ForumDao,
    private val topicId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TopicDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TopicDetailViewModel(forumDao, topicId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}