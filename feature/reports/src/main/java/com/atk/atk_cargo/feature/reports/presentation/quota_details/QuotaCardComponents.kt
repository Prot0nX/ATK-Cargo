package com.atk.atk_cargo.feature.reports.presentation.quota_details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.data.model.CalculationResult
import com.atk.atk_cargo.data.model.Quota
import com.atk.atk_cargo.data.model.QuotaEditData
import com.atk.atk_cargo.data.model.QuotaPercentageData
import com.atk.atk_cargo.feature.reports.domain.calculateProgress
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.feature.reports.domain.formatWeightWithDetail
import com.atk.atk_cargo.feature.reports.presentation.quota_details.components.DeleteQuotaDialog
import com.atk.atk_cargo.feature.reports.presentation.quota_details.components.ToggleQuotaStatusDialog
import com.atk.atk_cargo.ui.theme.CornerL
import java.util.Locale

// کارت تکی کوتاژ (کامل/مینیمال)، دکمه‌های اقدام، و دیالوگ ویرایش کوتاژ — از
// QuotasListScreen.kt به این فایل منتقل شد (DEEP_CODE_AUDIT.md #Phase3.7،
// شکستن God Composable). رنگ‌های Quota* و QuotaPercentageDialog در
// QuotasListScreen.kt / QuotaPercentageDialogSection.kt هستند؛ هم‌پکیج‌اند،
// نیازی به import اضافه نیست.

