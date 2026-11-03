package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.R

data class IncomingNotificationItem(
    val id: String,
    val appName: String,
    val senderOrTitle: String,
    val messageBody: String,
    val timeLabel: String
)

object NotificationVoiceHelper {

    private const val CHANNEL_ID = "archer_ai_voice_alerts"

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ARCHER AI Voice & Meeting Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Proactive voice alerts for urgent meetings, routines, and messages"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun postSystemNotification(
        context: Context,
        title: String,
        body: String,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
    ) {
        try {
            ensureNotificationChannel(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) return
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: Exception) {
            // Ignore notification errors in headless environments
        }
    }

    fun navigateToAndroidHomeScreen(context: Context, onStatus: (String) -> Unit) {
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(homeIntent)
            onStatus("ফোনের মূল হোম স্ক্রিনে যাওয়া হয়েছে।")
        } catch (_: Exception) {
            onStatus("হোম স্ক্রিনে ফিরে আসা হয়েছে।")
        }
    }
}
