package com.example.hyperlocal_forum.ui.screens.profile

import app.cash.turbine.test
import com.example.hyperlocal_forum.MainCoroutineRule
import com.example.hyperlocal_forum.data.AuthManager
import com.example.hyperlocal_forum.data.ForumRepository
import com.example.hyperlocal_forum.data.models.firestore.User
import com.google.common.truth.Truth.assertThat
import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class ProfileViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var forumRepository: ForumRepository
    private lateinit var authManager: AuthManager
    private lateinit var viewModel: ProfileViewModel
    private val currentUserIdFlow = MutableStateFlow<String?>(null)
    private val testUser = User(id = "user123", username = "tester", email = "test@example.com", timestamp = Timestamp.now())

    @Before
    fun setUp() {
        forumRepository = mock()
        authManager = mock()
        whenever(authManager.currentUserId).thenReturn(currentUserIdFlow)
    }

    private suspend fun initializeViewModelWithUser() {
        whenever(forumRepository.getUser(testUser.id)).thenReturn(testUser)
        viewModel = ProfileViewModel(forumRepository, authManager)
        currentUserIdFlow.value = testUser.id
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()
    }

    // Тест-кейс 1: Успешная загрузка данных пользователя при инициализации
    @Test
    fun `init loads user successfully when userId is available`() = runTest {
        whenever(forumRepository.getUser(testUser.id)).thenReturn(testUser)
        viewModel = ProfileViewModel(forumRepository, authManager)

        viewModel.isLoading.test {
            assertThat(awaitItem()).isFalse()
            currentUserIdFlow.value = testUser.id
            assertThat(awaitItem()).isTrue()
            assertThat(awaitItem()).isFalse()
        }

        assertThat(viewModel.user.value).isEqualTo(testUser)
        verify(forumRepository).getUser(testUser.id)
    }

    // Тест-кейс 2: Сохранение изменений только имени пользователя
    @Test
    fun `onSaveChanges updates username only`() = runTest {
        initializeViewModelWithUser()
        val newUsername = "newTester"
        val updatedUser = testUser.copy(username = newUsername)
        whenever(forumRepository.updateUser(updatedUser)).thenReturn(true)

        viewModel.onSaveChanges(newUsername, "")
        // <<< ДОБАВЛЕНО: Ждем завершения корутины сохранения
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()

        verify(forumRepository).updateUser(updatedUser)
        verify(forumRepository, never()).updatePassword(any(), any())
        assertThat(viewModel.user.value).isEqualTo(updatedUser)
    }

    // Тест-кейс 3: Сохранение изменений только пароля
    @Test
    fun `onSaveChanges updates password only`() = runTest {
        initializeViewModelWithUser()
        val newPassword = "newPassword123"

        viewModel.onSaveChanges(testUser.username, newPassword)
        // <<< ДОБАВЛЕНО: Ждем завершения корутины сохранения
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()

        verify(forumRepository, never()).updateUser(any())
        verify(forumRepository).updatePassword(testUser.id, newPassword)
        assertThat(viewModel.user.value).isEqualTo(testUser)
    }

    // Тест-кейс 4: Сохранение изменений имени и пароля
    @Test
    fun `onSaveChanges updates both username and password`() = runTest {
        initializeViewModelWithUser()
        val newUsername = "newTester"
        val newPassword = "newPassword123"
        val updatedUser = testUser.copy(username = newUsername)
        whenever(forumRepository.updateUser(updatedUser)).thenReturn(true)

        viewModel.onSaveChanges(newUsername, newPassword)
        // <<< ДОБАВЛЕНО: Ждем завершения корутины сохранения
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()

        verify(forumRepository).updateUser(updatedUser)
        verify(forumRepository).updatePassword(testUser.id, newPassword)
        assertThat(viewModel.user.value).isEqualTo(updatedUser)
    }

    // Тест-кейс 5: Выход из системы
    @Test
    fun `logout calls authManager and clears user state`() = runTest {
        initializeViewModelWithUser()
        val onLogoutCallback: () -> Unit = mock()

        assertThat(viewModel.user.value).isNotNull()

        viewModel.logout(onLogoutCallback)

        verify(authManager).logout()
        verify(onLogoutCallback).invoke()
        assertThat(viewModel.user.value).isNull()
    }
}