package com.atk.atk_cargo.feature.chat.data

import kotlinx.coroutines.flow.Flow

// انتزاع نازک روی تنظیمات ظاهری چت، جدا از UserPreferencesStore مشترک auth/admin
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
