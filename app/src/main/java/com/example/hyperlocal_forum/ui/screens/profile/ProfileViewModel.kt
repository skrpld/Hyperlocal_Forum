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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
        authManager.currentUserId.onEach { userId ->
            userId?.let {
                loadUser(it)
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Loads the user data for the given user ID from the repository.
     * Updates the user state and loading state.
     * @param userId The unique identifier of the user to load.
     */
    private fun loadUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                forumRepository.getUser(userId)?.let { user ->
                    _user.value = user
                }
            } catch (e: Exception) {
                // Handle error, e.g., show a message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Saves changes to the user's profile, including username and password.
     * If the username has changed, it updates the user object in the repository.
     * If a new password is provided, it updates the user's password.
     * @param username The new username for the user.
     * @param newPassword The new password. If empty, the password is not changed.
     */
    fun onSaveChanges(username: String, newPassword: String) {
        viewModelScope.launch {
            _user.value?.let { currentUser ->
                _isLoading.value = true
                try {
                    if (currentUser.username != username) {
                        val updatedUser = currentUser.copy(username = username)
                        val success = forumRepository.updateUser(updatedUser)
                        if (success) {
                            _user.value = updatedUser
                        }
                    }

                    if (newPassword.isNotEmpty()) {
                        forumRepository.updatePassword(currentUser.id, newPassword)
                    }
                } catch (e: Exception) {
                    // Handle error
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Logs out the current user.
     * Clears the user state and triggers a callback function.
     * @param onLogout A callback function to be executed after the user has been logged out.
     */
    fun logout(onLogout: () -> Unit) {
        authManager.logout()
        _user.value = null
        onLogout()
    }
}