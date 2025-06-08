package com.example.healme.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.healme.MainActivity
import com.example.healme.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "🆕 New FCM token generated: $token")
        // TODO: Optionally send this token to Firestore or your server
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "📩 Message received!")

        val notification = remoteMessage.notification
        val title = notification?.title ?: "No Title"
        val body = notification?.body ?: "No Message"

        Log.d("FCM", "🔔 Notification payload: title=$title, body=$body")

        if (notification != null) {
            showNotification(title, body)
        } else if (remoteMessage.data.isNotEmpty()) {
            val dataTitle = remoteMessage.data["title"] ?: "No Title"
            val dataBody = remoteMessage.data["body"] ?: "No Message"
            Log.d("FCM", "📦 Data payload received: title=$dataTitle, body=$dataBody")
            showNotification(dataTitle, dataBody)
        } else {
            Log.w("FCM", "⚠️ No notification or data payload found!")
        }
    }

    private fun showNotification(title: String, message: String) {
        Log.d("FCM", "🚀 Attempting to show notification: $title - $message")

        val channelId = "default_channel_id"
        val channelName = "Default Channel"

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Make sure this icon exists!
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
            Log.d("FCM", "✅ Notification channel created (API 26+)")
        }

        try {
            manager.notify(0, builder.build())
            Log.d("FCM", "✅ Notification posted successfully")
        } catch (e: Exception) {
            Log.e("FCM", "❌ Failed to show notification: ${e.message}", e)
        }
    }
}
