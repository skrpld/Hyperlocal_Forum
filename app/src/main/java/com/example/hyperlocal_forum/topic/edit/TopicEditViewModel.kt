package com.example.hyperlocal_forum.topic.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.ForumDao
import com.example.hyperlocal_forum.data.GeoCoordinates
import com.example.hyperlocal_forum.data.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TopicEditViewModel(private val forumDao: ForumDao) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onContentChange(newContent: String) {
        _content.value = newContent
    }

    fun saveTopic() {
        viewModelScope.launch {
            _isSaving.value = true
            forumDao.insertTopic(Topic(
                userId = 0, //TODO("get user id")
                location = GeoCoordinates(0.0, 0.0),    //TODO("get location")
                title = _title.value,
                content = _content.value
            ))
            _isSaving.value = false
            _saveSuccess.value = true
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