package com.atk.atk_cargo.feature.cargo.domain

import android.util.Log
import com.atk.atk_cargo.api.RetrofitClient.apiService
import com.atk.atk_cargo.data.model.InitialInfo
import com.atk.atk_cargo.data.model.MessageType
import com.atk.atk_cargo.data.model.QuotaValidationResult
import com.atk.atk_cargo.data.repository.ReportsRepository

class QuotaValidationUseCase(private val repository: ReportsRepository) {

    companion object {
        // فاصله‌ی ایمنی قبل از رسیدن دقیق به حد نصاب درصدی: وقتی «تناژ مجاز»
        // باقی‌مانده به این مقدار یا کمتر برسد، دیگر حوالهٔ تازه پذیرفته
        // نمی‌شود — چون یک محمولهٔ تک بعدی معمولاً چند تن است و می‌تواند
        // به‌سادگی از حد نصاب رد شود. باید با CargoService::NEW_ENTRY_TONNAGE_BUFFER_KG
        // (سمت سرور، ضامن واقعی) یکسان بماند.
        const val NEW_ENTRY_TONNAGE_BUFFER_KG = 5000f
    }

    suspend fun validateQuotaStatusAndPercentage(initialInfo: InitialInfo, isNewCargo: Boolean): QuotaValidationResult {
        try {
            val quotaStatus = repository.checkQuotaStatus(
                quotaNumber = initialInfo.loadingQuotaNumber.toString(),
                shipName = initialInfo.shipName,
                cargoType = initialInfo.cargoType,
                shippingCompany = initialInfo.shippingCompany,
                warehouse = initialInfo.loadingWarehouse
            )

            if (!quotaStatus.isActive) {
                return QuotaValidationResult(
                    isValid = false,
                    isActive = false,
                    percentageReached = false,
                    message = quotaStatus.message ?: "کوتاژ غیرفعال است",
                    messageType = MessageType.ERROR
                )
            }

            val response = apiService.getShipQuotas(shipName = initialInfo.shipName)
            if (response.isSuccessful) {
                val quotas = response.body()
                val quota = quotas?.find { it.number == initialInfo.loadingQuotaNumber.toString() }
                
                if (quota != null && quota.isPercentageRestricted == true && quota.percentage != null) {
                    val percentageAmount = quota.totalTonnage * (quota.percentage / 100)
                    val remainingTonnage = quota.remainingTonnage
                    val loadableTonnage = remainingTonnage - percentageAmount

                    if (remainingTonnage <= percentageAmount) {
                        // دقیقاً به حد نصاب رسیده — کوتاژ کاملاً غیرفعال
                        // می‌شود (رفتار قبلی، بدون تغییر). این باعث می‌شود
                        // خروج حواله‌های موجود هم مسدود شود.
                        return QuotaValidationResult(
                            isValid = false,
                            isActive = false,
                            percentageReached = true,
                            message = "امکان ثبت حواله جدید وجود ندارد",
                            messageType = MessageType.ERROR,
                            quotaIdToToggle = quota.id,
                            warningMessage = "کوتاژ ${quota.number} به حد نصاب ${quota.percentage}% رسیده است و غیرفعال خواهد شد"
                        )
                    }

                    // بافر هشدار زودهنگام: فقط ثبت حوالهٔ تازه را می‌بندد و
                    // کوتاژ را غیرفعال نمی‌کند، پس خروج/به‌روزرسانی حواله‌های
                    // از قبل ثبت‌شده حتی زیر این آستانه هم مجاز می‌ماند.
                    if (isNewCargo && loadableTonnage <= NEW_ENTRY_TONNAGE_BUFFER_KG) {
                        return QuotaValidationResult(
                            isValid = false,
                            isActive = true,
                            percentageReached = false,
                            message = "تناژ مجاز باقی‌مانده برای این کوتاژ کمتر از ${NEW_ENTRY_TONNAGE_BUFFER_KG.toInt()} کیلوگرم است. امکان ثبت حوالهٔ جدید وجود ندارد.",
                            messageType = MessageType.ERROR
                        )
                    }
                }
            } else {
                Log.e("ATK-Log", "QuotaValidationUseCase: Failed to fetch quotas for percentage check: ${response.code()}")
            }

            return QuotaValidationResult(
                isValid = true,
                isActive = true,
                percentageReached = false,
                message = "کوتاژ فعال و مجاز است",
                messageType = MessageType.SUCCESS
            )
        } catch (e: Exception) {
            return QuotaValidationResult(
                isValid = false,
                isActive = false,
                percentageReached = false,
                message = "خطا در بررسی وضعیت کوتاژ: ${e.message}",
                messageType = MessageType.ERROR
            )
        }
    }

    fun validateTempTonnage(initialInfo: InitialInfo): TempTonnageValidationResult {
        if (initialInfo.tempTonnageStatus && initialInfo.tempTonnageAmount != null) {
            if (initialInfo.tempTonnageAmount <= 0) {
                return TempTonnageValidationResult(
                    isValid = false,
                    errorMessage = "تناژ موقت به پایان رسیده است. امکان ثبت حواله جدید یا خروج وجود ندارد. لطفاً با مسئول خود بررسی کنید."
                )
            }
        }
        return TempTonnageValidationResult(isValid = true)
    }

    fun validateInputData(
        trackingNumber: String,
        netWeight: String,
        numberOfPeople: String,
        shortageWeight: String,
        excessWeight: String,
        scaleReceiptNumber: String
    ): InputValidationResult {
        if (trackingNumber.isBlank()) {
            return InputValidationResult(isValid = false, errorMessage = "شماره حواله نمی‌تواند خالی باشد.")
        }

        if (netWeight.isNotBlank()) {
            val weight = netWeight.toFloatOrNull()
            if (weight == null || weight <= 0) {
                return InputValidationResult(isValid = false, errorMessage = "وزن خالص باید عددی مثبت باشد.")
            }

            if (weight !in 1000.0..45000.0) {
                return InputValidationResult(isValid = false, errorMessage = "وزن خالص باید بین 1000 تا 45000 کیلوگرم باشد.")
            }

            if (scaleReceiptNumber.isBlank()) {
                return InputValidationResult(isValid = false, errorMessage = "برای ثبت خروج، شماره قبض باسکول الزامی است.")
            }

            if (scaleReceiptNumber.length !in 8..10) {
                return InputValidationResult(isValid = false, errorMessage = "شماره قبض باسکول باید بین 8 تا 10 رقم باشد.")
            }
        }

        if (shortageWeight.isNotBlank()) {
            val shortage = shortageWeight.toFloatOrNull()
            if (shortage == null || shortage < 0) {
                return InputValidationResult(isValid = false, errorMessage = "کسری بار باید عددی مثبت یا صفر باشد.")
            }
        }

        if (excessWeight.isNotBlank()) {
            val excess = excessWeight.toFloatOrNull()
            if (excess == null || excess < 0) {
                return InputValidationResult(isValid = false, errorMessage = "اضافه بار باید عددی مثبت یا صفر باشد.")
            }
        }
        return InputValidationResult(isValid = true)
    }
}

data class TempTonnageValidationResult(val isValid: Boolean, val errorMessage: String? = null)
data class InputValidationResult(val isValid: Boolean, val errorMessage: String? = null)
