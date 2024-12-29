package com.atk.atk_cargo.api

object Secrets {
    init {
        System.loadLibrary("secrets")
    }

    external fun getBaseUrl(): String
}