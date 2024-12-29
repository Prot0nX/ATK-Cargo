package com.atk.atk_cargo.api

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.Default).launch {
                val userPreferencesManager = UserPreferencesManager(context)
                val userType = userPreferencesManager.userType.first()
                if (userType == "admin") {
                    val serviceIntent = Intent(context, LoadingCheckService::class.java)
                    context.startForegroundService(serviceIntent)
                }
            }
        }
    }
}