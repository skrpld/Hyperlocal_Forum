package com.example.hyperlocal_forum.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.ForumDao
import com.example.hyperlocal_forum.data.Topic
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TopicsViewModel constructor(
    forumDao: ForumDao
) : ViewModel() {

    val topics: StateFlow<List<Topic>> = forumDao.getAllTopics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
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