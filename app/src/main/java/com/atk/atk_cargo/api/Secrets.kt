package com.atk.atk_cargo.api

object Secrets {
    init {
        System.loadLibrary("secrets")
    }

    external fun getBaseUrl(): String
    external fun getWebUserName(): String
    external fun getWebUserPass(): String
    external fun getAuthUser(): String
    external fun getAuthenticationX365(): String
}