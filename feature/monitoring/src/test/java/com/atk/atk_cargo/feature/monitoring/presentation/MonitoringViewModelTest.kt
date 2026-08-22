package com.atk.atk_cargo.feature.monitoring.presentation

import com.atk.atk_cargo.data.model.MonitoringEvent
import com.atk.atk_cargo.data.model.MonitoringHealthStatus
import com.atk.atk_cargo.data.model.MonitoringOpenAlertCounts
import com.atk.atk_cargo.data.model.MonitoringSummaryResponse
import com.atk.atk_cargo.feature.monitoring.data.AcknowledgeResult
import com.atk.atk_cargo.feature.monitoring.data.MonitoringRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MonitoringViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: MonitoringRepository
    private lateinit var viewModel: MonitoringViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = mockk(relaxed = true)
        viewModel = MonitoringViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun summary(healthy: Boolean = true) = MonitoringSummaryResponse(
        success = true,
        openAlerts = MonitoringOpenAlertCounts(openTotal = 3, openCritical = 1, openWarning = 1, openInfo = 1),
        health = MonitoringHealthStatus(healthy = healthy, status = emptyMap(), missingTables = emptyList(), checkedAt = "2026-08-22 10:00:00")
    )

    private fun event(id: Int = 1, acknowledgedAt: String? = null) = MonitoringEvent(
        id = id,
        eventType = "HEALTH_CHECK_FAILED",
        severity = "critical",
        message = "پیام تست",
        source = "health_check",
        dedupeKey = "HEALTH_CHECK_FAILED",
        createdAt = "2026-08-22 10:00:00",
        acknowledgedAt = acknowledgedAt,
        acknowledgedBy = null
    )

    @Test
    fun `refreshNow - populates events and summary on success`() = runTest(dispatcher) {
        coEvery { repository.getSummary() } returns summary()
        coEvery { repository.getEvents(status = "open", limit = 100, beforeId = null) } returns listOf(event())

        viewModel.refreshNow()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.events.size)
        assertEquals(3, state.openAlerts?.openTotal)
        assertEquals(true, state.health?.healthy)
        assertEquals(false, state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `refreshNow - repository failure surfaces error, not crash`() = runTest(dispatcher) {
        coEvery { repository.getSummary() } throws RuntimeException("network down")

        viewModel.refreshNow()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(true, state.events.isEmpty())
        assertEquals("network down", state.errorMessage)
    }

    @Test
    fun `setStatusFilter - reloads events with new status`() = runTest(dispatcher) {
        coEvery { repository.getSummary() } returns summary()
        coEvery { repository.getEvents(status = any(), limit = 100, beforeId = null) } returns emptyList()

        viewModel.setStatusFilter("acknowledged")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("acknowledged", viewModel.uiState.value.statusFilter)
        coVerify { repository.getEvents(status = "acknowledged", limit = 100, beforeId = null) }
    }

    @Test
    fun `acknowledge - success triggers refresh and calls onResult with null`() = runTest(dispatcher) {
        coEvery { repository.acknowledgeEvent(1) } returns AcknowledgeResult.Success
        coEvery { repository.getSummary() } returns summary()
        coEvery { repository.getEvents(status = "open", limit = 100, beforeId = null) } returns listOf(event(acknowledgedAt = "2026-08-22 10:05:00"))

        var receivedError: String? = "not-called"
        viewModel.acknowledge(1) { receivedError = it }
        dispatcher.scheduler.advanceUntilIdle()

        assertNull(receivedError)
        coVerify { repository.getSummary() }
    }

    @Test
    fun `acknowledge - failure calls onResult with server message, no refresh`() = runTest(dispatcher) {
        coEvery { repository.acknowledgeEvent(1) } returns AcknowledgeResult.Failure("این رویداد قبلاً تایید شده است")

        var receivedError: String? = null
        viewModel.acknowledge(1) { receivedError = it }
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("این رویداد قبلاً تایید شده است", receivedError)
        coVerify(exactly = 0) { repository.getSummary() }
    }
}
