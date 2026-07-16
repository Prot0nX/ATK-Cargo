package com.atk.atk_cargo.utils

import java.security.MessageDigest

fun hashPassword(password: String): String {
    return try {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray())
        bytes.fold("") { str, it -> str + "%02x".format(it) }
    } catch (_: Exception) {
        password
    }
}
