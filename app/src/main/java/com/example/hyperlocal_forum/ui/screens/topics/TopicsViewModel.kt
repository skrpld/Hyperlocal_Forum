package com.example.hyperlocal_forum.ui.topics

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.ForumRepository
import com.example.hyperlocal_forum.data.GeoCoordinates
import com.example.hyperlocal_forum.data.models.firestore.Topic
import com.example.hyperlocal_forum.data.models.firestore.User
import com.example.hyperlocal_forum.utils.ConnectivityObserver
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicsViewModel @Inject constructor(
    private val forumRepository: ForumRepository,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _topics = MutableStateFlow<List<Topic>>(emptyList())
    val topics: StateFlow<List<Topic>> = _topics.asStateFlow()

    private val _usersMap = MutableStateFlow<Map<String, User>>(emptyMap())
    val usersMap: StateFlow<Map<String, User>> = _usersMap.asStateFlow()

    private val _userLocation = MutableStateFlow<GeoCoordinates?>(null)
    val userLocation: StateFlow<GeoCoordinates?> = _userLocation.asStateFlow()

    private val _showNearbyOnly = MutableStateFlow(false)
    val showNearbyOnly: StateFlow<Boolean> = _showNearbyOnly.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        refreshTopics()
    }

    private fun loadTopics() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                forumRepository.getAllTopics().collect { topicsList ->
                    _topics.value = topicsList
                    updateUsersMap(topicsList)
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
                    forumRepository.findNearbyTopics(location, 1.0).collect { nearbyTopics ->
                        _topics.value = nearbyTopics
                        updateUsersMap(nearbyTopics)
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

    private suspend fun updateUsersMap(topics: List<Topic>) {
        if (topics.isEmpty()) {
            _usersMap.value = emptyMap()
            return
        }
        val userIds = topics.map { it.userId }.distinct()
        _usersMap.value = forumRepository.getUsers(userIds)
    }

    fun loadAllTopics() {
        if (_showNearbyOnly.value) {
            _showNearbyOnly.value = false
        }
        refreshTopics()
    }

    fun switchToNearbyFilter() {
        _showNearbyOnly.value = true
        refreshTopics()
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
        viewModelScope.launch {
            _isLoading.value = true

            val networkStatus = connectivityObserver.observe().first()
            if (networkStatus == ConnectivityObserver.Status.Unavailable || networkStatus == ConnectivityObserver.Status.Lost) {
                _errorMessage.value = "No internet connection."
            } else {
                val isServerAvailable = forumRepository.checkServerAvailability()
                if (!isServerAvailable) {
                    _errorMessage.value = "Server is unavailable"
                } else {
                    _errorMessage.value = null
                }
            }

            if (_showNearbyOnly.value) {
                updateUserLocation()
            } else {
                loadTopics()
            }
        }
    }
}