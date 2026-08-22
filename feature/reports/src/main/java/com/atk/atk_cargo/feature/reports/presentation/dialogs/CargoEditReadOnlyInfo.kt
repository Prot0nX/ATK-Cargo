package com.atk.atk_cargo.feature.reports.presentation.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.feature.reports.domain.formatNumber

// نمایش فقط‌خواندنی اطلاعات حواله در دیالوگ نتیجه‌ی جستجو — از CargoEditSearchDialogsSection.kt جدا شد (فاز۴ #۴۰)
@Composable
internal fun CargoMainInfo(cargoInfo: CargoInfo) {
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
internal fun CargoWeightInfo(cargoInfo: CargoInfo) {
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
internal fun CargoTimeInfo(cargoInfo: CargoInfo) {
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
internal fun CargoShippingInfo(cargoInfo: CargoInfo) {
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
