package com.example.hyperlocal_forum.topic.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.ForumDao
import com.example.hyperlocal_forum.data.GeoCoordinates
import com.example.hyperlocal_forum.data.Topic
import kotlinx.coroutines.launch

class TopicEditViewModel(private val forumDao: ForumDao) : ViewModel() {

    fun saveTopic(title: String, content: String) {
        viewModelScope.launch {
            forumDao.insertTopic(Topic(
                userId = 0, //TODO("get user id")
                location = GeoCoordinates(0.0, 0.0),    //TODO("get location")
                title = title,
                content = content
            ))
        }
    }
}

class TopicEditViewModelFactory(private val forumDao: ForumDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TopicEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TopicEditViewModel(forumDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}