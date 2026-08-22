package com.atk.atk_cargo.feature.cargo_entry.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atk.atk_cargo.data.model.CheckExistenceRequest
import com.atk.atk_cargo.data.model.InitialInfo
import com.atk.atk_cargo.feature.cargo_entry.data.InitialInfoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

enum class ExistenceCheckStatus { NOT_EXISTS, EXISTS, PARTIAL_MATCH }

sealed interface InitialInfoEvent {
    data class ExistenceChecked(val status: ExistenceCheckStatus) : InitialInfoEvent
    data object SubmitSucceeded : InitialInfoEvent
    data class OperationFailed(val message: String) : InitialInfoEvent
}

// این ViewModel از viewModelScope استفاده می‌کند تا فراخوانی‌های شبکه با خروج کاربر یا چرخش صفحه کنسل نشوند
class InitialInfoViewModel(
    private val repository: InitialInfoRepository
) : ViewModel() {

    private val _events = Channel<InitialInfoEvent>(Channel.BUFFERED)
    val events: Flow<InitialInfoEvent> = _events.receiveAsFlow()

    fun checkExistence(request: CheckExistenceRequest) {
        viewModelScope.launch {
            try {
                val status = repository.checkExistence(request)
                if (status != null) {
                    _events.send(InitialInfoEvent.ExistenceChecked(status))
                } else {
                    _events.send(InitialInfoEvent.OperationFailed("خطا در ارتباط با سرور"))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(InitialInfoEvent.OperationFailed("خطا در ارتباط با سرور: ${e.localizedMessage}"))
            }
        }
    }

    fun submitInitialInfo(info: InitialInfo) {
        viewModelScope.launch {
            try {
                if (repository.submitInitialInfo(info)) {
                    _events.send(InitialInfoEvent.SubmitSucceeded)
                } else {
                    _events.send(InitialInfoEvent.OperationFailed("خطا در ثبت اطلاعات"))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(InitialInfoEvent.OperationFailed("خطا در ارتباط با سرور: ${e.localizedMessage}"))
            }
        }
    }
}
