package com.atk.atk_cargo.api

object OpenRouterConfig {
    
    const val API_KEY = "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"
    
    // اگر API Key معتبر نیست، از ML Kit استفاده خواهد شد
    fun isApiKeyValid(): Boolean {
        return API_KEY != "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" && API_KEY.isNotBlank()
    }
} 