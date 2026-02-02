package com.atk.atk_cargo

import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atk.atk_cargo.api.ChatViewModel
import com.atk.atk_cargo.api.ChatViewModelFactory
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.data.ColorWheel
import com.atk.atk_cargo.data.db.ChatMessageEntity
import com.atk.atk_cargo.utils.JalaliDateUtils

// رنگ‌های سفارشی مطابق طراحی
private val ChatBackgroundLight = Color(0xFFF8FAFC)
private val DateHeaderColor = Color(0xFFE2E8F0)
private val TextColorPrimary = Color(0xFF1E293B)
private val TextColorSecondary = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userPreferencesManager: UserPreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(context, userPreferencesManager)
    )
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSending by viewModel.isSending.collectAsState()

    // اطلاعات برای قابلیت‌های هوشمند
    val users by viewModel.users.collectAsState()

    // Clear notifications when screen is opened
    val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
    LaunchedEffect(Unit) {
        notificationManager?.cancelAll()
    }
    val shipsData by viewModel.ships.collectAsState()
    val shipQuotas by viewModel.shipQuotas.collectAsState()

    // تنظیمات شخصی‌سازی
    // تنظیمات شخصی‌سازی
    val fontSize by viewModel.chatFontSize.collectAsState()
    val myBubbleColorLong by viewModel.chatMyBubbleColor.collectAsState()
    val otherBubbleColorLong by viewModel.chatOtherBubbleColor.collectAsState()
    val backgroundId by viewModel.chatBackgroundId.collectAsState()
    val bubbleShapeId by viewModel.chatBubbleShape.collectAsState()
    
    val myBubbleColor = Color(myBubbleColorLong)
    val otherBubbleColor = Color(otherBubbleColorLong)
    
    // Background Color Logic
    val backgroundColor = when (backgroundId) {
        0 -> ChatBackgroundLight // Default
        1 -> Color(0xFFECE5DD) // WhatsApp-like
        2 -> Color(0xFF202C33) // Dark Blue / Gray
        3 -> Color(0xFF000000) // Pure Black
        else -> ChatBackgroundLight
    }
    
    // Bubble Shape Logic
    val myBubbleShape = when (bubbleShapeId) {
        0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp) // Default
        1 -> RoundedCornerShape(4.dp) // Square
        2 -> RoundedCornerShape(topStart = 20.dp, topEnd = 2.dp, bottomStart = 20.dp, bottomEnd = 20.dp) // Modern
        else -> RoundedCornerShape(16.dp)
    }
    
    val otherBubbleShape = when (bubbleShapeId) {
        0 -> RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp) // Default
        1 -> RoundedCornerShape(4.dp) // Square
        2 -> RoundedCornerShape(topStart = 2.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp) // Modern
        else -> RoundedCornerShape(16.dp)
    }

    var showSettingsDialog by remember { mutableStateOf(false) }

    // مدیریت خطاها
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    val chatItems = remember(messages) {
        val items = processMessagesForDisplay(messages)
        items
    }
    val listState = rememberLazyListState()
    
    // اسکرول خودکار به پایین (ایندکس 0 چون reverse=true) هنگام ارسال یا دریافت پیام جدید
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // Update last read message ID
    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            val maxId = messages.maxOfOrNull { it.id } ?: 0
            userPreferencesManager.saveLastReadMessageId(maxId)
        }
    }

    val isAtEnd by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 10 && lastVisibleIndex >= totalItems - 5 // وقتی به 5 تای آخر رسیدیم
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
                // لیست پیام‌ها
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
                            
                            // لودینگ پایین لیست (در واقع بالا چون reverse است)
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

                // ورودی پیام
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

    // دیالوگ تنظیمات
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        shadowElevation = 2.dp,
        color = Color.White
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "اطلاع‌رسانی و گفتگو",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextColorPrimary
                    )
                    if (isLoading) {
                        Text(
                            text = "در حال بروزرسانی...",
                            fontSize = 11.sp,
                            color = TextColorSecondary
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "بازگشت", tint = TextColorPrimary)
                }
            },
            actions = {
                IconButton(onClick = onRefreshClick) {
                    Icon(Icons.Default.Refresh, "بروزرسانی", tint = TextColorSecondary)
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, "تنظیمات", tint = TextColorSecondary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
    }
}





