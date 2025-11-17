package com.example.hyperlocal_forum.ui.screens.topicdetail

import com.example.hyperlocal_forum.MainCoroutineRule
import com.example.hyperlocal_forum.data.AuthManager
import com.example.hyperlocal_forum.data.ForumRepository
import com.example.hyperlocal_forum.data.GeoCoordinates
import com.example.hyperlocal_forum.data.models.firestore.Topic
import com.example.hyperlocal_forum.data.models.firestore.TopicWithComments
import com.example.hyperlocal_forum.data.models.firestore.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class TopicDetailViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var forumRepository: ForumRepository
    private lateinit var authManager: AuthManager
    private lateinit var viewModel: TopicDetailViewModel
    private val currentUserIdFlow = MutableStateFlow<String?>(null)

    @Before
    fun setUp() {
        forumRepository = mock()
        authManager = mock()
        whenever(authManager.currentUserId).thenReturn(currentUserIdFlow)
        viewModel = TopicDetailViewModel(forumRepository, authManager)
    }

    /**
     * Тест-кейс 1: Установка ID для создания нового топика
     */
    @Test
    fun `setTopicId with 'new' should enter edit mode for new topic`() = runTest {
        viewModel.setTopicId("new", editMode = true)

        assertTrue(viewModel.isEditMode.value)
        assertTrue(viewModel.isCurrentUserOwner.value)
        assertEquals("", viewModel.editableTitle.value)
        assertEquals("", viewModel.editableContent.value)
        assertFalse(viewModel.isLoading.value)
    }

    /**
     * Тест-кейс 2: Установка ID для существующего топика
     */
    @Test
    fun `setTopicId for existing topic should load data and set owner status`() = runTest {
        val topicId = "existingId"
        val userId = "ownerId"
        val topic = Topic(id = topicId, userId = userId, title = "Title", content = "Content", location = GeoCoordinates(0.0, 0.0))
        val topicWithComments = TopicWithComments(topic, emptyList())

        whenever(forumRepository.getTopicWithComments(topicId)).thenReturn(flowOf(topicWithComments))
        whenever(forumRepository.getUser(userId)).thenReturn(User(id = userId, username = "owner"))
        currentUserIdFlow.value = userId

        viewModel.setTopicId(topicId)
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertNotNull(viewModel.topicDetailState.value)
        assertEquals("Title", viewModel.topicDetailState.value?.topicWithComments?.topic?.title)
        assertTrue(viewModel.isCurrentUserOwner.value)
        assertFalse(viewModel.isEditMode.value)
    }

    /**
     * Тест-кейс 3: Успешное сохранение нового топика
     */
    @Test
    fun `saveTopic for new topic with location should succeed`() = runTest {
        val newTopicId = "newTopic123"
        val onTopicSavedCallback = mock<(String) -> Unit>()
        currentUserIdFlow.value = "testUser"

        viewModel.setTopicId("new", editMode = true)
        viewModel.onTitleChange("New Title")
        viewModel.onContentChange("New Content")
        viewModel.setLocation(10.0, 10.0)
        whenever(forumRepository.createTopic(any())).thenReturn(newTopicId)

        viewModel.saveTopic(onTopicSavedCallback)

        // Выполняем все ожидающие корутины
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()

        // Проверяем результат ПОСЛЕ выполнения корутины
        verify(forumRepository).createTopic(any())
        verify(onTopicSavedCallback).invoke(newTopicId)
        assertFalse(viewModel.isSaving.value)
        assertFalse(viewModel.isEditMode.value)
    }

    /**
     * Тест-кейс 4: Попытка сохранить новый топик без локации
     */
    @Test
    fun `saveTopic for new topic without location should fail`() = runTest {
        val onTopicSavedCallback = mock<(String) -> Unit>()
        currentUserIdFlow.value = "testUser"

        viewModel.setTopicId("new", editMode = true)
        viewModel.onTitleChange("New Title")
        viewModel.onContentChange("New Content")

        viewModel.saveTopic(onTopicSavedCallback)
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()

        verify(forumRepository, never()).createTopic(any())
        assertEquals("Location access is required to create a topic.", viewModel.errorMessage.value)
        assertFalse(viewModel.isSaving.value)
    }

    /**
     * Тест-кейс 5: Успешное обновление существующего топика
     */
    @Test
    fun `saveTopic for existing topic should update content`() = runTest {
        val topicId = "existingId"
        val userId = "ownerId"
        val onTopicSavedCallback = mock<(String) -> Unit>()
        val topic = Topic(id = topicId, userId = userId, title = "Original Title", content = "Original Content", location = GeoCoordinates(0.0, 0.0))
        val topicWithComments = TopicWithComments(topic, emptyList())

        whenever(forumRepository.getTopicWithComments(topicId)).thenReturn(flowOf(topicWithComments))
        whenever(forumRepository.getUser(userId)).thenReturn(User(id = userId, username = "owner"))
        currentUserIdFlow.value = userId

        viewModel.setTopicId(topicId)
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleEditMode()
        viewModel.onContentChange("Updated Content")

        viewModel.saveTopic(onTopicSavedCallback)
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()

        verify(forumRepository).updateTopicContent(topicId, "Updated Content")
        verify(onTopicSavedCallback).invoke(topicId)
        assertFalse(viewModel.isSaving.value)
        assertFalse(viewModel.isEditMode.value)
    }

    /**
     * Тест-кейс 6: Удаление топика владельцем
     */
    @Test
    fun `deleteTopic as owner should succeed`() = runTest {
        val topicId = "topicToDelete"
        val userId = "ownerId"
        val onTopicDeletedCallback = mock<() -> Unit>()
        val topic = Topic(id = topicId, userId = userId, title = "Title", content = "Content", location = GeoCoordinates(0.0, 0.0))
        val topicWithComments = TopicWithComments(topic, emptyList())

        whenever(forumRepository.getTopicWithComments(topicId)).thenReturn(flowOf(topicWithComments))
        whenever(forumRepository.getUser(userId)).thenReturn(User(id = userId, username = "owner"))
        currentUserIdFlow.value = userId

        viewModel.setTopicId(topicId)
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteTopic(onTopicDeletedCallback)
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()

        verify(forumRepository).deleteTopic(topicId)
        verify(onTopicDeletedCallback).invoke()
    }

    /**
     * Тест-кейс 7: Попытка удаления топика не-владельцем
     */
    @Test
    fun `deleteTopic as non-owner should not call repository`() = runTest {
        val topicId = "topicToDelete"
        val onTopicDeletedCallback = mock<() -> Unit>()
        val topic = Topic(id = topicId, userId = "ownerId", title = "Title", content = "Content", location = GeoCoordinates(0.0, 0.0))
        val topicWithComments = TopicWithComments(topic, emptyList())

        whenever(forumRepository.getTopicWithComments(topicId)).thenReturn(flowOf(topicWithComments))
        whenever(forumRepository.getUser("ownerId")).thenReturn(User(id = "ownerId", username = "owner"))
        currentUserIdFlow.value = "notOwnerId"

        viewModel.setTopicId(topicId)
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteTopic(onTopicDeletedCallback)
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()

        verify(forumRepository, never()).deleteTopic(any())
        verify(onTopicDeletedCallback, never()).invoke()
    }

    /**
     * Тест-кейс 8: Добавление комментария
     */
    @Test
    fun `saveComment with content when logged in should succeed`() = runTest {
        val topicId = "topicId"
        val userId = "commenterId"
        val username = "commenter"
        val commentContent = "This is a new comment"
        val topic = Topic(id = topicId, userId = "ownerId", title = "Title", content = "Content", location = GeoCoordinates(0.0, 0.0))
        val topicWithComments = TopicWithComments(topic, emptyList())

        whenever(forumRepository.getTopicWithComments(topicId)).thenReturn(flowOf(topicWithComments))
        whenever(forumRepository.getUser(any())).thenReturn(User(id = userId, username = username))
        currentUserIdFlow.value = userId

        viewModel.setTopicId(topicId)
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()
        viewModel.onNewCommentContentChange(commentContent)
        viewModel.toggleCommentInput()

        viewModel.saveComment()
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()

        verify(forumRepository).addComment(argThat { comment ->
            comment.topicId == topicId && comment.userId == userId && comment.content == commentContent
        })
        assertEquals("", viewModel.newCommentContent.value)
        assertFalse(viewModel.showCommentInput.value)
    }
}