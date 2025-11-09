package com.example.hyperlocal_forum.ui.auth

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hyperlocal_forum.data.FakeForumDao
import com.example.hyperlocal_forum.data.models.firestore.User
import com.example.hyperlocal_forum.data.AuthManager
import com.example.hyperlocal_forum.ui.screens.auth.AuthScreen
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var authManager: AuthManager
    private lateinit var fakeDao: FakeForumDao
    private var loginSuccess = false

    @Before
    fun setUp() {
        fakeDao = FakeForumDao()
        // Initialize with a test user for login scenarios
        fakeDao.initUsers(listOf(User(id = 1, username = "test", passwordHash = "test".hashCode().toString())))

        val context: Context = ApplicationProvider.getApplicationContext()
        // Use UnconfinedTestDispatcher for synchronous execution in tests
        authManager = AuthManager(context, fakeDao, UnconfinedTestDispatcher())
        loginSuccess = false
    }

    @Test
    fun authScreen_loginAttempt_success() {
        composeTestRule.setContent {
            AuthScreen(
                authManager = authManager,
                onLoginSuccess = { loginSuccess = true }
            )
        }

        composeTestRule.onNodeWithTag("AuthScreen_Username").performTextInput("test")
        composeTestRule.onNodeWithTag("AuthScreen_Password").performTextInput("test")
        composeTestRule.onNodeWithTag("AuthScreen_LoginButton").performClick()
        assertTrue(loginSuccess)
    }

    @Test
    fun authScreen_loginAttempt_fail() {
        composeTestRule.setContent {
            AuthScreen(
                authManager = authManager,
                onLoginSuccess = { loginSuccess = true }
            )
        }

        composeTestRule.onNodeWithTag("AuthScreen_Username").performTextInput("wrong")
        composeTestRule.onNodeWithTag("AuthScreen_Password").performTextInput("user")
        composeTestRule.onNodeWithTag("AuthScreen_LoginButton").performClick()
        assertFalse(loginSuccess)
    }


    @Test
    fun authScreen_switchModes() {
        composeTestRule.setContent {
            AuthScreen(
                authManager = authManager,
                onLoginSuccess = { }
            )
        }

        composeTestRule.onNodeWithTag("AuthScreen_ConfirmPassword").assertDoesNotExist()
        composeTestRule.onNodeWithTag("AuthScreen_ToggleModeButton").performClick()
        composeTestRule.onNodeWithTag("AuthScreen_RegisterButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("AuthScreen_ConfirmPassword").assertIsDisplayed()
        composeTestRule.onNodeWithTag("AuthScreen_ToggleModeButton").performClick()
        composeTestRule.onNodeWithTag("AuthScreen_LoginButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("AuthScreen_ConfirmPassword").assertDoesNotExist()
    }

    @Test
    fun authScreen_registration_success() {
        composeTestRule.setContent {
            AuthScreen(
                authManager = authManager,
                onLoginSuccess = { loginSuccess = true }
            )
        }

        composeTestRule.onNodeWithTag("AuthScreen_ToggleModeButton").performClick()
        composeTestRule.onNodeWithTag("AuthScreen_Username").performTextInput("newuser")
        composeTestRule.onNodeWithTag("AuthScreen_Password").performTextInput("password")
        composeTestRule.onNodeWithTag("AuthScreen_ConfirmPassword").performTextInput("password")
        composeTestRule.onNodeWithTag("AuthScreen_RegisterButton").performClick()
        assertTrue(loginSuccess)
    }

    @Test
    fun authScreen_registration_fail_passwordMismatch() {
        composeTestRule.setContent {
            AuthScreen(
                authManager = authManager,
                onLoginSuccess = { loginSuccess = true }
            )
        }

        composeTestRule.onNodeWithTag("AuthScreen_ToggleModeButton").performClick()
        composeTestRule.onNodeWithTag("AuthScreen_Username").performTextInput("newuser")
        composeTestRule.onNodeWithTag("AuthScreen_Password").performTextInput("password")
        composeTestRule.onNodeWithTag("AuthScreen_ConfirmPassword").performTextInput("wrongpassword")
        composeTestRule.onNodeWithTag("AuthScreen_RegisterButton").performClick()
        assertFalse(loginSuccess)
    }
}