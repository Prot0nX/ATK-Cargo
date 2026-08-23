package com.atk.atk_cargo.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// سیستم رنگ‌های معنایی برای وضعیت‌های عملیاتی ATK-Cargo (ورودی، خروجی، بارگیری، تکمیل، هشدارها و غیره)
@Immutable
data class SemanticColors(
 // وضعیت‌های بارگیری و عملیات کشتیرانی
    val cargoEntry: Color,
    val onCargoEntry: Color,
    val cargoEntryContainer: Color,
    val onCargoEntryContainer: Color,

    val cargoExit: Color,
    val onCargoExit: Color,
    val cargoExitContainer: Color,
    val onCargoExitContainer: Color,

    val shipLoading: Color,
    val onShipLoading: Color,
    val shipLoadingContainer: Color,
    val onShipLoadingContainer: Color,

    val shipCompleted: Color,
    val onShipCompleted: Color,
    val shipCompletedContainer: Color,
    val onShipCompletedContainer: Color,

 // وضعیت‌های عمومی (Success, Warning, Error, Info)
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,

    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,

    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,

 // رنگ‌های سیستم، کارت‌ها و حاشیه‌ها
    val borderSubtle: Color,
    val borderStrong: Color,
    val cardBackground: Color,
    val dialogBackground: Color,
    val bottomSheetBackground: Color,
    val inputBackground: Color,
    val inputBorder: Color,

 // حالت‌های تعاملی
    val stateDisabled: Color,
    val onStateDisabled: Color,
    val statePressedAlpha: Float = 0.12f,
    val stateHoverAlpha: Float = 0.08f,
    val stateFocusAlpha: Float = 0.12f
)

val LightSemanticColors = SemanticColors(
    cargoEntry = Rose500,
    onCargoEntry = Color.White,
    cargoEntryContainer = Rose950,
    onCargoEntryContainer = Rose400,

    cargoExit = Emerald500,
    onCargoExit = Color.White,
    cargoExitContainer = Emerald950,
    onCargoExitContainer = Emerald400,

    shipLoading = Blue500,
    onShipLoading = Color.White,
    shipLoadingContainer = Blue50,
    onShipLoadingContainer = Blue700,

    shipCompleted = Green600,
    onShipCompleted = Color.White,
    shipCompletedContainer = Green50,
    onShipCompletedContainer = Green800,

    success = Green600,
    onSuccess = Color.White,
    successContainer = Green50,
    onSuccessContainer = Green800,

    warning = Amber700,
    onWarning = Color.White,
    warningContainer = Amber50,
    onWarningContainer = Amber900,

    info = Blue500,
    onInfo = Color.White,
    infoContainer = Blue50,
    onInfoContainer = Blue700,

    borderSubtle = BorderLight,
    borderStrong = Gray400,
    cardBackground = SurfaceLight,
    dialogBackground = SurfaceLight,
    bottomSheetBackground = SurfaceLight,
    inputBackground = BackgroundLight,
    inputBorder = BorderLight,

    stateDisabled = Gray300,
    onStateDisabled = Gray500
)

val DarkSemanticColors = SemanticColors(
    cargoEntry = Rose400,
    onCargoEntry = Rose950,
    cargoEntryContainer = Rose950,
    onCargoEntryContainer = Rose400,

    cargoExit = Emerald400,
    onCargoExit = Emerald950,
    cargoExitContainer = Emerald950,
    onCargoExitContainer = Emerald400,

    shipLoading = Blue400,
    onShipLoading = Slate950,
    shipLoadingContainer = Slate800,
    onShipLoadingContainer = Blue300,

    shipCompleted = Green400,
    onShipCompleted = Slate950,
    shipCompletedContainer = Slate800,
    onShipCompletedContainer = Green300,

    success = Green400,
    onSuccess = Slate950,
    successContainer = Slate800,
    onSuccessContainer = Green300,

    warning = Amber200,
    onWarning = Slate950,
    warningContainer = Slate800,
    onWarningContainer = Amber100,

    info = Blue400,
    onInfo = Slate950,
    infoContainer = Slate800,
    onInfoContainer = Blue300,

    borderSubtle = BorderDark,
    borderStrong = BorderDark3,
    cardBackground = SurfaceDark,
    dialogBackground = SurfaceDark,
    bottomSheetBackground = SurfaceDark,
    inputBackground = SurfaceVariantDark,
    inputBorder = BorderDark,

    stateDisabled = Slate700,
    onStateDisabled = Slate600
)

val LocalSemanticColors = staticCompositionLocalOf { LightSemanticColors }
