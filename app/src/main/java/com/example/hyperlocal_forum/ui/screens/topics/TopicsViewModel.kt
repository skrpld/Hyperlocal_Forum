package com.example.hyperlocal_forum.ui.topics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.ForumRepository
import com.example.hyperlocal_forum.data.GeoCoordinates
import com.example.hyperlocal_forum.data.models.firestore.Topic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicsViewModel @Inject constructor(
    private val forumRepository: ForumRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _topics = MutableStateFlow<List<Topic>>(emptyList())
    val topics: StateFlow<List<Topic>> = _topics.asStateFlow()

    private val _userLocation = MutableStateFlow<GeoCoordinates?>(null)
    val userLocation: StateFlow<GeoCoordinates?> = _userLocation.asStateFlow()

    private val _showNearbyOnly = MutableStateFlow(false)
    val showNearbyOnly: StateFlow<Boolean> = _showNearbyOnly.asStateFlow()

    init {
        loadTopics()
    }

    private fun loadTopics() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                forumRepository.getAllTopics().collect { topicsList ->
                    _topics.value = topicsList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun loadNearbyTopics(location: GeoCoordinates) {
        viewModelScope.launch {
            _isLoading.value = true
            _userLocation.value = location
            _showNearbyOnly.value = true

            try {
                forumRepository.findNearbyTopics(location, 10.0).collect { nearbyTopics ->
                    _topics.value = nearbyTopics
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun loadAllTopics() {
        _showNearbyOnly.value = false
        loadTopics()
    }

    fun refreshTopics() {
        if (_showNearbyOnly.value && _userLocation.value != null) {
            _userLocation.value?.let { location ->
                loadNearbyTopics(location)
            }
        } else {
            loadTopics()
        }
    }

    fun setUserLocation(location: GeoCoordinates) {
        _userLocation.value = location
    }
}
