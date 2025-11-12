package com.example.hyperlocal_forum.ui.topics

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
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

private const val TAG = "TopicsViewModel"

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

    private val _searchRadius = MutableStateFlow(5.0)
    val searchRadius: StateFlow<Double> = _searchRadius.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        refreshTopics()
    }

    private fun loadTopics() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "Loading all topics...")
            try {
                forumRepository.getAllTopics().collect { topicsList ->
                    _topics.value = topicsList
                    updateUsersMap(topicsList)
                    Log.d(TAG, "Loaded ${topicsList.size} topics in total.")
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e(TAG, "Error loading all topics", e)
            }
        }
    }

    private fun loadNearbyTopics() {
        _userLocation.value?.let { location ->
            viewModelScope.launch {
                _isLoading.value = true
                Log.d(TAG, "Loading nearby topics for location: $location with radius ${_searchRadius.value} km")
                try {
                    forumRepository.findNearbyTopics(location, _searchRadius.value).collect { nearbyTopics ->
                        _topics.value = nearbyTopics
                        updateUsersMap(nearbyTopics)
                        Log.d(TAG, "Found ${nearbyTopics.size} nearby topics.")
                        _isLoading.value = false
                    }
                } catch (e: Exception) {
                    _isLoading.value = false
                    Log.e(TAG, "Error loading nearby topics", e)
                }
            }
        } ?: run {
            _isLoading.value = false
            Log.w(TAG, "Cannot load nearby topics because user location is null.")
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
        Log.d(TAG, "Switching to nearby filter...")
        _showNearbyOnly.value = true
        refreshTopics()
    }

    fun setSearchRadius(radius: Double) {
        if (_searchRadius.value != radius) {
            _searchRadius.value = radius
            if (_showNearbyOnly.value) {
                refreshTopics()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun updateUserLocation() {
        if (_showNearbyOnly.value) {
            _isLoading.value = true
        }
        Log.d(TAG, "Attempting to update user location...")
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                _userLocation.value = GeoCoordinates(it.latitude, it.longitude)
                Log.d(TAG, "User location updated successfully: ${_userLocation.value}")
                if (_showNearbyOnly.value) {
                    loadNearbyTopics()
                }
            } ?: run {
                _isLoading.value = false
                _userLocation.value = null
                Log.w(TAG, "Failed to get user location, it was null.")
            }
        }.addOnFailureListener {
            _isLoading.value = false
            _userLocation.value = null
            Log.e(TAG, "Failed to get user location with an exception.", it)
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