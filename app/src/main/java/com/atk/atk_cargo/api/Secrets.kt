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

    external fun getExpectedSignatureHash(): String
    external fun getLicenseStatusPrefKey(): String
    external fun getSignatureCheckUrl(): String
    external fun getLicenseCheckUrl(): String
    external fun getLicenseInfoUrl(): String
    external fun getLicenseKey(): String
    external fun getApiKey(): String
}