package com.example.hyperlocal_forum.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hyperlocal_forum.data.AuthManager
import com.example.hyperlocal_forum.data.AuthResult
import com.example.hyperlocal_forum.data.ForumRepository
import com.example.hyperlocal_forum.data.models.firestore.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val forumRepository: ForumRepository
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _isLoginMode = MutableStateFlow(true)
    val isLoginMode: StateFlow<Boolean> = _isLoginMode.asStateFlow()

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun onUsernameChange(newUsername: String) {
        _username.value = newUsername
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
    }

    fun toggleLoginMode() {
        _isLoginMode.value = !_isLoginMode.value
        _authMessage.value = null
    }

    private fun isEmailValid(email: String): Boolean {
        val emailRegex = Regex("^[\\S]+@[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}\$")
        return emailRegex.matches(email)
    }

    fun authenticate(onLoginSuccess: () -> Unit) {
        if (!isEmailValid(_email.value)) {
            _authMessage.value = "Please enter a valid email address"
            return
        }

        if (!_isLoginMode.value && _password.value != _confirmPassword.value) {
            _authMessage.value = "Passwords do not match"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _authMessage.value = null

            try {
                val result = if (_isLoginMode.value) {
                    authManager.login(_email.value, _password.value)
                } else {
                    authManager.register(_email.value, _password.value)
                }

                when (result) {
                    is AuthResult.Success -> {
                        if (!_isLoginMode.value) {
                            val user = User(
                                id = result.userId,
                                username = _username.value,
                                email = _email.value
                            )
                            forumRepository.createUser(user)
                        }
                        _authMessage.value = result.message
                        onLoginSuccess()
                    }
                    is AuthResult.Error -> {
                        _authMessage.value = result.message
                    }
                }
            } catch (e: Exception) {
                _authMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _authMessage.value = null
    }
}