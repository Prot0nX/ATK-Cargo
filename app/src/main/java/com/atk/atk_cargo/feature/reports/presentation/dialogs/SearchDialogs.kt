package com.atk.atk_cargo.feature.reports.presentation.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.api.CargoInfo
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import kotlinx.coroutines.launch

enum class SearchType {
    RECEIPT_NUMBER,
    TRACKING_NUMBER
}

@Composable
fun AdvancedSearchDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSearchReceipt: (String) -> Unit,
    onSearchTracking: (String) -> Unit
) {
    var searchNumber by remember { mutableStateOf("") }
    var selectedSearchType by remember { mutableStateOf(SearchType.RECEIPT_NUMBER) }

    if (isOpen) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.42f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    SearchHeaderCard(
                        onClose = onDismiss,
                        selectedSearchType = selectedSearchType
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        SearchTypeTabRow(
                            selectedSearchType = selectedSearchType,
                            onSearchTypeSelected = {
                                selectedSearchType = it
                                searchNumber = ""
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = searchNumber,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) {
                                    searchNumber = it
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = when (selectedSearchType) {
                                        SearchType.RECEIPT_NUMBER -> Icons.Default.Receipt
                                        SearchType.TRACKING_NUMBER -> Icons.Default.Numbers
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            label = {
                                Text(
                                    when (selectedSearchType) {
                                        SearchType.RECEIPT_NUMBER -> "شماره قبض باسکول"
                                        SearchType.TRACKING_NUMBER -> "شماره حواله"
                                    }
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (searchNumber.isNotBlank()) {
                                        when (selectedSearchType) {
                                            SearchType.RECEIPT_NUMBER -> onSearchReceipt(searchNumber)
                                            SearchType.TRACKING_NUMBER -> onSearchTracking(searchNumber)
                                        }
                                    }
                                }
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("انصراف")
                            }

                            Button(
                                onClick = {
                                    if (searchNumber.isNotBlank()) {
                                        when (selectedSearchType) {
                                            SearchType.RECEIPT_NUMBER -> onSearchReceipt(searchNumber)
                                            SearchType.TRACKING_NUMBER -> onSearchTracking(searchNumber)
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = searchNumber.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("جستجو")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHeaderCard(
    onClose: () -> Unit,
    selectedSearchType: SearchType
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "جستجوی پیشرفته",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = when (selectedSearchType) {
                            SearchType.RECEIPT_NUMBER -> "جستجو بر اساس شماره قبض"
                            SearchType.TRACKING_NUMBER -> "جستجو بر اساس شماره حواله"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "بستن",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchTypeTabRow(
    selectedSearchType: SearchType,
    onSearchTypeSelected: (SearchType) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = remember {
        listOf(
            SearchTabInfo(SearchType.RECEIPT_NUMBER, "شماره قبض", Icons.Default.Receipt),
            SearchTabInfo(SearchType.TRACKING_NUMBER, "شماره حواله", Icons.Default.Numbers)
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedSearchType == tab.type

                Surface(
                    onClick = { onSearchTypeSelected(tab.type) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

private data class SearchTabInfo(
    val type: SearchType,
    val label: String,
    val icon: ImageVector
)

@Composable
fun MultipleSearchResultDialog(
    cargoInfoList: List<CargoInfo>,
    onDismiss: () -> Unit,
    onSelectCargo: (CargoInfo) -> Unit
) {
    val mainColor = MaterialTheme.colorScheme.primary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 700.dp)
                .padding(16.dp)
                .animateContentSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 1.dp,
                color = mainColor
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(mainColor.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                                tint = mainColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "نتایج جستجو",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${cargoInfoList.size} نتیجه یافت شد",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(mainColor.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = cargoInfoList,
                        key = { cargo -> (cargo.trackingNumber ?: "") + "_" + (cargo.scaleReceiptNumber ?: "") }
                    ) { cargoInfo ->
                        val context = LocalContext.current
                        CargoSearchResultCard(
                            cargoInfo = cargoInfo,
                            onClick = { onSelectCargo(cargoInfo) },
                            mainColor = mainColor,
                            onShare = { cargo ->
                                shareCargoInfo(cargo, context)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mainColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("بستن")
                }
            }
        }
    }
}

@Composable
private fun CargoSearchResultCard(
    cargoInfo: CargoInfo,
    onClick: () -> Unit,
    mainColor: Color,
    onShare: (CargoInfo) -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, mainColor.copy(alpha = 0.3f)),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(mainColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Numbers,
                            contentDescription = null,
                            tint = mainColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "حواله: ${cargoInfo.trackingNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = mainColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onShare(cargoInfo) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "اشتراک‌گذاری",
                            tint = mainColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val context = LocalContext.current
                    InfoRowCompact(
                        icon = Icons.Default.Receipt,
                        label = "قبض باسکول",
                        value = cargoInfo.scaleReceiptNumber,
                        isClickable = true,
                        onCopy = {
                            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData = ClipData.newPlainText("شماره قبض باسکول", cargoInfo.scaleReceiptNumber)
                            clipboardManager.setPrimaryClip(clipData)
                            Toast.makeText(context, "شماره قبض باسکول کپی شد", Toast.LENGTH_SHORT).show()
                        }
                    )
                    InfoRowCompact(
                        icon = Icons.Default.Scale,
                        label = "وزن خالص",
                        value = "${formatNumber(cargoInfo.netWeight.toIntOrNull() ?: 0)} کیلوگرم"
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoRowCompact(
                        icon = Icons.Default.DirectionsBoat,
                        label = "کشتی",
                        value = cargoInfo.shipName
                    )
                    InfoRowCompact(
                        icon = Icons.Default.LocalShipping,
                        label = "شرکت",
                        value = cargoInfo.shippingCompany
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "ورود: ${cargoInfo.entryTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (cargoInfo.exitTime != null)
                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else
                        Color(0xFFFF9800).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (cargoInfo.exitTime != null) "خروج شده" else "در انتظار خروج",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (cargoInfo.exitTime != null)
                            Color(0xFF4CAF50)
                        else
                            Color(0xFFFF9800),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRowCompact(
    icon: ImageVector,
    label: String,
    value: String,
    isClickable: Boolean = false,
    onCopy: (() -> Unit)? = null
) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = if (isClickable && onCopy != null) {
            Modifier.clickable {
                onCopy()
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(label, value)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "$label کپی شد", Toast.LENGTH_SHORT).show()
            }
        } else Modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isClickable && onCopy != null) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "کپی",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun SearchResultDialog(
    cargoInfo: CargoInfo,
    onDismiss: () -> Unit,
    onRefresh: (CargoInfo) -> Unit,
    searchType: SearchType?,
    searchValue: String?,
    viewModel: ReportsViewModel
) {
    val mainColor = MaterialTheme.colorScheme.primary
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isEditMode by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    var editedTrackingNumber by remember { mutableStateOf(cargoInfo.trackingNumber) }
    var editedNumberOfPeople by remember { mutableStateOf(cargoInfo.numberOfPeople) }
    var editedEntryTime by remember { mutableStateOf(cargoInfo.entryTime) }
    var editedNetWeight by remember { mutableStateOf(cargoInfo.netWeight) }
    var editedScaleReceiptNumber by remember { mutableStateOf(cargoInfo.scaleReceiptNumber) }
    var editedShortageWeight by remember { mutableStateOf(cargoInfo.shortageWeight) }
    var editedExcessWeight by remember { mutableStateOf(cargoInfo.excessWeight) }
    var editedExitTime by remember { mutableStateOf(cargoInfo.exitTime ?: "") }
    var editedExitDate by remember { mutableStateOf(cargoInfo.exitDate ?: "") }
    var editedStatus by remember { mutableStateOf(cargoInfo.status) }
    var editedLoadingQuotaNumber by remember { mutableStateOf(cargoInfo.loadingQuotaNumber) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(16.dp)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f)
                    .padding(paddingValues)
                    .padding(16.dp)
                    .animateContentSize(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = mainColor
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(mainColor.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = mainColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isEditMode) "ویرایش اطلاعات" else "نتیجه جستجو",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "قبض باسکول: ${if (isEditMode) editedScaleReceiptNumber else cargoInfo.scaleReceiptNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(mainColor.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "بستن",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            if (isEditMode) {
                                EditableCargoMainInfo(
                                    trackingNumber = editedTrackingNumber,
                                    onTrackingNumberChange = { editedTrackingNumber = it },
                                    scaleReceiptNumber = editedScaleReceiptNumber,
                                    onScaleReceiptNumberChange = { editedScaleReceiptNumber = it },
                                    loadingQuotaNumber = editedLoadingQuotaNumber,
                                    onLoadingQuotaNumberChange = { editedLoadingQuotaNumber = it },
                                    numberOfPeople = editedNumberOfPeople,
                                    onNumberOfPeopleChange = { editedNumberOfPeople = it }
                                )
                            } else {
                                CargoMainInfo(cargoInfo = cargoInfo)
                            }
                        }

                        item {
                            if (isEditMode) {
                                EditableCargoWeightInfo(
                                    netWeight = editedNetWeight,
                                    onNetWeightChange = { editedNetWeight = it },
                                    shortageWeight = editedShortageWeight,
                                    onShortageWeightChange = { editedShortageWeight = it },
                                    excessWeight = editedExcessWeight,
                                    onExcessWeightChange = { editedExcessWeight = it }
                                )
                            } else {
                                CargoWeightInfo(cargoInfo = cargoInfo)
                            }
                        }

                        item {
                            if (isEditMode) {
                                EditableCargoTimeInfo(
                                    entryTime = editedEntryTime,
                                    onEntryTimeChange = { editedEntryTime = it },
                                    exitTime = editedExitTime,
                                    onExitTimeChange = { editedExitTime = it },
                                    exitDate = editedExitDate,
                                    onExitDateChange = { editedExitDate = it },
                                    status = editedStatus,
                                    onStatusChange = { editedStatus = it }
                                )
                            } else {
                                CargoTimeInfo(cargoInfo = cargoInfo)
                            }
                        }

                        if (!isEditMode) {
                            item {
                                CargoShippingInfo(cargoInfo = cargoInfo)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isEditMode) {
                            OutlinedButton(
                                onClick = {
                                    isEditMode = false
                                    editedTrackingNumber = cargoInfo.trackingNumber
                                    editedNumberOfPeople = cargoInfo.numberOfPeople
                                    editedEntryTime = cargoInfo.entryTime
                                    editedNetWeight = cargoInfo.netWeight
                                    editedScaleReceiptNumber = cargoInfo.scaleReceiptNumber
                                    editedShortageWeight = cargoInfo.shortageWeight
                                    editedExcessWeight = cargoInfo.excessWeight
                                    editedExitTime = cargoInfo.exitTime ?: ""
                                    editedExitDate = cargoInfo.exitDate ?: ""
                                    editedStatus = cargoInfo.status
                                    editedLoadingQuotaNumber = cargoInfo.loadingQuotaNumber
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("لغو")
                            }

                            Button(
                                onClick = { showConfirmDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ذخیره")
                            }
                        } else {
                            Button(
                                onClick = { isEditMode = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ویرایش")
                            }

                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("بستن")
                            }
                        }
                    }
                }
            }
        }

        if (showConfirmDialog) {
            CargoEditConfirmDialog(
                cargoInfo = cargoInfo,
                editedTrackingNumber = editedTrackingNumber,
                editedNumberOfPeople = editedNumberOfPeople,
                editedEntryTime = editedEntryTime,
                editedNetWeight = editedNetWeight,
                editedScaleReceiptNumber = editedScaleReceiptNumber,
                editedShortageWeight = editedShortageWeight,
                editedExcessWeight = editedExcessWeight,
                editedExitTime = editedExitTime,
                editedExitDate = editedExitDate,
                editedStatus = editedStatus,
                editedLoadingQuotaNumber = editedLoadingQuotaNumber,
                onDismiss = { showConfirmDialog = false },
                onConfirm = {
                    showConfirmDialog = false
                    isSaving = true

                    val updatedCargo = cargoInfo.copy(
                        trackingNumber = editedTrackingNumber,
                        numberOfPeople = editedNumberOfPeople,
                        entryTime = editedEntryTime,
                        netWeight = editedNetWeight,
                        scaleReceiptNumber = editedScaleReceiptNumber,
                        shortageWeight = editedShortageWeight,
                        excessWeight = editedExcessWeight,
                        exitTime = editedExitTime.ifEmpty { null },
                        exitDate = editedExitDate.ifEmpty { null },
                        status = editedStatus,
                        loadingQuotaNumber = editedLoadingQuotaNumber
                    )

                    Log.d("CargoEdit", "🔄 شروع بروزرسانی - ID: ${updatedCargo.id}")
                    Log.d("CargoEdit", "📦 داده‌های ویرایش شده: $updatedCargo")

                    viewModel.updateCargoInfo(updatedCargo) { result ->
                        isSaving = false
                        result.fold(
                            onSuccess = { response ->
                                Log.d("CargoEdit", "✅ پاسخ موفق: $response")
                                if (response.error == false) {
                                    isEditMode = false
                                    if (searchType != null && searchValue != null) {
                                        when (searchType) {
                                            SearchType.RECEIPT_NUMBER -> {
                                                viewModel.performAdvancedSearch(searchValue) { searchResult ->
                                                    searchResult.fold(
                                                        onSuccess = { refreshedCargo ->
                                                            if (refreshedCargo != null) {
                                                                onRefresh(refreshedCargo)
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        message = "✅ ${response.message}",
                                                                        duration = SnackbarDuration.Short
                                                                    )
                                                                }
                                                            }
                                                        },
                                                        onFailure = {
                                                            scope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        message = "بروزرسانی انجام شد اما خطا در دریافت اطلاعات جدید",
                                                                        duration = SnackbarDuration.Long
                                                                    )
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                            SearchType.TRACKING_NUMBER -> {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "✅ ${response.message}",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "✅ ${response.message}",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                } else {
                                    Log.e("CargoEdit", "❌ خطا در response: ${response.message}")
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "❌ ${response.message}",
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                }
                            },
                            onFailure = { error ->
                                Log.e("CargoEdit", "💥 Exception: ${error.message}", error)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "❌ خطا: ${error.localizedMessage}",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun CargoEditConfirmDialog(
    cargoInfo: CargoInfo,
    editedTrackingNumber: String,
    editedNumberOfPeople: String,
    editedEntryTime: String,
    editedNetWeight: String,
    editedScaleReceiptNumber: String,
    editedShortageWeight: String,
    editedExcessWeight: String,
    editedExitTime: String,
    editedExitDate: String,
    editedStatus: String,
    editedLoadingQuotaNumber: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .heightIn(max = 700.dp)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                CargoEditConfirmHeader()

                Spacer(modifier = Modifier.height(24.dp))

                CargoChangesPreview(
                    cargoInfo = cargoInfo,
                    editedTrackingNumber = editedTrackingNumber,
                    editedNumberOfPeople = editedNumberOfPeople,
                    editedEntryTime = editedEntryTime,
                    editedNetWeight = editedNetWeight,
                    editedScaleReceiptNumber = editedScaleReceiptNumber,
                    editedShortageWeight = editedShortageWeight,
                    editedExcessWeight = editedExcessWeight,
                    editedExitTime = editedExitTime,
                    editedExitDate = editedExitDate,
                    editedStatus = editedStatus,
                    editedLoadingQuotaNumber = editedLoadingQuotaNumber
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("تایید و ذخیره")
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("انصراف")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CargoEditConfirmHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = ""
            )

            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .scale(scale)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "تأیید بروزرسانی",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "آیا از ذخیره تغییرات اطمینان دارید؟",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun CargoChangesPreview(
    cargoInfo: CargoInfo,
    editedTrackingNumber: String,
    editedNumberOfPeople: String,
    editedEntryTime: String,
    editedNetWeight: String,
    editedScaleReceiptNumber: String,
    editedShortageWeight: String,
    editedExcessWeight: String,
    editedExitTime: String,
    editedExitDate: String,
    editedStatus: String,
    editedLoadingQuotaNumber: String
) {
    val changes = remember(
        cargoInfo,
        editedTrackingNumber,
        editedNumberOfPeople,
        editedEntryTime,
        editedNetWeight,
        editedScaleReceiptNumber,
        editedShortageWeight,
        editedExcessWeight,
        editedExitTime,
        editedExitDate,
        editedStatus,
        editedLoadingQuotaNumber
    ) {
        mutableListOf<Triple<String, String, String>>().apply {
            if (cargoInfo.trackingNumber != editedTrackingNumber) {
                add(Triple("شماره حواله", cargoInfo.trackingNumber, editedTrackingNumber))
            }
            if (cargoInfo.numberOfPeople != editedNumberOfPeople) {
                add(Triple("تعداد نفرات", cargoInfo.numberOfPeople, editedNumberOfPeople))
            }
            if (cargoInfo.entryTime != editedEntryTime) {
                add(Triple("زمان ورود", cargoInfo.entryTime, editedEntryTime))
            }
            if (cargoInfo.netWeight != editedNetWeight) {
                add(Triple("وزن خالص", cargoInfo.netWeight, editedNetWeight))
            }
            if (cargoInfo.scaleReceiptNumber != editedScaleReceiptNumber) {
                add(Triple("شماره قبض باسکول", cargoInfo.scaleReceiptNumber, editedScaleReceiptNumber))
            }
            if (cargoInfo.shortageWeight != editedShortageWeight) {
                add(Triple("وزن کسری", cargoInfo.shortageWeight, editedShortageWeight))
            }
            if (cargoInfo.excessWeight != editedExcessWeight) {
                add(Triple("وزن اضافی", cargoInfo.excessWeight, editedExcessWeight))
            }
            if ((cargoInfo.exitTime ?: "") != editedExitTime) {
                add(Triple("زمان خروج", cargoInfo.exitTime ?: "", editedExitTime))
            }
            if ((cargoInfo.exitDate ?: "") != editedExitDate) {
                add(Triple("تاریخ خروج", cargoInfo.exitDate ?: "", editedExitDate))
            }
            if (cargoInfo.status != editedStatus) {
                add(Triple("وضعیت", cargoInfo.status, editedStatus))
            }
            if (cargoInfo.loadingQuotaNumber != editedLoadingQuotaNumber) {
                add(Triple("شماره کوتاژ", cargoInfo.loadingQuotaNumber, editedLoadingQuotaNumber))
            }
        }
    }

    if (changes.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "تغییرات اعمال شده (${changes.size} مورد)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                changes.forEach { (field, oldValue, newValue) ->
                    ChangeItem(
                        fieldName = field,
                        oldValue = oldValue,
                        newValue = newValue
                    )
                }
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "هیچ تغییری اعمال نشده است",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChangeItem(
    fieldName: String,
    oldValue: String,
    newValue: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = fieldName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = oldValue.ifEmpty { "خالی" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = newValue.ifEmpty { "خالی" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CargoMainInfo(cargoInfo: CargoInfo) {
    InfoCard(
        mainColor = MaterialTheme.colorScheme.primary,
        title = "اطلاعات اصلی",
        icon = Icons.Default.Description,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRowCargo(
                    icon = Icons.Default.Numbers,
                    label = "شماره حواله",
                    value = cargoInfo.trackingNumber
                )
                DetailRowCargo(
                    icon = Icons.Default.Receipt,
                    label = "قبض باسکول",
                    value = cargoInfo.scaleReceiptNumber
                )
                DetailRowCargo(
                    icon = Icons.Default.Newspaper,
                    label = "شماره کوتاژ",
                    value = cargoInfo.loadingQuotaNumber
                )
            }
        }
    )
}

@Composable
private fun CargoWeightInfo(cargoInfo: CargoInfo) {
    InfoCard(
        mainColor = MaterialTheme.colorScheme.secondary,
        title = "اطلاعات وزن",
        icon = Icons.Default.Scale,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRowCargo(
                    icon = Icons.Default.Scale,
                    label = "وزن خالص",
                    value = "${formatNumber(cargoInfo.netWeight.toIntOrNull() ?: 0)} کیلوگرم"
                )
                DetailRowCargo(
                    icon = Icons.Default.ArrowDownward,
                    label = "کسری بار",
                    value = "${formatNumber(cargoInfo.shortageWeight.toIntOrNull() ?: 0)} کیلوگرم"
                )
                DetailRowCargo(
                    icon = Icons.Default.ArrowUpward,
                    label = "اضافه بار",
                    value = "${formatNumber(cargoInfo.excessWeight.toIntOrNull() ?: 0)} کیلوگرم"
                )
            }
        }
    )
}

@Composable
private fun CargoTimeInfo(cargoInfo: CargoInfo) {
    InfoCard(
        mainColor = MaterialTheme.colorScheme.tertiary,
        title = "زمان‌بندی",
        icon = Icons.Default.Schedule,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRowCargo(
                    icon = Icons.AutoMirrored.Filled.Login,
                    label = "ساعت ورود",
                    value = cargoInfo.entryTime
                )
                DetailRowCargo(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    label = "ساعت خروج",
                    value = cargoInfo.exitTime ?: "-"
                )
                DetailRowCargo(
                    icon = Icons.Default.DateRange,
                    label = "تاریخ خروج",
                    value = cargoInfo.exitDate ?: "-"
                )
            }
        }
    )
}

@Composable
private fun CargoShippingInfo(cargoInfo: CargoInfo) {
    InfoCard(
        mainColor = MaterialTheme.colorScheme.primary,
        title = "اطلاعات تکمیلی",
        icon = Icons.Default.Info,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRowCargo(
                    icon = Icons.Default.DirectionsBoat,
                    label = "کشتی",
                    value = cargoInfo.shipName
                )
                DetailRowCargo(
                    icon = Icons.Default.Warehouse,
                    label = "انبار بارگیری",
                    value = cargoInfo.loadingWarehouse
                )
                DetailRowCargo(
                    icon = Icons.Default.Inventory,
                    label = "نوع کالا",
                    value = cargoInfo.cargoType
                )
                DetailRowCargo(
                    icon = Icons.Default.LocalShipping,
                    label = "شرکت بارگیری",
                    value = cargoInfo.shippingCompany
                )
            }
        }
    )
}

@Composable
private fun EditableCargoMainInfo(
    trackingNumber: String,
    onTrackingNumberChange: (String) -> Unit,
    scaleReceiptNumber: String,
    onScaleReceiptNumberChange: (String) -> Unit,
    loadingQuotaNumber: String,
    onLoadingQuotaNumberChange: (String) -> Unit,
    numberOfPeople: String,
    onNumberOfPeopleChange: (String) -> Unit
) {
    InfoCard(
        mainColor = MaterialTheme.colorScheme.primary,
        title = "اطلاعات اصلی",
        icon = Icons.Default.Description,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = trackingNumber,
                    onValueChange = onTrackingNumberChange,
                    label = { Text("شماره حواله") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Numbers, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = scaleReceiptNumber,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            onScaleReceiptNumberChange(it)
                        }
                    },
                    label = { Text("قبض باسکول") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = loadingQuotaNumber,
                    onValueChange = onLoadingQuotaNumberChange,
                    label = { Text("شماره کوتاژ") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Newspaper, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = numberOfPeople,
                    onValueChange = onNumberOfPeopleChange,
                    label = { Text("تعداد افراد") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.People, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    )
}

@Composable
private fun EditableCargoWeightInfo(
    netWeight: String,
    onNetWeightChange: (String) -> Unit,
    shortageWeight: String,
    onShortageWeightChange: (String) -> Unit,
    excessWeight: String,
    onExcessWeightChange: (String) -> Unit
) {
    InfoCard(
        mainColor = MaterialTheme.colorScheme.secondary,
        title = "اطلاعات وزن",
        icon = Icons.Default.Scale,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = netWeight,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            onNetWeightChange(it)
                        }
                    },
                    label = { Text("وزن خالص (کیلوگرم)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Scale, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = shortageWeight,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            onShortageWeightChange(it)
                        }
                    },
                    label = { Text("کسری بار (کیلوگرم)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = excessWeight,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            onExcessWeightChange(it)
                        }
                    },
                    label = { Text("اضافه بار (کیلوگرم)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    )
}

@Composable
private fun EditableCargoTimeInfo(
    entryTime: String,
    onEntryTimeChange: (String) -> Unit,
    exitTime: String,
    onExitTimeChange: (String) -> Unit,
    exitDate: String,
    onExitDateChange: (String) -> Unit,
    status: String,
    onStatusChange: (String) -> Unit
) {
    InfoCard(
        mainColor = MaterialTheme.colorScheme.tertiary,
        title = "زمان‌بندی",
        icon = Icons.Default.Schedule,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = entryTime,
                    onValueChange = onEntryTimeChange,
                    label = { Text("ساعت ورود") },
                    leadingIcon = {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Login, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = exitTime,
                    onValueChange = onExitTimeChange,
                    label = { Text("ساعت خروج") },
                    leadingIcon = {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = exitDate,
                    onValueChange = onExitDateChange,
                    label = { Text("تاریخ خروج") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = status,
                    onValueChange = onStatusChange,
                    label = { Text("وضعیت") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    )
}

@Composable
private fun InfoCard(
    mainColor: Color,
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, mainColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(mainColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = mainColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = mainColor,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
private fun DetailRowCargo(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun shareCargoInfo(cargoInfo: CargoInfo, context: Context) {
    val shareText = buildString {
        appendLine("📦 اطلاعات حواله")
        appendLine("━━━━━━━━━━━━━━━━━━━━")
        appendLine("🔢 شماره حواله: ${cargoInfo.trackingNumber}")
        appendLine("🧾 قبض باسکول: ${cargoInfo.scaleReceiptNumber}")
        appendLine("⚖️ وزن خالص: ${formatNumber(cargoInfo.netWeight.toIntOrNull() ?: 0)} کیلوگرم")
        appendLine("🚢 کشتی: ${cargoInfo.shipName}")
        appendLine("🏢 شرکت: ${cargoInfo.shippingCompany}")
        appendLine("📅 زمان ورود: ${cargoInfo.entryTime}")
        if (cargoInfo.exitTime != null) {
            appendLine("🚪 زمان خروج: ${cargoInfo.exitTime}")
            appendLine("✅ وضعیت: خروج شده")
        } else {
            appendLine("⏳ وضعیت: در انتظار خروج")
        }
        appendLine("━━━━━━━━━━━━━━━━━━━━")
        appendLine("📱 ارسال شده از اپلیکیشن ATK Cargo")
    }

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "اطلاعات حواله ${cargoInfo.trackingNumber}")
    }

    val chooserIntent = Intent.createChooser(shareIntent, "اشتراک‌گذاری اطلاعات حواله")

    try {
        context.startActivity(chooserIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "خطا در اشتراک‌گذاری: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
