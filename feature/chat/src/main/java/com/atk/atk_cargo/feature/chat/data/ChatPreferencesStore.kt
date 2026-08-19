package com.atk.atk_cargo.feature.chat.data

import kotlinx.coroutines.flow.Flow

/**
 * انتزاع نازک روی UserPreferencesManager، مخصوص تنظیمات ظاهری چت — همان
 * الگوی UserPreferencesStore (core:domain) برای auth/admin، ولی این یکی
 * عمداً در core:domain تعریف نشد چون این ۶ عضو (فونت/رنگ/پس‌زمینه/شکل
 * حباب چت) فقط مصرف‌کننده‌ی feature:chat دارند؛ اضافه‌کردنشان به اینترفیس
 * مشترک auth/admin نقض interface segregation بود (DEEP_CODE_AUDIT.md
 * #Phase5.13).
 */
interface ChatPreferencesStore {
    val username: Flow<String>
    val chatFontSize: Flow<Int>
    val chatMyBubbleColor: Flow<Long>
    val chatOtherBubbleColor: Flow<Long>
    val chatBackgroundId: Flow<Int>
    val chatBubbleShape: Flow<Int>

    suspend fun saveChatSettings(
        fontSize: Int,
        myColor: Long,
        otherColor: Long,
        backgroundId: Int,
        bubbleShape: Int
    )
}
