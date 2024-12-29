package com.atk.atk_cargo.api

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class UpdateManagerFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UpdateManager::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UpdateManager(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}