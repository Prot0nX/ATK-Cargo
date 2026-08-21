package com.atk.atk_cargo.feature.cargo.domain

import android.util.Log
import com.atk.atk_cargo.data.model.MessageType
import com.atk.atk_cargo.data.model.QuotaValidationResult
import com.atk.atk_cargo.domain.model.QuotaInfo
import com.atk.atk_cargo.domain.repository.QuotaRepository

// وابسته به اینترفیس QuotaRepository (core:domain) نه کلاس مشخص ReportsRepository، تا جهت وابستگی ماژول‌ها برعکس نشود
class QuotaValidationUseCase(private val repository: QuotaRepository) {

    companion object {
        const val NEW_ENTRY_TONNAGE_BUFFER_KG = 7000f
    }

    suspend fun validateQuotaStatusAndPercentage(initialInfo: QuotaInfo, isNewCargo: Boolean): QuotaValidationResult {
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

            val quotas = try {
                repository.getShipQuotas(initialInfo.shipName)
            } catch (e: Exception) {
                Log.e("ATK-Log", "QuotaValidationUseCase: Failed to fetch quotas for percentage check: ${e.message}")
                null
            }
            if (quotas != null) {
                val quota = quotas.find { it.number == initialInfo.loadingQuotaNumber.toString() }

                val quotaPercentage = quota?.percentage
                if (quota != null && quota.isPercentageRestricted == true && quotaPercentage != null) {
                    val percentageAmount = quota.totalTonnage * (quotaPercentage / 100)
                    val remainingTonnage = quota.remainingTonnage
                    val loadableTonnage = remainingTonnage - percentageAmount

                    if (remainingTonnage <= percentageAmount) {

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

    fun validateTempTonnage(initialInfo: QuotaInfo): TempTonnageValidationResult {
        // متغیر محلی لازم است چون property از ماژول دیگری می‌آید و Kotlin نمی‌تواند بعد از != null آن را smart-cast کند
        val tempTonnageAmount = initialInfo.tempTonnageAmount
        if (initialInfo.tempTonnageStatus && tempTonnageAmount != null) {
            if (tempTonnageAmount <= 0) {
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
