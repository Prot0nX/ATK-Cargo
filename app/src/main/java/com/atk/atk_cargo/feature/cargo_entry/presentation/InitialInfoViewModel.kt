package com.atk.atk_cargo.feature.cargo_entry.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atk.atk_cargo.api.ApiServiceV2
import com.atk.atk_cargo.api.CheckExistenceRequest
import com.atk.atk_cargo.api.InitialInfo
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

/**
 * دو تماس شبکه‌ی InitialInfoScreen (checkExistence، saveInitialInfo) قبلاً
 * داخل rememberCoroutineScope() اجرا می‌شدند — با خروج کاربر از صفحه (یا
 * recomposition ساختاری) کنسل می‌شدند، حتی برای saveInitialInfo که یک
 * عملیات نوشتن است (DEEP_CODE_REVIEW.md Top20 #5). viewModelScope در برابر
 * ناوبری/چرخش صفحه مقاوم است.
 */
class InitialInfoViewModel(
    private val apiServiceV2: ApiServiceV2
) : ViewModel() {

    private val _events = Channel<InitialInfoEvent>(Channel.BUFFERED)
    val events: Flow<InitialInfoEvent> = _events.receiveAsFlow()

    fun checkExistence(request: CheckExistenceRequest) {
        viewModelScope.launch {
            try {
                val response = apiServiceV2.checkExistence(request)
                val status = when (response.body()?.status) {
                    "not_exists" -> ExistenceCheckStatus.NOT_EXISTS
                    "exists" -> ExistenceCheckStatus.EXISTS
                    "partial_match" -> ExistenceCheckStatus.PARTIAL_MATCH
                    else -> null
                }
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
                val response = apiServiceV2.saveInitialInfo(info)
                if (response.isSuccessful) {
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
