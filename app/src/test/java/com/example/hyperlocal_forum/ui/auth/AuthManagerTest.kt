
package com.example.hyperlocal_forum.ui.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.hyperlocal_forum.data.ForumDao
import com.example.hyperlocal_forum.data.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class AuthManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPrefs: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    @Mock
    private lateinit var mockForumDao: ForumDao

    private lateinit var authManager: AuthManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(mockContext.getSharedPreferences(any(), any())).thenReturn(mockPrefs)
        whenever(mockPrefs.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putLong(any(), any())).thenReturn(mockEditor)

        authManager = AuthManager(mockContext, mockForumDao)
    }

    @Test
    fun `register success`() = runTest {
        val username = "testuser"
        val password = "password"
        val user = User(id = 1, username = username, passwordHash = password.hashCode().toString())

        whenever(mockForumDao.getUserByUsername(username)).thenReturn(null)
        whenever(mockForumDao.insertUser(any())).thenReturn(1L)

        val result = authManager.register(username, password)

        assert(result is AuthResult.Success)
        verify(mockForumDao).insertUser(any())
        verify(mockEditor).putBoolean("is_logged_in", true)
        verify(mockEditor).putString("username", username)
        verify(mockEditor).putLong("user_id", 1L)
    }

    @Test
    fun `register empty credentials`() = runTest {
        val result = authManager.register("", "")
        assert(result is AuthResult.Error)
        verify(mockForumDao, never()).insertUser(any())
    }

    @Test
    fun `register existing username`() = runTest {
        val username = "testuser"
        val password = "password"
        val existingUser = User(id = 1, username = username, passwordHash = "somehash")

        whenever(mockForumDao.getUserByUsername(username)).thenReturn(existingUser)

        val result = authManager.register(username, password)

        assert(result is AuthResult.Error)
        verify(mockForumDao, never()).insertUser(any())
    }

    @Test
    fun `login success`() = runTest {
        val username = "testuser"
        val password = "password"
        val passwordHash = password.hashCode().toString()
        val user = User(id = 1, username = username, passwordHash = passwordHash)

        whenever(mockForumDao.authenticateUser(username, passwordHash)).thenReturn(user)

        val result = authManager.login(username, password)

        assert(result is AuthResult.Success)
        verify(mockEditor).putBoolean("is_logged_in", true)
        verify(mockEditor).putString("username", username)
        verify(mockEditor).putLong("user_id", 1L)
    }

    @Test
    fun `login empty credentials`() = runTest {
        val result = authManager.login("", "")
        assert(result is AuthResult.Error)
        verify(mockForumDao, never()).authenticateUser(any(), any())
    }

    @Test
    fun `login invalid credentials`() = runTest {
        val username = "testuser"
        val password = "password"
        val passwordHash = password.hashCode().toString()

        whenever(mockForumDao.authenticateUser(username, passwordHash)).thenReturn(null)

        val result = authManager.login(username, password)

        assert(result is AuthResult.Error)
    }

    @Test
    fun `logout clears prefs`() {
        authManager.logout()
        verify(mockEditor).clear()
    }
}
