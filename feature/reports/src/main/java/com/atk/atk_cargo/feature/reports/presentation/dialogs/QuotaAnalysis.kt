package com.atk.atk_cargo.feature.reports.presentation.dialogs

import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atk.atk_cargo.data.model.QuotaCompletionData
import com.atk.atk_cargo.data.model.QuotaGroupingMode
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.feature.reports.viewmodel.ReportsViewModel
import kotlin.math.roundToInt

// صفحه‌ی اصلی «تحلیل کوتاژها» با گروه‌بندی و جستجو — از QuotaAnalysisSection.kt جدا شد (فاز۴ #۴۰)
@Composable
fun QuotaAnalysis(
    viewModel: ReportsViewModel
) {
    val context = LocalContext.current

    var expandedGroup by remember { mutableStateOf<String?>(null) }
    var selectedOwnerQuotas by remember { mutableStateOf<Pair<String, List<QuotaCompletionData>>?>(null) }
    // اشتراک‌گذاری کل مثل اشتراک‌گذاری تک‌گروه حالا نیاز به تأیید کاربر دارد، نه بازشدن بی‌درنگ chooser
    var pendingAllShareText by remember { mutableStateOf<String?>(null) }
    val groupingMode by viewModel.groupingMode.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    // فیلتر و گروه‌بندی/مرتب‌سازی دیگر اینجا تکرار نمی‌شود؛ همه در ReportsViewModel.analyticsGroups محاسبه شده است
    val groups by viewModel.analyticsGroups.collectAsStateWithLifecycle()

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
                    // گروه‌بندی/مرتب‌سازی و عنوان دیگر اینجا تکرار نمی‌شود؛ از groups و QuotaGroup.shareTitle مشترک استفاده می‌شود
                    val shareText = buildString {
                        val modeStr = when (groupingMode) {
                            QuotaGroupingMode.BY_CARGO_OWNER -> "صاحب کالا"
                            QuotaGroupingMode.BY_SHIP -> "کشتی"
                            QuotaGroupingMode.BY_CARRIER -> "باربری"
                        }
                        appendLine("📊 تحلیل جامع عملیات - دسته بندی: $modeStr\n")
                        groups.forEach { group ->
                            appendLine("🔹 ${group.shareTitle}")
                            appendLine("   تعداد کوتاژ: ${group.quotas.size} | تعداد حواله: ${group.totalVouchers} | تناژ کل: ${formatNumber(group.totalWeight.roundToInt())} تن")
                            appendLine()
                        }
                    }
                    pendingAllShareText = shareText
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

        if (groups.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "کوتاژ فعالی در این روز کاری وجود ندارد",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                flingBehavior = ScrollableDefaults.flingBehavior(),
                userScrollEnabled = true
            ) {
                items(
                    items = groups,
                    key = { group -> "group_${group.key}" }
                ) { group ->
                    AnalyticsQuotaGroupExpansionPanel(
                        group = group,
                        isExpanded = expandedGroup == group.key,
                        onExpandChange = { shouldExpand ->
                            expandedGroup = if (shouldExpand) group.key else null
                        },
                        onOwnerLongClick = { owner, ownerQuotas ->
                            selectedOwnerQuotas = owner to ownerQuotas
                        },
                        onShareConfirmed = { text, scope ->
                            viewModel.logAnalyticsExport(scope, 1)
                            shareAnalyticsText(context, text)
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

    pendingAllShareText?.let { text ->
        val modeStr = when (groupingMode) {
            QuotaGroupingMode.BY_CARGO_OWNER -> "صاحب کالا"
            QuotaGroupingMode.BY_SHIP -> "کشتی"
            QuotaGroupingMode.BY_CARRIER -> "باربری"
        }
        ShareConfirmDialog(
            onDismiss = { pendingAllShareText = null },
            onConfirm = {
                viewModel.logAnalyticsExport("همه گروه‌ها ($modeStr)", groups.size)
                shareAnalyticsText(context, text)
                pendingAllShareText = null
            }
        )
    }
}
