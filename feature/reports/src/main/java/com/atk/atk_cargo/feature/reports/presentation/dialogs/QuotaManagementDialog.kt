package com.atk.atk_cargo.feature.reports.presentation.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atk.atk_cargo.data.model.QuotaItem
import com.atk.atk_cargo.feature.reports.presentation.ships.SearchField
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel

internal val QuotaAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

internal val QuotaOnAccent: Color
    @Composable get() = MaterialTheme.colorScheme.onPrimary

internal val QuotaAccentBg: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

internal val QuotaAccentBorder: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)

internal val QuotaCardBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

internal val QuotaMutedBg: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

internal val QuotaMutedText: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

internal val QuotaTitleColor: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface

internal val QuotaScreenBg: Color
    @Composable get() = MaterialTheme.colorScheme.surface

internal val QuotaModalGradientTop: Color
    @Composable get() = MaterialTheme.colorScheme.surface

internal val QuotaModalGradientBottom: Color
    @Composable get() = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else Color(0xFFF2FAF8)

@Composable
fun QuotaManagementDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    viewModel: ReportsViewModel
) {
    if (!isVisible) return

    val reportsUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentShipName = reportsUiState.selectedShip
    var quotaData by remember { mutableStateOf<Map<String, Map<String, List<QuotaItem>>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var expandedShip by remember { mutableStateOf<String?>(null) }
    var expandedQuota by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }

    val refreshData: () -> Unit = {
        refreshTrigger++
    }

    LaunchedEffect(currentShipName, refreshTrigger) {
        isLoading = true
        viewModel.loadGroupedQuotas(currentShipName?.name ?: "") { result ->
            result.onSuccess { data ->
                quotaData = data
                errorMessage = null
            }.onFailure { e ->
                errorMessage = e.message
            }
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = QuotaScreenBg
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                QuotaManagementHeaderCard(onClose = onDismiss)
                HorizontalDivider(color = QuotaCardBorder.copy(alpha = 0.7f))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    SearchField(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "جستجوی کوتاژ",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        when {
                            isLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        CircularProgressIndicator(color = QuotaAccent)
                                        Text(
                                            text = "در حال بارگذاری کوتاژها...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                            errorMessage != null -> {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = "خطا: $errorMessage",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                            else -> {
                                QuotaManagementContent(
                                    quotaData = quotaData,
                                    searchQuery = searchQuery,
                                    expandedShip = expandedShip,
                                    expandedQuota = expandedQuota,
                                    onShipToggle = { shipName ->
                                        expandedShip = if (expandedShip == shipName) null else shipName
                                        expandedQuota = null
                                    },
                                    onQuotaToggle = { quotaKey ->
                                        expandedQuota = if (expandedQuota == quotaKey) null else quotaKey
                                    },
                                    onRefreshData = refreshData,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuotaManagementHeaderCard(
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(QuotaScreenBg)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(QuotaMutedBg)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "بستن",
                tint = QuotaTitleColor,
                modifier = Modifier.size(15.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(QuotaAccentBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = null,
                tint = QuotaAccent,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = "مدیریت کوتاژها",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = QuotaTitleColor
            )
            Text(
                text = "کوتاژهای هر کشتی",
                style = MaterialTheme.typography.labelSmall,
                color = QuotaMutedText
            )
        }
    }
}

// بدنه دیالوگ در QuotaManagementContent.kt و کارت‌های تکی کوتاژ/کشتی در QuotaCardComponents.kt هستند (شکستن God Composable)
