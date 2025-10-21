package com.example.hyperlocal_forum.ui.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.hyperlocal_forum.data.ForumDao
import com.example.hyperlocal_forum.data.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class AuthManager(
    context: Context,
    private val forumDao: ForumDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean(KEY_IS_LOGGED_IN, false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _currentUserId = MutableStateFlow(prefs.getLong(KEY_USER_ID, -1L))
    val currentUserId: StateFlow<Long> = _currentUserId

    private fun hashPassword(password: String): String {
        return password.hashCode().toString()  //TODO: Replace with actual hashing algorithm
    }

    suspend fun register(username: String, password: String): AuthResult = withContext(dispatcher) {
        if (username.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Username and password cannot be empty.")
        }
        val existingUser = forumDao.getUserByUsername(username)
        if (existingUser != null) {
            return@withContext AuthResult.Error("Username already exists.")
        }

        val passwordHash = hashPassword(password)
        val newUser = User(username = username, passwordHash = passwordHash)
        val userId = forumDao.insertUser(newUser)
        if (userId > 0) {
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).commit()
            prefs.edit().putString(KEY_USERNAME, username).commit()
            prefs.edit().putLong(KEY_USER_ID, userId).commit()
            _isLoggedIn.value = true
            _currentUserId.value = userId
            AuthResult.Success("Registration successful!")
        } else {
            AuthResult.Error("Registration failed.")
        }
    }

    suspend fun login(username: String, password: String): AuthResult = withContext(dispatcher) {
        if (username.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Username and password cannot be empty.")
        }
        val passwordHash = hashPassword(password)
        val user = forumDao.authenticateUser(username, passwordHash)
        if (user != null) {
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).commit()
            prefs.edit().putString(KEY_USERNAME, username).commit()
            prefs.edit().putLong(KEY_USER_ID, user.id).commit()
            _isLoggedIn.value = true
            _currentUserId.value = user.id
            AuthResult.Success("Login successful!")
        } else {
            AuthResult.Error("Invalid username or password.")
        }
    }

    fun logout() {
        prefs.edit().clear().commit()
        _isLoggedIn.value = false
        _currentUserId.value = -1L
    }

    fun getLoggedInUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_ID = "user_id"
    }
}

sealed class AuthResult {
    data class Success(val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
