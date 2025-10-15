package com.example.hyperlocal_forum.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.ForumDao
import com.example.hyperlocal_forum.data.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TopicsViewModel constructor(
    private val forumDao: ForumDao
): ViewModel() {

    private val _topics = MutableStateFlow<List<Topic>>(emptyList())
    val topics: StateFlow<List<Topic>> = _topics

    init {
        loadTopics()
    }

    private fun loadTopics() {
        viewModelScope.launch {
            _topics.value = forumDao.getAllTopics()
        }
    }
}

class TopicsViewModelFactory(private val forumDao: ForumDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TopicsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TopicsViewModel(forumDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}