package com.example.hyperlocal_forum.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authManager: AuthManager) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _isLoginMode = MutableStateFlow(true)
    val isLoginMode: StateFlow<Boolean> = _isLoginMode.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun onUsernameChange(newUsername: String) {
        _username.value = newUsername
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
    }

    fun toggleLoginMode() {
        _isLoginMode.value = !_isLoginMode.value
        _message.value = null
    }

    fun authenticate(onLoginSuccess: () -> Unit) {
        if (!_isLoginMode.value && _password.value != _confirmPassword.value) {
            _message.value = "Passwords do not match"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = null
            val result = if (_isLoginMode.value) {
                authManager.login(_username.value, _password.value)
            } else {
                authManager.register(_username.value, _password.value)
            }

            when (result) {
                is AuthResult.Success -> {
                    _message.value = result.message
                    onLoginSuccess()
                }
                is AuthResult.Error -> {
                    _message.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

class AuthViewModelFactory(private val authManager: AuthManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}