package com.example.hyperlocal_forum.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.utils.AuthManager
import com.example.hyperlocal_forum.data.ForumDao
import com.example.hyperlocal_forum.data.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val forumDao: ForumDao,
    private val authManager: AuthManager
) : ViewModel() {

    val user: StateFlow<User?> = forumDao.getUser(authManager.currentUserId.value)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateUsername(username: String) {
        viewModelScope.launch {
            user.value?.let { currentUser ->
                forumDao.updateUser(currentUser.copy(username = username))
            }
        }
    }

    fun updatePassword(password: String) {
        viewModelScope.launch {
            user.value?.let { currentUser ->
                forumDao.updateUser(currentUser.copy(passwordHash = password.hashCode().toString()))
            }
        }
    }

    fun logout() {
        authManager.logout()
    }
}

class ProfileViewModelFactory(
    private val forumDao: ForumDao,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(forumDao, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