@Composable
fun QuotaCard(
    quota: Quota,
    isExpanded: Boolean = false,
    onExpandToggle: (Boolean) -> Unit = { _ -> },
    onEdit: (String, QuotaEditData) -> Unit,
    onToggleStatus: (Int, String) -> Unit,
    onDelete: (Quota) -> Unit,
    onPercentageChange: (QuotaPercentageData) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showToggleDialog by remember { mutableStateOf(false) }
    var showPercentageDialog by remember { mutableStateOf(false) }
    val cardColor = if (quota.isActive) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    }
    val contentColor = if (quota.isActive) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }
    val accentColor = if (quota.isActive) {
        QuotaTealAccent
    } else {
        QuotaTealAccent.copy(alpha = 0.5f)
    }
    val loadedTonnage = quota.loadedTonnage
    val remainingTonnage = quota.remainingTonnage

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandToggle(!isExpanded) },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (quota.isActive) {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (quota.isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = if (quota.isActive) "فعال" else "غیرفعال",
                            tint = if (quota.isActive) QuotaGreenAccent else QuotaRedAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = quota.number,
                            style = MaterialTheme.typography.titleMedium,
                            color = contentColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "|",
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "${formatNumber(quota.voucherCount)} حواله",
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    val calculatedValues = calculateValues(
                        totalTonnage = quota.totalTonnage,
                        percentage = quota.percentage ?: 0.0,
                        remainingTonnage = quota.remainingTonnage
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        quota.cargoType?.let { type ->
                            Text(
                                text = type,
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = " | ",
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.5f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${formatWeightWithDetail(calculatedValues.totalRemainingAfterPercentage.toFloat())} (%.2f%%)".format(quota.percentage ?: 0.0),
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "کل:",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatNumber(quota.totalTonnage.toInt()),
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مانده:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "↓ ${formatNumber(remainingTonnage.toInt())}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = QuotaTealAccentBg.copy(alpha = if (quota.isActive) 1f else 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "بارگیری:",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "↑ ${formatNumber(loadedTonnage.toInt())}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    HorizontalDivider(
                        color = contentColor.copy(alpha = 0.1f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionButton(
                            icon = Icons.Default.Edit,
                            label = "ویرایش",
                            color = QuotaTealAccent,
                            onClick = { showEditDialog = true }
                        )
                        ActionButton(
                            icon = Icons.Default.Build,
                            label = "درصد",
                            color = QuotaPurpleAccent,
                            onClick = { showPercentageDialog = true }
                        )
                        ActionButton(
                            icon = if (quota.isActive) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                            label = if (quota.isActive) "غیرفعال" else "فعال",
                            color = if (quota.isActive) QuotaRedAccent else QuotaGreenActionAccent,
                            onClick = { showToggleDialog = true }
                        )
                        ActionButton(
                            icon = Icons.Default.Delete,
                            label = "حذف",
                            color = QuotaRedDeleteAccent,
                            onClick = { showDeleteDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteQuotaDialog(
            quotaNumber = quota.number,
            onConfirm = {
                onDelete(quota)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showPercentageDialog) {
        QuotaPercentageDialog(
            quota = quota,
            onDismiss = { showPercentageDialog = false },
            onConfirm = onPercentageChange
        )
    }

    if (showToggleDialog) {
        ToggleQuotaStatusDialog(
            quotaNumber = quota.number,
            isActive = quota.isActive,
            onConfirm = {
                onToggleStatus(quota.id ?: 0, quota.number)
                showToggleDialog = false
            },
            onDismiss = { showToggleDialog = false }
        )
    }

    if (showEditDialog) {
        EditQuotaDialog(
            quotaData = QuotaEditData(
                id = quota.id ?: 0,
                quotaNumber = quota.number,
                shipName = quota.shipName ?: "",
                shippingCompany = quota.shippingCompany,
                warehouse = quota.warehouse ?: "",
                cargoType = quota.cargoType ?: "",
                totalTonnage = quota.totalTonnage
            ),
            onConfirm = { editedData ->
                onEdit(quota.number, editedData)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun MinimalQuotaCard(
    quota: Quota,
    modifier: Modifier = Modifier
) {
    val accentColor = if (quota.isActive) {
        QuotaTealAccent
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    }

    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(CornerL),
        border = BorderStroke(
            1.dp,
            if (quota.isActive) QuotaTealAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quota.number,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (quota.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = if (quota.isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (quota.isActive) QuotaGreenAccent else QuotaRedAccent,
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Scale,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${formatNumber(quota.remainingTonnage.toInt())} تن",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }

            val progress = calculateProgress(quota.loadedTonnage, quota.totalTonnage)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EditFieldColumn(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}

@Composable
private fun EditFieldBox(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    readOnly: Boolean = false,
    ltr: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val borderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            readOnly = readOnly || onClick != null,
            enabled = onClick == null,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = if (ltr) TextAlign.Left else TextAlign.Right
            ),
            keyboardOptions = keyboardOptions,
            cursorBrush = SolidColor(QuotaTealAccent)
        )
        trailing?.invoke()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuotaDialog(
    quotaData: QuotaEditData,
    onConfirm: (QuotaEditData) -> Unit,
    onDismiss: () -> Unit
) {
    var editedData by remember { mutableStateOf(quotaData) }
    var expanded by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val cargoTypes = listOf("سویا", "دانه روغنی", "ذرت", "گندم", "جو")

    fun isValidQuotaNumber(number: String): Boolean {
        return number.all { it.isDigit() } && number.length in 5..10
    }

    fun formatNumberString(number: String): String {
        return number.map { char ->
            when (char) {
                '۰' -> '0'
                '۱' -> '1'
                '۲' -> '2'
                '۳' -> '3'
                '۴' -> '4'
                '۵' -> '5'
                '۶' -> '6'
                '۷' -> '7'
                '۸' -> '8'
                '۹' -> '9'
                else -> char
            }
        }.filter { it.isDigit() || it.isLetter() || it == ' ' }.joinToString("")
    }

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
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                EditQuotaDialogHeader(quotaData.quotaNumber)

                Spacer(modifier = Modifier.height(24.dp))

                EditFieldColumn(label = "شماره کوتاژ") {
                    EditFieldBox(
                        value = editedData.quotaNumber,
                        onValueChange = {
                            editedData = editedData.copy(quotaNumber = formatNumberString(it))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = !isValidQuotaNumber(editedData.quotaNumber)
                    )
                }
                if (!isValidQuotaNumber(editedData.quotaNumber)) {
                    Text(
                        "شماره کوتاژ باید حداقل 5 رقم باشد",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                EditFieldColumn(label = "نام کشتی") {
                    EditFieldBox(
                        value = editedData.shipName,
                        onValueChange = {
                            editedData = editedData.copy(shipName = formatNumberString(it).uppercase(Locale.ROOT))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                EditFieldColumn(label = "شرکت باربری") {
                    EditFieldBox(
                        value = editedData.shippingCompany,
                        onValueChange = {
                            editedData = editedData.copy(shippingCompany = formatNumberString(it))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EditFieldColumn(label = "انبار", modifier = Modifier.weight(1f)) {
                        EditFieldBox(
                            value = editedData.warehouse,
                            onValueChange = {
                                editedData = editedData.copy(warehouse = formatNumberString(it))
                            }
                        )
                    }

                    EditFieldColumn(label = "نوع کالا", modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            EditFieldBox(
                                value = editedData.cargoType,
                                onValueChange = {},
                                onClick = { expanded = !expanded },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                                trailing = {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                cargoTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            editedData = editedData.copy(cargoType = type)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                EditFieldColumn(label = "تناژ کل") {
                    EditFieldBox(
                        value = formatNumberString(editedData.totalTonnage.toInt().toString()),
                        onValueChange = {
                            val newValue = formatNumberString(it).toIntOrNull() ?: editedData.totalTonnage.toInt()
                            editedData = editedData.copy(totalTonnage = newValue.toFloat())
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        ltr = true
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(13.dp),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                        )
                    ) {
                        Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = {
                            if (isValidQuotaNumber(editedData.quotaNumber)) {
                                showConfirmationDialog = true
                            }
                        },
                        modifier = Modifier.weight(1.4f),
                        enabled = editedData != quotaData && isValidQuotaNumber(editedData.quotaNumber),
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = QuotaTealAccent)
                    ) {
                        Text("تایید و ذخیره", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showConfirmationDialog) {
        Dialog(
            onDismissRequest = { showConfirmationDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    ConfirmationDialogHeader()

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "آیا از تغییرات انجام شده مطمئن هستید",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                onConfirm(editedData)
                                showConfirmationDialog = false
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = QuotaTealAccent)
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
                                Text("ذخیره")
                            }
                        }

                        OutlinedButton(
                            onClick = { showConfirmationDialog = false },
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
}

fun calculateValues(
    totalTonnage: Float,
    percentage: Double,
    remainingTonnage: Float
): CalculationResult {
    val percentageAmount = totalTonnage * (percentage / 100)
    val remainingAfterPercentage = totalTonnage - percentageAmount
    val totalRemainingAfterPercentage = remainingTonnage - percentageAmount

    return CalculationResult(
        percentageAmount = percentageAmount,
        remainingAfterPercentage = remainingAfterPercentage,
        totalRemainingAfterPercentage = totalRemainingAfterPercentage
    )
}

@Composable
internal fun QuotaDialogHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color = QuotaTealAccentBg, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = QuotaTealAccent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun EditQuotaDialogHeader(quotaNumber: String) {
    QuotaDialogHeader(
        icon = Icons.Default.Edit,
        title = "ویرایش اطلاعات کوتاژ",
        subtitle = "شماره کوتاژ: $quotaNumber"
    )
}

@Composable
private fun ConfirmationDialogHeader() {
    QuotaDialogHeader(
        icon = Icons.Default.Check,
        title = "تأیید ذخیره‌سازی",
        subtitle = "لطفاً تصمیم خود را تأیید کنید"
    )
}
