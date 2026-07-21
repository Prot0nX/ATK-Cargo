package com.atk.atk_cargo.feature.reports.presentation.dialogs

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.api.QuotaCompletionData
import com.atk.atk_cargo.api.QuotaGroupingMode
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import kotlin.math.roundToInt

@Composable
fun ComprehensiveAnalyticsDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    viewModel: ReportsViewModel
) {
    val analyticsData by viewModel.comprehensiveAnalytics.collectAsState()
    val loadingState by viewModel.analyticsLoadingState.collectAsState()

    LaunchedEffect(isVisible) {
        if (isVisible) {
            viewModel.loadComprehensiveAnalytics()
        }
    }

    if (isVisible) {
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
                    .fillMaxHeight(0.9f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AnalyticsHeaderCard(onClose = onDismiss)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        AnalyticsDateNavigation(viewModel = viewModel)

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        ) {
                            when (loadingState) {
                                is ReportsViewModel.LoadingState.Loading -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "در حال بارگذاری تحلیل‌ها...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                                is ReportsViewModel.LoadingState.Error -> {
                                    val error = (loadingState as ReportsViewModel.LoadingState.Error).message
                                    ErrorStateCard(errorMessage = error)
                                }
                                ReportsViewModel.LoadingState.Success -> {
                                    analyticsData?.quotaCompletionAnalysis?.let { quotaData ->
                                        QuotaAnalysis(
                                            completionData = quotaData,
                                            viewModel = viewModel
                                        )
                                    } ?: EmptyStateCard("داده‌ای برای کوتاژها یافت نشد")
                                }
                                ReportsViewModel.LoadingState.Idle -> {
                                    LaunchedEffect(Unit) {
                                        viewModel.loadComprehensiveAnalytics()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsDateNavigation(
    viewModel: ReportsViewModel
) {
    val offset by viewModel.analyticsDateOffset.collectAsState()
    val analytics by viewModel.comprehensiveAnalytics.collectAsState()
    val dateInfo = analytics?.dateInfo

    val formattedDate = dateInfo?.jalaliDate ?: ""
    val dayName = dateInfo?.dayName ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.setAnalyticsDateOffset(offset + 1) },
                enabled = offset < 0
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "روز بعد",
                    tint = if (offset < 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (offset == 0) "امروز" else if (offset == -1) "دیروز" else dayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (formattedDate.isNotEmpty()) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = { viewModel.setAnalyticsDateOffset(offset - 1) },
                enabled = offset > -7
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "روز قبل",
                    tint = if (offset > -7) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsHeaderCard(
    onClose: () -> Unit
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
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "تحلیل جامع عملیات",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "گزارشات 24 ساعته",
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
private fun SearchField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    placeholder: String = "جستجو بر اساس نام کشتی، شماره...",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Surface(
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
    ) {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxSize(),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            leadingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "پاک کردن",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Right)
        )
    }
}

@Composable
fun QuotaAnalysis(
    completionData: List<QuotaCompletionData>,
    viewModel: ReportsViewModel
) {
    val context = LocalContext.current

    LaunchedEffect(completionData) {
        viewModel.updateInitialQuotas(completionData)
    }

    var expandedGroup by remember { mutableStateOf<String?>(null) }
    var selectedOwnerQuotas by remember { mutableStateOf<Pair<String, List<QuotaCompletionData>>?>(null) }
    val groupingMode by viewModel.groupingMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredQuotas by viewModel.filteredQuotas.collectAsState()

    val activeQuotas = filteredQuotas
        .filter { it.last_24h_vouchers > 0 }
        .sortedByDescending { it.last_24h_vouchers }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchField(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 12.dp, bottom = 4.dp),
                placeholder = "جستجو بر اساس شماره کوتاژ...",
                keyboardType = KeyboardType.Number
            )

            IconButton(
                onClick = {
                    val grouped = when (groupingMode) {
                        QuotaGroupingMode.BY_CARGO_OWNER -> {
                            activeQuotas.groupBy { "${it.shipName}|${it.cargoType ?: "نامشخص"}|${it.warehouse ?: "نامشخص"}" }
                        }
                        QuotaGroupingMode.BY_SHIP -> {
                            activeQuotas.groupBy { "${it.shipName}|${it.cargoType ?: "نامشخص"}" }
                        }
                        QuotaGroupingMode.BY_CARRIER -> {
                            activeQuotas.groupBy { it.shippingCompany }
                        }
                    }
                    val sortedForShare = grouped.map { (name, qs) ->
                        Triple(name, qs, qs.sumOf { it.last_24h_weight.toDouble() }.toFloat())
                    }.sortedWith(
                        if (groupingMode == QuotaGroupingMode.BY_CARGO_OWNER) {
                            val warehouseQuotaCounts = activeQuotas.groupBy { it.warehouse ?: "نامشخص" }.mapValues { it.value.size }
                            compareByDescending<Triple<String, List<QuotaCompletionData>, Float>> {
                                val parts = it.first.split("|")
                                val warehouse = parts.getOrNull(2)?.trim() ?: "نامشخص"
                                warehouseQuotaCounts[warehouse] ?: 0
                            }
                            .thenBy {
                                val parts = it.first.split("|")
                                parts.getOrNull(2)?.trim() ?: "نامشخص"
                            }
                            .thenByDescending { it.third }
                        } else {
                            compareByDescending<Triple<String, List<QuotaCompletionData>, Float>> { it.second.size }
                                .thenByDescending { it.third }
                        }
                    )
                    
                    val shareText = buildString {
                        val modeStr = when (groupingMode) {
                            QuotaGroupingMode.BY_CARGO_OWNER -> "صاحب کالا"
                            QuotaGroupingMode.BY_SHIP -> "کشتی"
                            QuotaGroupingMode.BY_CARRIER -> "باربری"
                        }
                        appendLine("📊 تحلیل جامع عملیات - دسته بندی: $modeStr\n")
                        sortedForShare.forEach { (name, qs, totalWeight) ->
                            val groupTitle = if (groupingMode == QuotaGroupingMode.BY_CARGO_OWNER) {
                                val parts = name.split("|")
                                if (parts.size >= 3) "کشتی: ${parts[0]} | کالا: ${parts[1]} | انبار: ${parts[2]}" 
                                else if (parts.size >= 2) "کشتی: ${parts[0]} | انبار: ${parts[1]}" else name
                            } else if (groupingMode == QuotaGroupingMode.BY_SHIP) {
                                val parts = name.split("|")
                                if (parts.size >= 2) "کشتی: ${parts[0]} | کالا: ${parts[1]}" else name
                            } else name
                            
                            appendLine("🔹 $groupTitle")
                            appendLine("   تعداد کوتاژ: ${qs.size} | تعداد حواله: ${qs.sumOf { it.last_24h_vouchers }} | تناژ کل: ${formatNumber(totalWeight.roundToInt())} تن")
                            appendLine()
                        }
                    }
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "ارسال اطلاعات")
                    context.startActivity(shareIntent)
                },
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "اشتراک گذاری کل",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 16.dp)
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnalyticsGroupingModeButton(
                text = "صاحب کالا",
                icon = Icons.Default.Person,
                isSelected = groupingMode == QuotaGroupingMode.BY_CARGO_OWNER,
                onClick = { viewModel.setGroupingMode(QuotaGroupingMode.BY_CARGO_OWNER) },
                modifier = Modifier.weight(1f)
            )
            AnalyticsGroupingModeButton(
                text = "کشتی",
                icon = Icons.Default.DirectionsBoat,
                isSelected = groupingMode == QuotaGroupingMode.BY_SHIP,
                onClick = { viewModel.setGroupingMode(QuotaGroupingMode.BY_SHIP) },
                modifier = Modifier.weight(1f)
            )
            AnalyticsGroupingModeButton(
                text = "باربری",
                icon = Icons.Default.LocalShipping,
                isSelected = groupingMode == QuotaGroupingMode.BY_CARRIER,
                onClick = { viewModel.setGroupingMode(QuotaGroupingMode.BY_CARRIER) },
                modifier = Modifier.weight(1f)
            )
        }

        if (activeQuotas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "کوتاژ فعالی در 24 ساعت گذشته وجود ندارد",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val grouped = when (groupingMode) {
                QuotaGroupingMode.BY_CARGO_OWNER -> {
                    activeQuotas.groupBy { "${it.shipName}|${it.cargoType ?: "نامشخص"}|${it.warehouse ?: "نامشخص"}" }
                }
                QuotaGroupingMode.BY_SHIP -> {
                    activeQuotas.groupBy { "${it.shipName}|${it.cargoType ?: "نامشخص"}" }
                }
                QuotaGroupingMode.BY_CARRIER -> {
                    activeQuotas.groupBy { it.shippingCompany }
                }
            }

            val sortedGroups = remember(activeQuotas, groupingMode) {
                grouped.map { (groupName, quotas) ->
                    Triple(
                        groupName,
                        quotas,
                        quotas.sumOf { it.last_24h_weight.toDouble() }.toFloat()
                    )
                }.sortedWith(
                    if (groupingMode == QuotaGroupingMode.BY_CARGO_OWNER) {
                        val warehouseQuotaCounts = activeQuotas.groupBy { it.warehouse ?: "نامشخص" }.mapValues { it.value.size }
                        compareByDescending<Triple<String, List<QuotaCompletionData>, Float>> {
                            val parts = it.first.split("|")
                            val warehouse = parts.getOrNull(2)?.trim() ?: "نامشخص"
                            warehouseQuotaCounts[warehouse] ?: 0
                        }
                        .thenBy {
                            val parts = it.first.split("|")
                            parts.getOrNull(2)?.trim() ?: "نامشخص"
                        }
                        .thenByDescending { it.third }
                    } else {
                        compareByDescending<Triple<String, List<QuotaCompletionData>, Float>> { it.second.size }
                            .thenByDescending { it.third }
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                flingBehavior = ScrollableDefaults.flingBehavior(),
                userScrollEnabled = true
            ) {
                items(
                    items = sortedGroups,
                    key = { (groupName, _, _) -> "group_$groupName" }
                ) { (groupName, quotas, _) ->
                    AnalyticsQuotaGroupExpansionPanel(
                        groupName = groupName,
                        quotas = quotas,
                        groupingMode = groupingMode,
                        isExpanded = expandedGroup == groupName,
                        onExpandChange = { shouldExpand ->
                            expandedGroup = if (shouldExpand) groupName else null
                        },
                        onOwnerLongClick = { owner, ownerQuotas ->
                            selectedOwnerQuotas = owner to ownerQuotas
                        }
                    )
                }
            }
        }
    }

    selectedOwnerQuotas?.let { (owner, quotas) ->
        OwnerQuotasDialog(
            owner = owner,
            quotas = quotas,
            onDismiss = { selectedOwnerQuotas = null }
        )
    }
}

@Composable
private fun AnalyticsGroupingModeButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = if (!isSelected) BorderStroke(1.dp, borderColor) else null,
        tonalElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AnalyticsQuotaGroupExpansionPanel(
    groupName: String,
    quotas: List<QuotaCompletionData>,
    groupingMode: QuotaGroupingMode,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onOwnerLongClick: (String, List<QuotaCompletionData>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalWeight = quotas.sumOf { it.last_24h_weight.toDouble() }.toFloat()
    val totalVouchers = quotas.sumOf { it.last_24h_vouchers }

    val groupIcon = when (groupingMode) {
        QuotaGroupingMode.BY_CARGO_OWNER -> Icons.Default.Person
        QuotaGroupingMode.BY_SHIP -> Icons.Default.DirectionsBoat
        QuotaGroupingMode.BY_CARRIER -> Icons.Default.LocalShipping
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onExpandChange(!isExpanded) },
                        onLongClick = {
                            val totalGroupVouchers = quotas.sumOf { it.last_24h_vouchers }
                            val totalGroupWeight = quotas.sumOf { it.last_24h_weight.toDouble() }.toFloat()
                            
                            val shareText = buildString {
                                val groupTitle = if (groupingMode == QuotaGroupingMode.BY_CARGO_OWNER) {
                                    val parts = groupName.split("|")
                                    if (parts.size >= 3) "کشتی: ${parts[0]} | کالا: ${parts[1]} | انبار: ${parts[2]}" 
                                    else if (parts.size >= 2) "کشتی: ${parts[0]} | انبار: ${parts[1]}" else groupName
                                } else if (groupingMode == QuotaGroupingMode.BY_SHIP) {
                                    val parts = groupName.split("|")
                                    if (parts.size >= 2) "کشتی: ${parts[0]} | کالا: ${parts[1]}" else groupName
                                } else groupName
                                appendLine("🔹 اطلاعات $groupTitle")
                                appendLine("   تعداد کوتاژ: ${quotas.size} | تعداد حواله: $totalGroupVouchers | تناژ کل: ${formatNumber(totalGroupWeight.roundToInt())} تن")
                                appendLine()
                                if (groupingMode == QuotaGroupingMode.BY_CARGO_OWNER) {
                                    val ownerSummaries = quotas.groupBy { it.cargoOwner ?: "نامشخص" }
                                        .map { (owner, ownerQuotas) ->
                                            Triple(
                                                owner,
                                                ownerQuotas.sumOf { it.last_24h_vouchers },
                                                ownerQuotas.sumOf { it.last_24h_weight.toDouble() }.toFloat()
                                            )
                                        }.sortedByDescending { it.third }
                                    
                                    ownerSummaries.forEach { (owner, vc, tw) ->
                                        appendLine("   👤 $owner: $vc حواله | ${formatNumber(tw.roundToInt())} تن")
                                    }
                                } else {
                                    quotas.forEach { q ->
                                        appendLine("   🔸 کوتاژ ${q.loadingQuotaNumber}: ${q.last_24h_vouchers} حواله | ${formatNumber(q.last_24h_weight.roundToInt())} تن")
                                    }
                                }
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "ارسال اطلاعات")
                            context.startActivity(shareIntent)
                        }
                    ),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (groupingMode) {
                            QuotaGroupingMode.BY_CARGO_OWNER -> {
                                val parts = groupName.split("|")
                                Text(
                                    text = when (parts.size) {
                                        3 -> "${parts[0]} | ${parts[1]} | ${parts[2]}"
                                        2 -> "${parts[0]} | ${parts[1]}"
                                        else -> groupName
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            QuotaGroupingMode.BY_SHIP -> {
                                val parts = groupName.split("|")
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = parts[0],
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (parts.size >= 2 && parts[1].isNotBlank() && parts[1] != "نامشخص") {
                                        Text(
                                            text = "|",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = parts[1],
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                            else -> {
                                Text(
                                    text = groupName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
                                        Color(0xFF1e293b) else Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = groupIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnalyticsStatChip(
                            value = formatNumber(totalVouchers),
                            label = "حواله"
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        AnalyticsStatChip(
                            value = formatNumber(totalWeight.roundToInt()),
                            label = "تن"
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        AnalyticsStatChip(
                            value = "${quotas.size}",
                            label = "کوتاژ"
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (groupingMode == QuotaGroupingMode.BY_CARGO_OWNER) {
                        val ownerSummaries = quotas.groupBy { it.cargoOwner ?: "نامشخص" }
                            .map { (owner, ownerQuotas) ->
                                Triple(
                                    owner,
                                    ownerQuotas.sumOf { it.last_24h_vouchers },
                                    ownerQuotas.sumOf { it.last_24h_weight.toDouble() }.toFloat()
                                )
                            }.sortedByDescending { it.third }

                        ownerSummaries.forEach { (owner, voucherCount, totalWeight) ->
                            AnalyticsOwnerSummaryCard(
                                owner = owner,
                                voucherCount = voucherCount,
                                totalWeight = totalWeight,
                                onLongClick = {
                                    val ownerQuotas = quotas.filter { (it.cargoOwner ?: "نامشخص") == owner }
                                    onOwnerLongClick(owner, ownerQuotas)
                                }
                            )
                        }
                    } else {
                        quotas.forEach { quota ->
                            AnalyticsQuotaCard(
                                quota = quota,
                                groupingMode = groupingMode
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsStatChip(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
            Color(0xFF1e293b) else Color(0xFFEFF6FF),
        border = BorderStroke(0.5.dp, if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
            Color(0xFF334155) else Color(0xFF93C5FD))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
                    Color(0xFF93c5fd) else Color(0xFF1E40AF),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
                    Color(0xFF93c5fd) else Color(0xFF1E40AF)
            )
        }
    }
}

@Composable
private fun AnalyticsQuotaCard(
    quota: QuotaCompletionData,
    groupingMode: QuotaGroupingMode,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = quota.loadingQuotaNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = when (groupingMode) {
                            QuotaGroupingMode.BY_SHIP -> quota.shippingCompany
                            QuotaGroupingMode.BY_CARRIER -> quota.shipName
                            QuotaGroupingMode.BY_CARGO_OWNER -> quota.shippingCompany
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = quota.cargoOwner ?: "نامشخص",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = quota.warehouse ?: "نامشخص",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
                                    Color(0xFF334155) else Color(0xFFF9FAFB),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
                                    Color(0xFF475569) else Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${formatNumber(quota.last_24h_vouchers)} حواله",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
                                Color(0xFF94a3b8) else Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
                                    Color(0xFF334155) else Color(0xFFF9FAFB),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
                                    Color(0xFF475569) else Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${formatNumber(quota.last_24h_weight.roundToInt())} تن",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
                                Color(0xFF94a3b8) else Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (quota.cargoType?.isNotBlank() == true) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
                                        Color(0xFF1e293b) else Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
                                        Color(0xFF2563eb).copy(alpha = 0.3f) else Color(0xFF3B82F6).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = quota.cargoType,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
                                    Color(0xFF60a5fa) else Color(0xFF2563EB),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AnalyticsOwnerSummaryCard(
    owner: String,
    voucherCount: Int,
    totalWeight: Float,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
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
                        .size(24.dp)
                        .background(
                            color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
                                Color(0xFF334155) else Color(0xFFF3F4F6),
                            shape = RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = owner,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnalyticsStatChip(
                    value = formatNumber(voucherCount),
                    label = "حواله"
                )
                AnalyticsStatChip(
                    value = formatNumber(totalWeight.toInt()),
                    label = "تن"
                )
            }
        }
    }
}

@Composable
private fun OwnerQuotasDialog(
    owner: String,
    quotas: List<QuotaCompletionData>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "کوتاژهای مرتبط",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = owner,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(
                        items = quotas,
                        key = { quota -> quota.loadingQuotaNumber + "_" + quota.shipName }
                    ) { quota ->
                        AnalyticsQuotaCard(
                            quota = quota,
                            groupingMode = QuotaGroupingMode.BY_SHIP
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Text(
                        text = "بستن",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorStateCard(
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "خطا در بارگذاری داده‌ها",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyStateCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "داده‌ای موجود نیست",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
