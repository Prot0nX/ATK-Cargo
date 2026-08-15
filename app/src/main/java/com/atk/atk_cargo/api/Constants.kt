package com.atk.atk_cargo.api

object Constants {
    val BASE_URL: String
        get() = Secrets.getBaseUrl()

    val API_KEY: String
        get() = Secrets.getApiKey()
}