package com.example.hyperlocal_forum.ui.topics

import android.location.Location
import android.util.Log
import com.example.hyperlocal_forum.MainCoroutineRule
import com.example.hyperlocal_forum.data.ForumRepository
import com.example.hyperlocal_forum.data.GeoCoordinates
import com.example.hyperlocal_forum.data.models.firestore.Topic
import com.example.hyperlocal_forum.utils.ConnectivityObserver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@ExperimentalCoroutinesApi
class TopicsViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @RelaxedMockK
    private lateinit var forumRepository: ForumRepository

    @RelaxedMockK
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @RelaxedMockK
    private lateinit var connectivityObserver: ConnectivityObserver

    @RelaxedMockK
    private lateinit var locationTask: Task<Location>

    @RelaxedMockK
    private lateinit var mockLocation: Location

    private lateinit var viewModel: TopicsViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        // **ИСПРАВЛЕНИЕ: Добавляем мок для Log.w(String, String)**
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<Throwable>()) } returns 0 // На всякий случай для других перегрузок


        every { mockLocation.latitude } returns 55.7558
        every { mockLocation.longitude } returns 37.6173
        every { fusedLocationClient.lastLocation } returns locationTask

        every { locationTask.addOnSuccessListener(any()) } answers {
            val listener = firstArg<OnSuccessListener<Location?>>()
            listener.onSuccess(mockLocation)
            locationTask
        }
        every { locationTask.addOnFailureListener(any()) } returns locationTask

        viewModel = TopicsViewModel(forumRepository, fusedLocationClient, connectivityObserver)
    }

    /**
     * Тест-кейс 1: Первоначальная загрузка всех топиков
     */
    @Test
    fun `refreshTopics with network loads all topics`() = runTest {
        val fakeTopics = listOf(Topic(id = "1", title = "Topic 1", userId = "u1", location = GeoCoordinates(1.0, 1.0), content = "...", timestamp = Timestamp(Date())))
        coEvery { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { forumRepository.checkServerAvailability() } returns true
        coEvery { forumRepository.getAllTopics() } returns flowOf(fakeTopics)

        viewModel.refreshTopics()
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertEquals(null, viewModel.errorMessage.value)
        assertEquals(fakeTopics, viewModel.topics.value)
        coVerify { forumRepository.getAllTopics() }
    }

    /**
     * Тест-кейс 2: Переключение на фильтр "Рядом"
     */
    @Test
    fun `switchToNearbyFilter with location permission loads nearby topics`() = runTest {
        val nearbyTopics = listOf(Topic(id = "nearby1", title = "Nearby Topic", userId = "u2", location = GeoCoordinates(1.1, 1.1), content = "...", timestamp = Timestamp(Date())))
        coEvery { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { forumRepository.checkServerAvailability() } returns true
        coEvery { forumRepository.findNearbyTopics(any(), any()) } returns flowOf(nearbyTopics)

        viewModel.switchToNearbyFilter()
        advanceUntilIdle()

        assertTrue(viewModel.showNearbyOnly.value)
        assertFalse(viewModel.isLoading.value)
        assertNotNull(viewModel.userLocation.value)
        assertEquals(nearbyTopics, viewModel.topics.value)
        coVerify { fusedLocationClient.lastLocation }
        coVerify { forumRepository.findNearbyTopics(any(), any()) }
    }

    /**
     * Тест-кейс 3: Переключение на фильтр "Рядом" без доступа к локации
     */
    @Test
    fun `switchToNearbyFilter without location access shows empty list`() = runTest {
        coEvery { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { forumRepository.checkServerAvailability() } returns true

        every { locationTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Location?>>().onSuccess(null)
            locationTask
        }

        viewModel.switchToNearbyFilter()
        advanceUntilIdle()

        assertTrue(viewModel.showNearbyOnly.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.userLocation.value)
        assertTrue(viewModel.topics.value.isEmpty())
        coVerify { fusedLocationClient.lastLocation }
        coVerify(exactly = 0) { forumRepository.findNearbyTopics(any(), any()) }
    }

    /**
     * Тест-кейс 4: Изменение радиуса поиска
     */
    @Test
    fun `setSearchRadius when nearby filter is on refreshes topics`() = runTest {
        coEvery { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { forumRepository.checkServerAvailability() } returns true
        coEvery { forumRepository.findNearbyTopics(any(), any()) } returns flowOf(emptyList())
        viewModel.switchToNearbyFilter()
        advanceUntilIdle()

        clearMocks(forumRepository, fusedLocationClient, answers = false, recordedCalls = true)

        val newRadius = 10.0

        viewModel.setSearchRadius(newRadius)
        advanceUntilIdle()

        assertEquals(newRadius, viewModel.searchRadius.value, 0.0)
        coVerify { forumRepository.findNearbyTopics(any(), newRadius) }
    }

    /**
     * Тест-кейс 5: Изменение радиуса поиска при выключенном фильтре "Рядом"
     */
    @Test
    fun `setSearchRadius when nearby filter is off does not refresh topics`() = runTest {
        assertFalse(viewModel.showNearbyOnly.value)
        val newRadius = 20.0

        viewModel.setSearchRadius(newRadius)
        advanceUntilIdle()

        assertEquals(newRadius, viewModel.searchRadius.value, 0.0)
        coVerify(exactly = 0) { forumRepository.getAllTopics() }
        coVerify(exactly = 0) { forumRepository.findNearbyTopics(any(), any()) }
    }

    /**
     * Тест-кейс 6: Обновление при отсутствии интернет-соединения
     */
    @Test
    fun `refreshTopics when no internet shows connection error`() = runTest {
        coEvery { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Unavailable)

        viewModel.refreshTopics()
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertEquals("No internet connection.", viewModel.errorMessage.value)
        coVerify(exactly = 0) { forumRepository.checkServerAvailability() }
        coVerify(exactly = 0) { forumRepository.getAllTopics() }
    }

    /**
     * Тест-кейс 7: Обновление при недоступности сервера
     */
    @Test
    fun `refreshTopics when server is unavailable shows server error`() = runTest {
        coEvery { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { forumRepository.checkServerAvailability() } returns false

        viewModel.refreshTopics()
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertEquals("Server is unavailable", viewModel.errorMessage.value)
        coVerify(exactly = 1) { forumRepository.checkServerAvailability() }
        coVerify(exactly = 0) { forumRepository.getAllTopics() }
    }
}