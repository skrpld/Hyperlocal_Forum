
package com.example.hyperlocal_forum.ui.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.hyperlocal_forum.data.ForumDao
import com.example.hyperlocal_forum.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class AuthViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var authViewModel: AuthViewModel
    private lateinit var authManager: AuthManager
    private lateinit var forumDao: ForumDao
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor


    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        forumDao = mock()
        context = mock()
        sharedPreferences = mock()
        editor = mock()

        whenever(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)
        whenever(sharedPreferences.edit()).thenReturn(editor)
        whenever(editor.putBoolean(anyString(), any())).thenReturn(editor)
        whenever(editor.putString(anyString(), anyString())).thenReturn(editor)
        whenever(editor.putLong(anyString(), any())).thenReturn(editor)
        whenever(editor.clear()).thenReturn(editor)


        authManager = AuthManager(context, forumDao, testDispatcher)
        authViewModel = AuthViewModel(authManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test login success`() = runTest {
        val username = "testuser"
        val password = "password"
        val user = User(id = 1, username = username, passwordHash = password.hashCode().toString())
        whenever(forumDao.authenticateUser(username, password.hashCode().toString())).thenReturn(user)

        authViewModel.onUsernameChange(username)
        authViewModel.onPasswordChange(password)
        authViewModel.authenticate {}

        assertEquals("Login successful!", authViewModel.message.value)
    }

    @Test
    fun `test login failure invalid credentials`() = runTest {
        val username = "testuser"
        val password = "wrongpassword"
        whenever(forumDao.authenticateUser(username, password.hashCode().toString())).thenReturn(null)

        authViewModel.onUsernameChange(username)
        authViewModel.onPasswordChange(password)
        authViewModel.authenticate {}

        assertEquals("Invalid username or password.", authViewModel.message.value)
    }

    @Test
    fun `test login failure empty fields`() = runTest {
        authViewModel.onUsernameChange("")
        authViewModel.onPasswordChange("")
        authViewModel.authenticate {}

        assertEquals("Username and password cannot be empty.", authViewModel.message.value)
    }

    @Test
    fun `test registration success`() = runTest {
        val username = "newuser"
        val password = "newpassword"
        whenever(forumDao.getUserByUsername(username)).thenReturn(null)
        whenever(forumDao.insertUser(any())).thenReturn(1L)

        authViewModel.toggleLoginMode()
        authViewModel.onUsernameChange(username)
        authViewModel.onPasswordChange(password)
        authViewModel.authenticate {}

        assertEquals("Registration successful!", authViewModel.message.value)
    }

    @Test
    fun `test registration failure username exists`() = runTest {
        val username = "existinguser"
        val password = "password"
        val existingUser = User(id = 1, username = username, passwordHash = "somehash")
        whenever(forumDao.getUserByUsername(username)).thenReturn(existingUser)

        authViewModel.toggleLoginMode()
        authViewModel.onUsernameChange(username)
        authViewModel.onPasswordChange(password)
        authViewModel.authenticate {}

        assertEquals("Username already exists.", authViewModel.message.value)
    }

    @Test
    fun `test registration failure empty fields`() = runTest {
        authViewModel.toggleLoginMode()
        authViewModel.onUsernameChange("")
        authViewModel.onPasswordChange("")
        authViewModel.authenticate {}

        assertEquals("Username and password cannot be empty.", authViewModel.message.value)
    }

    @Test
    fun `test toggle login mode`() {
        assertEquals(true, authViewModel.isLoginMode.value)
        authViewModel.toggleLoginMode()
        assertEquals(false, authViewModel.isLoginMode.value)
        authViewModel.toggleLoginMode()
        assertEquals(true, authViewModel.isLoginMode.value)
    }
}
