package com.atk.atk_cargo.api

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atk.atk_cargo.data.db.AppDatabase
import com.atk.atk_cargo.data.db.ChatMessageEntity
import com.atk.atk_cargo.data.repository.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    // جریان پیام‌ها از دیتابیس لوکال
    val messages: StateFlow<List<ChatMessageEntity>> = repository.messages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatFontSize = userPreferencesManager.chatFontSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 14)
    
    val chatMyBubbleColor = userPreferencesManager.chatMyBubbleColor
         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0xFF1E88E5)

    val chatOtherBubbleColor = userPreferencesManager.chatOtherBubbleColor
         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0xFFFFFFFF)

    val chatBackgroundId = userPreferencesManager.chatBackgroundId
         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val chatBubbleShape = userPreferencesManager.chatBubbleShape
         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private var isPolling = false

    companion object {
        private const val TAG = "ChatViewModel"
        private const val POLLING_INTERVAL = 5000L // 5 ثانیه (کمی بیشتر برای کاهش بار سرور)
    }

    // لیست کاربران برای تگ کردن
    private val _users = MutableStateFlow<List<com.atk.atk_cargo.api.User>>(emptyList())
    val users: StateFlow<List<com.atk.atk_cargo.api.User>> = _users.asStateFlow()

    // لیست کشتی‌ها برای ضمیمه کردن اطلاعات
    private val _ships = MutableStateFlow<com.atk.atk_cargo.api.ShipsData?>(null)
    val ships: StateFlow<com.atk.atk_cargo.api.ShipsData?> = _ships.asStateFlow()

    // لیست کوتاژهای کشتی انتخاب شده
    private val _shipQuotas = MutableStateFlow<List<com.atk.atk_cargo.api.Quota>>(emptyList())
    val shipQuotas: StateFlow<List<com.atk.atk_cargo.api.Quota>> = _shipQuotas.asStateFlow()

    init {
        refreshMessages()
        startPolling()
        loadUsers()
        loadShips()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            try {
                // فقط کاربران با سطح دسترسی admin
                val userList = repository.getAllUsers().filter { it.userType == "admin" }
                _users.value = userList
            } catch (e: Exception) {
                Log.e(TAG, "Error loading users", e)
            }
        }
    }

    private fun loadShips() {
        viewModelScope.launch {
            try {
                val shipsData = repository.getShipsList()
                _ships.value = shipsData
            } catch (e: Exception) {
                Log.e(TAG, "Error loading ships", e)
            }
        }
    }

    fun loadShipQuotas(shipName: String) {
        viewModelScope.launch {
            try {
                val quotas = repository.getShipQuotas(shipName)
                _shipQuotas.value = quotas
            } catch (e: Exception) {
                Log.e(TAG, "Error loading ship quotas", e)
                _shipQuotas.value = emptyList()
            }
        }
    }

    fun clearShipQuotas() {
        _shipQuotas.value = emptyList()
    }

    fun saveChatSettings(fontSize: Int, myColor: Long, otherColor: Long, backgroundId: Int, bubbleShape: Int) {
        viewModelScope.launch {
            userPreferencesManager.saveChatSettings(fontSize, myColor, otherColor, backgroundId, bubbleShape)
        }
    }

    fun startPolling() {
        if (isPolling) return
        isPolling = true
        viewModelScope.launch {
            while (isPolling) {
                delay(POLLING_INTERVAL)
                // فقط رفرش بی‌صدا انجام می‌دهیم
                try {
                    Log.d("ATK_CHAT_DEBUG", "Polling: Refreshing messages...")
                    repository.refreshMessages()
                } catch (e: Exception) {
                    Log.e(TAG, "Polling error", e)
                }
            }
        }
    }

    fun pausePolling() {
        isPolling = false
    }

    fun refreshMessages() {
        viewModelScope.launch {
            try {
                // _isLoading.value = true // برای پولینگ شاید لودینگ لازم نباشد، فقط برای رفرش دستی
                repository.refreshMessages()
            } catch (e: Exception) {
                _error.value = "خطا در بروزرسانی پیام‌ها"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadOlderMessages() {
        viewModelScope.launch {
            val currentMessages = messages.value
            if (currentMessages.isEmpty()) return@launch

            val oldestDetails = currentMessages.lastOrNull() // چون ترتیب DESC است، یا ASC? 
            // در DAO: ORDER BY id DESC (جدیدترین اول)
            // پس قدیمی‌ترین پیام در انتهای لیست است 
            // اما در UI ما معمولا ReverseLayout داریم یا لیست را معکوس نمایش می‌دهیم.
            // بیایید فرض کنیم لیست DAO نزولی است (ID: 100, 99, 98...)
            // پس قدیمی‌ترین ID، آخرین آیتم لیست است.
            
            val oldestId = currentMessages.minOfOrNull { it.id } ?: return@launch

            try {
                _isLoading.value = true
                Log.d("ATK_CHAT_DEBUG", "ViewModel: Loading older messages for ID < $oldestId")
                repository.loadOlderMessages(oldestId)
            } catch (e: Exception) {
                _error.value = "خطا در دریافت پیام‌های قدیمی"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(messageText: String) {
        if (messageText.isBlank()) return

        viewModelScope.launch {
            try {
                _isSending.value = true
                Log.d("ATK_CHAT_DEBUG", "ViewModel: Sending message...")
                repository.sendMessage(messageText)
                    .onSuccess {
                        Log.d("ATK_CHAT_DEBUG", "ViewModel: Message send SUCCESS")
                    }
                    .onFailure {
                        Log.e("ATK_CHAT_DEBUG", "ViewModel: Message send FAILED - ${it.message}")
                        _error.value = it.message
                    }
            } finally {
                _isSending.value = false
            }
        }
    }

    fun editMessage(messageId: Int, newMessage: String) {
        viewModelScope.launch {
            try {
                repository.editMessage(messageId, newMessage).onFailure {
                    _error.value = it.message
                }
            } catch (e: Exception) {
                _error.value = "خطا در ویرایش پیام"
            }
        }
    }

    fun deleteMessage(messageId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteMessage(messageId).onFailure {
                    _error.value = it.message
                }
            } catch (e: Exception) {
                _error.value = "خطا در حذف پیام"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun markAllMessagesAsRead() {
        viewModelScope.launch {
            repository.markAllMessagesAsRead()
        }
    }

    override fun onCleared() {
        super.onCleared()
        isPolling = false
    }
}

class ChatViewModelFactory(
    private val context: Context,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            val database = AppDatabase.getDatabase(context)
            val repository = ChatRepository(
                database.chatDao(),
                RetrofitClient.apiService,
                userPreferencesManager
            )
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository, userPreferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
