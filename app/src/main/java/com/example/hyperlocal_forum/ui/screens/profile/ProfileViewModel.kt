package com.example.hyperlocal_forum.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.AuthManager
import com.example.hyperlocal_forum.data.ForumRepository
import com.example.hyperlocal_forum.data.models.firestore.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val forumRepository: ForumRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = authManager.currentUserId.value
                if (userId != "-1") {
                    forumRepository.getUser(userId.toString())?.let { user ->
                        _user.value = user
                    }
                }
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUsername(username: String) {
        viewModelScope.launch {
            _user.value?.let { currentUser ->
                try {
                    val updatedUser = currentUser.copy(username = username)
                    forumRepository.createUser(updatedUser)
                    _user.value = updatedUser
                } catch (e: Exception) {
                }
            }
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _user.value?.let { currentUser ->
                try {
                    val success = forumRepository.updatePassword(currentUser.id, newPassword)
                    if (success) {
                    } else {
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    fun logout() {
        authManager.logout()
        _user.value = null
    }
}
