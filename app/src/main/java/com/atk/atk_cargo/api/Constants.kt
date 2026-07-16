package com.atk.atk_cargo.api

object Constants {
    val BASE_URL: String
        get() = Secrets.getBaseUrl()

    const val API_KEY = "atk_nk_9290VV42-38XQ02DI-F2WY4L2K-EJA7V682"

    @JvmStatic
    fun getBaseUrl(): String {
        return Secrets.getBaseUrl()
    }
}