@Composable
fun DateHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = DateHeaderColor,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = date,
                fontSize = 12.sp,
                color = TextColorSecondary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessageEntity,
    viewModel: ChatViewModel,
    fontSize: Int,
    myBubbleColor: Color,
    otherBubbleColor: Color,
    myShape: androidx.compose.ui.graphics.Shape,
    otherShape: androidx.compose.ui.graphics.Shape,
    users: List<com.atk.atk_cargo.api.User>
) {
    val isMe = message.isSelf
    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val alignment = if (isMe) Alignment.CenterStart else Alignment.CenterEnd // Fixed alignment as requested
    
    val bubbleShape = if (isMe) myShape else otherShape
    
    // Apply personalized colors
    val finalBubbleColor = if (isMe) myBubbleColor else otherBubbleColor
    
    // Determine text color based on background brightness for readability
    val textColor = if (ColorUtils.calculateLuminance(finalBubbleColor.toArgb()) > 0.5) 
        Color.Black else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.Start else Alignment.End,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            // نمایش نام حذف شده
            if (message.isDeleted) {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "🚫 این پیام حذف شده است",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            } else {
                // کارت پیام
                Surface(
                    shape = bubbleShape,
                    color = finalBubbleColor,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .combinedClickable(
                            onClick = { showInfoDialog = true },
                            onLongClick = {
                                if (isMe) showMenu = true
                            }
                        )
                ) {


                    Column(modifier = Modifier.padding(12.dp)) {
                        
                        // Check for ship info pattern AND regular text
                        val (cleanText, shipInfo) = remember(message.message) { 
                            extractShipInfoAndText(message.message) 
                        }
                        
                        // 1. Render Regular Text (if exists)
                        if (cleanText.isNotBlank()) {
                            val displayText = remember(cleanText, users) {
                                replaceUsernamesWithFullNames(cleanText, users)
                            }
                            
                            Text(
                                text = displayText,
                                fontSize = fontSize.sp,
                                color = textColor,
                                textAlign = TextAlign.Start,
                                lineHeight = (fontSize * 1.5).sp
                            )
                            
                            if (shipInfo != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color.Black.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        // 2. Render Ship Info Card (if exists)
                        if (shipInfo != null) {
                            ShipInfoCard(shipInfo, fontSize, textColor)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // زمان و وضعیت
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val timeStr = JalaliDateUtils.formatTime(message.timestamp)
                            val metaColor = textColor.copy(alpha = 0.7f)
                            
                            Text(
                                text = timeStr,
                                fontSize = 10.sp,
                                color = metaColor
                            )
                            
                            if (message.updatedAt != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(ویرایش شده)",
                                    fontSize = 9.sp,
                                    color = metaColor
                                )
                            }

                            if (isMe) {
                                Spacer(modifier = Modifier.width(4.dp))
                                val icon = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check
                                val tint = if (message.isRead) Color(0xFF2196F3) else metaColor
                                Icon(
                                    imageVector = icon,
                                    contentDescription = if (message.isRead) "Read" else "Sent",
                                    tint = tint,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    
                    // منوی عملیات
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("ویرایش") },
                            onClick = {
                                showMenu = false
                                showEditDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("حذف", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
                
                // نام فرستنده (خارج از حباب برای پیام‌های دیگران)
                if (!isMe) {
                    val senderName = users.find { it.username == message.username }?.fullName ?: message.fullName
                    Text(
                        text = senderName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextColorSecondary,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
        }
    }

    // دیالوگ ویرایش
    if (showEditDialog) {
        EditMessageDialog(
            initialMessage = message.message,
            onDismiss = { showEditDialog = false },
            onConfirm = { newText ->
                viewModel.editMessage(message.id, newText)
                showEditDialog = false
            }
        )
    }

    // دیالوگ حذف
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("حذف پیام") },
            text = { Text("آیا از حذف این پیام اطمینان دارید؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMessage(message.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    if (showInfoDialog) {
        MessageInfoDialog(
            message = message,
            onDismiss = { showInfoDialog = false }
        )
    }
}

@Composable
fun EditMessageDialog(
    initialMessage: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialMessage) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ویرایش پیام") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank() && text != initialMessage
            ) {
                Text("ثبت تغییرات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("لغو")
            }
        }
    )
}

@Composable
fun MessageInputArea(
    isSending: Boolean,
    myBubbleColor: Color,
    onSendMessage: (String) -> Unit,
    users: List<com.atk.atk_cargo.api.User> = emptyList(),
    shipsData: com.atk.atk_cargo.api.ShipsData? = null,
    shipQuotas: List<com.atk.atk_cargo.api.Quota> = emptyList(),
    onShipSelected: (com.atk.atk_cargo.api.Ship) -> Unit = {},
    onQuotaDialogDismiss: () -> Unit = {}
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var showShipDialog by remember { mutableStateOf(false) }
    var showQuotaDialog by remember { mutableStateOf(false) }
    var showUserPopup by remember { mutableStateOf(false) }
    
    // اگر Quotas پر شد، دیالوگ کوتاژ را نمایش بده
    LaunchedEffect(shipQuotas) {
        if (shipQuotas.isNotEmpty()) {
            showShipDialog = false
            showQuotaDialog = true
        }
    }

    Surface(
        shadowElevation = 16.dp,
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column {
            // پاپ‌آپ لیست کاربران (فقط ادمین‌ها)
            if (showUserPopup && users.isNotEmpty()) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(0, -300),
                    onDismissRequest = { showUserPopup = false }
                ) {
                    Card(
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .widthIn(max = 250.dp)
                            .height(200.dp)
                    ) {
                        LazyColumn {
                            items(users) { user ->
                                Text(
                                    text = user.fullName ?: user.username,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // اضافه کردن تگ
                                            val currentText = textFieldValue.text
                                            val selectionStart = textFieldValue.selection.start
                                            val newText = StringBuilder(currentText)
                                                .insert(selectionStart, "@${user.username} ")
                                                .toString()
                                            
                                            textFieldValue = TextFieldValue(
                                                text = newText,
                                                selection = TextRange(selectionStart + user.username.length + 2)
                                            )
                                            showUserPopup = false
                                        }
                                        .padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // دکمه پیوست اطلاعات کشتی
                IconButton(onClick = { showShipDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.AddCircle,
                        contentDescription = "افزودن اطلاعات کشتی",
                        tint = TextColorSecondary
                    )
                }

                // دکمه تگ کردن مخاطب
                IconButton(onClick = { showUserPopup = !showUserPopup }) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "تگ کردن مخاطب",
                        tint = TextColorSecondary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("پیام خود را بنویسید...", color = Color.Gray) },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = myBubbleColor, 
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = ChatBackgroundLight,
                        unfocusedContainerColor = ChatBackgroundLight
                    ),
                    enabled = !isSending
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                FloatingActionButton(
                    onClick = {
                        if (textFieldValue.text.isNotBlank()) {
                            onSendMessage(textFieldValue.text)
                            textFieldValue = TextFieldValue("")
                            showUserPopup = false
                        }
                    },
                    containerColor = myBubbleColor,
                    contentColor = if (ColorUtils.calculateLuminance(myBubbleColor.toArgb()) > 0.5) Color.Black else Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, "ارسال", modifier = Modifier.rotateIcon(180f))
                    }
                }
            }
        }
    }

    if (showShipDialog && shipsData != null) {
        ShipSelectionDialog(
            shipsData = shipsData,
            onDismiss = { showShipDialog = false },
            onShipSelected = { ship ->
                // بارگذاری کوتاژها به جای اضافه کردن مستقیم متن
                onShipSelected(ship)
            }
        )
    }

    if (showQuotaDialog) {
        QuotaSelectionDialog(
            quotas = shipQuotas,
            onDismiss = {
                showQuotaDialog = false
                onQuotaDialogDismiss()
            },
            onQuotaSelected = { quota ->
                val quotaInfo = """
                    🚢 کشتی: ${quota.shipName ?: "-"}
                    📄 کوتاژ: ${quota.number}
                    🏭 انبار: ${quota.warehouse ?: "-"}
                    👤 صاحب کالا: ${quota.cargoOwner ?: "-"}
                    🚚 باربری: ${quota.shippingCompany}
                """.trimIndent()
                
                val currentText = textFieldValue.text
                val newText = if (currentText.isBlank()) quotaInfo else "$currentText\n\n$quotaInfo"
                textFieldValue = TextFieldValue(newText, TextRange(newText.length))
                
                showQuotaDialog = false
                onQuotaDialogDismiss()
            }
        )
    }
}

@Composable
fun ShipSelectionDialog(
    shipsData: com.atk.atk_cargo.api.ShipsData,
    onDismiss: () -> Unit,
    onShipSelected: (com.atk.atk_cargo.api.Ship) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allShips = remember(shipsData) { shipsData.activeShips + shipsData.inactiveShips }
    val filteredShips = remember(searchQuery, allShips) {
        if (searchQuery.isBlank()) allShips
        else allShips.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب کشتی") },
        text = {
            Column {
                // نوار جستجو
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    placeholder = { Text("جستجوی نام کشتی...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
                )

                LazyColumn(
                    modifier = Modifier.height(300.dp)
                ) {
                    items(filteredShips) { ship ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onShipSelected(ship) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ship.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            if (ship.isActive) {
                                Text(
                                    text = "فعال",
                                    fontSize = 10.sp,
                                    color = Color(0xFF43A047)
                                )
                            } else {
                                Text(
                                    text = "غیرفعال",
                                    fontSize = 10.sp,
                                    color = TextColorSecondary
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("لغو")
            }
        }
    )
}

@Composable
fun QuotaSelectionDialog(
    quotas: List<com.atk.atk_cargo.api.Quota>,
    onDismiss: () -> Unit,
    onQuotaSelected: (com.atk.atk_cargo.api.Quota) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب کوتاژ") },
        text = {
            if (quotas.isEmpty()) {
                Text("هیچ کوتاژی برای این کشتی یافت نشد.")
            } else {
                LazyColumn(
                    modifier = Modifier.height(300.dp)
                ) {
                    items(quotas) { quota ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onQuotaSelected(quota) },
                            elevation = CardDefaults.cardElevation(2.dp),
                            colors = CardDefaults.cardColors(containerColor = ChatBackgroundLight)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(16.dp), tint = TextColorSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "کوتاژ: ${quota.number}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("صاحب کالا: ${quota.cargoOwner ?: "-"}", fontSize = 12.sp, color = TextColorSecondary)
                                Text("انبار: ${quota.warehouse ?: "-"}", fontSize = 12.sp, color = TextColorSecondary)
                                Text("باربری: ${quota.shippingCompany}", fontSize = 12.sp, color = TextColorSecondary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بازگشت")
            }
        }
    )
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("💬", fontSize = 48.sp)
        Text("پیامی وجود ندارد", color = TextColorSecondary, fontSize = 16.sp)
    }
}

sealed class ChatUiItem {
    abstract val id: String
    data class Header(val date: String, override val id: String) : ChatUiItem()
    data class Message(val entity: ChatMessageEntity) : ChatUiItem() {
        override val id: String = entity.id.toString()
    }
}

private fun processMessagesForDisplay(messages: List<ChatMessageEntity>): List<ChatUiItem> {
    val result = mutableListOf<ChatUiItem>()

    messages.forEachIndexed { index, message ->
        // Extract date YYYY-MM-DD
        val datePart = message.timestamp.split(" ")[0]
        val jalaliDate = JalaliDateUtils.formatDate(datePart) // This converts to "9 Bahman 1402"
        
        // Add message
        result.add(ChatUiItem.Message(message))
        
        // Check if next message (which is older) has different date
        val nextMessage = messages.getOrNull(index + 1)
        val nextDatePart = nextMessage?.timestamp?.split(" ")?.get(0)
        
        if (nextDatePart != datePart) {
            // Date boundary changed. The current group (from top to here) belongs to 'jalaliDate'.
            // Since we are adding from Newest to Oldest, the header for 'jalaliDate' should appear AFTER these messages in the list 
            // so it renders ABOVE them in reverse layout.
            result.add(ChatUiItem.Header(jalaliDate, "header_$datePart"))
        }
    }
    
    return result
}

private fun Modifier.rotateIcon(degrees: Float) = this.then(
    Modifier.graphicsLayer(rotationZ = degrees)
)

// --- Helper Functions & Composables for Chat Enhancements ---

data class ShipInfoModel(
    val shipName: String,
    val kotazh: String,
    val warehouse: String,
    val owner: String,
    val shippingCompany: String
)


fun extractShipInfoAndText(message: String): Pair<String, ShipInfoModel?> {
    val lines = message.lines()
    val shipInfoLines = mutableListOf<String>()
    val textLines = mutableListOf<String>()

    lines.forEach { line ->
        if (line.contains("کشتی:") || 
            line.contains("کوتاژ:") || 
            line.contains("انبار:") || 
            line.contains("صاحب کالا:") || 
            line.contains("باربری:")
        ) {
            shipInfoLines.add(line)
        } else {
            textLines.add(line)
        }
    }

    // Attempt to build ShipInfoModel from collected lines
    var shipInfo: ShipInfoModel? = null
    val shipLine = shipInfoLines.find { it.contains("کشتی:") }
    val kotazhLine = shipInfoLines.find { it.contains("کوتاژ:") }
    val warehouseLine = shipInfoLines.find { it.contains("انبار:") }
    val ownerLine = shipInfoLines.find { it.contains("صاحب کالا:") }
    val companyLine = shipInfoLines.find { it.contains("باربری:") }

    if (shipLine != null && kotazhLine != null) {
        shipInfo = ShipInfoModel(
            shipName = shipLine.substringAfter("کشتی:").trim(),
            kotazh = kotazhLine.substringAfter("کوتاژ:").trim(),
            warehouse = warehouseLine?.substringAfter("انبار:")?.trim() ?: "-",
            owner = ownerLine?.substringAfter("صاحب کالا:")?.trim() ?: "-",
            shippingCompany = companyLine?.substringAfter("باربری:")?.trim() ?: "-"
        )
    }

    // Reconstruct the cleanest text block
    val cleanText = textLines.joinToString("\n").trim()
    
    // If no valid ship info found, return original text as "text" (though we split it, joining non-matches is safer to avoid data loss)
    if (shipInfo == null) {
        return Pair(message, null)
    }

    return Pair(cleanText, shipInfo)
}

fun replaceUsernamesWithFullNames(text: String, users: List<com.atk.atk_cargo.api.User>): String {
    var result = text
    // Replace all occurrences of @username with @FullName
    // Using a regex to find @word pattern could be safer, but simple replacement works if usernames are unique enough
    // Ideally, we iterate over mentioned users.
    
    // Simple approach: Iterate known users and replace their tags
    users.forEach { user ->
        if (result.contains("@${user.username}")) {
            val fullName = user.fullName ?: user.username
            result = result.replace("@${user.username}", "@$fullName")
        }
    }
    return result
}

@Composable
fun ShipInfoCard(info: ShipInfoModel, fontSize: Int, textColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ShipInfoRow(label = "کشتی", value = info.shipName, icon = "🚢", fontSize = fontSize, textColor = textColor)
        HorizontalDivider(color = textColor.copy(alpha = 0.2f), thickness = 0.5.dp)
        
        ShipInfoRow(label = "کوتاژ", value = info.kotazh, icon = "📄", fontSize = fontSize, isMono = true, textColor = textColor)
        HorizontalDivider(color = textColor.copy(alpha = 0.2f), thickness = 0.5.dp)
        
        ShipInfoRow(label = "انبار", value = info.warehouse, icon = "🏭", fontSize = fontSize, textColor = textColor)
        HorizontalDivider(color = textColor.copy(alpha = 0.2f), thickness = 0.5.dp)
        
        ShipInfoRow(label = "صاحب کالا", value = info.owner, icon = "👤", fontSize = fontSize, textColor = textColor)
        HorizontalDivider(color = textColor.copy(alpha = 0.2f), thickness = 0.5.dp)
        
        ShipInfoRow(label = "باربری", value = info.shippingCompany, icon = "🚚", fontSize = fontSize, textColor = textColor)
    }
}

@Composable
fun ShipInfoRow(
    label: String, 
    value: String, 
    icon: String, 
    fontSize: Int,
    textColor: Color,
    isMono: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = (fontSize - 2).sp,
            color = textColor.copy(alpha = 0.8f)
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = if (isMono) androidx.compose.ui.text.font.FontFamily.Monospace else null,
                color = textColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = icon, fontSize = (fontSize + 4).sp)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ChatSettingsDialogEnhanced(
    currentFontSize: Int,
    currentMyColor: Long,
    currentOtherColor: Long,
    currentBackgroundId: Int,
    currentBubbleShape: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Long, Long, Int, Int) -> Unit
) {
    // مدیریت حالت‌ها (State Management)
    var fontSize by remember { mutableFloatStateOf(currentFontSize.toFloat()) }
    var myColor by remember { mutableLongStateOf(currentMyColor) }
    var otherColor by remember { mutableLongStateOf(currentOtherColor) }
    var backgroundId by remember { mutableIntStateOf(currentBackgroundId) }
    var bubbleShape by remember { mutableIntStateOf(currentBubbleShape) }
    
    var showAdvancedColorPicker by remember { mutableStateOf(false) }
    var advancedColorTarget by remember { mutableStateOf("MY") }

    val colorPalette = listOf(
        0xFF2196F3, 0xFF4CAF50, 0xFFE91E63, 0xFF9C27B0,
        0xFFFF9800, 0xFF607D8B, 0xFF212121, 0xFFFFFFFF,
        0xFF075E54, 0xFF128C7E, 0xFF25D366, 0xFF34B7F1
    )

    if (showAdvancedColorPicker) {
        AdvancedColorPickerDialog(
            initialColor = if (advancedColorTarget == "MY") myColor else otherColor,
            onColorSelected = { selectedColor ->
                if (advancedColorTarget == "MY") {
                    myColor = selectedColor
                } else {
                    otherColor = selectedColor
                }
                showAdvancedColorPicker = false
            },
            onDismiss = { showAdvancedColorPicker = false }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End // RTL
            ) {
                // هدر دیالوگ
                Text(
                    text = "تنظیمات ظاهر گفتگو",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Column(
                    modifier = Modifier
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // بخش پیش‌نمایش زنده
                    Text(
                        text = "پیش‌نمایش زنده",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ChatSettingsPreviewRefined(
                        fontSize = fontSize.toInt(),
                        myColor = Color(myColor),
                        otherColor = Color(otherColor),
                        backgroundId = backgroundId,
                        bubbleShapeId = bubbleShape
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // تنظیم اندازه متن
                    SettingSectionRefined(title = "اندازه متن") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("-A", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                            Slider(
                                value = fontSize,
                                onValueChange = { fontSize = it },
                                valueRange = 12f..26f,
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            )
                            Text("+A", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // انتخاب رنگ پیام‌های من
                    SettingSectionRefined(title = "رنگ پیام‌های من", icon = Icons.Default.Person) {
                        LazyRowColorsRefined(
                            colors = colorPalette,
                            selectedColor = myColor,
                            onSelect = { myColor = it },
                            onCustomClick = {
                                advancedColorTarget = "MY"
                                showAdvancedColorPicker = true
                            }
                        )
                    }

                    // انتخاب رنگ پیام‌های دیگران
                    SettingSectionRefined(title = "رنگ پیام‌های دیگران", icon = Icons.Default.Person) {
                        LazyRowColorsRefined(
                            colors = colorPalette.reversed(),
                            selectedColor = otherColor,
                            onSelect = { otherColor = it },
                            onCustomClick = {
                                advancedColorTarget = "OTHER"
                                showAdvancedColorPicker = true
                            }
                        )
                    }

                    // انتخاب پس‌زمینه
                    SettingSectionRefined(title = "پس‌زمینه گفتگو") {
                        val backgroundOptions = listOf(
                            0 to Color(0xFFF8FAFC),
                            1 to Color(0xFFECE5DD),
                            2 to Color(0xFF202C33),
                            3 to Color(0xFF000000)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            reverseLayout = true
                        ) {
                            items(backgroundOptions) { (id, color) ->
                                Box(
                                    modifier = Modifier
                                        .size(60.dp, 40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(color)
                                        .border(
                                            width = if (backgroundId == id) 2.dp else 1.dp,
                                            color = if (backgroundId == id) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { backgroundId = id },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (backgroundId == id) {
                                        Icon(Icons.Default.Check, null, tint = if (ColorUtils.calculateLuminance(color.toArgb()) > 0.5) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // شکل حباب‌ها
                    SettingSectionRefined(title = "حالت نمایش پیام‌ها") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val shapes = listOf("کلاسیک" to 0, "مربعی" to 1, "مدرن" to 2)
                            shapes.forEach { (label, id) ->
                                ShapeCardRefined(
                                    selected = bubbleShape == id,
                                    label = label,
                                    shapeId = id,
                                    onClick = { bubbleShape = id },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // دکمه‌های پایانی
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("انصراف", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = { onSave(fontSize.toInt(), myColor, otherColor, backgroundId, bubbleShape) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text(
                            "ذخیره تنظیمات",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingSectionRefined(
    title: String,
    icon: ImageVector? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (icon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        content()
    }
}

@Composable
fun ChatSettingsPreviewRefined(
    fontSize: Int,
    myColor: Color,
    otherColor: Color,
    backgroundId: Int,
    bubbleShapeId: Int
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (backgroundId) {
            0 -> Color(0xFFF8FAFC)
            1 -> Color(0xFFECE5DD)
            2 -> Color(0xFF202C33)
            3 -> Color(0xFF000000)
            else -> Color(0xFFF8FAFC)
        }
    )

    val animatedFontSize by animateFloatAsState(targetValue = fontSize.toFloat())
    val animatedMyColor by animateColorAsState(targetValue = myColor)
    val animatedOtherColor by animateColorAsState(targetValue = otherColor)

    val myBubbleShape = when (bubbleShapeId) {
        0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        1 -> RoundedCornerShape(4.dp)
        2 -> RoundedCornerShape(topStart = 20.dp, topEnd = 2.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
        else -> RoundedCornerShape(16.dp)
    }
    
    val otherBubbleShape = when (bubbleShapeId) {
        0 -> RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        1 -> RoundedCornerShape(4.dp)
        2 -> RoundedCornerShape(topStart = 2.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
        else -> RoundedCornerShape(16.dp)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            // Received Message (Left Alignment for Preview context usually, but we'll follow RTL logic)
            val otherTextColor = if (ColorUtils.calculateLuminance(animatedOtherColor.toArgb()) > 0.5) Color.Black else Color.White
            Surface(
                color = animatedOtherColor,
                shape = otherBubbleShape,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text(
                    "چطوری؟ طرح جدید رو دیدی؟",
                    fontSize = animatedFontSize.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = otherTextColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sent Message
            val myTextColor = if (ColorUtils.calculateLuminance(animatedMyColor.toArgb()) > 0.5) Color.Black else Color.White
            Surface(
                color = animatedMyColor,
                shape = myBubbleShape,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    "آره، عالی و مینیمال شده! 😍",
                    fontSize = animatedFontSize.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = myTextColor
                )
            }
        }
    }
}

@Composable
fun LazyRowColorsRefined(
    colors: List<Long>, 
    selectedColor: Long, 
    onSelect: (Long) -> Unit, 
    onCustomClick: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
        reverseLayout = true // RTL
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
                    .clickable(onClick = onCustomClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Custom", modifier = Modifier.size(20.dp))
            }
        }

        items(colors) { colorLong ->
            val color = Color(colorLong)
            val isSelected = selectedColor == colorLong
            
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onSelect(colorLong) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(if (ColorUtils.calculateLuminance(color.toArgb()) > 0.5) Color.Black else Color.White, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun ShapeCardRefined(
    selected: Boolean, 
    label: String, 
    shapeId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val previewShape = when (shapeId) {
        0 -> RoundedCornerShape(topStart = 8.dp, topEnd = 1.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        1 -> RoundedCornerShape(2.dp)
        2 -> RoundedCornerShape(topStart = 10.dp, topEnd = 2.dp, bottomStart = 10.dp, bottomEnd = 10.dp)
        else -> RoundedCornerShape(8.dp)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent,
                    RoundedCornerShape(12.dp)
                )
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp, 18.dp)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        previewShape
                    )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.Button(onClick = onRetry) {
                Text("تلاش مجدد")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedColorPickerDialog(
    initialColor: Long,
    onColorSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var hexString by remember { mutableStateOf(String.format("#%06X", (0xFFFFFF and initialColor.toInt()))) }
    
    // Parse initial color
    val initialC = Color(initialColor)
    var currentColor by remember { mutableStateOf(initialC) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب پیشرفته رنگ", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Color Wheel
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .padding(16.dp)
                ) {
                    ColorWheel(
                        modifier = Modifier.fillMaxSize(),
                        initialColor = currentColor,
                        onColorChanged = {
                            currentColor = it
                            hexString = String.format("#%06X", (0xFFFFFF and it.toArgb()))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Preview & Hex
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Preview
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(currentColor, CircleShape)
                            .border(1.dp, Color.Gray, CircleShape)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Hex Input
                    OutlinedTextField(
                        value = hexString,
                        onValueChange = { 
                            hexString = it
                            try {
                                val colorInt = it.toColorInt()
                                currentColor = Color(colorInt)
                            } catch (_: Exception) {
                                // Invalid hex, ignore
                            }
                        },
                        label = { Text("کد HEX") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onColorSelected(currentColor.toArgb().toLong()) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("تایید")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("لغو")
            }
        }
    )
}

@Composable
fun MessageInfoDialog(message: ChatMessageEntity, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = TextColorPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("جزئیات پیام")
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                ChatInfoRow("فرستنده:", message.fullName)
                ChatInfoRow("نام کاربری:", "@${message.username}")
                ChatInfoRow("زمان ارسال:", message.timestamp)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    "خوانده شده توسط:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextColorSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                if (!message.readByNames.isNullOrEmpty()) {
                    message.readByNames.split(",").forEach { name ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF2196F3))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(name.trim(), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    Text("هنوز خوانده نشده است", style = MaterialTheme.typography.bodyMedium, color = TextColorSecondary)
                }

                if (message.updatedAt != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ChatInfoRow("آخرین ویرایش:", message.updatedAt)
                }
            }
        },
        confirmButton = {
             TextButton(onClick = onDismiss) { Text("بستن") }
        }
    )
}

@Composable
private fun ChatInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextColorSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = TextColorPrimary)
    }
}
