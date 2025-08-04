package com.atk.atk_cargo.api

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.atk.atk_cargo.MainActivity

class PopupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val kotazh = intent.getStringExtra("kotazh") ?: ""
            val loadingsCount = intent.getIntExtra("loadingsCount", 0)

            AlertDialog(
                onDismissRequest = { finish() },
                title = { Text("بارگیری‌های جدید") },
                text = { Text("$loadingsCount بارگیری جدید برای کوتاژ $kotazh") },
                confirmButton = {
                    TextButton(onClick = {
                        // اینجا می‌توانید به صفحه جزئیات بارگیری هدایت کنید
                        startActivity(Intent(this@PopupActivity, MainActivity::class.java))
                        finish()
                    }) {
                        Text("مشاهده جزئیات")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { finish() }) {
                        Text("بستن")
                    }
                }
            )
        }
    }
}