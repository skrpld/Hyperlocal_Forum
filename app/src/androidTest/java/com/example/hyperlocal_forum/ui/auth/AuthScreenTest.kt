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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var authManager: AuthManager
    private var loginSuccess = false

    @Before
    fun setUp() {
        val fakeDao = FakeForumDao()
        val context: Context = ApplicationProvider.getApplicationContext()
        authManager = AuthManager(context, fakeDao)
    }

    @Test
    fun authScreen_loginAttempt() {
        composeTestRule.setContent {
            AuthScreen(
                authManager = authManager,
                onLoginSuccess = { loginSuccess = true }
            )
        }

        composeTestRule.onNodeWithTag("AuthScreen_Username").performTextInput("testuser")
        composeTestRule.onNodeWithTag("AuthScreen_Password").performTextInput("password")
        composeTestRule.onNodeWithTag("AuthScreen_LoginButton").performClick()

    }

    @Test
    fun authScreen_switchModes() {
        composeTestRule.setContent {
            AuthScreen(
                authManager = authManager,
                onLoginSuccess = { }
            )
        }

        composeTestRule.onNodeWithTag("AuthScreen_ToggleModeButton").performClick()
        composeTestRule.onNodeWithTag("AuthScreen_RegisterButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("AuthScreen_ToggleModeButton").performClick()
        composeTestRule.onNodeWithTag("AuthScreen_LoginButton").assertIsDisplayed()
    }
}
