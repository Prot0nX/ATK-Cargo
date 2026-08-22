package com.atk.atk_cargo.startup.data

// قرارداد بررسی اعتبار نشست که ارتباط مستقیم StartupViewModel با ApiServiceV2/RetrofitClient را حذف می‌کند (DEEP_CODE_AUDIT.md فاز۳ #۲۱)
interface StartupSessionRepository {
    suspend fun checkSession(username: String, deviceId: String, sessionToken: String?): SessionCheckOutcome
}

sealed class SessionCheckOutcome {
    data object Valid : SessionCheckOutcome()
    data object Invalid : SessionCheckOutcome()
    // خطای شبکه یا سرور (IOException یا کد ۵xx)؛ فراخوان‌کننده باید دوره‌ی فیض آفلاین را بررسی کند
    data object Unreachable : SessionCheckOutcome()
}
