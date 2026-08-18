package com.atk.atk_cargo.feature.chat.presentation

import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atk.atk_cargo.api.ChatViewModel
import com.atk.atk_cargo.api.ChatViewModelFactory
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.data.db.ChatMessageEntity
import com.atk.atk_cargo.feature.chat.domain.ChatUiItem
import com.atk.atk_cargo.feature.chat.domain.getChatBackgroundColor
import com.atk.atk_cargo.feature.chat.domain.getDateHeaderColor
import com.atk.atk_cargo.feature.chat.presentation.components.ChatSettingsDialogEnhanced
import com.atk.atk_cargo.feature.chat.presentation.components.ChatTopBar
import com.atk.atk_cargo.feature.chat.presentation.components.ErrorView
import com.atk.atk_cargo.feature.chat.presentation.components.MessageBubble
import com.atk.atk_cargo.feature.chat.presentation.components.MessageInputArea
import com.atk.atk_cargo.utils.JalaliDateUtils
import kotlinx.coroutines.awaitCancellation

@Composable
fun ChatScreen(
    userPreferencesManager: UserPreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(context, userPreferencesManager)
    )
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isSending by viewModel.isSending.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
    
    LaunchedEffect(Unit) {
        notificationManager?.cancelAll()
        viewModel.markAllMessagesAsRead()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.startPolling()
            try {
                awaitCancellation()
            } finally {
                viewModel.pausePolling()
            }
        }
    }

    val shipsData by viewModel.ships.collectAsStateWithLifecycle()
    val shipQuotas by viewModel.shipQuotas.collectAsStateWithLifecycle()
    val fontSize by viewModel.chatFontSize.collectAsStateWithLifecycle()
    val myBubbleColorLong by viewModel.chatMyBubbleColor.collectAsStateWithLifecycle()
    val otherBubbleColorLong by viewModel.chatOtherBubbleColor.collectAsStateWithLifecycle()
    val backgroundId by viewModel.chatBackgroundId.collectAsStateWithLifecycle()
    val bubbleShapeId by viewModel.chatBubbleShape.collectAsStateWithLifecycle()
    
    val myBubbleColor = Color(myBubbleColorLong)
    val otherBubbleColor = Color(otherBubbleColorLong)
    val backgroundColor = getChatBackgroundColor(backgroundId)
    
    val myBubbleShape = when (bubbleShapeId) {
        0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        1 -> RoundedCornerShape(4.dp)
        2 -> RoundedCornerShape(topStart = 20.dp, topEnd = 2.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
        else -> RoundedCornerShape(16.dp)
    }
    val otherBubbleShape = when (bubbleShapeId) {
        0 -> RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        1 -> RoundedCornerShape(4.dp)
        2 -> RoundedCornerShape(topStart = 2.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
        else -> RoundedCornerShape(16.dp)
    }
    var showSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    val chatItems = remember(messages) {
        processMessagesForDisplay(messages)
    }
    val listState = rememberLazyListState()
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            viewModel.markAllMessagesAsRead()
        }
    }

    val isAtEnd by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 10 && lastVisibleIndex >= totalItems - 5
        }
    }

    LaunchedEffect(isAtEnd) {
        if (isAtEnd && !isLoading) {
            viewModel.loadOlderMessages()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                ChatTopBar(
                    onBackClick = onBackClick,
                    onRefreshClick = { viewModel.refreshMessages() },
                    onSettingsClick = { showSettingsDialog = true },
                    isLoading = isLoading
                )
            },
            containerColor = backgroundColor
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (messages.isEmpty() && !isLoading) {
                        if (error != null) {
                            ErrorView(
                                message = error ?: "خطایی رخ داده است",
                                onRetry = { viewModel.refreshMessages() }
                            )
                        } else {
                            EmptyState()
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            reverseLayout = true,
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(chatItems, key = { it.id }) { item ->
                                when (item) {
                                    is ChatUiItem.Header -> DateHeader(item.date)
                                    is ChatUiItem.Message -> MessageBubble(
                                        message = item.entity,
                                        viewModel = viewModel,
                                        fontSize = fontSize,
                                        myBubbleColor = myBubbleColor,
                                        otherBubbleColor = otherBubbleColor,
                                        myShape = myBubbleShape,
                                        otherShape = otherBubbleShape,
                                        users = users
                                    )
                                }
                            }
                            
                            if (isLoading && messages.isNotEmpty()) {
                                item { 
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp)) 
                                    }
                                }
                            }
                        }
                    }
                }

                MessageInputArea(
                    isSending = isSending,
                    myBubbleColor = myBubbleColor,
                    onSendMessage = { viewModel.sendMessage(it) },
                    users = users,
                    shipsData = shipsData,
                    shipQuotas = shipQuotas,
                    onShipSelected = { viewModel.loadShipQuotas(it.name) },
                    onQuotaDialogDismiss = { viewModel.clearShipQuotas() }
                )
            }
        }
    }

    if (showSettingsDialog) {
        ChatSettingsDialogEnhanced(
            currentFontSize = fontSize,
            currentMyColor = myBubbleColorLong,
            currentOtherColor = otherBubbleColorLong,
            currentBackgroundId = backgroundId,
            currentBubbleShape = bubbleShapeId,
            onDismiss = { showSettingsDialog = false },
            onSave = { fs, mc, oc, bg, sh ->
                viewModel.saveChatSettings(fs, mc, oc, bg, sh)
                showSettingsDialog = false
            }
        )
    }
}

@Composable
fun DateHeader(date: String) {
    val isDark = isSystemInDarkTheme()
    val headerColor = getDateHeaderColor(isDark)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = headerColor)
        ) {
            Text(
                text = date,
                fontSize = 11.sp,
                color = if (isDark) Color.LightGray else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "هنوز پیامی ارسال نشده است.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun processMessagesForDisplay(messages: List<ChatMessageEntity>): List<ChatUiItem> {
    val result = mutableListOf<ChatUiItem>()

    messages.forEachIndexed { index, message ->
        val datePart = message.timestamp.split(" ")[0]
        val jalaliDate = JalaliDateUtils.formatDate(datePart)
        
        result.add(ChatUiItem.Message(message))
        
        val nextMessage = messages.getOrNull(index + 1)
        val nextDatePart = nextMessage?.timestamp?.split(" ")?.get(0)
        
        if (nextDatePart != datePart) {
            result.add(ChatUiItem.Header(jalaliDate, "header_$datePart"))
        }
    }
    
    return result
}
