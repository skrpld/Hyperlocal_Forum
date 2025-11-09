package com.example.hyperlocal_forum.ui.topic.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.ForumRepository
import com.example.hyperlocal_forum.data.GeoCoordinates
import com.example.hyperlocal_forum.data.firebase.Topic
import com.example.hyperlocal_forum.utils.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicEditViewModel @Inject constructor(
    private val forumRepository: ForumRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _location = MutableStateFlow<GeoCoordinates?>(null)
    val location: StateFlow<GeoCoordinates?> = _location.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onContentChange(newContent: String) {
        _content.value = newContent
    }

    fun setLocation(lat: Double, lon: Double) {
        _location.value = GeoCoordinates(lat, lon)
    }

    fun saveTopic() {
        viewModelScope.launch {
            if (_title.value.isBlank() || _content.value.isBlank()) {
                _errorMessage.value = "Title and content cannot be empty"
                return@launch
            }

            if (_location.value == null) {
                _errorMessage.value = "Location is required"
                return@launch
            }

            _isSaving.value = true
            _errorMessage.value = null

            try {
                val topic = Topic(
                    userId = authManager.currentUserId.value.toString(),
                    location = _location.value!!,
                    title = _title.value,
                    content = _content.value
                )
                forumRepository.createTopic(topic)
                _isSaving.value = false
                _saveSuccess.value = true
            } catch (e: Exception) {
                _isSaving.value = false
                _errorMessage.value = "Failed to save topic: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
