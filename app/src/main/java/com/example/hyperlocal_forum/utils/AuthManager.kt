package com.example.hyperlocal_forum.utils

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        auth.currentUser?.let { user ->
            _currentUserId.value = user.uid
            _isLoggedIn.value = true
        }
    }

    suspend fun login(username: String, password: String): AuthResult {
        return try {
            val email = if (username.contains("@")) username else "$username@forum.com"
            val result = auth.signInWithEmailAndPassword(email, password).await()
            if (result.user != null) {
                _currentUserId.value = result.user!!.uid
                _isLoggedIn.value = true
                AuthResult.Success("Login successful")
            } else {
                AuthResult.Error("Login failed")
            }
        } catch (e: Exception) {
            AuthResult.Error("Login error: ${e.message}")
        }
    }

    suspend fun register(username: String, password: String): AuthResult {
        return try {
            val email = if (username.contains("@")) username else "$username@forum.com"
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            if (result.user != null) {
                _currentUserId.value = result.user!!.uid
                _isLoggedIn.value = true
                AuthResult.Success("Registration successful")
            } else {
                AuthResult.Error("Registration failed")
            }
        } catch (e: Exception) {
            AuthResult.Error("Registration error: ${e.message}")
        }
    }

    fun logout() {
        auth.signOut()
        _currentUserId.value = null
        _isLoggedIn.value = false
    }
}

sealed class AuthResult {
    data class Success(val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}