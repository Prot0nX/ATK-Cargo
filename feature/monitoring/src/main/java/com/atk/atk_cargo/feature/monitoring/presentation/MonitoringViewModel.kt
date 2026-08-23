package com.atk.atk_cargo.feature.monitoring.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atk.atk_cargo.data.model.MonitoringEvent
import com.atk.atk_cargo.data.model.MonitoringHealthStatus
import com.atk.atk_cargo.data.model.MonitoringOpenAlertCounts
import com.atk.atk_cargo.feature.monitoring.data.AcknowledgeResult
import com.atk.atk_cargo.feature.monitoring.data.MonitoringRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MonitoringUiState(
    val events: List<MonitoringEvent> = emptyList(),
    val openAlerts: MonitoringOpenAlertCounts? = null,
    val health: MonitoringHealthStatus? = null,
    val statusFilter: String = "open",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdatedAtMillis: Long? = null
)

// بازخوانی خودکار هر ۳۰ ثانیه، هم‌الگو با داشبورد وب (PHP/Monitoring/assets/app.js) — کل هدف فاز الف
// یک مدل pull-based بود، پس بازخوانی دوره‌ای اینجا هم لازم است
class MonitoringViewModel(
    private val repository: MonitoringRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonitoringUiState())
    val uiState: StateFlow<MonitoringUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null

    fun setStatusFilter(status: String) {
        if (_uiState.value.statusFilter == status) return
        _uiState.update { it.copy(statusFilter = status) }
        viewModelScope.launch { refresh() }
    }

    // فقط زمانی که صفحه در حال نمایش است باید فعال باشد؛ فراخوان‌کننده (Composable) با DisposableEffect مدیریتش می‌کند
    fun startAutoRefresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            while (isActive) {
                refresh()
                delay(REFRESH_INTERVAL_MS)
            }
        }
    }

    fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    fun refreshNow() {
        viewModelScope.launch { refresh() }
    }

    private suspend fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            val summary = repository.getSummary()
            val events = repository.getEvents(status = _uiState.value.statusFilter)
            _uiState.update {
                it.copy(
                    events = events,
                    openAlerts = summary.openAlerts,
                    health = summary.health,
                    isLoading = false,
                    lastUpdatedAtMillis = System.currentTimeMillis()
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "خطا در دریافت اطلاعات") }
        }
    }

    fun acknowledge(id: Int, onResult: (errorMessage: String?) -> Unit = {}) {
        viewModelScope.launch {
            when (val result = repository.acknowledgeEvent(id)) {
                is AcknowledgeResult.Success -> {
                    refresh()
                    onResult(null)
                }
                is AcknowledgeResult.Failure -> onResult(result.message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }

    companion object {
        private const val REFRESH_INTERVAL_MS = 30_000L
    }
}
