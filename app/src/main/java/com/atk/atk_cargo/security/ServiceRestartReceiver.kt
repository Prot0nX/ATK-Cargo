package com.atk.atk_cargo.security

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.atk.atk_cargo.api.LoadingCheckService

class ServiceRestartReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        context.startForegroundService(Intent(context, LoadingCheckService::class.java))
    }
}