package com.atk.atk_cargo.feature.chat.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.core.graphics.ColorUtils
import com.atk.atk_cargo.feature.chat.domain.rotateIcon
import com.atk.atk_cargo.ui.theme.PlaceholderDark
import com.atk.atk_cargo.ui.theme.PlaceholderLight

@Composable
fun MessageInputArea(
    isSending: Boolean,
    myBubbleColor: Color,
    onSendMessage: (String) -> Unit,
    users: List<com.atk.atk_cargo.data.model.User> = emptyList(),
    shipsData: com.atk.atk_cargo.data.model.ShipsData? = null,
    shipQuotas: List<com.atk.atk_cargo.data.model.Quota> = emptyList(),
    onShipSelected: (com.atk.atk_cargo.data.model.Ship) -> Unit = {},
    onQuotaDialogDismiss: () -> Unit = {}
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var showShipDialog by remember { mutableStateOf(false) }
    var showQuotaDialog by remember { mutableStateOf(false) }
    var showUserPopup by remember { mutableStateOf(false) }
    
    LaunchedEffect(shipQuotas) {
        if (shipQuotas.isNotEmpty()) {
            showShipDialog = false
            showQuotaDialog = true
        }
    }

    Surface(
        shadowElevation = 16.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column {
            if (showUserPopup && users.isNotEmpty()) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(0, -300),
                    onDismissRequest = { showUserPopup = false }
                ) {
                    Card(
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .widthIn(max = 250.dp)
                            .height(200.dp)
                    ) {
                        LazyColumn {
                            items(users, key = { it.username }) { user ->
                                Text(
                                    text = user.fullName ?: user.username,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
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
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { 
                        Text(
                            "پیام خود را بنویسید...", 
                            color = if (isSystemInDarkTheme()) PlaceholderDark else PlaceholderLight 
                        ) 
                    },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = myBubbleColor, 
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isSending
                )
                
                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = { showShipDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.AddCircle,
                        contentDescription = "افزودن اطلاعات کشتی",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { showUserPopup = !showUserPopup }) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "تگ کردن مخاطب",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showShipDialog && shipsData != null) {
        ShipSelectionDialog(
            shipsData = shipsData,
            onDismiss = { showShipDialog = false },
            onShipSelected = { ship ->
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
    shipsData: com.atk.atk_cargo.data.model.ShipsData,
    onDismiss: () -> Unit,
    onShipSelected: (com.atk.atk_cargo.data.model.Ship) -> Unit
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
                    items(filteredShips, key = { it.name }) { ship ->
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    quotas: List<com.atk.atk_cargo.data.model.Quota>,
    onDismiss: () -> Unit,
    onQuotaSelected: (com.atk.atk_cargo.data.model.Quota) -> Unit
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
                    items(quotas, key = { it.id ?: it.number }) { quota ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onQuotaSelected(quota) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "کوتاژ: ${quota.number}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("صاحب کالا: ${quota.cargoOwner ?: "-"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("انبار: ${quota.warehouse ?: "-"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("باربری: ${quota.shippingCompany}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
