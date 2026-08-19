package com.atk.atk_cargo.feature.chat.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.atk.atk_cargo.feature.chat.viewmodel.ChatViewModel
import com.atk.atk_cargo.data.db.ChatMessageEntity
import com.atk.atk_cargo.feature.chat.domain.ShipInfoModel
import com.atk.atk_cargo.feature.chat.domain.extractShipInfoAndText
import com.atk.atk_cargo.feature.chat.domain.getAdaptiveBubbleColor
import com.atk.atk_cargo.feature.chat.domain.replaceUsernamesWithFullNames
import com.atk.atk_cargo.utils.JalaliDateUtils

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
    users: List<com.atk.atk_cargo.data.model.User>
) {
    val isMe = message.isSelf
    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val alignment = if (isMe) Alignment.CenterStart else Alignment.CenterEnd
    
    val bubbleShape = if (isMe) myShape else otherShape
    
    val baseBubbleColor = if (isMe) myBubbleColor else otherBubbleColor
    val finalBubbleColor = getAdaptiveBubbleColor(baseBubbleColor, isMe)
    
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
                        val (cleanText, shipInfo) = remember(message.message) { 
                            extractShipInfoAndText(message.message) 
                        }
                        
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

                        if (shipInfo != null) {
                            ShipInfoCard(shipInfo, fontSize, textColor)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

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
                
                if (!isMe) {
                    val senderName = users.find { it.username == message.username }?.fullName ?: message.fullName
                    Text(
                        text = senderName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
        }
    }

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

@Composable
fun MessageInfoDialog(message: ChatMessageEntity, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                val readByNames = message.readByNames
                if (!readByNames.isNullOrEmpty()) {
                    readByNames.split(",").forEach { name ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF2196F3))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(name.trim(), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    Text("هنوز خوانده نشده است", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                val updatedAt = message.updatedAt
                if (updatedAt != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ChatInfoRow("آخرین ویرایش:", updatedAt)
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
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
