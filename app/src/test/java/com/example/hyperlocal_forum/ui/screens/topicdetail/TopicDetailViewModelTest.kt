package com.example.hyperlocal_forum.ui.screens.topicdetail

import app.cash.turbine.test
import com.example.hyperlocal_forum.data.AuthManager
import com.example.hyperlocal_forum.data.ForumRepository
import com.example.hyperlocal_forum.data.GeoCoordinates
import com.example.hyperlocal_forum.data.models.firestore.Topic
import com.example.hyperlocal_forum.data.models.firestore.TopicWithComments
import com.example.hyperlocal_forum.data.models.firestore.User
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class TopicDetailViewModelTest {

    private lateinit var forumRepository: ForumRepository
    private lateinit var authManager: AuthManager
    private lateinit var viewModel: TopicDetailViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val ownerId = "ownerUserId"
    private val currentUserIdFlow = MutableStateFlow<String?>(ownerId)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        forumRepository = mockk(relaxed = true)
        authManager = mockk(relaxed = true)

        every { authManager.currentUserId } returns currentUserIdFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `TC-D-1 setTopicId with 'new' should enter edit mode for new topic`() {
        // Act
        viewModel = TopicDetailViewModel(forumRepository, authManager)
        viewModel.setTopicId("new")

        // Assert
        assertTrue(viewModel.isEditMode.value)
        assertTrue(viewModel.isCurrentUserOwner.value)
        assertEquals("", viewModel.editableTitle.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.topicDetailState.value)
    }

    @Test
    fun `TC-D-3 saveTopic for a new topic should call createTopic`() = runTest {
        // Arrange
        val newTopicId = "new-topic-123"
        val onTopicSaved: (String) -> Unit = mockk(relaxed = true)
        coEvery { forumRepository.createTopic(any()) } returns newTopicId

        viewModel = TopicDetailViewModel(forumRepository, authManager)
        viewModel.setTopicId("new")
        viewModel.setLocation(10.0, 10.0) // Provide location
        viewModel.onTitleChange("New Title")
        viewModel.onContentChange("New Content")

        // Act
        viewModel.saveTopic(onTopicSaved)

        // Assert
        coVerify(exactly = 1) { forumRepository.createTopic(any()) }
        verify { onTopicSaved(newTopicId) }
        assertFalse(viewModel.isEditMode.value)
    }

    @Test
    fun `TC-D-4 saveTopic for a new topic without location should show error`() = runTest {
        // Arrange
        val onTopicSaved: (String) -> Unit = mockk(relaxed = true)
        viewModel = TopicDetailViewModel(forumRepository, authManager)
        viewModel.setTopicId("new")
        viewModel.onTitleChange("New Title")
        viewModel.onContentChange("New Content")
        // No location is set

        // Act
        viewModel.saveTopic(onTopicSaved)

        // Assert
        coVerify(exactly = 0) { forumRepository.createTopic(any()) }
        assertEquals("Location access is required to create a topic.", viewModel.errorMessage.value)
    }

    @Test
    fun `TC-D-6 deleteTopic by owner should call repository delete`() = runTest {
        // Arrange
        val topicId = "topic-to-delete"
        val topic = mockk<Topic> {
            every { userId } returns ownerId
        }
        val topicWithComments = TopicWithComments(topic, emptyList())
        every { forumRepository.getTopicWithComments(topicId) } returns flowOf(topicWithComments)
        coEvery { forumRepository.getUser(ownerId) } returns mockk()
        coEvery { forumRepository.deleteTopic(topicId) } just Runs
        val onTopicDeleted: () -> Unit = mockk(relaxed = true)

        viewModel = TopicDetailViewModel(forumRepository, authManager)
        viewModel.setTopicId(topicId)

        // Act
        viewModel.deleteTopic(onTopicDeleted)

        // Assert
        coVerify(exactly = 1) { forumRepository.deleteTopic(topicId) }
        verify(exactly = 1) { onTopicDeleted() }
    }
}