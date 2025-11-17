package com.example.hyperlocal_forum.ui.screens.auth

import app.cash.turbine.test
import com.example.hyperlocal_forum.MainCoroutineRule
import com.example.hyperlocal_forum.data.AuthManager
import com.example.hyperlocal_forum.data.AuthResult
import com.example.hyperlocal_forum.data.ForumRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class AuthViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var authManager: AuthManager
    private lateinit var forumRepository: ForumRepository
    private lateinit var viewModel: AuthViewModel

    private val onLoginSuccess: () -> Unit = mock()

    @Before
    fun setUp() {
        authManager = mock()
        forumRepository = mock()
        viewModel = AuthViewModel(authManager, forumRepository)
    }

    // Тест-кейс 1: Успешный вход пользователя
    @Test
    fun `authenticate when login successful then isLoading changes and onLoginSuccess is called`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val successResult = AuthResult.Success("userId123", "Login successful")
        whenever(authManager.login(email, password)).thenReturn(successResult)

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)

        viewModel.isLoading.test {
            assertThat(awaitItem()).isFalse()

            viewModel.authenticate(onLoginSuccess)

            assertThat(awaitItem()).isTrue()
            assertThat(awaitItem()).isFalse()

            verify(authManager).login(email, password)
            verify(onLoginSuccess).invoke()
            assertThat(viewModel.authMessage.value).isEqualTo("Login successful")
        }
    }

    // Тест-кейс 2: Неудачный вход пользователя
    @Test
    fun `authenticate when login fails then authMessage shows error and onLoginSuccess is not called`() = runTest {
        val email = "test@example.com"
        val password = "wrongpassword"
        val errorResult = AuthResult.Error("Invalid credentials")
        whenever(authManager.login(email, password)).thenReturn(errorResult)

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)

        viewModel.isLoading.test {
            assertThat(awaitItem()).isFalse()
            viewModel.authenticate(onLoginSuccess)
            assertThat(awaitItem()).isTrue()
            assertThat(awaitItem()).isFalse()

            verify(authManager).login(email, password)
            verify(onLoginSuccess, never()).invoke()
            assertThat(viewModel.authMessage.value).isEqualTo("Invalid credentials")
        }
    }

    // Тест-кейс 3: Успешная регистрация
    @Test
    fun `authenticate when registration is successful then creates user and calls onLoginSuccess`() = runTest {
        val username = "tester"
        val email = "newuser@example.com"
        val password = "password123"
        val successResult = AuthResult.Success("newUserId", "Registration successful")
        whenever(authManager.register(email, password)).thenReturn(successResult)

        viewModel.toggleLoginMode()
        viewModel.onUsernameChange(username)
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        viewModel.onConfirmPasswordChange(password)

        viewModel.isLoading.test {
            assertThat(awaitItem()).isFalse()
            viewModel.authenticate(onLoginSuccess)
            assertThat(awaitItem()).isTrue()
            assertThat(awaitItem()).isFalse()

            verify(authManager).register(email, password)
            verify(forumRepository).createUser(any())
            verify(onLoginSuccess).invoke()
            assertThat(viewModel.authMessage.value).isEqualTo("Registration successful")
        }
    }

    // Тест-кейс 4: Регистрация с несовпадающими паролями
    @Test
    fun `authenticate when passwords do not match then shows error and does not call authManager`() = runTest {
        viewModel.toggleLoginMode()
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password456")

        viewModel.authenticate(onLoginSuccess)

        verify(authManager, never()).register(any(), any())
        assertThat(viewModel.authMessage.value).isEqualTo("Passwords do not match")
        assertThat(viewModel.isLoading.value).isFalse()
    }

    // Тест-кейс 5: Аутентификация с невалидным email
    @Test
    fun `authenticate when email is invalid then shows error and does not call authManager`() = runTest {
        viewModel.onEmailChange("invalid-email")
        viewModel.onPasswordChange("password123")

        viewModel.authenticate(onLoginSuccess)

        verify(authManager, never()).login(any(), any())
        verify(authManager, never()).register(any(), any())
        assertThat(viewModel.authMessage.value).isEqualTo("Please enter a valid email address")
        assertThat(viewModel.isLoading.value).isFalse()
    }

    // Тест-кейс 6: Переключение между режимами входа и регистрации
    @Test
    fun `toggleLoginMode inverts isLoginMode state and clears authMessage`() = runTest {
        assertThat(viewModel.isLoginMode.value).isTrue()

        viewModel.toggleLoginMode()
        assertThat(viewModel.isLoginMode.value).isFalse()

        viewModel.authenticate {}
        assertThat(viewModel.authMessage.value).isNotNull()

        viewModel.toggleLoginMode()
        assertThat(viewModel.isLoginMode.value).isTrue()
        assertThat(viewModel.authMessage.value).isNull()
    }

    // Тест-кейс 7: Обновление полей ввода
    @Test
    fun `input change functions update corresponding state flows`() {
        viewModel.onUsernameChange("testuser")
        assertThat(viewModel.username.value).isEqualTo("testuser")

        viewModel.onEmailChange("test@email.com")
        assertThat(viewModel.email.value).isEqualTo("test@email.com")

        viewModel.onPasswordChange("pass1")
        assertThat(viewModel.password.value).isEqualTo("pass1")

        viewModel.onConfirmPasswordChange("pass2")
        assertThat(viewModel.confirmPassword.value).isEqualTo("pass2")
    }

    // Тест-кейс 8: Сетевая ошибка при аутентификации
    @Test
    fun `authenticate when authManager throws exception then shows network error message`() = runTest {
        val email = "test@example.com"
        val password = "password"
        val exception = RuntimeException("Network connection failed")
        whenever(authManager.login(email, password)).thenThrow(exception)

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)

        viewModel.isLoading.test {
            assertThat(awaitItem()).isFalse()
            viewModel.authenticate(onLoginSuccess)
            assertThat(awaitItem()).isTrue()
            assertThat(awaitItem()).isFalse()

            verify(onLoginSuccess, never()).invoke()
            assertThat(viewModel.authMessage.value).isEqualTo("Network error: ${exception.message}")
        }
    }
}