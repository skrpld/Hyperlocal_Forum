package com.example.hyperlocal_forum.ui.topics

import android.annotation.SuppressLint
import android.location.Location
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
import com.google.android.gms.location.FusedLocationProviderClient

@HiltViewModel
class TopicsViewModel @Inject constructor(
    private val forumRepository: ForumRepository,
    private val fusedLocationClient: FusedLocationProviderClient
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
        loadAllTopics()
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

    private fun loadNearbyTopics() {
        _userLocation.value?.let { location ->
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    forumRepository.findNearbyTopics(location, 10.0).collect { nearbyTopics ->
                        _topics.value = nearbyTopics
                        _isLoading.value = false
                    }
                } catch (e: Exception) {
                    _isLoading.value = false
                }
            }
        } ?: run {
            _isLoading.value = false
        }
    }

    fun loadAllTopics() {
        if (_showNearbyOnly.value) {
            _showNearbyOnly.value = false
        }
        loadTopics()
    }

    fun switchToNearbyFilter() {
        _showNearbyOnly.value = true
        updateUserLocation()
    }

    @SuppressLint("MissingPermission")
    fun updateUserLocation() {
        if (_showNearbyOnly.value) {
            _isLoading.value = true
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                _userLocation.value = GeoCoordinates(it.latitude, it.longitude)
                if (_showNearbyOnly.value) {
                    loadNearbyTopics()
                }
            } ?: run {
                _isLoading.value = false
                _userLocation.value = null
            }
        }.addOnFailureListener {
            _isLoading.value = false
            _userLocation.value = null
        }
    }

    fun refreshTopics() {
        if (_showNearbyOnly.value) {
            updateUserLocation()
        } else {
            loadTopics()
        }
    }
}
