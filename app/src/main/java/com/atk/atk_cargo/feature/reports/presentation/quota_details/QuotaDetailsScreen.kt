package com.atk.atk_cargo.feature.reports.presentation.quota_details

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atk.atk_cargo.api.ColorSelector
import com.atk.atk_cargo.api.Quota
import com.atk.atk_cargo.api.QuotaEditData
import com.atk.atk_cargo.api.adjustColorForTheme
import com.atk.atk_cargo.api.cardColors
import com.atk.atk_cargo.api.toTon
import com.atk.atk_cargo.feature.reports.domain.calculatePercentage
import com.atk.atk_cargo.feature.reports.domain.calculateProgress
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.feature.reports.presentation.components.InfoCard
import com.atk.atk_cargo.feature.reports.presentation.components.ProgressBar
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import com.atk.atk_cargo.api.QuotaDetails as ApiQuotaDetails

@Composable
fun QuotaDetails(
    quotaNumber: String,
    viewModel: ReportsViewModel
) {
    val reportsUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val quotaDetails = reportsUiState.selectedQuotaDetails
    val uiState = reportsUiState.status

    LaunchedEffect(quotaNumber) {
        viewModel.loadQuotaDetails(quotaNumber)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (uiState) {
                is ReportsViewModel.UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                is ReportsViewModel.UiState.Error -> {
                    Text(
                        text = (uiState as ReportsViewModel.UiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is ReportsViewModel.UiState.Success -> {
                    quotaDetails?.let { details ->
                        QuotaMainCard(details)
                        Spacer(modifier = Modifier.height(16.dp))
                        QuotaInfoCards(details)
                        Spacer(modifier = Modifier.height(16.dp))
                        QuotaProgressBar(details)
                        Spacer(modifier = Modifier.height(16.dp))
                        QuotaAdditionalInfo(details)
                    } ?: run {
                        Text("اطلاعات کوتاژ در دسترس نیست")
                    }
                }
            }
        }
    }
}

@Composable
fun QuotaMainCard(details: ApiQuotaDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "کوتاژ ${details.number}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun QuotaInfoCards(details: ApiQuotaDetails) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        InfoCard(
            title = "تناژ کل",
            value = formatNumber(details.totalTonnage.toTon()),
            description = "تن",
            icon = Icons.Default.Scale,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        InfoCard(
            title = "تناژ بارگیری شده",
            value = formatNumber(details.loadedTonnage.toTon()),
            description = "تن",
            icon = Icons.Default.LocalShipping,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        InfoCard(
            title = "تناژ مانده",
            value = formatNumber(details.remainingTonnage.toTon()),
            description = "تن",
            icon = Icons.Default.PendingActions,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        InfoCard(
            title = "تعداد حواله",
            value = formatNumber(details.voucherCount),
            description = "حواله",
            icon = Icons.Default.Receipt,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuotaProgressBar(details: ApiQuotaDetails) {
    ProgressBar(
        title = "درصد بارگیری",
        progress = calculateProgress(details.loadedTonnage, details.totalTonnage),
        value = calculatePercentage(details.loadedTonnage, details.totalTonnage),
        color = MaterialTheme.colorScheme.primary,
        suffix = "%"
    )
}

@Composable
fun QuotaAdditionalInfo(details: ApiQuotaDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("تاریخ شروع: ${details.startDate}", style = MaterialTheme.typography.bodyMedium)
            Text("تاریخ پایان: ${details.endDate}", style = MaterialTheme.typography.bodyMedium)
            details.additionalInfo?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text("اطلاعات اضافی: $it", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotasDialog(
    shipName: String,
    quotas: List<Quota>,
    onDismiss: () -> Unit,
    onEdit: (String, QuotaEditData) -> Unit,
    onToggleStatus: (Int, String) -> Unit,
    onDelete: (Quota) -> Unit,
    viewModel: ReportsViewModel
) {
    val sheetState = rememberModalBottomSheetState()
    var searchQuery by remember { mutableStateOf("") }
    val isDarkTheme = isSystemInDarkTheme()

    val sortedQuotas = quotas.sortedWith(
        compareBy<Quota> { !it.isActive }
            .thenBy { it.remainingTonnage }
    )

    val groupedAndSortedQuotas = sortedQuotas
        .groupBy { it.shippingCompany }
        .toSortedMap()

    val filteredQuotas = remember(searchQuery, groupedAndSortedQuotas) {
        if (searchQuery.isEmpty()) {
            groupedAndSortedQuotas
        } else {
            groupedAndSortedQuotas.mapValues { (_, quotas) ->
                quotas.filter { it.number.contains(searchQuery, ignoreCase = true) }
            }.filter { it.value.isNotEmpty() }
        }
    }

    val colorSelector = remember { ColorSelector(cardColors) }
    val colorMap = remember(groupedAndSortedQuotas) {
        colorSelector.reset()
        groupedAndSortedQuotas.keys.associateWith {
            adjustColorForTheme(colorSelector.getNextColor(), isDarkTheme).copy(alpha = 0.6f)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "کوتاژهای کشتی $shipName",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("جستجوی کوتاژ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredQuotas.forEach { (company, companyQuotas) ->
                            item {
                                Text(
                                    text = company,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colorMap[company] ?: MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(companyQuotas, key = { it.id ?: "${it.number}_${it.warehouse}_${it.cargoType}_${it.shippingCompany}" }) { quota ->
                                QuotaCard(
                                    quota = quota,
                                    onEdit = onEdit,
                                    onToggleStatus = onToggleStatus,
                                    onDelete = { onDelete(it) },
                                    onPercentageChange = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
