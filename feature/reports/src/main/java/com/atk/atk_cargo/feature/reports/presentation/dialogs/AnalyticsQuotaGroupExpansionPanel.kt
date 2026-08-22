package com.atk.atk_cargo.feature.reports.presentation.dialogs

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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.data.model.QuotaCompletionData
import com.atk.atk_cargo.data.model.QuotaGroupingMode
import com.atk.atk_cargo.feature.reports.domain.QuotaGroup
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import kotlin.math.roundToInt

// پنل باز/بسته‌شونده‌ی هر گروه در بخش تحلیل کوتاژها — از QuotaAnalysisSection.kt جدا شد (فاز۴ #۴۰)
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AnalyticsQuotaGroupExpansionPanel(
    group: QuotaGroup,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onOwnerLongClick: (String, List<QuotaCompletionData>) -> Unit,
    onShareConfirmed: (text: String, scope: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quotas = group.quotas
    val totalWeight = group.totalWeight
    val totalVouchers = group.totalVouchers

    // به‌جای بازکردن بی‌درنگ chooser اشتراک‌گذاری با long-press ناخواسته، فقط متن آماده و منتظر تأیید کاربر می‌ماند
    var pendingShareText by remember { mutableStateOf<String?>(null) }

    val groupIcon = when (group.mode) {
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
                            // عنوان از group.shareTitle می‌آید؛ دیگر نیازی به split("|") یا بازسازی groupTitle نیست
                            val shareText = buildString {
                                appendLine("🔹 اطلاعات ${group.shareTitle}")
                                appendLine("   تعداد کوتاژ: ${quotas.size} | تعداد حواله: $totalVouchers | تناژ کل: ${formatNumber(totalWeight.roundToInt())} تن")
                                appendLine()
                                if (group.mode == QuotaGroupingMode.BY_CARGO_OWNER) {
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
                            pendingShareText = shareText
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
                        // دیگر split("|") روی کلید رشته‌ای نیست؛ فیلدهای تایپ‌شده group.ship/cargoType/warehouse/carrier مستقیم خوانده می‌شوند
                        when (group.mode) {
                            QuotaGroupingMode.BY_CARGO_OWNER -> {
                                Text(
                                    text = "${group.ship} | ${group.cargoType} | ${group.warehouse}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            QuotaGroupingMode.BY_SHIP -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = group.ship.orEmpty(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val cargoType = group.cargoType
                                    if (!cargoType.isNullOrBlank() && cargoType != "نامشخص") {
                                        Text(
                                            text = "|",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = cargoType,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                            QuotaGroupingMode.BY_CARRIER -> {
                                Text(
                                    text = group.carrier.orEmpty(),
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
                    if (group.mode == QuotaGroupingMode.BY_CARGO_OWNER) {
                        val ownerSummaries = quotas.groupBy { it.cargoOwner ?: "نامشخص" }
                            .map { (owner, ownerQuotas) ->
                                Triple(
                                    owner,
                                    ownerQuotas.sumOf { it.last_24h_vouchers },
                                    ownerQuotas.sumOf { it.last_24h_weight.toDouble() }.toFloat()
                                )
                            }.sortedByDescending { it.third }

                        ownerSummaries.forEach { (owner, voucherCount, ownerTotalWeight) ->
                            AnalyticsOwnerSummaryCard(
                                owner = owner,
                                voucherCount = voucherCount,
                                totalWeight = ownerTotalWeight,
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
                                groupingMode = group.mode,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    pendingShareText?.let { text ->
        ShareConfirmDialog(
            onDismiss = { pendingShareText = null },
            onConfirm = {
                onShareConfirmed(text, group.shareTitle)
                pendingShareText = null
            }
        )
    }
}
