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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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

// این فایل بخش «تحلیل کوتاژها» (QuotaAnalysis) و تمام کامپوننت‌های وابسته‌اش را از
// ComprehensiveAnalyticsDialog.kt جدا نگه می‌دارد (A1-6، بازسازی ساختاری). وابسته به پالت
// رنگ internal و EmptyStateCard تعریف‌شده در ComprehensiveAnalyticsDialog.kt که چون
// هم‌پکیج هستند نیازی به import ندارند.

@Composable
private fun SearchField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "جستجو بر اساس نام کشتی، شماره...",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Surface(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(11.dp),
        color = AnalyticsMutedBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = AnalyticsMutedText,
                modifier = Modifier.size(18.dp)
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodySmall,
                        color = AnalyticsMutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = AnalyticsTitleColor
                    ),
                    cursorBrush = SolidColor(AnalyticsAccent)
                )
            }

            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "پاک کردن",
                        tint = AnalyticsMutedText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
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
                    tint = AnalyticsAccent
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
    val backgroundColor = if (isSelected) AnalyticsAccentBg else Color.Transparent
    val contentColor = if (isSelected) AnalyticsAccent else AnalyticsMutedText
    val borderColor = if (isSelected) AnalyticsAccentBorder else MaterialTheme.colorScheme.outlineVariant

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
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
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, AnalyticsCardBorder)
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
                                .size(28.dp)
                                .background(
                                    color = AnalyticsAccentBg,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = groupIcon,
                                contentDescription = null,
                                tint = AnalyticsAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnalyticsStatChip(
                            value = "${quotas.size}",
                            label = "کوتاژ",
                            modifier = Modifier.weight(1f)
                        )

                        AnalyticsStatChip(
                            value = formatNumber(totalWeight.roundToInt()),
                            label = "تن",
                            modifier = Modifier.weight(1f)
                        )

                        AnalyticsStatChip(
                            value = formatNumber(totalVouchers),
                            label = "حواله",
                            modifier = Modifier.weight(1f)
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
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
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
                                groupingMode = groupingMode,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
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
        shape = RoundedCornerShape(9.dp),
        color = AnalyticsMutedBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = AnalyticsTitleColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = AnalyticsMutedText
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
            containerColor = AnalyticsMutedBg
        ),
        border = BorderStroke(1.dp, AnalyticsCardBorder)
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
                        color = AnalyticsTitleColor
                    )

                    Text(
                        text = when (groupingMode) {
                            QuotaGroupingMode.BY_SHIP -> quota.shippingCompany
                            QuotaGroupingMode.BY_CARRIER -> quota.shipName
                            QuotaGroupingMode.BY_CARGO_OWNER -> quota.shippingCompany
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AnalyticsAccent
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
                            tint = AnalyticsMutedText,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = quota.cargoOwner ?: "نامشخص",
                            style = MaterialTheme.typography.bodySmall,
                            color = AnalyticsMutedText,
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
                            tint = AnalyticsMutedText,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = quota.warehouse ?: "نامشخص",
                            style = MaterialTheme.typography.bodySmall,
                            color = AnalyticsMutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = AnalyticsAccentBg,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${formatNumber(quota.last_24h_vouchers)} حواله",
                            style = MaterialTheme.typography.labelSmall,
                            color = AnalyticsAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = AnalyticsAccentBg,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${formatNumber(quota.last_24h_weight.roundToInt())} تن",
                            style = MaterialTheme.typography.labelSmall,
                            color = AnalyticsAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (quota.cargoType?.isNotBlank() == true) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = AnalyticsCardBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = quota.cargoType,
                                style = MaterialTheme.typography.labelSmall,
                                color = AnalyticsTitleColor,
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
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            color = AnalyticsCardBorder,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
                .padding(vertical = 9.dp, horizontal = 12.dp),
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
                    .size(22.dp)
                    .background(color = AnalyticsMutedBg, shape = RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AnalyticsAccent,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text = owner,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = AnalyticsTitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(AnalyticsAccentBg, RoundedCornerShape(8.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${formatNumber(totalWeight.toInt())} تن",
                    style = MaterialTheme.typography.labelSmall,
                    color = AnalyticsAccent,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .background(AnalyticsAccentBg, RoundedCornerShape(8.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${formatNumber(voucherCount)} حواله",
                    style = MaterialTheme.typography.labelSmall,
                    color = AnalyticsAccent,
                    fontWeight = FontWeight.Bold
                )
            }
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
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = AnalyticsScreenBg
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AnalyticsMutedBg)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بستن",
                            tint = AnalyticsTitleColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AnalyticsAccentBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AnalyticsAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "کوتاژهای مرتبط",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AnalyticsTitleColor
                        )
                        Text(
                            text = owner,
                            style = MaterialTheme.typography.labelSmall,
                            color = AnalyticsMutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                HorizontalDivider(color = AnalyticsCardBorder.copy(alpha = 0.7f))

                if (quotas.isEmpty()) {
                    EmptyStateCard("کوتاژی برای این صاحب کالا یافت نشد")
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
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
                }
            }
        }
        }
    }
}
