
package com.example.hyperlocal_forum.ui.auth

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hyperlocal_forum.data.ForumDao
import com.example.hyperlocal_forum.data.ForumDatabase
import com.example.hyperlocal_forum.data.User
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthManagerIntegrationTest {

    private lateinit var db: ForumDatabase
    private lateinit var forumDao: ForumDao
    private lateinit var authManager: AuthManager
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(
            context, ForumDatabase::class.java).build()
        forumDao = db.forumDao()
        authManager = AuthManager(context, forumDao)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun registerAndLogin() = runBlocking {
        // Register a new user
        val registerResult = authManager.register("testuser", "password")
        assert(registerResult is AuthResult.Success)

        // Logout
        authManager.logout()

        // Login with the new user
        val loginResult = authManager.login("testuser", "password")
        assert(loginResult is AuthResult.Success)
    }

    @Test
    fun registerExistingUser() = runBlocking {
        // Register a new user
        authManager.register("testuser", "password")

        // Try to register the same user again
        val registerResult = authManager.register("testuser", "password")
        assert(registerResult is AuthResult.Error)
    }

    @Test
    fun loginWithInvalidCredentials() = runBlocking {
        // Register a new user
        authManager.register("testuser", "password")

        // Logout
        authManager.logout()

        // Try to login with incorrect password
        val loginResult = authManager.login("testuser", "wrongpassword")
        assert(loginResult is AuthResult.Error)
    }
}
