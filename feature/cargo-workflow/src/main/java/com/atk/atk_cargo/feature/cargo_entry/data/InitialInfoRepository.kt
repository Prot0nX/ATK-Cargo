package com.atk.atk_cargo.feature.cargo_entry.data

import com.atk.atk_cargo.data.model.CheckExistenceRequest
import com.atk.atk_cargo.data.model.InitialInfo
import com.atk.atk_cargo.feature.cargo_entry.presentation.ExistenceCheckStatus

// قرارداد لایه داده ثبت اطلاعات اولیه که ارتباط مستقیم InitialInfoViewModel با ApiServiceV2 را حذف می‌کند
interface InitialInfoRepository {

 // null یعنی پاسخ سرور وضعیت شناخته‌شده‌ای نداشت
    suspend fun checkExistence(request: CheckExistenceRequest): ExistenceCheckStatus?

    suspend fun submitInitialInfo(info: InitialInfo): Boolean
}
