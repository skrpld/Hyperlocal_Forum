package com.example.hyperlocal_forum.topic

import androidx.lifecycle.ViewModel
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