package com.atk.atk_cargo.feature.cargo.domain

import com.atk.atk_cargo.data.model.MessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.LinkedList
import java.util.Queue

class CargoSnackbarQueue {
    private val _pendingMessages = MutableStateFlow<Queue<Pair<String, MessageType>>>(LinkedList())
    private val _isShowingMessage = MutableStateFlow(false)
    
    private val _resultMessage = MutableStateFlow("")
    val resultMessage: StateFlow<String> = _resultMessage.asStateFlow()
    
    private val _showAnimatedMessage = MutableStateFlow(false)
    val showAnimatedMessage: StateFlow<Boolean> = _showAnimatedMessage.asStateFlow()
    
    private val _messageType = MutableStateFlow(MessageType.SUCCESS)
    val messageType: StateFlow<MessageType> = _messageType.asStateFlow()

    val pendingMessagesFlow: StateFlow<Queue<Pair<String, MessageType>>> = _pendingMessages.asStateFlow()
    val isShowingMessage: StateFlow<Boolean> = _isShowingMessage.asStateFlow()

    fun addMessageToQueue(message: String, type: MessageType) {
        val currentQueue = _pendingMessages.value
        currentQueue.offer(message to type)
        _pendingMessages.value = currentQueue
        
        if (!_isShowingMessage.value) {
            showNextMessage()
        }
    }

    fun showNextMessage() {
        val messageQueue = _pendingMessages.value
        if (messageQueue.isNotEmpty()) {
            val (message, type) = messageQueue.poll()!!
            _resultMessage.value = message
            _showAnimatedMessage.value = true
            _messageType.value = type
            _isShowingMessage.value = true
        }
    }

    fun dismissMessage() {
        _showAnimatedMessage.value = false
        _resultMessage.value = ""
        _isShowingMessage.value = false
        if (_pendingMessages.value.isNotEmpty()) {
            showNextMessage()
        }
    }

    fun showMessage(message: String, type: MessageType) {
        _resultMessage.value = message
        _showAnimatedMessage.value = true
        _messageType.value = type
    }
}
